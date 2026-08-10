package expressionpb

// marshal_types 维护 protobuf 枚举与 types 包内部枚举的双向查找表，MarshalType 方法完成 protobuf → native 方向映射。

import (
	fmt "fmt"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

var (
	nativeUnaryOpLookup = map[UnaryOp]types.UnaryOp{
		UNARY_OP_INVALID:       types.UnaryOpInvalid,
		UNARY_OP_NOT:           types.UnaryOpNot,
		UNARY_OP_ABS:           types.UnaryOpAbs,
		UNARY_OP_CAST_FLOAT:    types.UnaryOpCastFloat,
		UNARY_OP_CAST_BYTES:    types.UnaryOpCastBytes,
		UNARY_OP_CAST_DURATION: types.UnaryOpCastDuration,
	}

	nativeBinaryOpLookup = map[BinaryOp]types.BinaryOp{
		BINARY_OP_INVALID:                       types.BinaryOpInvalid,
		BINARY_OP_EQ:                            types.BinaryOpEq,
		BINARY_OP_NEQ:                           types.BinaryOpNeq,
		BINARY_OP_GT:                            types.BinaryOpGt,
		BINARY_OP_GTE:                           types.BinaryOpGte,
		BINARY_OP_LT:                            types.BinaryOpLt,
		BINARY_OP_LTE:                           types.BinaryOpLte,
		BINARY_OP_AND:                           types.BinaryOpAnd,
		BINARY_OP_OR:                            types.BinaryOpOr,
		BINARY_OP_XOR:                           types.BinaryOpXor,
		BINARY_OP_ADD:                           types.BinaryOpAdd,
		BINARY_OP_SUB:                           types.BinaryOpSub,
		BINARY_OP_MUL:                           types.BinaryOpMul,
		BINARY_OP_DIV:                           types.BinaryOpDiv,
		BINARY_OP_MOD:                           types.BinaryOpMod,
		BINARY_OP_POW:                           types.BinaryOpPow,
		BINARY_OP_MATCH_SUBSTR:                  types.BinaryOpMatchSubstr,
		BINARY_OP_NOT_MATCH_SUBSTR:              types.BinaryOpNotMatchSubstr,
		BINARY_OP_MATCH_RE:                      types.BinaryOpMatchRe,
		BINARY_OP_NOT_MATCH_RE:                  types.BinaryOpNotMatchRe,
		BINARY_OP_MATCH_PATTERN:                 types.BinaryOpMatchPattern,
		BINARY_OP_NOT_MATCH_PATTERN:             types.BinaryOpNotMatchPattern,
		BINARY_OP_EQ_CASE_INSENSITIVE:           types.BinaryOpEqCaseInsensitive,
		BINARY_OP_NEQ_CASE_INSENSITIVE:          types.BinaryOpNotEqCaseInsensitive,
		BINARY_OP_MATCH_SUBSTR_CASE_INSENSITIVE: types.BinaryOpMatchSubstrCaseInsensitive,
		BINARY_OP_NOT_MATCH_SUBSTR_CASE_INSENSITIVE: types.BinaryOpNotMatchSubstrCaseInsensitive,
	}

	nativeVariadicOpLookup = map[VariadicOp]types.VariadicOp{
		VARIADIC_OP_INVALID:      types.VariadicOpInvalid,
		VARIADIC_OP_PARSE_LOGFMT: types.VariadicOpParseLogfmt,
		VARIADIC_OP_PARSE_JSON:   types.VariadicOpParseJSON,
	}

	nativeColumnTypeLookup = map[ColumnType]types.ColumnType{
		COLUMN_TYPE_INVALID:   types.ColumnTypeInvalid,
		COLUMN_TYPE_BUILTIN:   types.ColumnTypeBuiltin,
		COLUMN_TYPE_LABEL:     types.ColumnTypeLabel,
		COLUMN_TYPE_METADATA:  types.ColumnTypeMetadata,
		COLUMN_TYPE_PARSED:    types.ColumnTypeParsed,
		COLUMN_TYPE_AMBIGUOUS: types.ColumnTypeAmbiguous,
		COLUMN_TYPE_GENERATED: types.ColumnTypeGenerated,
	}
)

// MarshalType converts a protobuf UnaryOp to its internal types.UnaryOp representation.
// Returns an error if the conversion fails or the operator is unsupported.
// UnaryOp.MarshalType 查 nativeUnaryOpLookup，含 NOT/ABS/类型转换等一元算子。
func (o UnaryOp) MarshalType() (types.UnaryOp, error) {
	out, ok := nativeUnaryOpLookup[o]
	if !ok {
		return types.UnaryOpInvalid, fmt.Errorf("unsupported unary operator %s", o)
	}
	return out, nil
}

// MarshalType converts a protobuf BinaryOp to its internal types.BinaryOp representation.
// Returns an error if the conversion fails or the operator is unsupported.
// BinaryOp.MarshalType 映射比较、逻辑、算术及字符串匹配类二元算子。
func (o BinaryOp) MarshalType() (types.BinaryOp, error) {
	out, ok := nativeBinaryOpLookup[o]
	if !ok {
		return types.BinaryOpInvalid, fmt.Errorf("unsupported binary operator %s", o)
	}
	return out, nil
}

// MarshalType converts a protobuf VariadicOp to its internal types.VariadicOp representation.
// Returns an error if the conversion fails or the operator is unsupported.
// VariadicOp.MarshalType 映射 parse_logfmt、parse_json 等变参解析算子。
func (o VariadicOp) MarshalType() (types.VariadicOp, error) {
	out, ok := nativeVariadicOpLookup[o]
	if !ok {
		return types.VariadicOpInvalid, fmt.Errorf("unsupported variadic operator %s", o)
	}
	return out, nil
}

// MarshalType converts a protobuf ColumnType to its internal types.ColumnType representation.
// Returns an error if the conversion fails or the column type is unsupported.
// ColumnType.MarshalType 区分 builtin/label/metadata/parsed 等列来源类型。
func (t ColumnType) MarshalType() (types.ColumnType, error) {
	out, ok := nativeColumnTypeLookup[t]
	if !ok {
		return types.ColumnTypeInvalid, fmt.Errorf("unsupported column type %s", t)
	}
	return out, nil
}
// lookup 表未覆盖的枚举值返回 Invalid 常量并附带 unsupported 错误信息。
