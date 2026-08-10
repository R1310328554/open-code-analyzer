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

package service

// ask_service.go 实现基于知识库的流式问答服务。

import (
	"context"
	"strings"

	"ragflow/internal/common"
	modelModule "ragflow/internal/entity/models"

	"go.uber.org/zap"
)

// Ask 流水线默认参数，与 Python bot_api.py 保持一致。
const (
	DefaultAskPage                   = 1
	DefaultAskPageSize               = 12
	DefaultAskTopK                   = 1024
	DefaultAskSimilarityThreshold    = 0.1
	DefaultAskVectorSimilarityWeight = 0.3
	DefaultAskTokenBudget            = 4096
	DefaultAskStreamMinTokens        = 16
)

// AskDeltaKind 对流式事件进行分类（答案片段、思考标记、错误、终局）。
type AskDeltaKind int

const (
	AskDeltaAnswer AskDeltaKind = iota // 可见答案文本增量
	AskDeltaMarker                     // 思考区边界标记
	AskDeltaError                      // 非致命错误或提前终止
	AskDeltaFinal                      // 终局事件，携带引用块
)

// AskDelta 表示 AskService 流式输出中的单条事件。
type AskDelta struct {
	Kind  AskDeltaKind
	Value string
	Refs  interface{} // populated on AskDeltaFinal: {chunks, doc_aggs}
}

// AskStreamOptions 允许 Saved Search 传入检索配置覆盖默认值。
// search_config. Zero values keep the same defaults as Stream.
type AskStreamOptions struct {
	SearchID               string
	DocIDs                 []string
	UseKG                  *bool
	TopK                   *int
	CrossLanguages         []string
	Filter                 map[string]interface{}
	TenantRerankID         *string
	RerankID               *string
	Keyword                *bool
	SimilarityThreshold    *float64
	VectorSimilarityWeight *float64
}

// Retriever 抽象分块检索，供 AskService 调用 RetrievalTest。
type Retriever interface {
	RetrievalTest(req *RetrievalTestRequest, userID string) (*RetrievalTestResponse, error)
}

// StreamingLLM 抽象流式聊天模型接口。
type StreamingLLM interface {
	ChatStream(ctx context.Context, messages []modelModule.Message, config *modelModule.ChatConfig) (<-chan string, error)
}

// AskService 执行检索增强问答；embedder 为 nil 时跳过引用插入。
// Embedder may be nil; if nil, citation insertion is skipped.
type AskService struct {
	retriever       Retriever
	embedder        Embedder
	tokenBudget     int
	minStreamTokens int
}

// NewAskService 构造 AskService，tokenBudget/minStreamTokens 非正时使用默认值。
func NewAskService(retriever Retriever, embedder Embedder, tokenBudget, minStreamTokens int) *AskService {
	if tokenBudget <= 0 {
		tokenBudget = DefaultAskTokenBudget
	}
	if minStreamTokens <= 0 {
		minStreamTokens = DefaultAskStreamMinTokens
	}
	return &AskService{
		retriever:       retriever,
		embedder:        embedder,
		tokenBudget:     tokenBudget,
		minStreamTokens: minStreamTokens,
	}
}

// Stream 运行完整 Ask 流水线，llm 不可为 nil；返回 channel 在结束或 ctx 取消时关闭。
// channel is closed when the pipeline completes or ctx is cancelled.
func (s *AskService) Stream(ctx context.Context, llm StreamingLLM, userID, question string, kbIDs []string) <-chan AskDelta {
	return s.StreamWithOptions(ctx, llm, userID, question, kbIDs, AskStreamOptions{})
}

// StreamWithOptions 在 Stream 基础上透传 search_config 检索选项。
// apps to pass search_config retrieval options through to RetrievalTest.
func (s *AskService) StreamWithOptions(ctx context.Context, llm StreamingLLM, userID, question string, kbIDs []string, opts AskStreamOptions) <-chan AskDelta {
	out := make(chan AskDelta, 32)
	go func() {
		defer close(out)
		s.run(ctx, llm, userID, question, kbIDs, opts, out)
	}()
	return out
}

