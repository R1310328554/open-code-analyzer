//go:build windows || darwin

// responses 包定义桌面 UI REST/SSE API 的 JSON 响应类型，供 Go 与 TypeScript 代码生成共享。
package responses

import (
	"time"

	"github.com/ollama/ollama/app/store"
	"github.com/ollama/ollama/types/model"
)

// ChatInfo 聊天会话列表项：ID、标题、用户消息摘要与时间戳。
type ChatInfo struct {
	ID          string    `json:"id"`
	Title       string    `json:"title"`
	UserExcerpt string    `json:"userExcerpt"`
	CreatedAt   time.Time `json:"createdAt" ts_type:"Date" ts_transform:"new Date(__VALUE__)"`
	UpdatedAt   time.Time `json:"updatedAt" ts_type:"Date" ts_transform:"new Date(__VALUE__)"`
}

// ChatsResponse 聊天列表 API 响应体。
type ChatsResponse struct {
	ChatInfos []ChatInfo `json:"chatInfos"`
}

// ChatResponse 单条聊天详情 API 响应体。
type ChatResponse struct {
	Chat store.Chat `json:"chat"`
}

// Model 本地或远程模型标识（名称、摘要与修改时间）。
type Model struct {
	Model      string     `json:"model"`
	Digest     string     `json:"digest,omitempty"`
	ModifiedAt *time.Time `json:"modified_at,omitempty"`
}

// ModelsResponse 模型列表 API 响应体。
type ModelsResponse struct {
	Models []Model `json:"models"`
}

// InferenceCompute 单块推理设备的库、驱动与显存描述。
type InferenceCompute struct {
	Library string `json:"library"`
	Variant string `json:"variant"`
	Compute string `json:"compute"`
	Driver  string `json:"driver"`
	Name    string `json:"name"`
	VRAM    string `json:"vram"`
}

// InferenceComputeResponse 推理算力探测结果与默认上下文长度。
type InferenceComputeResponse struct {
	InferenceComputes    []InferenceCompute `json:"inferenceComputes"`
	DefaultContextLength int                `json:"defaultContextLength"`
}

// ModelCapabilitiesResponse 模型能力标签列表（如 vision、tools）。
type ModelCapabilitiesResponse struct {
	Capabilities []model.Capability `json:"capabilities"`
}

// ChatEvent 流式聊天事件：正文、思考、工具调用与完成等。
type ChatEvent struct {
	EventName string `json:"eventName" ts_type:"\"chat\" | \"thinking\" | \"assistant_with_tools\" | \"tool_call\" | \"tool\" | \"tool_result\" | \"done\" | \"chat_created\""`

	// 聊天/助手消息字段
	Content           *string    `json:"content,omitempty"`
	Thinking          *string    `json:"thinking,omitempty"`
	ThinkingTimeStart *time.Time `json:"thinkingTimeStart,omitempty" ts_type:"Date | undefined" ts_transform:"__VALUE__ && new Date(__VALUE__)"`
	ThinkingTimeEnd   *time.Time `json:"thinkingTimeEnd,omitempty" ts_type:"Date | undefined" ts_transform:"__VALUE__ && new Date(__VALUE__)"`

	// 工具相关字段
	ToolCalls      []store.ToolCall `json:"toolCalls,omitempty"`
	ToolCall       *store.ToolCall  `json:"toolCall,omitempty"`
	ToolName       *string          `json:"toolName,omitempty"`
	ToolResult     *bool            `json:"toolResult,omitempty"`
	ToolResultData any              `json:"toolResultData,omitempty"`

	// 新聊天创建
	ChatID *string `json:"chatId,omitempty"`

	// 浏览器工具状态快照
	ToolState any `json:"toolState,omitempty"`
}

// DownloadEvent 模型下载进度流式事件。
type DownloadEvent struct {
	EventName string `json:"eventName" ts_type:"\"download\""`
	Total     int64  `json:"total" ts_type:"number"`
	Completed int64  `json:"completed" ts_type:"number"`
	Done      bool   `json:"done" ts_type:"boolean"`
}

// ErrorEvent 聊天或 API 错误流式事件。
type ErrorEvent struct {
	EventName string `json:"eventName" ts_type:"\"error\""`
	Error     string `json:"error"`
	Code      string `json:"code,omitempty"`    // 可选错误码，区分错误类型
	Details   string `json:"details,omitempty"` // 可选附加详情
}

// SettingsResponse 设置读写 API 的响应包装。
type SettingsResponse struct {
	Settings store.Settings `json:"settings"`
}

// HealthResponse 健康检查布尔结果。
type HealthResponse struct {
	Healthy bool `json:"healthy"`
}

// User 已登录 Ollama 账户用户信息。
type User struct {
	ID        string `json:"id"`
	Email     string `json:"email"`
	Name      string `json:"name"`
	Bio       string `json:"bio,omitempty"`
	AvatarURL string `json:"avatarurl,omitempty"`
	FirstName string `json:"firstname,omitempty"`
	LastName  string `json:"lastname,omitempty"`
	Plan      string `json:"plan,omitempty"`
}

// Attachment 发送消息时的附件（文件名与 base64 数据）。
type Attachment struct {
	Filename string `json:"filename"`
	Data     string `json:"data,omitempty"` // omitempty：无 data 表示引用已有文件
}

// ChatRequest 向聊天端点发送的用户提示与选项。
type ChatRequest struct {
	Model       string       `json:"model"`
	Prompt      string       `json:"prompt"`
	Index       *int         `json:"index,omitempty"`
	Attachments []Attachment `json:"attachments,omitempty"`
	WebSearch   *bool        `json:"web_search,omitempty"`
	FileTools   *bool        `json:"file_tools,omitempty"`
	ForceUpdate bool         `json:"forceUpdate,omitempty"`
	Think       any          `json:"think,omitempty"`
}

// Error 通用错误消息包装。
type Error struct {
	Error string `json:"error"`
}

// ModelUpstreamResponse 模型在上游 registry 是否过期的探测结果。
type ModelUpstreamResponse struct {
	Stale bool   `json:"stale"`
	Error string `json:"error,omitempty"`
}

// BrowserStateData Agent 浏览器工具的页面栈与 token 视图状态（可序列化）。
type BrowserStateData struct {
	PageStack  []string         `json:"page_stack"`  // 页面 URL 顺序栈
	ViewTokens int              `json:"view_tokens"` // 视口展示的 token 数
	URLToPage  map[string]*Page `json:"url_to_page"` // URL 到页面内容的映射
}

// Page 浏览器工具抓取的网页快照。
type Page struct {
	URL       string         `json:"url"`
	Title     string         `json:"title"`
	Text      string         `json:"text"`
	Lines     []string       `json:"lines"`
	Links     map[int]string `json:"links,omitempty" ts_type:"Record<number, string>"`
	FetchedAt time.Time      `json:"fetched_at"`
}
