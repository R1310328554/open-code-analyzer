package engine

// assertions 对外入口：EnableParanoidMode 开启 V2 引擎执行流水线运行时不变量断言。

import "github.com/grafana/loki/v3/pkg/engine/internal/assertions"

// EnableParanoidMode 启用列名唯一、标签唯一等检查，会降低查询性能，仅调试用。
// EnableParanoidMode turns on runtime assertions for execution pipelines that
// will check important invariants on input and output records, such as column
// names uniqueness and labels uniqueness. This affects performance if enabled.
func EnableParanoidMode() {
	assertions.Enabled = true
}
// 断言开关写入 internal/assertions.Enabled 全局变量供 executor 各阶段读取。
