// types.go — LLM 模型驱动核心类型：ModelDriver 接口、Message/ChatConfig/APIConfig 及 ChatModel/EmbeddingModel 包装器。

package models

import "encoding/json"

// Message 对话消息（role + 多态 content，支持纯文本与多模态）
//
// Content 为 interface{} 以兼容多种上游格式：
//   - string: plain text message (e.g., "Hello")
//   - []interface{}: multimodal content array where each element is map[string]interface{}
//     (e.g., [{"type": "text", "text": "..."}, {"type": "image_url", "image_url": {"url": "..."}}])
type Message struct {
	Role       string                   `json:"role"`
	Content    interface{}              `json:"content"`
	ToolCallID string                   `json:"tool_call_id,omitempty"`
	ToolCalls  []map[string]interface{} `json:"tool_calls,omitempty"`
}

// ToolCallSession 对齐 Python mcp_tool_call_conn.ToolCallSession 协议。
type ToolCallSession interface {
	ToolCall(name string, arguments map[string]interface{}) (string, error)
}

// ModelDriver 统一 LLM/Embed/Rerank/ASR/TTS/OCR 等能力入口
type ModelDriver interface {
	NewInstance(baseURL map[string]string) ModelDriver

	Name() string

	// ChatWithMessages 同步多轮对话
	ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error)
	// ChatStreamlyWithSender 流式多轮对话，经 sender 推送增量
	ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error
	// Embed 批量文本向量化
	Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error)
	// Rerank 查询与文档相关性重排
	Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error)
	// TranscribeAudio 语音转文字
	TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error)
	TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error
	// AudioSpeech 文字转语音
	AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error)
	AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error
	// OCRFile 光学字符识别
	OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error)
	// ParseFile 文档结构化解析
	ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error)
	// ListModels 列出可用模型
	ListModels(apiConfig *APIConfig) ([]ListModelResponse, error)

	Balance(apiConfig *APIConfig) (map[string]interface{}, error)

	CheckConnection(apiConfig *APIConfig) error

	ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error)

	ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error)
}

type ChatResponse struct {
	Answer        *string                  `json:"answer"`
	ReasonContent *string                  `json:"reason_content"`
	ToolCalls     []map[string]interface{} `json:"tool_calls,omitempty"`
	Usage         *ChatUsage               `json:"usage,omitempty"`
}

// ChatUsage 单次 LLM 调用的 token 用量拆分，供 Langfuse 与运行聚合使用；
// LLMBundle for accurate Langfuse reporting and run aggregation.
// Mirrors Python's common.token_utils.usage_from_response() split.
type ChatUsage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

type EmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Index     int       `json:"index"`
}

type RerankResult struct {
	Index          int     `json:"index"`
	RelevanceScore float64 `json:"relevance_score"`
}

type RerankResponse struct {
	Data []RerankResult `json:"data"`
}

type ASRResponse struct {
	Text string `json:"text"`
}

type TTSResponse struct {
	Audio []byte `json:"audio"`
}

type OCRFileResponse struct {
	Text *string `json:"text"`
}

type ListModelResponse struct {
	Name         string         `json:"name"`
	MaxTokens    *int           `json:"max_tokens"`
	ModelTypes   []string       `json:"model_types"`
	Thinking     *ModelThinking `json:"thinking"`
	MaxDimension *int           `json:"max_dimension"` // 嵌入模型最大维度
	Dimensions   []int          `json:"dimensions"`
}

type ParseFileResponse struct {
	TaskID string `json:"task_id"`
}

type ListTaskStatus struct {
	TaskID string `json:"task_id"`
	Status string `json:"status"`
}

type TaskSegment struct {
	Index   int    `json:"index"`
	Content string `json:"content"`
}

type TaskResponse struct {
	Segments []TaskSegment `json:"segments"`
}

type ModelList struct {
	Object string    `json:"object"`
	Models []DSModel `json:"data"`
}

// URLSuffix 各 API 端点的 URL 后缀（chat/embed/rerank/asr/tts 等）
type URLSuffix struct {
	Chat          string `json:"chat"`
	AsyncChat     string `json:"async_chat"`
	AsyncResult   string `json:"async_result"`
	Embedding     string `json:"embedding"`
	Rerank        string `json:"rerank"`
	TTS           string `json:"tts"`
	ASR           string `json:"asr"`
	OCR           string `json:"ocr"`
	DocumentParse string `json:"doc_parse"`
	Models        string `json:"models"`
	Balance       string `json:"balance"`
	Files         string `json:"files"`
	Status        string `json:"status"`
	Tasks         string `json:"tasks"`
	Task          string `json:"task"`
}

