//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package tokenizer

// tokenizer.go 封装 RAG 分词器弹性连接池与 cl100k token 计数工具。

import (
	"context"
	"fmt"
	"os"
	"ragflow/internal/common"
	"runtime"
	"sync"
	"sync/atomic"
	"time"
	"unicode/utf8"

	"github.com/pkoukk/tiktoken-go"
	"go.uber.org/zap"

	rag "ragflow/internal/binding"
)

// engineTypeProvider 由 engine.RegisterEngineType 注入，打破 tokenizer↔engine 循环依赖。
var engineTypeProvider = func() string { return "" }

// RegisterEngineType 注册引擎类型查询函数，供 Infinity 分支跳过本地分词。
func RegisterEngineType(get func() string) {
	if get == nil {
		engineTypeProvider = func() string { return "" }
		return
	}
	engineTypeProvider = get
}

// PoolConfig 分词器连接池配置：字典路径、容量上下限与超时。
type PoolConfig struct {
	DictPath       string        // 词典文件目录路径
	MinSize        int           // 预热实例下限（默认 2×CPU）
	MaxSize        int           // 实例上限（默认 16×CPU）
	IdleTimeout    time.Duration // 空闲缩容超时（默认 5 分钟）
	AcquireTimeout time.Duration // 获取实例等待超时（默认 10 秒）
}

// poolInstance 包装 Analyzer 实例及最后使用时间，供池管理。
type poolInstance struct {
	analyzer   *rag.Analyzer
	lastUsedAt time.Time
}

// analyzerPool is the elastic pool for analyzer instances
type analyzerPool struct {
	config       PoolConfig
	baseAnalyzer *rag.Analyzer      // 作为 Copy 模板的基准 Analyzer
	instances    chan *poolInstance // 可用实例 channel 池
	currentSize  int32              // 当前实例数（原子计数）
	initialized  bool
	mu           sync.RWMutex
	stopCh       chan struct{}
	wg           sync.WaitGroup
}

var (
	globalPool    *analyzerPool
	poolOnce      sync.Once
	poolInitError error
)

// Init 初始化全局分词器池；池已关闭时可重置 poolOnce 重新初始化。
func Init(cfg *PoolConfig) error {
	// Check if we need to reset poolOnce (for testing or re-initialization)
	if globalPool != nil && !globalPool.initialized {
		// Pool was closed, reset poolOnce for re-initialization
		poolOnce = sync.Once{}
	}

	poolOnce.Do(func() {
		if cfg == nil {
			cfg = &PoolConfig{}
		}

		// 填充默认配置（环境变量 RAGFLOW_DICT_PATH、CPU 倍数等）
		if cfg.DictPath == "" {
			if env := os.Getenv("RAGFLOW_DICT_PATH"); env != "" {
				cfg.DictPath = env
			} else {
				cfg.DictPath = "/usr/share/infinity/resource"
			}
		}
		if cfg.MinSize <= 0 {
			cfg.MinSize = runtime.NumCPU() * 2
		}
		if cfg.MaxSize <= 0 {
			cfg.MaxSize = runtime.NumCPU() * 16
		}
		if cfg.MinSize > cfg.MaxSize {
			cfg.MinSize = cfg.MaxSize
		}
		if cfg.IdleTimeout <= 0 {
			cfg.IdleTimeout = 5 * time.Minute
		}
		if cfg.AcquireTimeout <= 0 {
			cfg.AcquireTimeout = 10 * time.Second
		}

		common.Info("Initializing analyzer pool",
			zap.String("dict_path", cfg.DictPath),
			zap.Int("min_size", cfg.MinSize),
			zap.Int("max_size", cfg.MaxSize),
			zap.Duration("idle_timeout", cfg.IdleTimeout),
			zap.Duration("acquire_timeout", cfg.AcquireTimeout))

		globalPool = &analyzerPool{
			config:    *cfg,
			instances: make(chan *poolInstance, cfg.MaxSize),
			stopCh:    make(chan struct{}),
		}

		// 创建并 Load 基准 Analyzer 供 Copy
		baseAnalyzer, err := rag.NewAnalyzer(cfg.DictPath)
		if err != nil {
			poolInitError = fmt.Errorf("failed to create base analyzer: %w", err)
			common.Error("Failed to create base analyzer", poolInitError)
			return
		}

		if err = baseAnalyzer.Load(); err != nil {
			poolInitError = fmt.Errorf("failed to load base analyzer: %w", err)
			common.Error("Failed to load base analyzer", poolInitError)
			baseAnalyzer.Close()
			return
		}

		globalPool.baseAnalyzer = baseAnalyzer

		// 预热 minSize 个实例放入 channel
		for i := 0; i < cfg.MinSize; i++ {
			instance, err := globalPool.createInstance()
			if err != nil {
				poolInitError = fmt.Errorf("failed to create instance %d: %w", i, err)
				common.Error("Failed to create pool instance", poolInitError)
				globalPool.Close()
				return
			}
			globalPool.instances <- instance
			atomic.AddInt32(&globalPool.currentSize, 1)
		}

		globalPool.initialized = true
		common.Info("Analyzer pool initialized successfully",
			zap.Int("pre_warmed", cfg.MinSize),
			zap.Int32("current_size", atomic.LoadInt32(&globalPool.currentSize)))

		// 启动 shrinkLoop 定期回收空闲实例
		globalPool.wg.Add(1)
		go globalPool.shrinkLoop()
	})

	return poolInitError
}

