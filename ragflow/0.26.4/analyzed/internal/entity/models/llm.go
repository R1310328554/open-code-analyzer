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

// Package models — EinoChatModel 薄包装（Phase 2 P0，计划 §2.11.6 D1）。
//
// 将现有 RAGFlow 各厂商 *ChatModel 桥接到 eino 的 BaseChatModel/ToolCallingChatModel，供 ReAct Agent 直接使用；仅做消息/配置转换，不重复实现厂商逻辑。
//
// Why a separate file: the plan forbids editing existing files in this
// // llm.go — EinoChatModel 桥接层（Phase 2 P0）：将 RAGFlow *ChatModel 适配为 eino model.BaseChatModel/ToolCallingChatModel，供 ReAct Agent 直接消费，不重复实现厂商逻辑。

package (types.go, dummy.go, openai.go, …). Adding llm.go keeps the bridge
// self-contained and easy to remove if/when providers get first-class eino
// adapters.
package models

import (
	"context"
	"fmt"
	"sync"

	"github.com/cloudwego/eino/components/model"
	"github.com/cloudwego/eino/schema"
)

// EinoChatModel 将 RAGFlow *ChatModel 适配为 eino 聊天模型接口；并发安全：WithTools 返回新实例而非原地修改 receiver。
type EinoChatModel struct {
	inner   *ChatModel
	chatCfg *ChatConfig
	tools   []*schema.ToolInfo
}

// NewEinoChatModel 包装现有 *ChatModel 供 eino ReAct/Workflow 使用；chatConfig 携带 temperature/max_tokens 等，nil 则用厂商默认；Driver/ModelName/APIConfig 在包装器生命周期内固定。
func NewEinoChatModel(cm *ChatModel, chatConfig *ChatConfig) *EinoChatModel {
	return &EinoChatModel{
		inner:   cm,
		chatCfg: chatConfig,
	}
}

// name 返回底层模型名（尽力而为，nil 安全）
func (m *EinoChatModel) name() string {
	if m == nil || m.inner == nil || m.inner.ModelName == nil {
		return ""
	}
	return *m.inner.ModelName
}

// toInternalMessages 将 eino []schema.Message 转为 RAGFlow []Message；保留 system/user/assistant 角色，tool 映射为 "tool"。
func toInternalMessages(msgs []*schema.Message) []Message {
	if len(msgs) == 0 {
		return nil
	}
	out := make([]Message, 0, len(msgs))
	for _, mm := range msgs {
		if mm == nil {
			continue
		}
		role := string(mm.Role)
		if role == "" {
			role = "user"
		}
		out = append(out, Message{Role: role, Content: mm.Content})
	}
	return out
}

// fromInternalResponse 将 *ChatResponse 转为 *schema.Message（Role=Assistant，Content=answer）
func fromInternalResponse(resp *ChatResponse) *schema.Message {
	if resp == nil {
		return &schema.Message{Role: schema.Assistant, Content: ""}
	}
	content := ""
	if resp.Answer != nil {
		content = *resp.Answer
	}
	return &schema.Message{Role: schema.Assistant, Content: content}
}

