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

// novita.go — Novita.ai ModelDriver：OpenAI 兼容 Chat/Embed/Rerank，<think> 推理链拆分与流式提取器。
//

package models

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"strings"
)

// NovitaModel Novita.ai 平台 ModelDriver
type NovitaModel struct {
	baseModel BaseModel
}

// NewNovitaModel 创建 Novita 驱动实例
func NewNovitaModel(baseURL map[string]string, urlSuffix URLSuffix) *NovitaModel {
	return &NovitaModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Novita 驱动实例
func (n *NovitaModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewNovitaModel(baseURL, n.baseModel.URLSuffix)
}

// Name 返回提供商标识 "novita"，供工厂层路由
func (n *NovitaModel) Name() string {
	return "novita"
}

const (
	novitaThinkOpen  = "<think>"
	novitaThinkClose = "</think>"
)

// splitNovitaThink 遍历完整回复，拆分可见正文与 <think> 推理链
// visible portion + the concatenated chain-of-thought from inside
// any <think>...</think> blocks. Multiple think blocks are
// concatenated; tags themselves are stripped. Used by the
// non-streaming path where the whole content is available at once.
func splitNovitaThink(raw string) (visible, reasoning string) {
	var v, r strings.Builder
	inside := false
	for {
		var marker string
		if inside {
			marker = novitaThinkClose
		} else {
			marker = novitaThinkOpen
		}
		idx := strings.Index(raw, marker)
		if idx < 0 {
			if inside {
				r.WriteString(raw)
			} else {
				v.WriteString(raw)
			}
			break
		}
		if inside {
			r.WriteString(raw[:idx])
		} else {
			v.WriteString(raw[:idx])
		}
		raw = raw[idx+len(marker):]
		inside = !inside
	}
	return v.String(), r.String()
}

// novitaThinkExtractor 跨 SSE 分片维护状态，正确拆分推理标签
// that a <think>...</think> block spanning multiple SSE events still
// gets split correctly between content and reasoning. The buffer
// preserves up to (len(closingMarker)-1) trailing bytes of each
// chunk in case the next chunk completes a partial tag.
type novitaThinkExtractor struct {
	buf    strings.Builder
	inside bool
}

// novitaThinkSegment 路由决策：content 走 sender 第一参数，reasoning 走第二参数
// sender's first arg, or emit `reasoning` via the sender's second arg.
// Exactly one of the two fields is non-empty.
type novitaThinkSegment struct {
	content   string
	reasoning string
}

// Feed 追加流式分片，返回可安全下发的 content/reasoning 片段
// now safe to emit. Trailing bytes that could be the start of a tag
// are held back in the buffer until the next call.
func (e *novitaThinkExtractor) Feed(chunk string) []novitaThinkSegment {
	e.buf.WriteString(chunk)
	s := e.buf.String()
	var out []novitaThinkSegment
	for {
		var marker, otherMarker string
		if e.inside {
			marker = novitaThinkClose
			otherMarker = novitaThinkOpen
		} else {
			marker = novitaThinkOpen
			otherMarker = novitaThinkClose
		}
		idx := strings.Index(s, marker)
		if idx < 0 {
			// No closing/opening marker yet. Emit everything except a
			// possible partial-tag suffix at the very end. Reserve
			// (max marker length - 1) trailing bytes so we don't
			// emit "<thin" as content when the next chunk completes
			// it to "<think>".
			reserve := len(marker) - 1
			if len(otherMarker)-1 > reserve {
				reserve = len(otherMarker) - 1
			}
			safe := len(s) - reserve
			if safe < 0 {
				safe = 0
			}
			// Don't reserve if the trailing bytes can't possibly be
			// the start of a tag (no '<' suffix).
			if safe < len(s) && !strings.Contains(s[safe:], "<") {
				safe = len(s)
			}
			if safe > 0 {
				if e.inside {
					out = append(out, novitaThinkSegment{reasoning: s[:safe]})
				} else {
					out = append(out, novitaThinkSegment{content: s[:safe]})
				}
				s = s[safe:]
			}
			break
		}
		if idx > 0 {
			if e.inside {
				out = append(out, novitaThinkSegment{reasoning: s[:idx]})
			} else {
				out = append(out, novitaThinkSegment{content: s[:idx]})
			}
		}
		s = s[idx+len(marker):]
		e.inside = !e.inside
	}
	e.buf.Reset()
	e.buf.WriteString(s)
	return out
}

// Flush 流结束时冲刷缓冲区尾部，避免推理片段丢失
// ends mid-tag would not normally happen with a well-behaved upstream,
// but if it does the partial bytes are emitted according to the
// current mode so nothing is silently lost.
func (e *novitaThinkExtractor) Flush() *novitaThinkSegment {
	s := e.buf.String()
	e.buf.Reset()
	if s == "" {
		return nil
	}
	if e.inside {
		return &novitaThinkSegment{reasoning: s}
	}
	return &novitaThinkSegment{content: s}
}

// ChatWithMessages 非流式 chat/completions，拆分 thinking 标签
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (n *NovitaModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Chat)

	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": msg.Content,
		}
	}

	reqBody := map[string]interface{}{
		"model":    modelName,
		"messages": apiMessages,
		"stream":   false,
	}

	if chatModelConfig != nil {
		if chatModelConfig.MaxTokens != nil {
			reqBody["max_tokens"] = *chatModelConfig.MaxTokens
		}
		if chatModelConfig.Temperature != nil {
			reqBody["temperature"] = *chatModelConfig.Temperature
		}
		if chatModelConfig.TopP != nil {
			reqBody["top_p"] = *chatModelConfig.TopP
		}
		if chatModelConfig.Stop != nil {
			reqBody["stop"] = *chatModelConfig.Stop
		}
		// Map ChatConfig.Thinking -> Novita's `enable_thinking`.
		// Per https://novita.ai/docs/api-reference/model-apis-llm-create-chat-completion,
		// enable_thinking (boolean | null, default true) "controls the
		// switches between thinking and non-thinking modes" for
		// zai-org/glm-4.5, deepseek/deepseek-v3.1[-terminus|-exp]. For
		// models outside that supported set Novita ignores the field,
		// so it's safe to forward whenever the caller opts in. Tenants
		// can now disable thinking mode at request time without having
		// to use prompt-level hacks like "/no_think".
		if chatModelConfig.Thinking != nil {
			reqBody["enable_thinking"] = *chatModelConfig.Thinking
		}
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	var result map[string]interface{}
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	choices, ok := result["choices"].([]interface{})
	if !ok || len(choices) == 0 {
		return nil, fmt.Errorf("no choices in response")
	}

	firstChoice, ok := choices[0].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid choice format")
	}

	messageMap, ok := firstChoice["message"].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid message format")
	}

	rawContent, ok := messageMap["content"].(string)
	if !ok {
		return nil, fmt.Errorf("invalid content format")
	}

	// Novita emits chain-of-thought in two different shapes depending
	// on the model and on enable_thinking:
	//   - qwen3-* and other inline-style models: chain-of-thought is
	//     embedded inside content as <think>...</think> tags.
	//   - deepseek-v3.1 / glm-4.5 (and any model with separate
	//     reasoning enabled): chain-of-thought arrives in a separate
	//     `reasoning_content` field, with `content` already cleaned.
	// Handle both so the visible Answer is always tag-free and any
	// reasoning the upstream supplied is preserved.
	visible, reasoning := splitNovitaThink(rawContent)
	if r, ok := messageMap["reasoning_content"].(string); ok && r != "" {
		if reasoning != "" {
			reasoning += "\n" + r
		} else {
			reasoning = r
		}
	}

	return &ChatResponse{
		Answer:        &visible,
		ReasonContent: &reasoning,
	}, nil
}

