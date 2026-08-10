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

// Package tokenizer — 单次 Canvas 回合的 Token 用量追踪。
//
// 回合开始时通过 WithRunUsage 在 context 挂载可变累加器；回合内每次 LLM 调用
// 经 RecordRunTokenUsage 累加 prompt/completion/total；结束时服务层读取总量并写入
// workflow_finished SSE。对齐 Python common.token_utils 的 ContextVar 与 record 函数。
package tokenizer

import (
	"context"
	"encoding/json"
	"sync"
)

// context 键类型（未导出），防止外部直接访问。
type runUsageKeyType struct{}
type runAttrsKeyType struct{}

// RunUsage 单次回合 Token 累加器；mutex 保护并发 tool 协程共享同一 sink。
type RunUsage struct {
	mu               sync.Mutex
	PromptTokens     int
	CompletionTokens int
	TotalTokens      int
	Calls            int
}

// Add 累加单次 LLM 调用的 token 计数并递增 Calls；并发安全。
func (u *RunUsage) Add(prompt, completion, total int) {
	if u == nil {
		return
	}
	u.mu.Lock()
	defer u.mu.Unlock()
	if prompt > 0 {
		u.PromptTokens += prompt
	}
	if completion > 0 {
		u.CompletionTokens += completion
	}
	if total > 0 {
		u.TotalTokens += total
	}
	u.Calls++
}

// Snapshot 返回当前累计 prompt/completion/total/calls 快照。
func (u *RunUsage) Snapshot() (prompt, completion, total, calls int) {
	if u == nil {
		return 0, 0, 0, 0
	}
	u.mu.Lock()
	defer u.mu.Unlock()
	return u.PromptTokens, u.CompletionTokens, u.TotalTokens, u.Calls
}

// RunAttrs 单次回合 Langfuse 关联属性（session_id、user_id）。
type RunAttrs struct {
	SessionID string
	UserID    string
}

// WithRunUsage 在 ctx 安装新的 RunUsage，每回合开始调用一次。
func WithRunUsage(ctx context.Context) context.Context {
	return context.WithValue(ctx, runUsageKeyType{}, &RunUsage{})
}

// GetRunUsage 从 ctx 取出 RunUsage；非 Canvas 回合时为 nil。
func GetRunUsage(ctx context.Context) *RunUsage {
	if v := ctx.Value(runUsageKeyType{}); v != nil {
		if sink, ok := v.(*RunUsage); ok {
			return sink
		}
	}
	return nil
}

// WithRunAttrs 在 ctx 挂载 Langfuse 关联属性。
func WithRunAttrs(ctx context.Context, attrs *RunAttrs) context.Context {
	if attrs == nil {
		return ctx
	}
	return context.WithValue(ctx, runAttrsKeyType{}, attrs)
}

// GetRunAttrs 从 ctx 读取 RunAttrs。
func GetRunAttrs(ctx context.Context) *RunAttrs {
	if v := ctx.Value(runAttrsKeyType{}); v != nil {
		if attrs, ok := v.(*RunAttrs); ok {
			return attrs
		}
	}
	return nil
}

// RecordRunTokenUsage 将单次 LLM 用量写入 ctx 上的 sink；无 sink 时为 no-op。
func RecordRunTokenUsage(ctx context.Context, promptTokens, completionTokens, totalTokens int) {
	sink := GetRunUsage(ctx)
	if sink == nil {
		return
	}
	sink.Add(promptTokens, completionTokens, totalTokens)
}

// UsageFromMap 从原始 API 响应 map 解析 usage 字段，对齐 Python usage_from_response。
func UsageFromMap(raw map[string]interface{}) (promptTokens, completionTokens, totalTokens int) {
	if raw == nil {
		return 0, 0, 0
	}
	usageRaw, ok := raw["usage"]
	if !ok {
		return 0, 0, 0
	}
	usage, ok := usageRaw.(map[string]interface{})
	if !ok {
		return 0, 0, 0
	}
	pt := getInt(usage, "prompt_tokens", "input_tokens")
	ct := getInt(usage, "completion_tokens", "output_tokens")
	tt := getInt(usage, "total_tokens")
	if tt == 0 {
		tt = pt + ct
	}
	return pt, ct, tt
}

func getInt(m map[string]interface{}, keys ...string) int {
	for _, k := range keys {
		v, ok := m[k]
		if !ok {
			continue
		}
		switch val := v.(type) {
		case float64:
			return int(val)
		case int:
			return val
		case json.Number:
			n, err := val.Int64()
			if err == nil {
				return int(n)
			}
		}
	}
	return 0
}
// usage.go — Canvas 回合级 LLM Token 用量累加器（context 挂载）。