type ChatConfig struct {
	Stream          *bool
	Vision          *bool
	Thinking        *bool
	MaxTokens       *int
	Temperature     *float64
	TopP            *float64
	DoSample        *bool
	Stop            *[]string
	ModelClass      *string
	Effort          *string
	Verbosity       *string
	Tools           interface{}               `json:"tools,omitempty"`
	ToolChoice      *string                   `json:"tool_choice,omitempty"`
	ToolCallsResult *[]map[string]interface{} `json:"-"`
	// UsageResult 流式结束时接收 include_usage 提取的 token 统计；
	// streaming chunk when stream_options.include_usage is true.
	// The ChatStreamlyWithSender driver writes to this pointer (if
	// non-nil) after the stream completes; callers read it the same
	// way they read ToolCallsResult.
	UsageResult *ChatUsage `json:"-"`
}

type APIConfig struct {
	ApiKey  *string
	Region  *string
	BaseURL *string
}

type EmbeddingConfig struct {
	Dimension int
}

type RerankConfig struct {
	TopN int
}

type ASRConfig struct {
	Params map[string]interface{} `json:"params"`
}

type TTSConfig struct {
	Format string                 `json:"format"`
	Params map[string]interface{} `json:"params"`
}

type OCRConfig struct {
	Algorithm string
}

type ParseFileConfig struct {
}

// EmbeddingModel 包装 ModelDriver 并附加嵌入模型配置
type EmbeddingModel struct {
	ModelDriver ModelDriver
	ModelName   *string
	APIConfig   *APIConfig
	MaxTokens   int // 嵌入模型最大输入 token，用于截断
}

// NewEmbeddingModel 创建嵌入模型包装器
func NewEmbeddingModel(driver ModelDriver, modelName *string, apiConfig *APIConfig, maxTokens int) *EmbeddingModel {
	return &EmbeddingModel{
		ModelDriver: driver,
		ModelName:   modelName,
		APIConfig:   apiConfig,
		MaxTokens:   maxTokens,
	}
}

// RerankModel 包装 ModelDriver 并附加重排模型配置
type RerankModel struct {
	ModelDriver ModelDriver
	ModelName   *string
	APIConfig   *APIConfig
}

// NewRerankModel 创建重排模型包装器
func NewRerankModel(driver ModelDriver, modelName *string, apiConfig *APIConfig) *RerankModel {
	return &RerankModel{
		ModelDriver: driver,
		ModelName:   modelName,
		APIConfig:   apiConfig,
	}
}

// Rerank 委托底层 ModelDriver 执行 query-documents 重排
func (r *RerankModel) Rerank(query string, texts []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return r.ModelDriver.Rerank(r.ModelName, query, texts, apiConfig, rerankConfig)
}

// ToolConfig 聚合 ChatModel 的工具调用配置（JSON 工具列表、轮次与重试上限）。
type ToolConfig struct {
	Tools           string          // JSON-encoded tools list
	MaxRounds       int             // max tool-calling rounds (default: 5)
	MaxRetries      int             // max retries on failure (default: 3)
	ToolCallSession ToolCallSession // session that executes tool calls
}

// ChatModel 包装 ModelDriver 并附加对话配置与工具调用状态
type ChatModel struct {
	ModelDriver ModelDriver
	ModelName   *string
	APIConfig   *APIConfig
	ToolConfig  *ToolConfig
	// LastUsage 最近一次对话调用的 token 用量，每次调用前重置；
	// recent chat call. Consumed by callers for accurate Langfuse reporting
	// and per-run token aggregation. Reset before each call.
	LastUsage *ChatUsage
}

// NewChatModel 创建对话模型包装器
func NewChatModel(driver ModelDriver, modelName *string, apiConfig *APIConfig) *ChatModel {
	return &ChatModel{
		ModelDriver: driver,
		ModelName:   modelName,
		APIConfig:   apiConfig,
	}
}

// BindTools 注册可调用工具，对齐 Python Base.bind_tools()。
// Mirrors Python's Base.bind_tools() in rag/llm/chat_model.py.
func (cm *ChatModel) BindTools(session ToolCallSession, tools interface{}) {
	// Serialize tools to JSON if it's a list/map.
	toolsJSON := ""
	switch v := tools.(type) {
	case string:
		toolsJSON = v
	case []byte:
		toolsJSON = string(v)
	default:
		if b, err := json.Marshal(tools); err == nil {
			toolsJSON = string(b)
		}
	}
	cm.ToolConfig = &ToolConfig{
		Tools:           toolsJSON,
		MaxRounds:       defaultMaxRounds,
		MaxRetries:      defaultMaxRetries,
		ToolCallSession: session,
	}
}

// ModelDriver 为各厂商驱动的统一契约；Message.Content 支持 string 或多模态数组。ChatModel.BindTools 序列化 tools 并绑定 ToolCallSession；URLSuffix 由 factory 按厂商注入相对路径。
