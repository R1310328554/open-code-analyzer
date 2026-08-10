// openai 包提供与 OpenAI REST API 部分兼容的核心转换逻辑。
// openai package provides core transformation logic for partial compatibility with the OpenAI REST API
package openai

import (
	"bytes"
	"cmp"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"log/slog"
	"net/http"
	"slices"
	"strings"
	"time"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/types/model"
)

// Error 表示 OpenAI 风格 API 错误详情。
type Error struct {
	Message string  `json:"message"`
	Type    string  `json:"type"`
	Param   any     `json:"param"`
	Code    *string `json:"code"`
}

// ErrorResponse 为错误响应包装。
type ErrorResponse struct {
	Error Error `json:"error"`
}

// Message 表示聊天消息（角色、内容、工具调用等）。
type Message struct {
	Role       string     `json:"role"`
	Content    any        `json:"content"`
	Reasoning  string     `json:"reasoning,omitempty"`
	ToolCalls  []ToolCall `json:"tool_calls,omitempty"`
	Name       string     `json:"name,omitempty"`
	ToolCallID string     `json:"tool_call_id,omitempty"`
}

// Delta 用于流式 chunk 响应；字段 omitempty 使结束 chunk 的 delta 为 {}。
// Delta is used in streaming chunk responses. All fields use omitempty so
// that a finish chunk produces a truly empty delta `{}` matching the OpenAI spec.
type Delta struct {
	Role      string     `json:"role,omitempty"`
	Content   any        `json:"content,omitempty"`
	Reasoning string     `json:"reasoning,omitempty"`
	ToolCalls []ToolCall `json:"tool_calls,omitempty"`
}

// ChoiceLogprobs 包装选项的对数概率。
type ChoiceLogprobs struct {
	Content []api.Logprob `json:"content"`
}

// Choice 表示非流式 chat completion 的单个选项。
type Choice struct {
	Index        int             `json:"index"`
	Message      Message         `json:"message"`
	FinishReason *string         `json:"finish_reason"`
	Logprobs     *ChoiceLogprobs `json:"logprobs,omitempty"`
}

// ChunkChoice 表示流式 chunk 的单个选项。
type ChunkChoice struct {
	Index        int             `json:"index"`
	Delta        Delta           `json:"delta"`
	FinishReason *string         `json:"finish_reason"`
	Logprobs     *ChoiceLogprobs `json:"logprobs,omitempty"`
}

// CompleteChunkChoice 表示文本补全流/非流式选项。
type CompleteChunkChoice struct {
	Text         string          `json:"text"`
	Index        int             `json:"index"`
	FinishReason *string         `json:"finish_reason"`
	Logprobs     *ChoiceLogprobs `json:"logprobs,omitempty"`
}

// Usage 表示 token 用量统计。
type Usage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

// ResponseFormat 指定结构化输出格式。
type ResponseFormat struct {
	Type       string      `json:"type"`
	JsonSchema *JsonSchema `json:"json_schema,omitempty"`
}

// JsonSchema 携带 JSON Schema 定义。
type JsonSchema struct {
	Schema json.RawMessage `json:"schema"`
}

// EmbedRequest 表示嵌入 API 请求。
type EmbedRequest struct {
	Input          any    `json:"input"`
	Model          string `json:"model"`
	Dimensions     int    `json:"dimensions,omitempty"`
	EncodingFormat string `json:"encoding_format,omitempty"` // "float" or "base64"
}

// StreamOptions 控制流式响应选项（如是否包含 usage）。
type StreamOptions struct {
	IncludeUsage bool `json:"include_usage"`
}

// Reasoning 表示推理/思考 effort 配置。
type Reasoning struct {
	Effort string `json:"effort,omitempty"`
}

