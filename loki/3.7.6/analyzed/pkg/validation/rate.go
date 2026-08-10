package validation

// validation 包 RateLimit 将 golang.org/x/time/rate 的 Limit 与 Burst 成对保存：避免顺序读取 Limits 配置时因竞态导致 limit/burst 不匹配。

import "golang.org/x/time/rate"

// RateLimit is a colocated limit & burst config. It largely exists to
// eliminate accidental misconfigurations due to race conditions when
// requesting the limit & burst config sequentially, between which the
// Limits configuration may have updated.
// RateLimit 由 distributor/ingester 在创建 limiter 时一次性快照当前租户限额。
type RateLimit struct {
	Limit rate.Limit
	Burst int
}

var Unlimited = RateLimit{
	Limit: rate.Inf,
	Burst: 0,
}
// 成对结构体便于在热路径传递，无需再次查询 Overrides 或持有 mutex。
