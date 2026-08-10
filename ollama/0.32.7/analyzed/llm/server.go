// LlamaServer 接口、模型加载与请求/响应类型定义。
package llm

import (
	"context"
	"encoding/json"
	"errors"
	"log/slog"
	"os"
	"slices"
	"strings"
	"time"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/envconfig"
	"github.com/ollama/ollama/format"
	"github.com/ollama/ollama/fs/ggml"
	"github.com/ollama/ollama/ml"
)

// ErrLoadRequiredFull 表示无法在 GPU 上完整加载模型。
var ErrLoadRequiredFull = errors.New("unable to load full model on GPU")

// filteredEnv 用于 slog 中过滤敏感 GPU/路径环境变量。
type filteredEnv []string

// LogValue 将允许记录的环境变量键值对转为 slog 属性组。
func (e filteredEnv) LogValue() slog.Value {
	var attrs []slog.Attr
	for _, env := range e {
		if key, value, ok := strings.Cut(env, "="); ok {
			if filteredEnvLogKey(key) {
				attrs = append(attrs, slog.String(key, filteredEnvLogValue(key, value)))
			}
		}
	}
	return slog.GroupValue(attrs...)
}

// filteredEnvLogKey 判断环境变量键是否允许写入日志。
func filteredEnvLogKey(key string) bool {
	return strings.HasPrefix(key, "CUDA_") ||
		strings.HasPrefix(key, "ROCR_") ||
		strings.HasPrefix(key, "ROCM_") ||
		strings.HasPrefix(key, "HIP_") ||
		strings.HasPrefix(key, "HSA_") ||
		strings.HasPrefix(key, "GGML_") ||
		slices.Contains([]string{
			"PATH",
			"LD_LIBRARY_PATH",
			"DYLD_LIBRARY_PATH",
		}, key)
}

// filteredEnvLogValue 对含 API/KEY 等敏感词的值脱敏。
func filteredEnvLogValue(key, value string) string {
	for _, token := range []string{"API", "KEY", "TOKEN", "SECRET", "PASSWORD", "PASS", "CREDENTIAL", "AUTH"} {
		if strings.Contains(strings.ToUpper(key), token) {
			return "[redacted]"
		}
	}
	return value
}

// LlamaServer 抽象 llama-server runner 的加载、推理与生命周期操作。
type LlamaServer interface {
	ModelPath() string
	Load(ctx context.Context, systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, requireFull bool) ([]ml.DeviceID, error)
	Ping(ctx context.Context) error
	WaitUntilRunning(ctx context.Context) error
	Completion(ctx context.Context, req CompletionRequest, fn func(CompletionResponse)) error
	Chat(ctx context.Context, req ChatRequest, fn func(ChatResponse)) error
	ApplyChatTemplate(ctx context.Context, req ChatRequest) (string, error)
	Embedding(ctx context.Context, input string) ([]float32, int, error)
	Tokenize(ctx context.Context, content string) ([]int, error)
	Detokenize(ctx context.Context, tokens []int) (string, error)
	Close() error
	MemorySize() (total, vram uint64)
	VRAMByGPU(id ml.DeviceID) uint64
	Pid() int
	GetPort() int
	GetDeviceInfos(ctx context.Context) []ml.DeviceInfo
	HasExited() bool
	ContextLength() int
}

// LlamaServerConfig 控制 Jinja、context shift、MTP 等 runner 行为。
type LlamaServerConfig struct {
	DisableJinja   bool
	ContextShift   bool
	EnableMTP      bool
	DraftModelPath string
}

// LoadModel 从磁盘加载 GGML/GGUF 模型并解码元数据。
// LoadModel will load a model from disk. The model must be in the GGML format.
//
// It collects array values for arrays with a size less than or equal to
// maxArraySize. If maxArraySize is 0, the default value of 1024 is used. If
// the maxArraySize is negative, all arrays are collected.
func LoadModel(model string, maxArraySize int) (*ggml.GGML, error) {
	if _, err := os.Stat(model); err != nil {
		return nil, err
	}

	f, err := os.Open(model)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	return ggml.Decode(f, maxArraySize)
}

// NewLlamaServer 校验上下文长度并创建 llama-server runner。
// NewLlamaServer creates a new llama-server runner for the given model.
// All GGML models are served via the upstream llama-server subprocess.
func NewLlamaServer(systemInfo ml.SystemInfo, gpus []ml.DeviceInfo, modelPath string, f *ggml.GGML, adapters, projectors []string, opts api.Options, numParallel int, config LlamaServerConfig) (LlamaServer, error) {
	slog.Info("using llama-server for model", "model", modelPath)

	// 请求的 num_ctx 不得超过模型训练上下文长度。
	// Verify the requested context size is <= the model training size
	trainCtx := f.KV().ContextLength()
	if opts.NumCtx > int(trainCtx) && trainCtx > 0 {
		slog.Warn("requested context size too large for model", "num_ctx", opts.NumCtx, "n_ctx_train", trainCtx)
		opts.NumCtx = int(trainCtx)
	}

	kvct := strings.ToLower(envconfig.KvCacheType())
	return NewLlamaServerRunner(gpus, modelPath, f, adapters, projectors, opts, numParallel, kvct, config)
}

// 服务器状态枚举
// Server status types

// ServerStatus 表示 llama-server 健康/加载/错误状态。
type ServerStatus int

const (
	ServerStatusReady ServerStatus = iota
	ServerStatusNoSlotsAvailable
	ServerStatusLaunched
	ServerStatusLoadingModel
	ServerStatusNotResponding
	ServerStatusError
)

