// Package expr 提供针对 RecordBatch 的表达式求值（含选择向量）；实验性，供 dataobj 使用。
// Package expr provides utilities for evaluating expressions against a
// [columnar.RecordBatch] with a selection vector.
//
// Package expr is EXPERIMENTAL and currently only intended to be used by
// [github.com/grafana/loki/v3/pkg/dataobj].
package expr

import (
	"github.com/grafana/regexp"

	"github.com/grafana/loki/v3/pkg/columnar"
)

// Expression 为 sealed 接口，仅列出的具体类型可通过 isExpr() 标记实现。
// Expression represents an operation that can be evaluated to produce a result.
type Expression interface{ isExpr() }

// 实现类包括 Constant、Column、Unary、Binary、Regexp（仅作 MatchRegex 右操作数）与 ValueSet（In 右操作数）。
// Types implementing [Expression].
type (
	// Constant 求值直接返回嵌入的 columnar.Scalar，与 batch 行数无关。
// Constant is an [Expression] that produces a single scalar value when
	// evaluated.
	Constant struct{ Value columnar.Scalar }

	// Column 按名查 batch Schema；列不存在则生成全 null 数组。
// Column is an [Expression] that looks up the column by name in the record
	// batch supplied to [Evaluate].
	//
	// If the column doesn't exist, a Null column is produced.
	Column struct{ Name string }

	// Unary 行为由 UnaryOp 决定，当前仅逻辑非。
// Unary is an [Expression] that performs a unary operation against a single
	// argument.
	//
	// The result of the expression depends on value of [UnaryOp]. The documentation
	// of [UnaryOp] will describe the behavior of the expression.
	Unary struct {
		Op    UnaryOp
		Value Expression
	}

	// Binary 行为由 BinaryOp 决定，涵盖比较、逻辑、正则与子串等运算。
// Binary is an [Expression] that performs a binary operation against a left and
	// a right expression.
	//
	// The result of the expression depends on value of [BinaryOp]. The documentation
	// of [BinaryOp] will describe the behavior of the expression.
	Binary struct {
		Left  Expression
		Op    BinaryOp
		Right Expression
	}

	// Regexp 不可独立求值为 Datum，仅作为 BinaryOpMatchRegex 的模式操作数。
// Regexp is an [Expression] used as the right-hand side of a
	// [BinaryOpMatchRegex].
	//
	// Regexp cannot be evaluated directly into a datum.
	Regexp struct{ Expression *regexp.Regexp }

	// ValueSet 封装 columnar.Set，供 BinaryOpIn 成员检测使用。
// ValueSet is an [Expression] used as the right-hand side of a [BinaryOpIn].
	//
	// ValueSet cannot be evaluated directly into a datum.
	ValueSet struct{ Values *columnar.Set }
)

func (*Constant) isExpr() {}
func (*Column) isExpr()   {}
func (*Unary) isExpr()    {}
func (*Binary) isExpr()   {}
func (*Regexp) isExpr()   {}
func (*ValueSet) isExpr() {}
// isExpr 空方法实现 sealed interface 模式，防止外部类型冒充 Expression。