// createInstance 复制 baseAnalyzer 生成独立 Analyzer 实例。
func (p *analyzerPool) createInstance() (*poolInstance, error) {
	if p.baseAnalyzer == nil {
		return nil, fmt.Errorf("base analyzer is nil")
	}

	// Copy 基准分词器得到互不共享状态的新实例
	copied := p.baseAnalyzer.Copy()
	if copied == nil {
		return nil, fmt.Errorf("failed to copy analyzer")
	}

	return &poolInstance{
		analyzer:   copied,
		lastUsedAt: time.Now(),
	}, nil
}

// acquire 从池取实例；池空且未达 MaxSize 则动态扩容。
func (p *analyzerPool) acquire() (*poolInstance, error) {
	if !p.initialized {
		return nil, fmt.Errorf("pool not initialized")
	}

	// 快路径：非阻塞从 channel 取实例
	select {
	case instance := <-p.instances:
		instance.lastUsedAt = time.Now()
		return instance, nil
	default:
	}

	// 慢路径：动态扩容或带超时阻塞等待
	current := atomic.LoadInt32(&p.currentSize)
	if current < int32(p.config.MaxSize) {
		// CAS 增加 currentSize 并创建新实例
		if atomic.CompareAndSwapInt32(&p.currentSize, current, current+1) {
			instance, err := p.createInstance()
			if err != nil {
				// Decrement counter on failure
				atomic.AddInt32(&p.currentSize, -1)
				return nil, fmt.Errorf("failed to dynamically create instance: %w", err)
			}
			common.Info("Pool expanded dynamically",
				zap.Int32("previous_size", current),
				zap.Int32("new_size", current+1),
				zap.Int("max_size", p.config.MaxSize))
			return instance, nil
		}
		// CAS 失败说明其他协程已扩容，转入等待
	}

	// 带 AcquireTimeout 阻塞等待可用实例
	ctx, cancel := context.WithTimeout(context.Background(), p.config.AcquireTimeout)
	defer cancel()

	select {
	case instance := <-p.instances:
		instance.lastUsedAt = time.Now()
		return instance, nil
	case <-ctx.Done():
		return nil, fmt.Errorf("timeout waiting for analyzer instance (current_size=%d, max=%d)",
			atomic.LoadInt32(&p.currentSize), p.config.MaxSize)
	}
}

// release 归还实例到池；池满则关闭并递减计数。
func (p *analyzerPool) release(instance *poolInstance) {
	if instance == nil || instance.analyzer == nil {
		return
	}

	if !p.initialized {
		instance.analyzer.Close()
		return
	}

	select {
	case p.instances <- instance:
		// 成功放回 channel
	default:
		// 池满时销毁多余实例
		common.Warn("Pool full when releasing instance, destroying it",
			zap.Int32("current_size", atomic.LoadInt32(&p.currentSize)))
		instance.analyzer.Close()
		atomic.AddInt32(&p.currentSize, -1)
	}
}

