package chunk

// Predicate 封装 LogQL 查询的标签匹配器与 QueryPlan，供 chunk 层过滤与计划传递（命名待重构）。

import (
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/querier/plan"
)

// TODO(owen-d): rename. This is not a predicate and is confusing.
type Predicate struct {
	Matchers []*labels.Matcher
	plan     *plan.QueryPlan
}

// NewPredicate 构造带匹配器与查询计划的 Predicate 值对象。
func NewPredicate(m []*labels.Matcher, p *plan.QueryPlan) Predicate {
	return Predicate{Matchers: m, plan: p}
}

// Plan 返回内嵌 QueryPlan，nil 时返回空 plan.QueryPlan{}。
func (p Predicate) Plan() plan.QueryPlan {
	if p.plan != nil {
		return *p.plan
	}
	return plan.QueryPlan{}
}
// TODO 注释指出 Predicate 命名易与谓词混淆，后续可能重命名为更准确的类型名。