// ChatCompletionRequest 表示 /v1/chat/completions 请求。
type ChatCompletionRequest struct {
	Model            string          `json:"model"`
	Messages         []Message       `json:"messages"`
	Stream           bool            `json:"stream"`
	StreamOptions    *StreamOptions  `json:"stream_options"`
	MaxTokens        *int            `json:"max_tokens"`
	Seed             *int            `json:"seed"`
	Stop             any             `json:"stop"`
	Temperature      *float64        `json:"temperature"`
	FrequencyPenalty *float64        `json:"frequency_penalty"`
	PresencePenalty  *float64        `json:"presence_penalty"`
	TopP             *float64        `json:"top_p"`
	ResponseFormat   *ResponseFormat `json:"response_format"`
	Tools            []api.Tool      `json:"tools"`
	Reasoning        *Reasoning      `json:"reasoning,omitempty"`
	ReasoningEffort  *string         `json:"reasoning_effort,omitempty"`
	Logprobs         *bool           `json:"logprobs"`
	TopLogprobs      int             `json:"top_logprobs"`
	DebugRenderOnly  bool            `json:"_debug_render_only"`
}

// ChatCompletion 表示完整 chat completion 响应。
type ChatCompletion struct {
	Id                string         `json:"id"`
	Object            string         `json:"object"`
	Created           int64          `json:"created"`
	Model             string         `json:"model"`
	SystemFingerprint string         `json:"system_fingerprint"`
	Choices           []Choice       `json:"choices"`
	Usage             Usage          `json:"usage,omitempty"`
	DebugInfo         *api.DebugInfo `json:"_debug_info,omitempty"`
}

// ChatCompletionChunk 表示流式 chat completion chunk。
type ChatCompletionChunk struct {
	Id                string        `json:"id"`
	Object            string        `json:"object"`
	Created           int64         `json:"created"`
	Model             string        `json:"model"`
	SystemFingerprint string        `json:"system_fingerprint"`
	Choices           []ChunkChoice `json:"choices"`
	Usage             *Usage        `json:"usage,omitempty"`
}

// TODO (https://github.com/ollama/ollama/issues/5259): support []string, []int and [][]int
// CompletionRequest 表示 /v1/completions 请求。
type CompletionRequest struct {
	Model            string         `json:"model"`
	Prompt           string         `json:"prompt"`
	FrequencyPenalty float32        `json:"frequency_penalty"`
	MaxTokens        *int           `json:"max_tokens"`
	PresencePenalty  float32        `json:"presence_penalty"`
	Seed             *int           `json:"seed"`
	Stop             any            `json:"stop"`
	Stream           bool           `json:"stream"`
	StreamOptions    *StreamOptions `json:"stream_options"`
	Temperature      *float32       `json:"temperature"`
	TopP             float32        `json:"top_p"`
	Suffix           string         `json:"suffix"`
	Logprobs         *int           `json:"logprobs"`
	DebugRenderOnly  bool           `json:"_debug_render_only"`
}

// Completion 表示完整文本补全响应。
type Completion struct {
	Id                string                `json:"id"`
	Object            string                `json:"object"`
	Created           int64                 `json:"created"`
	Model             string                `json:"model"`
	SystemFingerprint string                `json:"system_fingerprint"`
	Choices           []CompleteChunkChoice `json:"choices"`
	Usage             Usage                 `json:"usage,omitempty"`
}

// CompletionChunk 表示流式文本补全 chunk。
type CompletionChunk struct {
	Id                string                `json:"id"`
	Object            string                `json:"object"`
	Created           int64                 `json:"created"`
	Choices           []CompleteChunkChoice `json:"choices"`
	Model             string                `json:"model"`
	SystemFingerprint string                `json:"system_fingerprint"`
	Usage             *Usage                `json:"usage,omitempty"`
}

// ToolCall 表示 OpenAI 格式的工具调用。
type ToolCall struct {
	ID       string `json:"id"`
	Index    int    `json:"index"`
	Type     string `json:"type"`
	Function struct {
		Name      string `json:"name"`
		Arguments string `json:"arguments"`
	} `json:"function"`
}

// Model 表示模型列表项。
type Model struct {
	Id      string `json:"id"`
	Object  string `json:"object"`
	Created int64  `json:"created"`
	OwnedBy string `json:"owned_by"`
}

