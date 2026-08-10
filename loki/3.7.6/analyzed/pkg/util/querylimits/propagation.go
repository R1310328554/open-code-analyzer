package querylimits

// querylimits 包 propagation 定义 QueryLimits 与 Context 的 JSON 序列化、HTTP 头 X-Loki-Query-Limits 及 context 键值注入/提取工具。

import (
	"context"
	"encoding/json"
	"net/http"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util/flagext"
)

// key 为私有 context 键类型，避免与其他包 WithValue 键冲突。
// Context key type used to avoid collisions
type key int

const (
	queryLimitsCtxKey        key = 1
	queryLimitsContextCtxKey key = 2

	HTTPHeaderQueryLimitsKey        = "X-Loki-Query-Limits"
	HTTPHeaderQueryLimitsContextKey = "X-Loki-Query-Limits-Context"
)

// QueryLimits 使用 model.Duration 支持 JSON 中 1h30m 等人类可读时长格式。
// NOTE: we use custom `model.Duration` instead of standard `time.Duration` because,
// to support user-friendly duration format (e.g: "1h30m45s") in JSON value.
type QueryLimits struct {
	MaxQueryLength          model.Duration   `json:"maxQueryLength,omitempty"`
	MaxQueryRange           model.Duration   `json:"maxQueryInterval,omitempty"`
	MaxQueryLookback        model.Duration   `json:"maxQueryLookback,omitempty"`
	MaxEntriesLimitPerQuery int              `json:"maxEntriesLimitPerQuery,omitempty"`
	QueryTimeout            model.Duration   `json:"maxQueryTime,omitempty"`
	RequiredLabels          []string         `json:"requiredLabels,omitempty"`
	RequiredNumberLabels    int              `json:"minimumLabelsNumber,omitempty"`
	MaxQueryBytesRead       flagext.ByteSize `json:"maxQueryBytesRead,omitempty"`
}

func UnmarshalQueryLimits(data []byte) (*QueryLimits, error) {
	limits := &QueryLimits{}
	err := json.Unmarshal(data, limits)
	return limits, err
}

func MarshalQueryLimits(limits *QueryLimits) ([]byte, error) {
	return json.Marshal(limits)
}

// InjectQueryLimitsHTTP 先 Del 旧头再 Add JSON 编码的限额，避免重复策略集。
// InjectQueryLimitsHTTP adds the query limits to the request headers.
func InjectQueryLimitsHTTP(r *http.Request, limits *QueryLimits) error {
	return InjectQueryLimitsHeader(&r.Header, limits)
}

// InjectQueryLimitsHeader adds the query limits to the headers.
func InjectQueryLimitsHeader(h *http.Header, limits *QueryLimits) error {
	// Ensure any existing policy sets are erased
	h.Del(HTTPHeaderQueryLimitsKey)

	encodedLimits, err := MarshalQueryLimits(limits)
	if err != nil {
		return err
	}
	h.Add(HTTPHeaderQueryLimitsKey, string(encodedLimits))
	return nil
}

// ExtractQueryLimitsHTTP retrieves the query limit policy from the HTTP header and returns it.
func ExtractQueryLimitsHTTP(r *http.Request) (*QueryLimits, error) {
	headerValues := r.Header.Values(HTTPHeaderQueryLimitsKey)

	// Iterate through each set header value
	for _, headerValue := range headerValues {
		return UnmarshalQueryLimits([]byte(headerValue))

	}

	return nil, nil
}

// ExtractQueryLimitsFromContext 从 queryLimitsCtxKey 读取指针，类型不匹配返回 nil。
// ExtractQueryLimitsFromContext gets the embedded limits from the context
func ExtractQueryLimitsFromContext(ctx context.Context) *QueryLimits {
	source, ok := ctx.Value(queryLimitsCtxKey).(*QueryLimits)

	if !ok {
		return nil
	}

	return source
}

// InjectQueryLimitsIntoContext returns a derived context containing the provided query limits
func InjectQueryLimitsIntoContext(ctx context.Context, limits QueryLimits) context.Context {
	return context.WithValue(ctx, interface{}(queryLimitsCtxKey), &limits)
}

// Context 携带 expr/from/to 供限额中间件还原查询上下文做细粒度校验。
type Context struct {
	Expr string    `json:"expr"`
	From time.Time `json:"from"`
	To   time.Time `json:"to"`
}

func UnmarshalQueryLimitsContext(data []byte) (*Context, error) {
	limitsCtx := &Context{}
	err := json.Unmarshal(data, limitsCtx)
	return limitsCtx, err
}

func MarshalQueryLimitsContext(limits *Context) ([]byte, error) {
	return json.Marshal(limits)
}

// InjectQueryLimitsContextHTTP adds the query limits context to the request headers.
func InjectQueryLimitsContextHTTP(r *http.Request, limitsCtx *Context) error {
	return InjectQueryLimitsContextHeader(&r.Header, limitsCtx)
}

// InjectQueryLimitsContextHeader adds the query limits context to the headers.
func InjectQueryLimitsContextHeader(h *http.Header, limitsCtx *Context) error {
	// Ensure any existing policy sets are erased
	h.Del(HTTPHeaderQueryLimitsContextKey)

	encodedLimits, err := MarshalQueryLimitsContext(limitsCtx)
	if err != nil {
		return err
	}
	h.Add(HTTPHeaderQueryLimitsContextKey, string(encodedLimits))
	return nil
}

// ExtractQueryLimitsContextHTTP retrieves the query limits context from the HTTP header and returns it.
func ExtractQueryLimitsContextHTTP(r *http.Request) (*Context, error) {
	headerValues := r.Header.Values(HTTPHeaderQueryLimitsContextKey)

	// Iterate through each set header value
	for _, headerValue := range headerValues {
		return UnmarshalQueryLimitsContext([]byte(headerValue))

	}

	return nil, nil
}

// ExtractQueryLimitsContextFromContext gets the embedded query limits context from the context
func ExtractQueryLimitsContextFromContext(ctx context.Context) *Context {
	source, ok := ctx.Value(queryLimitsContextCtxKey).(*Context)

	if !ok {
		return nil
	}

	return source
}

// InjectQueryLimitsContextIntoContext returns a derived context containing the provided query limits context
func InjectQueryLimitsContextIntoContext(ctx context.Context, limitsCtx Context) context.Context {
	return context.WithValue(ctx, any(queryLimitsContextCtxKey), &limitsCtx)
}
// ExtractQueryLimitsHTTP 仅取首个头值，多值场景由网关保证单策略传递。
