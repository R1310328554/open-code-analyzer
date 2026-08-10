package validation

// util/validation 包定义查询时间范围校验错误模板：ErrQueryTooLong 与 ErrQueryTooOld 供 chunk store、querier 与 query frontend 统一返回。

const (
	// ErrQueryTooLong 占位符依次为实际查询跨度与租户 max_query_length 限额。
// ErrQueryTooLong is used in chunk store, querier and query frontend.
	ErrQueryTooLong = "the query time range exceeds the limit (query length: %s, limit: %s)"

// ErrQueryTooOld 在查询窗口早于 now-max_query_lookback 时格式化提示信息。
	ErrQueryTooOld = "this data is no longer available, it is past now - max_query_lookback (%s)"
)
// 常量字符串供 fmt.Errorf 复用，保证各组件对用户展示一致的拒绝原因。