// ChatStreamlyWithSender 流式 chat/completions，经 novitaThinkExtractor 推送 delta
// the sender. Handles both reasoning shapes Novita can emit:
//   - delta.reasoning_content (deepseek-v3.1 / glm-4.5 / any model
//     with separate reasoning): forwarded as-is to the second arg.
//   - delta.content containing <think>...</think> (qwen3-* and other
//     inline-style models): a stateful extractor splits tag bytes
//     across SSE chunk boundaries, then routes content/reasoning to
//     the first/second sender arg respectively.
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (n *NovitaModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if sender == nil {
		return fmt.Errorf("sender is required")
	}
	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Chat)

	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": msg.Content,
		}
	}

	reqBody := map[string]interface{}{
		"model":    modelName,
		"messages": apiMessages,
		"stream":   true,
	}

	if chatModelConfig != nil {
		if chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
			return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
		}
		if chatModelConfig.MaxTokens != nil {
			reqBody["max_tokens"] = *chatModelConfig.MaxTokens
		}
		if chatModelConfig.Temperature != nil {
			reqBody["temperature"] = *chatModelConfig.Temperature
		}
		if chatModelConfig.TopP != nil {
			reqBody["top_p"] = *chatModelConfig.TopP
		}
		if chatModelConfig.Stop != nil {
			reqBody["stop"] = *chatModelConfig.Stop
		}
		// See ChatWithMessages for why we forward this.
		if chatModelConfig.Thinking != nil {
			reqBody["enable_thinking"] = *chatModelConfig.Thinking
		}
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequestWithContext(context.Background(), "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	extractor := &novitaThinkExtractor{}
	sawTerminal := false
	done, err := ParseSSEStream[map[string]interface{}](resp.Body, func(event map[string]interface{}) error {
		choices, ok := event["choices"].([]interface{})
		if !ok || len(choices) == 0 {
			return nil
		}
		firstChoice, ok := choices[0].(map[string]interface{})
		if !ok {
			return nil
		}
		delta, ok := firstChoice["delta"].(map[string]interface{})
		if !ok {
			return nil
		}
		// deepseek-v3.1 / glm-4.5 (and other models that emit reasoning
		// separately) put chain-of-thought in delta.reasoning_content
		// rather than inside content as <think>...</think>. Surface it
		// before any content from the same chunk so callers piping to
		// a UI render reasoning before the visible answer for that
		// token, matching the wire ordering Novita emits.
		if r, ok := delta["reasoning_content"].(string); ok && r != "" {
			rr := r
			if err := sender(nil, &rr); err != nil {
				return err
			}
		}
		if c, ok := delta["content"].(string); ok && c != "" {
			for _, seg := range extractor.Feed(c) {
				if seg.reasoning != "" {
					r := seg.reasoning
					if err := sender(nil, &r); err != nil {
						return err
					}
				}
				if seg.content != "" {
					cc := seg.content
					if err := sender(&cc, nil); err != nil {
						return err
					}
				}
			}
		}
		if finish, ok := firstChoice["finish_reason"].(string); ok && finish != "" {
			sawTerminal = true
		}
		return nil
	})
	if err != nil {
		return fmt.Errorf("failed to scan response body: %w", err)
	}

	// Flush any buffered tail (rare, but covers the case where the
	// stream ends right after the last chunk without us seeing the
	// closing tag).
	if seg := extractor.Flush(); seg != nil {
		if seg.reasoning != "" {
			r := seg.reasoning
			if err := sender(nil, &r); err != nil {
				return err
			}
		}
		if seg.content != "" {
			cc := seg.content
			if err := sender(&cc, nil); err != nil {
				return err
			}
		}
	}

	if !done && !sawTerminal {
		return fmt.Errorf("novita: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}

	return nil
}

// ListModels 列出 API Key 可见的模型 ID
// ListModels 列出当前 API Key 可见的模型目录
func (n *NovitaModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Models)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Parse response
	var modelList ModelList
	if err = json.Unmarshal(body, &modelList); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if modelList.Models == nil {
		return nil, fmt.Errorf("invalid models list format")
	}

	return ParseListModel(modelList), nil
}

// CheckConnection 轻量 ListModels 探活验证密钥
// CheckConnection 轻量探活，验证密钥与端点可用
func (n *NovitaModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := n.ListModels(apiConfig)
	return err
}

type novitaEmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Object    string    `json:"object"`
	Index     int       `json:"index"`
}