// Embedding 表示单条嵌入向量（float 或 base64）。
type Embedding struct {
	Object    string `json:"object"`
	Embedding any    `json:"embedding"` // Can be []float32 (float format) or string (base64 format)
	Index     int    `json:"index"`
}

// ListCompletion 表示 /v1/models 列表响应。
type ListCompletion struct {
	Object string  `json:"object"`
	Data   []Model `json:"data"`
}

// EmbeddingList 表示嵌入 API 列表响应。
type EmbeddingList struct {
	Object string         `json:"object"`
	Data   []Embedding    `json:"data"`
	Model  string         `json:"model"`
	Usage  EmbeddingUsage `json:"usage,omitempty"`
}

// EmbeddingUsage 表示嵌入请求的 token 用量。
type EmbeddingUsage struct {
	PromptTokens int `json:"prompt_tokens"`
	TotalTokens  int `json:"total_tokens"`
}

// NewError 按 HTTP 状态码构造 OpenAI 风格错误响应。
func NewError(code int, message string) ErrorResponse {
	var etype string
	switch code {
	case http.StatusBadRequest:
		etype = "invalid_request_error"
	case http.StatusNotFound:
		etype = "not_found_error"
	default:
		etype = "api_error"
	}

	return ErrorResponse{Error{Type: etype, Message: message}}
}

// ToUsage 将 api.ChatResponse 指标转换为 Usage。
// ToUsage converts an api.ChatResponse to Usage
func ToUsage(r api.ChatResponse) Usage {
	return Usage{
		PromptTokens:     r.Metrics.PromptEvalCount,
		CompletionTokens: r.Metrics.EvalCount,
		TotalTokens:      r.Metrics.PromptEvalCount + r.Metrics.EvalCount,
	}
}

// ToToolCalls 将 api.ToolCall 转为 OpenAI ToolCall 格式。
// ToToolCalls converts api.ToolCall to OpenAI ToolCall format
func ToToolCalls(tc []api.ToolCall) []ToolCall {
	toolCalls := make([]ToolCall, len(tc))
	for i, tc := range tc {
		toolCalls[i].ID = tc.ID
		toolCalls[i].Type = "function"
		toolCalls[i].Function.Name = tc.Function.Name
		toolCalls[i].Index = tc.Function.Index

		args, err := json.Marshal(tc.Function.Arguments)
		if err != nil {
			slog.Error("could not marshall function arguments to json", "error", err)
			continue
		}

		toolCalls[i].Function.Arguments = string(args)
	}
	return toolCalls
}

// ToChatCompletion 将 api.ChatResponse 转为 ChatCompletion。
// ToChatCompletion converts an api.ChatResponse to ChatCompletion
func ToChatCompletion(id string, r api.ChatResponse) ChatCompletion {
	toolCalls := ToToolCalls(r.Message.ToolCalls)

	var logprobs *ChoiceLogprobs
	if len(r.Logprobs) > 0 {
		logprobs = &ChoiceLogprobs{Content: r.Logprobs}
	}

	return ChatCompletion{
		Id:                id,
		Object:            "chat.completion",
		Created:           r.CreatedAt.Unix(),
		Model:             r.Model,
		SystemFingerprint: "fp_ollama",
		Choices: []Choice{{
			Index:   0,
			Message: Message{Role: r.Message.Role, Content: r.Message.Content, ToolCalls: toolCalls, Reasoning: r.Message.Thinking},
			FinishReason: func(reason string) *string {
				if reason == "stop" && len(toolCalls) > 0 {
					reason = "tool_calls"
				}
				if len(reason) > 0 {
					return &reason
				}
				return nil
			}(r.DoneReason),
			Logprobs: logprobs,
		}}, Usage: ToUsage(r),
		DebugInfo: r.DebugInfo,
	}
}

