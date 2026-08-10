package astmapper

// astmapper 包 parallel 判定 PromQL AST 子树是否可并行化，供 query-frontend 分片与 shard summer 使用。

import (
	"fmt"

	"github.com/go-kit/log/level"
	"github.com/prometheus/prometheus/promql/parser"

	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

var summableAggregates = map[parser.ItemType]struct{}{
	parser.SUM:     {},
	parser.MIN:     {},
	parser.MAX:     {},
	parser.TOPK:    {},
	parser.BOTTOMK: {},
	parser.COUNT:   {},
}

// nonParallelFuncs 为不可并行的 PromQL 函数名（分位数/直方图/absent）。
var nonParallelFuncs = []string{
	"histogram_quantile",
	"quantile_over_time",
	"absent",
}

// CanParallelize 递归检查 AST 节点：可并行的 sum 聚合、Call 与非 BinaryExpr 子树。
// CanParallelize tests if a subtree is parallelizable.
// A subtree is parallelizable if all of its components are parallelizable.
func CanParallelize(node parser.Node) bool {
	switch n := node.(type) {
	case nil:
		// nil handles cases where we check optional fields that are not set
		return true

	case parser.Expressions:
		for _, e := range n {
			if !CanParallelize(e) {
				return false
			}
		}
		return true

	case *parser.AggregateExpr:
		_, ok := summableAggregates[n.Op]
		if !ok {
			return false
		}

		// Ensure there are no nested aggregations
		nestedAggs, err := Predicate(n.Expr, func(node parser.Node) (bool, error) {
			_, ok := node.(*parser.AggregateExpr)
			return ok, nil
		})

		return err == nil && !nestedAggs && CanParallelize(n.Expr)

	case *parser.BinaryExpr:
		// since binary exprs use each side for merging, they cannot be parallelized
		return false

	case *parser.Call:
		if n.Func == nil {
			return false
		}
		if !ParallelizableFunc(*n.Func) {
			return false
		}

		for _, e := range n.Args {
			if !CanParallelize(e) {
				return false
			}
		}
		return true

	case *parser.SubqueryExpr:
		return CanParallelize(n.Expr)

	case *parser.ParenExpr:
		return CanParallelize(n.Expr)

	case *parser.UnaryExpr:
		// Since these are only currently supported for Scalars, should be parallel-compatible
		return true

	case *parser.EvalStmt:
		return CanParallelize(n.Expr)

	case *parser.MatrixSelector, *parser.NumberLiteral, *parser.StringLiteral, *parser.VectorSelector:
		return true

	default:
		level.Error(util_log.Logger).Log("err", fmt.Sprintf("CanParallel: unhandled node type %T", node)) //lint:ignore faillint allow global logger for now
		return false
	}

}

// ParallelizableFunc 排除 histogram_quantile 等需全局合并的函数。
// ParallelizableFunc ensures that a promql function can be part of a parallel query.
func ParallelizableFunc(f parser.Function) bool {

	for _, v := range nonParallelFuncs {
		if v == f.Name {
			return false
		}
	}
	return true
}
// BinaryExpr 因两侧需合并结果而不可并行，AggregateExpr 需无嵌套聚合。
