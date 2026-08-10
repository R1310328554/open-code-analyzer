package errors

// errors 包定义存储层查询相关的用户侧错误类型，与底层 I/O 或系统故障区分处理。

var ErrQueryMustContainMetricName = QueryError("query must contain metric name")

// QueryError 表示调用方输入或查询语义错误，应返回 4xx 而非 500。
// Query errors are to be treated as user errors, rather than storage errors.
type QueryError string

// Error 实现 error 接口，直接返回底层字符串消息。
func (e QueryError) Error() string {
	return string(e)
}
// 存储实现应优先返回 QueryError，便于 querier 将问题归因于用户而非集群故障。