// toChunk 构造单个 ChatCompletionChunk。
func toChunk(id string, r api.ChatResponse, includeRole bool) ChatCompletionChunk {
	toolCalls := ToToolCalls(r.Message.ToolCalls)

	var logprobs *ChoiceLogprobs
	if len(r.Logprobs) > 0 {
		logprobs = &ChoiceLogprobs{Content: r.Logprobs}
	}

	var role string
	if includeRole {
		role = "assistant"
	}

	// Content is typed as any with omitempty: nil is omitted, "" is kept.
	// Use the string value from the response so empty-string content (e.g. first
	// chunk or reasoning-only) is explicitly serialized as "content":"".
	var content any = r.Message.Content

	// Stamp the chunk with the response's timestamp; OpenAI reuses one created
	// value across a stream. Fall back to now when the response carries none
	// (e.g. synthetic responses).
	created := r.CreatedAt.Unix()
	if r.CreatedAt.IsZero() {
		created = time.Now().Unix()
	}

	return ChatCompletionChunk{
		Id:                id,
		Object:            "chat.completion.chunk",
		Created:           created,
		Model:             r.Model,
		SystemFingerprint: "fp_ollama",
		Choices: []ChunkChoice{{
			Index:    0,
			Delta:    Delta{Role: role, Content: content, ToolCalls: toolCalls, Reasoning: r.Message.Thinking},
			Logprobs: logprobs,
		}},
	}
}

// ToStreamChunks 将响应转为一个或多个 chunk；混合思考/正文时拆成多块。
// ToStreamChunks converts an api.ChatResponse to one or more ChatCompletionChunk values.
// includeRole controls whether the "role" field appears in the delta (should be true
// only for the first chunk in a stream, matching the OpenAI spec).
func ToStreamChunks(id string, r api.ChatResponse, includeRole bool) []ChatCompletionChunk {
	hasMixedResponse := r.Message.Thinking != "" && (r.Message.Content != "" || len(r.Message.ToolCalls) > 0)
	if !hasMixedResponse {
		return []ChatCompletionChunk{toChunk(id, r, includeRole)}
	}

	reasoningChunk := toChunk(id, r, includeRole)
	// The logprobs here might include tokens not in this chunk because we now split between thinking and content/tool calls.
	reasoningChunk.Choices[0].Delta.Content = nil
	reasoningChunk.Choices[0].Delta.ToolCalls = nil

	contentOrToolCallsChunk := toChunk(id, r, false)
	// Keep both split chunks on the same timestamp since they represent one logical emission.
	contentOrToolCallsChunk.Created = reasoningChunk.Created
	contentOrToolCallsChunk.Choices[0].Delta.Reasoning = ""
	contentOrToolCallsChunk.Choices[0].Logprobs = nil

	return []ChatCompletionChunk{
		reasoningChunk,
		contentOrToolCallsChunk,
	}
}

// FinishChunk 创建仅含 finish_reason 的空 delta 结束 chunk。
// FinishChunk creates a dedicated finish-reason chunk with an empty delta,
// matching the OpenAI spec where finish_reason is sent on its own chunk.
func FinishChunk(id string, r api.ChatResponse, toolCallSent bool) ChatCompletionChunk {
	// Only remap known terminal reasons; pass anything else through untouched.
	// tool_calls only overrides stop — an unfinished or unknown done reason
	// must not be relabeled tool_calls.
	reason := cmp.Or(r.DoneReason, "stop")
	if reason == "stop" && toolCallSent {
		reason = "tool_calls"
	}
	// Stamp the chunk with the completion's timestamp like OpenAI does; fall
	// back to now when the response carries none (e.g. synthetic responses).
	created := r.CreatedAt.Unix()
	if r.CreatedAt.IsZero() {
		created = time.Now().Unix()
	}
	return ChatCompletionChunk{
		Id:                id,
		Object:            "chat.completion.chunk",
		Created:           created,
		Model:             r.Model,
		SystemFingerprint: "fp_ollama",
		Choices: []ChunkChoice{{
			Index:        0,
			Delta:        Delta{},
			FinishReason: &reason,
		}},
	}
}