// String 返回人类可读的状态描述。
func (s ServerStatus) String() string {
	switch s {
	case ServerStatusReady:
		return "llm server ready"
	case ServerStatusNoSlotsAvailable:
		return "llm busy - no slots available"
	case ServerStatusLaunched:
		return "llm server launched"
	case ServerStatusLoadingModel:
		return "llm server loading model"
	case ServerStatusNotResponding:
		return "llm server not responding"
	default:
		return "llm server error"
	}
}

// ServerStatusResponse 对应 /health 端点 JSON 响应。
type ServerStatusResponse struct {
	Status   ServerStatus `json:"status"`
	Progress float32      `json:"progress"`
}

// 推理请求与响应类型
// Request/Response types

const (
	llamaServerStreamInitialBufferSize = 64 * 1024 // SSE 流初始读缓冲
	// llamaServerStreamMaxBufferSize 限制单行 SSE 响应最大 8 MiB。
	// llamaServerStreamMaxBufferSize bounds a single runner response stream line.
	llamaServerStreamMaxBufferSize = 8 * format.MegaByte
)

// MediaKind 区分图像、音频与未知媒体。
type MediaKind string

const (
	MediaKindUnknown MediaKind = ""
	MediaKindImage   MediaKind = "image"
	MediaKindAudio   MediaKind = "audio"
)

// MediaData 携带多模态输入的原始字节、ID 与类型。
type MediaData struct {
	Data []byte `json:"data"`
	ID   int    `json:"id"`
	Kind MediaKind
}

// Message 为 llama-server 聊天路径使用的内部消息结构。
type Message struct {
	Role       string
	Content    string
	Thinking   string
	Media      []MediaData
	ToolCalls  []api.ToolCall
	ToolName   string
	ToolCallID string
}

// MessageFromAPI 将 API Message 转为内部 Message（含图像 MediaData）。
func MessageFromAPI(msg api.Message) Message {
	media := make([]MediaData, len(msg.Images))
	for i, data := range msg.Images {
		media[i] = NewMediaData(i, data)
	}

	return Message{
		Role:       msg.Role,
		Content:    msg.Content,
		Thinking:   msg.Thinking,
		Media:      media,
		ToolCalls:  msg.ToolCalls,
		ToolName:   msg.ToolName,
		ToolCallID: msg.ToolCallID,
	}
}

// CompletionRequest 封装补全请求的 prompt、媒体、grammar 与 logprobs 选项。
type CompletionRequest struct {
	Prompt  string
	Format  json.RawMessage
	Media   []MediaData
	Options *api.Options

	Grammar         string // set before sending the request to the subprocess
	Shift           bool
	Truncate        bool
	PreservedTokens []string // parser tokens to render as text; ignored by non-llama-server runners
	ToolCallTag     string   // raw generic tool parser tag, if any
	LeadingBOS      string   // textual BOS emitted by Go rendering, if any

	// Logprobs 是否在响应中包含 log 概率。
	// Logprobs specifies whether to include log probabilities in the response
	Logprobs bool

	// TopLogprobs 返回的 top-k 备选 token 数量（0-20）。
	// TopLogprobs specifies the number of most likely alternative tokens to return (0-20)
	TopLogprobs int
}

// ChatRequest 封装多轮聊天、工具与 thinking 选项。
type ChatRequest struct {
	Messages []api.Message
	Tools    api.Tools
	Format   json.RawMessage
	Options  *api.Options
	Think    *api.ThinkValue
	Shift    bool

	Logprobs    bool
	TopLogprobs int
}

// ChatResponse 为流式/非流式聊天单块响应。
type ChatResponse struct {
	Message            api.Message   `json:"message"`
	DoneReason         DoneReason    `json:"done_reason"`
	Done               bool          `json:"done"`
	PromptEvalCount    int           `json:"prompt_eval_count"`
	PromptEvalDuration time.Duration `json:"prompt_eval_duration"`
	EvalCount          int           `json:"eval_count"`
	EvalDuration       time.Duration `json:"eval_duration"`
	Logprobs           []Logprob     `json:"logprobs,omitempty"`
}

// DoneReason 表示生成结束原因（stop/length/连接关闭）。
// DoneReason represents the reason why a completion response is done
type DoneReason int

const (
	DoneReasonStop DoneReason = iota
	DoneReasonLength
	DoneReasonConnectionClosed
)

// String 返回 "stop"、"length" 或空字符串。
func (d DoneReason) String() string {
	switch d {
	case DoneReasonLength:
		return "length"
	case DoneReasonStop:
		return "stop"
	default:
		return ""
	}
}

// TokenLogprob 单个备选 token 的 log 概率。
// TokenLogprob represents log probability information for a single token alternative.
type TokenLogprob struct {
	Token   string  `json:"token"`
	Logprob float64 `json:"logprob"`
}

// Logprob 包含生成 token 及其 top-k 备选 log 概率。
// Logprob contains log probability information for a generated token.
type Logprob struct {
	TokenLogprob
	TopLogprobs []TokenLogprob `json:"top_logprobs,omitempty"`
}

// CompletionResponse 为补全流式/非流式单块响应。
type CompletionResponse struct {
	Content            string        `json:"content"`
	DoneReason         DoneReason    `json:"done_reason"`
	Done               bool          `json:"done"`
	PromptEvalCount    int           `json:"prompt_eval_count"`
	PromptEvalDuration time.Duration `json:"prompt_eval_duration"`
	EvalCount          int           `json:"eval_count"`
	EvalDuration       time.Duration `json:"eval_duration"`

	// Logprobs 在请求时包含 log 概率列表。
	// Logprobs contains log probability information if requested
	Logprobs []Logprob `json:"logprobs,omitempty"`
}