type novitaEmbeddingResponse struct {
	Data   []novitaEmbeddingData `json:"data"`
	Model  string                `json:"model"`
	Object string                `json:"object"`
}

// Embed 调用 Novita embeddings API 批量向量化
// /v3/embeddings endpoint. The output has one vector per input, in the
// same order the inputs were given.
// Embed 将文本列表编码为向量嵌入
func (n *NovitaModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Embedding)

	reqBody := map[string]interface{}{
		"model": *modelName,
		"input": texts,
	}
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody["dimensions"] = embeddingConfig.Dimension
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Novita embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed novitaEmbeddingResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	embeddings := make([]EmbeddingData, len(texts))
	filled := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index < 0 || item.Index >= len(texts) {
			return nil, fmt.Errorf("novita: response index %d out of range for %d inputs", item.Index, len(texts))
		}
		if filled[item.Index] {
			return nil, fmt.Errorf("novita: duplicate embedding index %d in response", item.Index)
		}
		embeddings[item.Index] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		}
		filled[item.Index] = true
	}
	for i, ok := range filled {
		if !ok {
			return nil, fmt.Errorf("novita: missing embedding for input index %d", i)
		}
	}

	return embeddings, nil
}

type novitaRerankResult struct {
	Document struct {
		Text string `json:"text"`
	} `json:"document"`
	Index          int     `json:"index"`
	RelevanceScore float64 `json:"relevance_score"`
}