func (s *AskService) run(ctx context.Context, llm StreamingLLM, userID, question string, kbIDs []string, opts AskStreamOptions, out chan<- AskDelta) {
	// 阶段一：检索相关分块。
	topK := DefaultAskTopK
	if opts.TopK != nil {
		topK = *opts.TopK
	}
	similarityThreshold := DefaultAskSimilarityThreshold
	if opts.SimilarityThreshold != nil {
		similarityThreshold = *opts.SimilarityThreshold
	}
	vectorSimilarityWeight := DefaultAskVectorSimilarityWeight
	if opts.VectorSimilarityWeight != nil {
		vectorSimilarityWeight = *opts.VectorSimilarityWeight
	}

	req := &RetrievalTestRequest{
		Datasets:               common.StringSlice(kbIDs),
		Question:               question,
		DocIDs:                 opts.DocIDs,
		UseKG:                  opts.UseKG,
		TopK:                   ptrInt(topK),
		CrossLanguages:         opts.CrossLanguages,
		Filter:                 opts.Filter,
		TenantRerankID:         opts.TenantRerankID,
		RerankID:               opts.RerankID,
		Keyword:                opts.Keyword,
		SimilarityThreshold:    ptrFloat64(similarityThreshold),
		VectorSimilarityWeight: ptrFloat64(vectorSimilarityWeight),
	}
	if opts.SearchID != "" {
		req.SearchID = &opts.SearchID
	}
	page := DefaultAskPage
	ps := DefaultAskPageSize
	req.Page = &page
	req.Size = &ps

	result, err := s.retriever.RetrievalTest(req, userID)
	if err != nil {
		common.Warn("AskService retrieval failed", zap.Error(err))
		s.sendOrCancel(out, AskDelta{Kind: AskDeltaError, Value: "retrieval failed"}, ctx)
		return
	}
	if result == nil || len(result.Chunks) == 0 {
		s.sendOrCancel(out, AskDelta{Kind: AskDeltaError, Value: "Sorry, no relevant information provided."}, ctx)
		return
	}

	chunks := NewSourcedChunks(result.Chunks)

	// 阶段二：组装系统提示词（knowledge + ask_summary 模板）。
	knowledge := KbPrompt(chunks, s.tokenBudget)
	prompt, err := LoadPrompt("ask_summary")
	if err != nil {
		common.Warn("AskService failed to load prompt", zap.Error(err))
		s.sendOrCancel(out, AskDelta{Kind: AskDeltaError, Value: "prompt configuration error"}, ctx)
		return
	}
	sysPrompt := RenderPrompt(prompt, map[string]interface{}{"knowledge": knowledge})

	messages := []modelModule.Message{
		{Role: "system", Content: sysPrompt},
		{Role: "user", Content: question},
	}
	genConf := &modelModule.ChatConfig{Temperature: ptrFloat64(0.1)}

	ch, err := llm.ChatStream(ctx, messages, genConf)
	if err != nil {
		common.Warn("AskService LLM stream failed", zap.Error(err))
		s.sendOrCancel(out, AskDelta{Kind: AskDeltaError, Value: "LLM call failed"}, ctx)
		return
	}

	// 阶段三：流式 LLM 输出并处理思考标签。
	var fullAnswer string
	for delta := range StreamThinkTagDelta(ctx, ch, s.minStreamTokens) {
		switch delta.Kind {
		case ThinkDeltaMarker:
			s.sendOrCancel(out, AskDelta{Kind: AskDeltaMarker, Value: delta.Value}, ctx)
		case ThinkDeltaText:
			fullAnswer += delta.Value
			s.sendOrCancel(out, AskDelta{Kind: AskDeltaAnswer, Value: delta.Value}, ctx)
		}
	}

	// 阶段四：终局化——引用插入与 reference 格式化。
	visible := ExtractVisibleAnswer(fullAnswer)
	if strings.TrimSpace(visible) == "" {
		common.Warn("AskService LLM stream completed without visible answer")
		s.sendOrCancel(out, AskDelta{Kind: AskDeltaError, Value: "LLM call failed"}, ctx)
		return
	}
	chunkRefs := ChunksFormat(chunks)

	// Attempt citation insertion if embedder is available.
	chunkVectors := ExtractChunkVectors(result.Chunks)
	if len(chunkVectors) > 0 && s.embedder != nil {
		if decorated, cited := InsertCitations(visible, chunks, s.embedder, chunkVectors); len(cited) > 0 {
			visible = decorated
		}
	}

	refs := map[string]interface{}{
		"chunks":   chunkRefs,
		"doc_aggs": result.DocAggs,
	}
	s.sendOrCancel(out, AskDelta{Kind: AskDeltaFinal, Value: visible, Refs: refs}, ctx)
}

func (s *AskService) sendOrCancel(out chan<- AskDelta, d AskDelta, ctx context.Context) {
	select {
	case out <- d:
	case <-ctx.Done():
	}
}

// ExtractChunkVectors 从检索结果中提取 float64 向量；空向量或全零返回 nil 槽位。
// Returns nil for chunks that have no, empty, or all-zero vectors.
func ExtractChunkVectors(chunks []map[string]interface{}) [][]float64 {
	if len(chunks) == 0 {
		return nil
	}
	out := make([][]float64, 0, len(chunks))
	for _, ck := range chunks {
		v := toFloat64Slice(ck["vector"])
		if len(v) == 0 || common.IsZeroVector(v) {
			out = append(out, nil)
		} else {
			out = append(out, v)
		}
	}
	return out
}

func toFloat64Slice(v interface{}) []float64 {
	switch val := v.(type) {
	case []float64:
		out := make([]float64, len(val))
		copy(out, val)
		return out
	case []interface{}:
		out := make([]float64, len(val))
		for i, x := range val {
			if f, ok := x.(float64); ok {
				out[i] = f
			} else {
				return nil
			}
		}
		return out
	default:
		return nil
	}
}

func ptrInt(v int) *int             { return &v }
func ptrFloat64(v float64) *float64 { return &v }
// ask_service.go — 检索增强问答流水线：检索 → 提示词 → 流式 LLM → 引用插入。
