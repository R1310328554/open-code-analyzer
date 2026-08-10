package grpc

// Compactor gRPC 生成代码的自定义扩展：
// 为 JobType 枚举提供 Humanize 便于日志与 UI 展示。

// Humanize 将 JobType 转为可读字符串，删除任务返回 deletion。
// Humanize returns a more human-friendly string form of job types.
func (x JobType) Humanize() string {
	switch x {
	case JOB_TYPE_DELETION:
		return "deletion"
	default:
		return x.String()
	}
}
