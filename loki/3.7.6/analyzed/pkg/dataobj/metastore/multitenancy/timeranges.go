package multitenancy

// multitenancy 子包定义租户级时间窗口，供 metastore 目录写入与查询使用。

import (
	"time"
)

// TimeRange 绑定租户 ID 与其数据对象覆盖的最小/最大时间戳。
// TimeRange represents a time range for a specific tenant.
type TimeRange struct {
	Tenant  string
	MinTime time.Time
	MaxTime time.Time
}
// 租户时间范围在目录合并时按窗口切片写入对应 ToC 文件。