// ToUsageGenerate 将 GenerateResponse 指标转为 Usage。
// ToUsageGenerate converts an api.GenerateResponse to Usage
func ToUsageGenerate(r api.GenerateResponse) Usage {
	return Usage{
		PromptTokens:     r.Metrics.PromptEvalCount,
		CompletionTokens: r.Metrics.EvalCount,
		TotalTokens:      r.Metrics.PromptEvalCount + r.Metrics.EvalCount,
	}
}

// ToCompletion 将 GenerateResponse 转为 Completion。
// ToCompletion converts an api.GenerateResponse to Completion
func ToCompletion(id string, r api.GenerateResponse) Completion {
	return Completion{
		Id:                id,
		Object:            "text_completion",
		Created:           r.CreatedAt.Unix(),
		Model:             r.Model,
		SystemFingerprint: "fp_ollama",
		Choices: []CompleteChunkChoice{{
			Text:  r.Response,
			Index: 0,
			FinishReason: func(reason string) *string {
				if len(reason) > 0 {
					return &reason
				}
				return nil
			}(r.DoneReason),
		}},
		Usage: ToUsageGenerate(r),
	}
}

// ToCompleteChunk 将 GenerateResponse 转为 CompletionChunk。
// ToCompleteChunk converts an api.GenerateResponse to CompletionChunk
func ToCompleteChunk(id string, r api.GenerateResponse) CompletionChunk {
	return CompletionChunk{
		Id:                id,
		Object:            "text_completion",
		Created:           time.Now().Unix(),
		Model:             r.Model,
		SystemFingerprint: "fp_ollama",
		Choices: []CompleteChunkChoice{{
			Text:  r.Response,
			Index: 0,
			FinishReason: func(reason string) *string {
				if len(reason) > 0 {
					return &reason
				}
				return nil
			}(r.DoneReason),
		}},
	}
}

// ToListCompletion 将 api.ListResponse 转为 ListCompletion。
// ToListCompletion converts an api.ListResponse to ListCompletion
func ToListCompletion(r api.ListResponse) ListCompletion {
	var data []Model
	for _, m := range r.Models {
		id := m.Model
		if id == "" {
			id = m.Name
		}

		data = append(data, Model{
			Id:      id,
			Object:  "model",
			Created: m.ModifiedAt.Unix(),
			OwnedBy: model.ParseName(id).Namespace,
		})
	}

	return ListCompletion{
		Object: "list",
		Data:   data,
	}
}

// ToEmbeddingList 将 EmbedResponse 转为 EmbeddingList（支持 float/base64）。
// ToEmbeddingList converts an api.EmbedResponse to EmbeddingList
// encodingFormat can be "float", "base64", or empty (defaults to "float")
func ToEmbeddingList(model string, r api.EmbedResponse, encodingFormat string) EmbeddingList {
	if r.Embeddings != nil {
		var data []Embedding
		for i, e := range r.Embeddings {
			var embedding any
			if strings.EqualFold(encodingFormat, "base64") {
				embedding = floatsToBase64(e)
			} else {
				embedding = e
			}

			data = append(data, Embedding{
				Object:    "embedding",
				Embedding: embedding,
				Index:     i,
			})
		}

		return EmbeddingList{
			Object: "list",
			Data:   data,
			Model:  model,
			Usage: EmbeddingUsage{
				PromptTokens: r.PromptEvalCount,
				TotalTokens:  r.PromptEvalCount,
			},
		}
	}

	return EmbeddingList{}
}

// floatsToBase64 将 []float32 小端编码为 base64 字符串。
// floatsToBase64 encodes a []float32 to a base64 string
func floatsToBase64(floats []float32) string {
	var buf bytes.Buffer
	binary.Write(&buf, binary.LittleEndian, floats)
	return base64.StdEncoding.EncodeToString(buf.Bytes())
}

// ToModel 将 ShowResponse 转为 Model。
// ToModel converts an api.ShowResponse to Model
func ToModel(r api.ShowResponse, m string) Model {
	return Model{
		Id:      m,
		Object:  "model",
		Created: r.ModifiedAt.Unix(),
		OwnedBy: model.ParseName(m).Namespace,
	}
}