// Generate 阻塞直至模型返回完整回复，对应 eino BaseChatModel.Generate
func (m *EinoChatModel) Generate(ctx context.Context, msgs []*schema.Message, opts ...model.Option) (*schema.Message, error) {
	if m == nil || m.inner == nil || m.inner.ModelDriver == nil {
		return nil, fmt.Errorf("models: EinoChatModel: nil inner ModelDriver")
	}
	internal := toInternalMessages(msgs)
	if m.inner.ModelName == nil {
		return nil, fmt.Errorf("models: EinoChatModel: nil model name")
	}
	// ChatWithMessages does not take a context.Context today — Phase 0 kept
	// the signature stable. We log a guard so a future context-aware
	// signature can be slotted in without changing call sites.
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	// Reset stale per-call usage before the call so that a response
	// without a usage block doesn't leak the previous call's data.
	// Mirrors Python's LLMBundle._reset_last_usage().
	m.inner.LastUsage = nil
	resp, err := m.inner.ModelDriver.ChatWithMessages(*m.inner.ModelName, internal, m.inner.APIConfig, m.chatCfg)
	if err != nil {
		return nil, fmt.Errorf("models: EinoChatModel.Generate(%s): %w", *m.inner.ModelName, err)
	}
	// Record the per-call token usage so the canvas-level aggregator (and
	// Langfuse) can compute the run total. Mirrors Python's
	// LLMBundle._report_usage() / self.mdl.last_usage pattern.
	if resp != nil && resp.Usage != nil {
		m.inner.LastUsage = &ChatUsage{
			PromptTokens: resp.Usage.PromptTokens, CompletionTokens: resp.Usage.CompletionTokens, TotalTokens: resp.Usage.TotalTokens,
		}
		recordUsageFromResponse(ctx, m.inner)
	}
	return fromInternalResponse(resp), nil
}

// Stream 返回增量推送 message chunk 的 StreamReader，复用 ChatStreamlyWithSender 路径
func (m *EinoChatModel) Stream(ctx context.Context, msgs []*schema.Message, opts ...model.Option) (*schema.StreamReader[*schema.Message], error) {
	if m == nil || m.inner == nil || m.inner.ModelDriver == nil {
		return nil, fmt.Errorf("models: EinoChatModel: nil inner ModelDriver")
	}
	if err := ctx.Err(); err != nil {
		return nil, err
	}
	if m.inner.ModelName == nil {
		return nil, fmt.Errorf("models: EinoChatModel: nil model name")
	}
	internal := toInternalMessages(msgs)

	sr, sw := schema.Pipe[*schema.Message](1)
	var sendMu sync.Mutex
	sender := func(content *string, _ *string) error {
		sendMu.Lock()
		defer sendMu.Unlock()
		if content == nil {
			return nil
		}
		// Copy the string — the underlying buffer may be reused.
		chunk := *content
		if closed := sw.Send(&schema.Message{Role: schema.Assistant, Content: chunk}, nil); closed {
			return fmt.Errorf("models: stream closed before send completed")
		}
		return nil
	}
	go func() {
		defer sw.Close()
		if err := m.inner.ModelDriver.ChatStreamlyWithSender(*m.inner.ModelName, internal, m.inner.APIConfig, m.chatCfg, sender); err != nil {
			_ = sw.Send(nil, err)
		}
	}()
	return sr, nil
}

// WithTools 返回绑定 tools 的新 EinoChatModel 实例（receiver 不变）；P0 限制：tools 暂存于 wrapper，Phase 2.5 才接入驱动调用，当前 wire 层为 no-op。
func (m *EinoChatModel) WithTools(tools []*schema.ToolInfo) (model.ToolCallingChatModel, error) {
	if m == nil {
		return nil, fmt.Errorf("models: EinoChatModel.WithTools: nil receiver")
	}
	cp := *m
	cp.tools = append([]*schema.ToolInfo(nil), tools...)
	return &cp, nil
}

// Tools 返回当前绑定的 tools（自省用，非 eino 接口）
func (m *EinoChatModel) Tools() []*schema.ToolInfo {
	if m == nil {
		return nil
	}
	return append([]*schema.ToolInfo(nil), m.tools...)
}

// Inner 暴露底层 *ChatModel，供自定义 Generate 后读取 token 用量等
func (m *EinoChatModel) Inner() *ChatModel {
	if m == nil {
		return nil
	}
	return m.inner
}

// Name 返回包装模型名（工具/调试用）
func (m *EinoChatModel) Name() string {
	return m.name()
}

// EinoChatModel 桥接层：Generate 走 ChatWithMessages，Stream 走 ChatStreamlyWithSender；WithTools 满足 ToolCallingChatModel 契约但不 mutate receiver。Phase 3 ReAct 集成前 tool calling 在 wire 层仍为 no-op。
