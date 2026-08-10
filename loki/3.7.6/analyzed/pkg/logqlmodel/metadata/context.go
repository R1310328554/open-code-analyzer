/*
// metadata 包通过 context 在查询路径上累积 HTTP 响应头与警告信息。
Package metadata provides primitives for recording metadata across the query path.
Metadata is passed through the query context.
*/
package metadata

import (
	"context"
	"errors"
	"maps"
	"slices"
	"sort"
	"sync"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase/definitions"
)

type (
	ctxKeyType string
)

const (
	metadataKey ctxKeyType = "metadata"
)

var (
	ErrNoCtxData = errors.New("unable to add headers to context: no existing context data")
)

// Context 线程安全地合并各子查询返回的 headers 与 warnings。
// Context is the metadata context. It is passed through the query path and accumulates metadata.
type Context struct {
	mtx      sync.Mutex
	headers  map[string][]string
	warnings map[string]struct{}
}

// NewContext 在 context 中挂载新的 Context 并返回可变的 metadata 句柄。
// NewContext creates a new metadata context
func NewContext(ctx context.Context) (*Context, context.Context) {
	contextData := &Context{
		headers:  map[string][]string{},
		warnings: map[string]struct{}{},
	}
	ctx = context.WithValue(ctx, metadataKey, contextData)
	return contextData, ctx
}

// FromContext 取回 metadata；缺失时返回空 map 的默认 Context 避免 nil  panic。
// FromContext returns the metadata context.
func FromContext(ctx context.Context) *Context {
	v, ok := ctx.Value(metadataKey).(*Context)
	if !ok {
		return &Context{
			headers:  map[string][]string{},
			warnings: map[string]struct{}{},
		}
	}
	return v
}

// Headers 将 map 转为按名称排序的 PrometheusResponseHeader 切片供 HTTP 响应。
// Headers returns the cache headers accumulated in the context so far.
func (c *Context) Headers() []*definitions.PrometheusResponseHeader {
	c.mtx.Lock()
	defer c.mtx.Unlock()

	headers := make([]*definitions.PrometheusResponseHeader, 0, len(c.headers))
	for k, vs := range c.headers {
		header := definitions.PrometheusResponseHeader{
			Name:   k,
			Values: vs,
		}
		headers = append(headers, &header)
	}

	sort.Slice(headers, func(i, j int) bool {
		return headers[i].Name < headers[j].Name
	})

	return headers
}

// AddWarning 以 set 语义去重存储警告字符串。
func (c *Context) AddWarning(warning string) {
	c.mtx.Lock()
	defer c.mtx.Unlock()

	c.warnings[warning] = struct{}{}
}
func (c *Context) Warnings() []string {
	c.mtx.Lock()
	defer c.mtx.Unlock()

	warnings := slices.Sorted(maps.Keys(c.warnings))

	return warnings
}

func (c *Context) Reset() {
	c.mtx.Lock()
	defer c.mtx.Unlock()

	clear(c.headers)
	clear(c.warnings)
}

// JoinHeaders 将下游响应头合并进 context；无 metadata 时返回 ErrNoCtxData。
// JoinHeaders merges a Headers with the embedded Headers in a context in a concurrency-safe manner.
// JoinHeaders will consolidate all distinct headers but will override same-named headers in an
// undefined way
func JoinHeaders(ctx context.Context, headers []*definitions.PrometheusResponseHeader) error {
	context, ok := ctx.Value(metadataKey).(*Context)
	if !ok {
		return ErrNoCtxData
	}

	context.mtx.Lock()
	defer context.mtx.Unlock()

	ExtendHeaders(context.headers, headers)

	return nil
}

// ExtendHeaders 按 header 名称覆盖写入 dst，同名 header 后者覆盖前者。
func ExtendHeaders(dst map[string][]string, src []*definitions.PrometheusResponseHeader) {
	for _, header := range src {
		dst[header.Name] = header.Values
	}
}

func AddWarnings(ctx context.Context, warnings ...string) error {
	if len(warnings) == 0 {
		return nil
	}

	context, ok := ctx.Value(metadataKey).(*Context)
	if !ok {
		return ErrNoCtxData
	}

	context.mtx.Lock()
	defer context.mtx.Unlock()

	for _, w := range warnings {
		context.warnings[w] = struct{}{}
	}

	return nil
}
// AddWarnings 批量加入警告；Reset 清空 headers/warnings 以便 context 复用。
