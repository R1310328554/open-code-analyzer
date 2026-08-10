// Assertions 包提供可选运行时断言以增强引擎安全性。
// 默认关闭，测试环境可开启；生产路径因性能开销通常保持禁用。
package assertions

var (
	Enabled = false
)
// 断言失败直接 panic，便于在单测中尽早发现 schema 或标签冲突问题。
