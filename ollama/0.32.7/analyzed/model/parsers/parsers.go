// 模型输出解析器注册表：按名称构造 Parser 及流式辅助函数。
package parsers

import (
	"strings"
	"unicode"
	"unicode/utf8"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/harmony"
)

// Parser 抽象各模型流式输出解析：Init/Add/PreservedTokens/能力查询。
type Parser interface {
		// Init 用工具、可选预填充消息与 think 偏好初始化解析器。
	// Init initializes the parser with tools, optional last message for chat prefill, and think value
	// Returns processed tools if the parser needs to modify them (e.g., harmony renames them)
	Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool
		// Add 处理流式片段，返回正文、思考与工具调用；done 表示末块。
	// Add processes streamed content and returns parsed content, thinking, and tool calls
	// The done flag indicates if this is the last chunk (used for draining accumulators)
	Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error)
		// PreservedTokens 返回 llama-server 去 token 后须保留的语法 token。
	// PreservedTokens returns parser grammar tokens that must remain visible in
	// llama-server detokenized output for this parser to recognize boundaries.
	PreservedTokens() []string
	HasToolSupport() bool
	HasThinkingSupport() bool
}

// ParserConstructor 构造具体 Parser 实例。
type ParserConstructor func() Parser

// ParserRegistry 维护解析器名称到构造函数的映射。
type ParserRegistry struct {
	constructors map[string]ParserConstructor
}

// Register 注册命名解析器构造函数。
func (r *ParserRegistry) Register(name string, constructor ParserConstructor) {
	r.constructors[name] = constructor
}

var registry = ParserRegistry{
	constructors: make(map[string]ParserConstructor),
}

// Register 向全局 registry 注册解析器。
func Register(name string, constructor ParserConstructor) {
	registry.Register(name, constructor)
}

// ParserForName 按 Modelfile/parser 名称返回对应 Parser 实例。
func ParserForName(name string) Parser {
	if parser, ok := registry.constructors[name]; ok {
		return parser()
	}
	var p Parser

	switch name {
	case "qwen3":
		p = &Qwen3Parser{hasThinkingSupport: false, defaultThinking: false}
	case "qwen3-thinking":
		p = &Qwen3Parser{hasThinkingSupport: true, defaultThinking: true}
	case "qwen3.5":
		p = &Qwen35Parser{}
	case "ornith":
		p = &Qwen35Parser{}
	case "qwen3-coder":
		p = &Qwen3CoderParser{}
	case "qwen3-vl-instruct":
		p = &Qwen3VLParser{hasThinkingSupport: false}
	case "qwen3-vl-thinking":
		p = &Qwen3VLParser{hasThinkingSupport: true}
	case "ministral":
		p = &MinistralParser{hasThinkingSupport: false}
	case "passthrough":
		return &PassthroughParser{}
	case "harmony":
		return harmony.NewHarmonyMessageHandler()
	case "cogito":
		return &CogitoParser{}
	case "deepseek3":
		return &DeepSeek3Parser{hasThinkingSupport: true}
	case "olmo3":
		return &Olmo3Parser{}
	case "olmo3-think":
		return &Olmo3ThinkParser{}
	case "nemotron-3-nano":
		return &Nemotron3NanoParser{}
	case "functiongemma":
		return &FunctionGemmaParser{}
	case "glm-4.7":
		return &GLM47Parser{}
	case "gemma4":
		return &Gemma4Parser{hasThinkingSupport: true}
	case "gemma4-no-thinking":
		return &Gemma4Parser{hasThinkingSupport: false}
	case "glm-ocr":
		return &GlmOcrParser{}
	case "lfm2":
		return &LFM2Parser{hasThinkingSupport: false}
	case "lfm2-thinking":
		return &LFM2Parser{hasThinkingSupport: true}
	case "laguna":
		return &LagunaParser{}
	case "poolside-v1":
		return &LagunaV8Parser{}
	case "cohere":
		return &CohereParser{}
	case "glimmer":
		return &GlimmerParser{}
	default:
		return nil
	}
	return p
}

// PassthroughParser 原样透传流式输出。
type PassthroughParser struct{}

// Init 不修改工具列表。
func (p *PassthroughParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	return tools // passthrough doesn't modify tools
}

// Add 将输入原样作为正文返回。
func (p *PassthroughParser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	return s, "", nil, nil
}

func (p *PassthroughParser) PreservedTokens() []string {
	return nil
}

func (p *PassthroughParser) HasToolSupport() bool {
	return false
}

func (p *PassthroughParser) HasThinkingSupport() bool {
	return false
}

// splitAtTag 在 tag 处分割 Builder 内容并可选 trim 后半段。
func splitAtTag(sb *strings.Builder, tag string, trimAfter bool) (string, string) {
	split := strings.SplitN(sb.String(), tag, 2)
	if len(split) == 1 {
		sb.Reset()
		return split[0], ""
	}
	before := split[0]
	before = strings.TrimRightFunc(before, unicode.IsSpace)
	after := split[1]
	if trimAfter {
		after = strings.TrimLeftFunc(after, unicode.IsSpace)
	}
	sb.Reset()
	sb.WriteString(after)
	return before, after // return events
}

// overlap 计算 s 后缀与 delim 前缀的最长重叠长度。
// overlap returns the longest overlap between the suffix of s and the prefix of delim
func overlap(s, delim string) int {
	max := min(len(delim), len(s))
	for i := max; i > 0; i-- {
		if strings.HasSuffix(s, delim[:i]) {
			return i
		}
	}
	return 0
}

// trailingWhitespaceLen 返回 s 末尾 UTF-8 空白字节长度。
// trailingWhitespaceLen returns the length in bytes of trailing whitespace in s
func trailingWhitespaceLen(s string) int {
	remaining := s
	total := 0
	for len(remaining) > 0 {
		r, size := utf8.DecodeLastRuneInString(remaining)
		// if it's an invalid utf8 rune, assume it isn't whitespace
		if r == utf8.RuneError && size == 1 {
			break
		}
		if !unicode.IsSpace(r) {
			break
		}
		total += size
		remaining = remaining[:len(remaining)-size]
	}
	return total
}
