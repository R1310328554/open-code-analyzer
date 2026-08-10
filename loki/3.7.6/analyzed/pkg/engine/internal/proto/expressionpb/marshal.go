package expressionpb

// marshal 包提供 expressionpb 到 physical.Expression 的转换，供物理计划从 protobuf 反序列化后构建内存表达式树。

import (
	fmt "fmt"

	"github.com/grafana/loki/v3/pkg/engine/internal/planner/physical"
	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

type marshaler interface {
	MarshalPhysical() (physical.Expression, error)
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// Expression 入口：按 Kind oneof 分派到具体变体的 MarshalPhysical。
func (e *Expression) MarshalPhysical() (physical.Expression, error) {
	m, ok := e.Kind.(marshaler)
	if !ok {
		return nil, fmt.Errorf("unsupported physical expression type: %T", e.Kind)
	}
	return m.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
func (e *Expression_Unary) MarshalPhysical() (physical.Expression, error) {
	return e.Unary.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
func (e *Expression_Binary) MarshalPhysical() (physical.Expression, error) {
	return e.Binary.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
func (e *Expression_Variadic) MarshalPhysical() (physical.Expression, error) {
	return e.Variadic.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
func (e *Expression_Literal) MarshalPhysical() (physical.Expression, error) {
	return e.Literal.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
func (e *Expression_Column) MarshalPhysical() (physical.Expression, error) {
	return e.Column.MarshalPhysical()
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// UnaryExpression 递归转换子表达式并映射 UnaryOp 为 types.UnaryOp。
func (e *UnaryExpression) MarshalPhysical() (physical.Expression, error) {
	value, err := e.Value.MarshalPhysical()
	if err != nil {
		return nil, err
	}

	op, err := e.Op.MarshalType()
	if err != nil {
		return nil, err
	}

	return &physical.UnaryExpr{
		Op:   op,
		Left: value,
	}, nil
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// BinaryExpression 分别转换左右子树，再组装 physical.BinaryExpr。
func (e *BinaryExpression) MarshalPhysical() (physical.Expression, error) {
	left, err := e.Left.MarshalPhysical()
	if err != nil {
		return nil, err
	}
	right, err := e.Right.MarshalPhysical()
	if err != nil {
		return nil, err
	}

	op, err := e.Op.MarshalType()
	if err != nil {
		return nil, err
	}

	return &physical.BinaryExpr{
		Op:    op,
		Left:  left,
		Right: right,
	}, nil
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// VariadicExpression 批量转换 Args 并映射 parse_logfmt/parse_json 等变参算子。
func (e *VariadicExpression) MarshalPhysical() (physical.Expression, error) {
	expressions := make([]physical.Expression, len(e.Args))
	for i, arg := range e.Args {
		expr, err := arg.MarshalPhysical()
		if err != nil {
			return nil, err
		}
		expressions[i] = expr
	}

	op, err := e.Op.MarshalType()
	if err != nil {
		return nil, err
	}

	return &physical.VariadicExpr{
		Op:          op,
		Expressions: expressions,
	}, nil
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// LiteralExpression 通过 literalMarshaler 将 protobuf 字面量转为 types.Literal。
func (e *LiteralExpression) MarshalPhysical() (physical.Expression, error) {
	m, ok := e.Kind.(literalMarshaler)
	if !ok {
		return nil, fmt.Errorf("unsupported literal expression type: %T", e.Kind)
	}
	literal, err := m.MarshalLiteral()
	if err != nil {
		return nil, err
	}

	return physical.NewLiteral(literal.Any()), nil
}

// MarshalPhysical converts a protobuf expression into a physical plan
// expression. Returns an error if the conversion fails or is unsupported.
// ColumnExpression 将列名与 ColumnType 映射为 physical.ColumnExpr 列引用。
func (e *ColumnExpression) MarshalPhysical() (physical.Expression, error) {
	columnType, err := e.Type.MarshalType()
	if err != nil {
		return nil, err
	}

	return &physical.ColumnExpr{
		Ref: types.ColumnRef{
			Column: e.Name,
			Type:   columnType,
		},
	}, nil
}
// 不支持的 Kind 类型返回 fmt.Errorf，调用方应在上层捕获并拒绝非法计划。
