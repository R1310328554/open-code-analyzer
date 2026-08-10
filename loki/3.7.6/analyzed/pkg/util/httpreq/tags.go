package httpreq

// 查询标签中间件：从 X-Query-Tags 与 X-Query-Queue-Time 头提取元数据写入 context 供日志与限流。

import (
	"context"
	"net/http"
	"regexp"
	"strings"
	"time"

	"github.com/grafana/dskit/middleware"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

// ctxKey 自定义类型避免 linter 禁止 string 作 context key 的规范问题。
// NOTE(kavi): Why new type?
// Our linter won't allow to use basic types like string to be used as key in context.
// TODO(chaudum): Can we safely change the type of the header key?
type ctxKey string

var (
	QueryTagsHTTPHeader ctxKey = "X-Query-Tags"
	safeQueryTags              = regexp.MustCompile("[^a-zA-Z0-9-=.@, ]+") // only alpha-numeric, ' ', ',', '=', '@', '.' and `-`

	QueryQueueTimeHTTPHeader ctxKey = "X-Query-Queue-Time"
)

// ExtractQueryTagsMiddleware 读取并 sanitize 查询标签头，InjectQueryTags 后传递给下游 handler。
func ExtractQueryTagsMiddleware() middleware.Interface {
	return middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			ctx := req.Context()

			if tags := ExtractQueryTagsFromHTTP(req); tags != "" {
				ctx = InjectQueryTags(ctx, tags)
				req = req.WithContext(ctx)
			}
			next.ServeHTTP(w, req)
		})
	})
}

// ExtractQueryTagsFromHTTP 用 safeQueryTags 正则剔除非法字符，防止日志注入。
func ExtractQueryTagsFromHTTP(req *http.Request) string {
	tags := req.Header.Get(string(QueryTagsHTTPHeader))
	return safeQueryTags.ReplaceAllString(tags, "_")
}

func ExtractQueryTagsFromContext(ctx context.Context) string {
	// if the cast fails then v will be an empty string
	v, _ := ctx.Value(QueryTagsHTTPHeader).(string)
	return v
}

func InjectQueryTags(ctx context.Context, tags string) context.Context {
	tags = safeQueryTags.ReplaceAllString(tags, "_")
	return context.WithValue(ctx, QueryTagsHTTPHeader, tags)
}

// ExtractQueryMetricsMiddleware 解析 X-Query-Queue-Time 为 time.Duration 写入 context。
func ExtractQueryMetricsMiddleware() middleware.Interface {
	return middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			ctx := req.Context()

			queueTimeHeader := req.Header.Get(string(QueryQueueTimeHTTPHeader))
			if queueTimeHeader != "" {
				queueTime, err := time.ParseDuration(queueTimeHeader)
				if err == nil {
					ctx = context.WithValue(ctx, QueryQueueTimeHTTPHeader, queueTime)
					req = req.WithContext(ctx)
				}
			}

			next.ServeHTTP(w, req)
		})
	})
}

// TagsToKeyValues 将 Source=foo,Feature=beta 转为小写键值交替的 []interface{} 供结构化日志。
// TagsToKeyValues converts QueryTags to form that is easy to log.
// e.g: `Source=foo,Feature=beta` -> []interface{}{"source", "foo", "feature", "beta"}
// so that we could log nicely!
// If queryTags is not in canonical form then its completely ignored (e.g: `key1=value1,key2=value`)
func TagsToKeyValues(queryTags string) []interface{} {
	toks := strings.FieldsFunc(queryTags, func(r rune) bool {
		return r == ','
	})

	vals := make([]string, 0)

	for _, tok := range toks {
		val := strings.FieldsFunc(tok, func(r rune) bool {
			return r == '='
		})

		if len(val) != 2 {
			continue
		}
		vals = append(vals, strings.ToLower(val[0]), val[1])
	}

	res := make([]interface{}, 0, len(vals))

	for _, val := range vals {
		res = append(res, val)
	}

	return res
}

// IsLogsDrilldownRequest 检查 source 标签是否等于 Logs Drilldown 应用名常量。
// IsLogsDrilldownRequest checks if the request comes from Logs Drilldown by examining the X-Query-Tags header
func IsLogsDrilldownRequest(ctx context.Context) bool {
	tags := ExtractQueryTagsFromContext(ctx)
	kvs := TagsToKeyValues(tags)

	// KVs is an []interface{} of key value pairs, so iterate by keys
	for i := 0; i < len(kvs); i += 2 {
		current, ok := kvs[i].(string)
		if !ok {
			continue
		}

		next, ok := kvs[i+1].(string)
		if !ok {
			continue
		}

		if current == "source" && strings.EqualFold(next, constants.LogsDrilldownAppName) {
			return true
		}
	}
	return false
}
// InjectQueryTags 写入前同样经 safeQueryTags 过滤，保证 context 内标签字符集安全。
