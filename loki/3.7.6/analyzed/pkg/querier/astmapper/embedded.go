package astmapper

// embedded 实现查询嵌入机制：将并行化 AST 子树编码为 VectorSelector 上的保留标签，供 storage.Queryable 在运行时展开执行。

import (
	"encoding/json"

	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/promql/parser"
)

/*
Design:

The prometheus api package enforces a (*promql.Engine argument), making it infeasible to do lazy AST
evaluation and substitution from within this package.
This leaves the (storage.Queryable) interface as the remaining target for conducting application level sharding.

The main idea is to analyze the AST and determine which subtrees can be parallelized. With those in hand, the queries may
be remapped into vector or matrix selectors utilizing a reserved label containing the original query. These may then be parallelized in the storage implementation.
*/

const (
// QueryLabel 标签值存放 JSON 编码的 EmbeddedQueries，标记待展开的子查询列表。
	// QueryLabel is a reserved label containing an embedded query
	QueryLabel = "__cortex_queries__"
// EmbeddedQueriesMetricName 作为 VectorSelector 的 metric 名标识嵌入查询节点。
	// EmbeddedQueriesMetricName is a reserved label (metric name) denoting an embedded query
	EmbeddedQueriesMetricName = "__embedded_queries__"
)

// EmbeddedQueries is a wrapper type for encoding queries
// EmbeddedQueries 包装待并行执行的 PromQL 子表达式字符串列表。
type EmbeddedQueries struct {
	Concat []string `json:"Concat"`
}

// JSONCodec is a Codec that uses JSON representations of EmbeddedQueries structs
var JSONCodec jsonCodec

type jsonCodec struct{}

// JSONCodec.Encode 将子查询列表序列化为 JSON 字符串写入 QueryLabel matcher。
func (c jsonCodec) Encode(queries []string) (string, error) {
	embedded := EmbeddedQueries{
		Concat: queries,
	}
	b, err := json.Marshal(embedded)
	return string(b), err
}

func (c jsonCodec) Decode(encoded string) (queries []string, err error) {
	var embedded EmbeddedQueries
	err = json.Unmarshal([]byte(encoded), &embedded)
	if err != nil {
		return nil, err
	}

	return embedded.Concat, nil
}

// VectorSquash 将多个 OR 分支合并为带 EmbeddedQueries 标签的 VectorSelector 供 Queryable 劫持。
// VectorSquash reduces an AST into a single vector query which can be hijacked by a Queryable impl.
// It always uses a VectorSelector as the substitution node.
// This is important because logical/set binops can only be applied against vectors and not matrices.
func VectorSquasher(nodes ...parser.Node) (parser.Expr, error) {

	// concat OR legs
	strs := make([]string, 0, len(nodes))
	for _, node := range nodes {
		strs = append(strs, node.String())
	}

	encoded, err := JSONCodec.Encode(strs)
	if err != nil {
		return nil, err
	}

	embeddedQuery, err := labels.NewMatcher(labels.MatchEqual, QueryLabel, encoded)
	if err != nil {
		return nil, err
	}

	return &parser.VectorSelector{
		Name:          EmbeddedQueriesMetricName,
		LabelMatchers: []*labels.Matcher{embeddedQuery},
	}, nil

}
// 设计动机：promql.Engine 接口限制无法在包内懒求值，故通过 Queryable 层展开嵌入查询。