// shrinkLoop 每 30 秒触发 shrink 回收空闲实例。
func (p *analyzerPool) shrinkLoop() {
	defer p.wg.Done()

	ticker := time.NewTicker(30 * time.Second) // Check every 30 seconds
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			p.shrink()
		case <-p.stopCh:
			return
		}
	}
}

// shrink 移除超过 IdleTimeout 的空闲实例，但不低于 MinSize。
func (p *analyzerPool) shrink() {
	if !p.initialized {
		return
	}

	currentSize := atomic.LoadInt32(&p.currentSize)
	minSize := int32(p.config.MinSize)

	// 仅当实例数大于 MinSize 时才缩容
	if currentSize <= minSize {
		return
	}

	now := time.Now()
	timeout := p.config.IdleTimeout
	var toRemove []*poolInstance

	// 非阻塞收集超时未用的实例
	for i := 0; i < int(currentSize-minSize); i++ {
		select {
		case instance := <-p.instances:
			if now.Sub(instance.lastUsedAt) > timeout {
				toRemove = append(toRemove, instance)
			} else {
				// 未超时则放回池中
				select {
				case p.instances <- instance:
				default:
					// Pool full, should not happen
					toRemove = append(toRemove, instance)
				}
			}
		default:
			// No more instances in pool
			break
		}
	}

	if len(toRemove) > 0 {
		// 关闭并销毁待移除实例
		for _, instance := range toRemove {
			instance.analyzer.Close()
		}

		newSize := atomic.AddInt32(&p.currentSize, -int32(len(toRemove)))
		common.Info("Pool shrunk",
			zap.Int("removed_instances", len(toRemove)),
			zap.Int32("previous_size", currentSize),
			zap.Int32("new_size", newSize),
			zap.Int("min_size", p.config.MinSize))
	}
}

// Close 停止 shrinkLoop 并关闭全部 Analyzer 与 channel。
func (p *analyzerPool) Close() {
	if p == nil {
		return
	}

	p.mu.Lock()
	if !p.initialized {
		p.mu.Unlock()
		return
	}
	p.initialized = false
	p.mu.Unlock()

	// 通知 shrinkLoop 退出
	close(p.stopCh)
	p.wg.Wait()

	// 排空 channel 并 Close 每个实例
	close(p.instances)
	for instance := range p.instances {
		if instance != nil && instance.analyzer != nil {
			instance.analyzer.Close()
		}
	}

	// 关闭基准 Analyzer
	if p.baseAnalyzer != nil {
		p.baseAnalyzer.Close()
		p.baseAnalyzer = nil
	}

	common.Info(fmt.Sprintf("Analyzer pool closed, final_size: %d", atomic.LoadInt32(&p.currentSize)))
}

// GetPoolStats 返回池初始化状态、当前/最小/最大容量等统计。
func GetPoolStats() map[string]interface{} {
	if globalPool == nil {
		return map[string]interface{}{
			"initialized": false,
		}
	}

	return map[string]interface{}{
		"initialized":         globalPool.initialized,
		"current_size":        atomic.LoadInt32(&globalPool.currentSize),
		"min_size":            globalPool.config.MinSize,
		"max_size":            globalPool.config.MaxSize,
		"idle_timeout":        globalPool.config.IdleTimeout.String(),
		"instances_available": len(globalPool.instances),
	}
}

// Close 关闭全局 analyzerPool。
func Close() {
	if globalPool != nil {
		globalPool.Close()
	}
}

// withAnalyzer 借出独占 Analyzer 执行 fn 后自动 release。
func withAnalyzer(fn func(*rag.Analyzer) error) error {
	if globalPool == nil {
		return fmt.Errorf("tokenizer pool not initialized")
	}

	instance, err := globalPool.acquire()
	if err != nil {
		return err
	}
	defer globalPool.release(instance)

	return fn(instance.analyzer)
}

// withAnalyzerResult 带返回值的 withAnalyzer 泛型封装。
func withAnalyzerResult[T any](fn func(*rag.Analyzer) (T, error)) (T, error) {
	var result T
	if globalPool == nil {
		return result, fmt.Errorf("tokenizer pool not initialized")
	}

	instance, err := globalPool.acquire()
	if err != nil {
		return result, err
	}
	defer globalPool.release(instance)

	return fn(instance.analyzer)
}

