package xcap

// xcap 包 Observation 接口表示某统计量的一次观测值：由 Statistic.Observe 构造，经 Region.Record 或 Span.Record 写入聚合器。

// Observation 对外暴露 statistic/value 访问器，隐藏具体 observation 结构体实现。
// Observation holds a value for a particular statistic. Observations
// are created from statistics and then recorded into a Region using
// [Region.Record].
type Observation interface {
	// statistic returns the statistic this observation is for.
	statistic() Statistic
	// value returns the raw value of this observation.
	value() any
}

// observation is the internal implementation of Observation.
// observation 为内部实现，stat 指向定义，val 保存 int64/float64/bool 原始值。
type observation struct {
	stat Statistic
	val  any
}

func (o *observation) statistic() Statistic {
	return o.stat
}

func (o *observation) value() any {
	return o.val
}
// Observation 为只读接口，聚合逻辑由 Region 根据 Statistic 的 Aggregation 类型完成。
