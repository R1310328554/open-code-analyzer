package volume

// volume 包定义 logcli volume 子命令的查询参数结构体。

import "time"

type Query struct {
	QueryString       string
	Start             time.Time
	End               time.Time
	Step              time.Duration
	Quiet             bool
	Limit             int
// TargetLabels 指定参与 volume 统计的标签键列表。
	TargetLabels      []string
	AggregateByLabels bool
}
// 具体查询与打印逻辑由 logcli 命令层调用 client 完成，本包仅承载配置。