// Tokenize 分词并返回空格分隔 token 串；Infinity 引擎原样返回以对齐 Python。
func Tokenize(text string) (string, error) {
	if engineTypeProvider() == "infinity" {
		return text, nil
	}
	return withAnalyzerResult(func(a *rag.Analyzer) (string, error) {
		return a.Tokenize(text)
	})
}

// TokenizeWithPosition 分词并返回带位置信息的 token 列表。
func TokenizeWithPosition(text string) ([]rag.TokenWithPosition, error) {
	return withAnalyzerResult(func(a *rag.Analyzer) ([]rag.TokenWithPosition, error) {
		return a.TokenizeWithPosition(text)
	})
}

// Analyze 深度分析文本，返回完整 Token 结构列表。
func Analyze(text string) ([]rag.Token, error) {
	return withAnalyzerResult(func(a *rag.Analyzer) ([]rag.Token, error) {
		return a.Analyze(text)
	})
}

// SetFineGrained 池模式下为 no-op（每请求独立实例，需在 Init 前配置基准）。
func SetFineGrained(fineGrained bool) {
	// In pool mode, we don't set global state on instances
	// Each request gets a fresh instance with default settings
	common.Debug("SetFineGrained is no-op in pool mode", zap.Bool("fine_grained", fineGrained))
}

// FineGrainedTokenize 对已有 token 串做细粒度切分；Infinity 引擎跳过。
func FineGrainedTokenize(tokens string) (string, error) {
	if engineTypeProvider() == "infinity" {
		return tokens, nil
	}
	return withAnalyzerResult(func(a *rag.Analyzer) (string, error) {
		return a.FineGrainedTokenize(tokens)
	})
}

// SetEnablePosition 池模式下为 no-op。
func SetEnablePosition(enablePosition bool) {
	common.Debug("SetEnablePosition is no-op in pool mode", zap.Bool("enable_position", enablePosition))
}

// IsInitialized 判断全局分词器池是否已成功 Init。
func IsInitialized() bool {
	return globalPool != nil && globalPool.initialized
}

// GetTermFreq 查询词项词频，对齐 Python rag_tokenizer.freq。
func GetTermFreq(term string) int32 {
	result, _ := withAnalyzerResult(func(a *rag.Analyzer) (int32, error) {
		return a.GetTermFreq(term), nil
	})
	return result
}

// GetTermTag 查询词项词性标签，对齐 Python rag_tokenizer.tag。
func GetTermTag(term string) string {
	result, _ := withAnalyzerResult(func(a *rag.Analyzer) (string, error) {
		return a.GetTermTag(term), nil
	})
	return result
}

var cl100kEncoder struct {
	sync.Once
	enc *tiktoken.Tiktoken
	err error
}

func getCL100KEncoder() (*tiktoken.Tiktoken, error) {
	cl100kEncoder.Do(func() {
		cl100kEncoder.enc, cl100kEncoder.err = tiktoken.GetEncoding("cl100k_base")
	})
	return cl100kEncoder.enc, cl100kEncoder.err
}

// NumTokensFromString 用 cl100k_base BPE 统计字符串 token 数；编码器不可用时按字节保守估计。
func NumTokensFromString(s string) int {
	if s == "" {
		return 0
	}
	enc, err := getCL100KEncoder()
	if err != nil {
		// 编码器失败时按字节长度保守计数，避免预算低估。
		return len([]byte(s))
	}
	return len(enc.Encode(s, nil, nil))
}

// TrimContentToTokenLimit 按 cl100k token 上限截断内容，对齐 Python trim_content。
func TrimContentToTokenLimit(s string, limit int) string {
	if limit < 0 {
		limit = 0
	}
	enc, err := getCL100KEncoder()
	if err != nil {
		// 编码器不可用时按字节截断并保证 UTF-8 边界有效。
		if limit <= 0 {
			return ""
		}
		b := []byte(s)
		if len(b) <= limit {
			return s
		}
		for limit > 0 && !utf8.Valid(b[:limit]) {
			limit--
		}
		return string(b[:limit])
	}
	tokens := enc.Encode(s, nil, nil)
	if len(tokens) <= limit {
		return s
	}
	return enc.Decode(tokens[:limit])
}
// tokenizer.go — 弹性分词器池、cl100k BPE 计数与 Infinity 引擎分支。
