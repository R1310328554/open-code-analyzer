package filter

// filter 包定义日志行过滤函数类型 Func：给定时间戳、日志正文与结构化元数据标签，返回是否保留该条记录。

import (
	"time"

	"github.com/prometheus/prometheus/model/labels"
)

type Func func(ts time.Time, s string, structuredMetadata labels.Labels) bool
// structuredMetadata 携带 detected_level 等键值，过滤器可据此做级别或字段匹配。