// FromChatRequest 将 OpenAI ChatCompletionRequest 转为 api.ChatRequest。
// FromChatRequest converts a ChatCompletionRequest to api.ChatRequest
func FromChatRequest(r ChatCompletionRequest) (*api.ChatRequest, error) {
	var messages []api.Message
	for _, msg := range r.Messages {
		toolName := ""
		if strings.ToLower(msg.Role) == "tool" {
			toolName = msg.Name
			if toolName == "" && msg.ToolCallID != "" {
				toolName = nameFromToolCallID(r.Messages, msg.ToolCallID)
			}
		}
		switch content := msg.Content.(type) {
		case string:
			toolCalls, err := FromCompletionToolCall(msg.ToolCalls)
			if err != nil {
				return nil, err
			}
			messages = append(messages, api.Message{Role: msg.Role, Content: content, Thinking: msg.Reasoning, ToolCalls: toolCalls, ToolName: toolName, ToolCallID: msg.ToolCallID})
		case []any:
			for _, c := range content {
				data, ok := c.(map[string]any)
				if !ok {
					return nil, errors.New("invalid message format")
				}
				switch data["type"] {
				case "text":
					text, ok := data["text"].(string)
					if !ok {
						return nil, errors.New("invalid message format")
					}
					messages = append(messages, api.Message{Role: msg.Role, Content: text})
				case "image_url":
					var url string
					if urlMap, ok := data["image_url"].(map[string]any); ok {
						if url, ok = urlMap["url"].(string); !ok {
							return nil, errors.New("invalid message format")
						}
					} else {
						if url, ok = data["image_url"].(string); !ok {
							return nil, errors.New("invalid message format")
						}
					}

					img, err := decodeImageURL(url)
					if err != nil {
						return nil, err
					}

					messages = append(messages, api.Message{Role: msg.Role, Images: []api.ImageData{img}})
				case "input_audio":
					audioMap, ok := data["input_audio"].(map[string]any)
					if !ok {
						return nil, errors.New("invalid input_audio format")
					}
					b64Data, ok := audioMap["data"].(string)
					if !ok {
						return nil, errors.New("invalid input_audio format: missing data")
					}
					audioBytes, err := base64.StdEncoding.DecodeString(b64Data)
					if err != nil {
						return nil, fmt.Errorf("invalid input_audio base64 data: %w", err)
					}
					messages = append(messages, api.Message{Role: msg.Role, Images: []api.ImageData{audioBytes}})
				default:
					return nil, errors.New("invalid message format")
				}
			}
			// since we might have added multiple messages above, if we have tools
			// calls we'll add them to the last message
			if len(messages) > 0 && len(msg.ToolCalls) > 0 {
				toolCalls, err := FromCompletionToolCall(msg.ToolCalls)
				if err != nil {
					return nil, err
				}
				messages[len(messages)-1].ToolCalls = toolCalls
				messages[len(messages)-1].ToolName = toolName
				messages[len(messages)-1].ToolCallID = msg.ToolCallID
				messages[len(messages)-1].Thinking = msg.Reasoning
			}
		default:
			// content is only optional if tool calls are present
			if msg.ToolCalls == nil {
				return nil, fmt.Errorf("invalid message content type: %T", content)
			}

			toolCalls, err := FromCompletionToolCall(msg.ToolCalls)
			if err != nil {
				return nil, err
			}
			messages = append(messages, api.Message{Role: msg.Role, Thinking: msg.Reasoning, ToolCalls: toolCalls, ToolCallID: msg.ToolCallID})
		}
	}

	options := make(map[string]any)

	switch stop := r.Stop.(type) {
	case string:
		options["stop"] = []string{stop}
	case []any:
		var stops []string
		for _, s := range stop {
			if str, ok := s.(string); ok {
				stops = append(stops, str)
			}
		}
		options["stop"] = stops
	}

	if r.MaxTokens != nil {
		options["num_predict"] = *r.MaxTokens
	}

	if r.Temperature != nil {
		options["temperature"] = *r.Temperature
	} else {
		options["temperature"] = 1.0
	}

	if r.Seed != nil {
		options["seed"] = *r.Seed
	}

	if r.FrequencyPenalty != nil {
		options["frequency_penalty"] = *r.FrequencyPenalty
	}

	if r.PresencePenalty != nil {
		options["presence_penalty"] = *r.PresencePenalty
	}

	if r.TopP != nil {
		options["top_p"] = *r.TopP
	} else {
		options["top_p"] = 1.0
	}

	var format json.RawMessage
	if r.ResponseFormat != nil {
		switch strings.ToLower(strings.TrimSpace(r.ResponseFormat.Type)) {
		// Support the old "json_object" type for OpenAI compatibility
		case "json_object":
			format = json.RawMessage(`"json"`)
		case "json_schema":
			if r.ResponseFormat.JsonSchema != nil {
				format = r.ResponseFormat.JsonSchema.Schema
			}
		}
	}

	var think *api.ThinkValue
	var effort string

	if r.Reasoning != nil {
		effort = r.Reasoning.Effort
	} else if r.ReasoningEffort != nil {
		effort = *r.ReasoningEffort
	}

	if effort != "" {
		if !slices.Contains([]string{"high", "medium", "low", "max", "none"}, effort) {
			return nil, fmt.Errorf("invalid reasoning value: '%s' (must be \"high\", \"medium\", \"low\", \"max\", or \"none\")", effort)
		}

		if effort == "none" {
			think = &api.ThinkValue{Value: false}
		} else {
			think = &api.ThinkValue{Value: effort}
		}
	}

	return &api.ChatRequest{
		Model:           r.Model,
		Messages:        messages,
		Format:          format,
		Options:         options,
		Stream:          &r.Stream,
		Tools:           r.Tools,
		Think:           think,
		Logprobs:        r.Logprobs != nil && *r.Logprobs,
		TopLogprobs:     r.TopLogprobs,
		DebugRenderOnly: r.DebugRenderOnly,
	}, nil
}

