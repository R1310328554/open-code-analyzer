package queryrangebase

// queryrangebase 包 alias 将 definitions 与 resultscache 中的核心类型导出为本地别名，打破 queryrange 与 queryrangebase 间的循环 import。

import (
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase/definitions"
	"github.com/grafana/loki/v3/pkg/storage/chunk/cache/resultscache"
)

// 以下类型别名保持 API 稳定，调用方无需直接依赖 definitions 子包。
// Helpful aliases for refactoring circular imports

type CachingOptions = definitions.CachingOptions
type PrometheusResponseHeader = definitions.PrometheusResponseHeader
type PrometheusRequestHeader = definitions.PrometheusRequestHeader
// Codec/Merger/Request/Response 等别名指向 definitions 接口定义。
type Codec = definitions.Codec
type Merger = definitions.Merger
type CacheGenNumberLoader = resultscache.CacheGenNumberLoader

type Request = definitions.Request
type Response = definitions.Response
// Extent 表示 results cache 中已缓存的时间区间片段。
type Extent = resultscache.Extent
// CacheGenNumberLoader 别名用于跨 frontend/querier 传递缓存世代号。