type novitaRerankResponse struct {
	Results []novitaRerankResult `json:"results"`
}

// Rerank 调用 Novita rerank API 对文档相关性打分
// /openai/v1/rerank endpoint and returns one RerankResult per scored
// document in the API's ranking order. Caller may sort by Index to
// recover original input order.
// Rerank 对候选文档按 query 相关性重排序
func (n *NovitaModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(documents) == 0 {
		return &RerankResponse{}, nil
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	if n.baseModel.URLSuffix.Rerank == "" {
		return nil, fmt.Errorf("novita: no rerank URL suffix configured")
	}
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Rerank)

	topN := len(documents)
	if rerankConfig != nil && rerankConfig.TopN > 0 && rerankConfig.TopN < topN {
		topN = rerankConfig.TopN
	}

	reqBody := map[string]interface{}{
		"model":     *modelName,
		"query":     query,
		"documents": documents,
		"top_n":     topN,
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Novita rerank API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed novitaRerankResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	rerankResponse := RerankResponse{Data: make([]RerankResult, 0, len(parsed.Results))}
	seen := make([]bool, len(documents))
	for _, item := range parsed.Results {
		if item.Index < 0 || item.Index >= len(documents) {
			return nil, fmt.Errorf("novita: rerank index %d out of range for %d inputs", item.Index, len(documents))
		}
		if seen[item.Index] {
			return nil, fmt.Errorf("novita: duplicate rerank index %d in response", item.Index)
		}
		rerankResponse.Data = append(rerankResponse.Data, RerankResult{
			Index:          item.Index,
			RelevanceScore: item.RelevanceScore,
		})
		seen[item.Index] = true
	}

	return &rerankResponse, nil
}

// Balance 查询 Novita 账户剩余额度
// Balance 查询账户余额（若上游支持）
func (n *NovitaModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	baseURL, err := n.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", baseURL, n.baseModel.URLSuffix.Balance)

	// Build request body
	reqBody := map[string]interface{}{}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequest("GET", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Parse response
	var result map[string]interface{}
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	balanceInterface, exists := result["availableBalance"]
	if !exists || balanceInterface == nil {
		return nil, fmt.Errorf("missing 'availableBalance' in response. Raw body: %s", string(body))
	}

	balanceStr, ok := balanceInterface.(string)
	if !ok {
		return nil, fmt.Errorf("'availableBalance' is not a string. Raw body: %s", string(body))
	}
	balance, err := strconv.ParseFloat(balanceStr, 64)
	if err != nil {
		return nil, fmt.Errorf("failed to parse 'availableBalance' as float: %w. Raw body: %s", err, string(body))
	}

	var response = map[string]interface{}{
		"balance":  balance,
		"currency": "USD",
	}

	return response, nil
}

// TranscribeAudio 语音转文字（ASR）
func (n *NovitaModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (n *NovitaModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", n.Name())
}

// AudioSpeech 文字转语音（TTS）
func (n *NovitaModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (n *NovitaModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", n.Name())
}

// OCRFile Novita 暂不支持 OCR
// OCRFile 对图片/PDF 执行 OCR 识别
func (n *NovitaModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ParseFile Novita 暂不支持文档解析
// ParseFile 解析文档为结构化文本
func (n *NovitaModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ListTasks 列出异步任务状态
func (n *NovitaModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (n *NovitaModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// Novita 驱动实现 Chat/Embed/Rerank/Balance/ListModels/CheckConnection；非流式与流式路径均解析 <think> 推理块；ASR/TTS/OCR/ParseFile 返回不支持。
