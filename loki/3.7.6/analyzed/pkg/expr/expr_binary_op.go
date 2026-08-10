package expr

// expr_binary_op 定义二元运算符枚举及字符串化；各常量注释说明操作数类型约束与返回 bool 标量/数组规则。

// BinaryOp 为 int 枚举，从 BinaryOpInvalid 起按 iota 递增。
// BinaryOp denotes a binary operation to perform against two arguments.
type BinaryOp int

const (
	// BinaryOpInvalid indicates an invalid binary operation. Evaluating a
	// BinaryOpInvalid will result in an error.
	BinaryOpInvalid BinaryOp = iota

	// BinaryOpEQ 要求左右同类型，结果为 bool 标量或按行 bool 数组。
// BinaryOpEQ performs an equality (==) check of the left and right
	// expressions. The expressions must be of the same type.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpEQ

	// BinaryOpNEQ performs an inequality (!=) check of the left and right
	// expressions. The expressions must be of the same type.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpNEQ

	// BinaryOpGT performs a greater than (>) check of the left and right
	// expressions. The expressions must be of the same type, and must be
	// ordered (numeric or UTF8).
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpGT

	// BinaryOpGTE performs a greater than or equal (>=) check of the left and
	// right expressions. The expressions must be of the same type, and must be
	// ordered (numeric or UTF8).
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpGTE

	// BinaryOpLT performs a less than (<) check of the left and right
	// expressions. The expressions must be of the same type, and must be
	// ordered (numeric or UTF8).
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpLT

	// BinaryOpLTE performs a less than or equal (<=) check of the left and
	// right expressions. The expressions must be of the same type, and must be
	// ordered (numeric or UTF8).
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpLTE

	// BinaryOpAND performs a logical AND (&&) operation on the left and right
	// expressions. The expressions must be of bool type.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpAND

	// BinaryOpOR performs a logical OR (||) operation on the left and right
	// expressions. The expressions must be of bool type.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpOR

	// BinaryOpMatchRegex 左为 UTF8 标量/数组，右为 Regexp；匹配则对应行为 true。
// BinaryOpMatchRegex performs a regex match of the left and right expressions.
	//
	// The left expression denotes the datum to search, and must be a UTF8
	// scalar or array. The right expression denotes the regular expression to
	// match with, and must be a [Regexp]. If the expression matches the UTF8
	// value, the result is true.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpMatchRegex

	// BinaryOpIn 左为待测 Datum，右为同类型 ValueSet；命中集合则 true。
// BinaryOpIn performs a membership check of the left and right expressions.
	//
	// The left expression denotes the datum to search. The right
	// expression denotes the set of values to search for, and must
	// be a [ValueSet] matching the left datum's type.
	//
	// If the value is found in the set, the result is true.
	//
	// The result is a bool datum, which is either a bool scalar if the
	// left datum is a scalar, otherwise the result is a bool array.
	BinaryOpIn

	// BinaryOpHasSubstr 左为 haystack（UTF8），右为 needle 标量，区分大小写子串搜索。
// BinaryOpHasSubstr performs a case-sensitive substring check of the left
	// and right expressions.
	//
	// The left expression denotes the "haystack" to search, and must be a UTF8
	// scalar or array. The right expression denotes the "needle" to search
	// with, and must be a UTF8 scalar. If the needle is found in the haystack,
	// the result is true.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpHasSubstr

	// BinaryOpHasSubstrIgnoreCase 与 HasSubstr 类似但忽略大小写。
// BinaryOpHasSubstrIgnoreCase performs a case-insensitive substring check
	// of the left and right expressions.
	//
	// The left expression denotes the "haystack" to search, and must be a UTF8
	// scalar or array. The right expression denotes the "needle" to search
	// with, and must be a UTF8 scalar. If the needle is found in the haystack
	// (ignoring case), the result is true.
	//
	// The result is a bool datum, which is either a bool scalar if both
	// arguments are scalars, otherwise the result is a bool array.
	BinaryOpHasSubstrIgnoreCase
)

var binaryOpStrings = [...]string{
	BinaryOpInvalid: "INVALID",

	BinaryOpEQ:  "EQ",
	BinaryOpNEQ: "NEQ",
	BinaryOpGT:  "GT",
	BinaryOpGTE: "GTE",
	BinaryOpLT:  "LT",
	BinaryOpLTE: "LTE",

	BinaryOpAND: "AND",
	BinaryOpOR:  "OR",

	BinaryOpMatchRegex:          "MATCH_REGEX",
	BinaryOpHasSubstr:           "HAS_SUBSTR",
	BinaryOpHasSubstrIgnoreCase: "HAS_SUBSTR_IGNORECASE",
}

// String 映射到 EQ/NEQ/MATCH_REGEX 等调试名；越界返回 INVALID。
// String returns the string representation of op. If op is out of bounds, it
// returns "INVALID."
func (op BinaryOp) String() string {
	if op < 0 || int(op) >= len(binaryOpStrings) {
		return "INVALID"
	}
	return binaryOpStrings[op]
}
// binaryOpStrings 与枚举顺序必须严格对齐，否则 String() 会错位。