// nameFromToolCallID 按 tool_call_id 逆查函数名（后写覆盖）。
func nameFromToolCallID(messages []Message, toolCallID string) string {
	// iterate backwards to be more resilient to duplicate tool call IDs (this
	// follows "last one wins")
	for i := len(messages) - 1; i >= 0; i-- {
		msg := messages[i]
		for _, tc := range msg.ToolCalls {
			if tc.ID == toolCallID {
				return tc.Function.Name
			}
		}
	}
	return ""
}

// decodeImageURL 将 base64 data URI 解码为原始图像字节。
// decodeImageURL decodes a base64 data URI into raw image bytes.
func decodeImageURL(url string) (api.ImageData, error) {
	if strings.HasPrefix(url, "http://") || strings.HasPrefix(url, "https://") {
		return nil, errors.New("image URLs are not currently supported, please use base64 encoded data instead")
	}

	types := []string{"jpeg", "jpg", "png", "webp"}

	// Support blank mime type to match /api/chat's behavior of taking just unadorned base64
	if strings.HasPrefix(url, "data:;base64,") {
		url = strings.TrimPrefix(url, "data:;base64,")
	} else {
		valid := false
		for _, t := range types {
			prefix := "data:image/" + t + ";base64,"
			if strings.HasPrefix(url, prefix) {
				url = strings.TrimPrefix(url, prefix)
				valid = true
				break
			}
		}
		if !valid {
			return nil, errors.New("invalid image input")
		}
	}

	img, err := base64.StdEncoding.DecodeString(url)
	if err != nil {
		return nil, errors.New("invalid image input")
	}
	return img, nil
}

// FromCompletionToolCall 将 OpenAI ToolCall 转为 api.ToolCall。
// FromCompletionToolCall converts OpenAI ToolCall format to api.ToolCall
func FromCompletionToolCall(toolCalls []ToolCall) ([]api.ToolCall, error) {
	apiToolCalls := make([]api.ToolCall, len(toolCalls))
	for i, tc := range toolCalls {
		apiToolCalls[i].ID = tc.ID
		apiToolCalls[i].Function.Name = tc.Function.Name
		err := json.Unmarshal([]byte(tc.Function.Arguments), &apiToolCalls[i].Function.Arguments)
		if err != nil {
			return nil, errors.New("invalid tool call arguments")
		}
	}

	return apiToolCalls, nil
}

// FromCompleteRequest 将 CompletionRequest 转为 api.GenerateRequest。
// FromCompleteRequest converts a CompletionRequest to api.GenerateRequest
func FromCompleteRequest(r CompletionRequest) (api.GenerateRequest, error) {
	options := make(map[string]any)

	switch stop := r.Stop.(type) {
	case string:
		options["stop"] = []string{stop}
	case []any:
		var stops []string
		for _, s := range stop {
			if str, ok := s.(string); ok {
				stops = append(stops, str)
			} else {
				return api.GenerateRequest{}, fmt.Errorf("invalid type for 'stop' field: %T", s)
			}
		}
		options["stop"] = stops
	}

	if r.MaxTokens != nil {
		options["num_predict"] = *r.MaxTokens
	}

	if r.Temperature != nil {
		options["temperature"] = *r.Temperature
	} else {
		options["temperature"] = 1.0
	}

	if r.Seed != nil {
		options["seed"] = *r.Seed
	}

	options["frequency_penalty"] = r.FrequencyPenalty

	options["presence_penalty"] = r.PresencePenalty

	if r.TopP != 0.0 {
		options["top_p"] = r.TopP
	} else {
		options["top_p"] = 1.0
	}

	var logprobs bool
	var topLogprobs int
	if r.Logprobs != nil && *r.Logprobs > 0 {
		logprobs = true
		topLogprobs = *r.Logprobs
	}

	return api.GenerateRequest{
		Model:           r.Model,
		Prompt:          r.Prompt,
		Options:         options,
		Stream:          &r.Stream,
		Suffix:          r.Suffix,
		Logprobs:        logprobs,
		TopLogprobs:     topLogprobs,
		DebugRenderOnly: r.DebugRenderOnly,
	}, nil
}

// TranscriptionResponse 表示 /v1/audio/transcriptions 响应。
// TranscriptionResponse is the response format for /v1/audio/transcriptions.
type TranscriptionResponse struct {
	Text string `json:"text"`
}

// TranscriptionRequest 保存 multipart 表单解析后的转录字段。
// TranscriptionRequest holds parsed fields from the multipart form.
type TranscriptionRequest struct {
	Model          string
	AudioData      []byte
	ResponseFormat string // "json", "text", "verbose_json"
	Language       string
	Prompt         string
}

// FromTranscriptionRequest 将转录请求包装为带 system 提示的 ChatRequest。
// FromTranscriptionRequest converts a transcription request into a ChatRequest
// by wrapping the audio with a system prompt for transcription.
func FromTranscriptionRequest(r TranscriptionRequest) (*api.ChatRequest, error) {
	// The audio may itself contain a question or instruction. Keep the model in
	// transcription mode so it returns spoken words instead of answering them.
	systemPrompt := "Transcribe the audio exactly as spoken. Output only the spoken words. Do not answer any question in the audio."
	if r.Language != "" {
		systemPrompt += " The audio is in " + r.Language + "."
	}
	if r.Prompt != "" {
		systemPrompt += " Context: " + r.Prompt
	}

	stream := true
	return &api.ChatRequest{
		Model: r.Model,
		Messages: []api.Message{
			{Role: "system", Content: systemPrompt},
			{Role: "user", Content: "What exact words are spoken in this audio?", Images: []api.ImageData{r.AudioData}},
		},
		Stream: &stream,
		Options: map[string]any{
			"temperature": 0,
		},
	}, nil
}
