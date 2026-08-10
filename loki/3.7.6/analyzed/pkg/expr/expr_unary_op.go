package expr

// expr_unary_op 定义一元运算符：当前仅 UnaryOpNOT 对 bool 值逻辑取反。

// UnaryOp 为 int 枚举，UnaryOpInvalid 表示无效/未定义操作。
// UnaryOp denotes a unary operation to perform against a single argument.
type UnaryOp int

const (
	// UnaryOpInvalid indicates an invalid unary operation. Evaluating a
	// UnaryOpInvalid will result in an error.
	UnaryOpInvalid UnaryOp = iota

	// UnaryOpNOT 在 evaluateUnary 中递归求值子表达式后调用 compute.Not。
// UnaryOpNOT represents a logical NOT operation over a boolean value.
	UnaryOpNOT
)

var unaryOpStrings = [...]string{
	UnaryOpInvalid: "INVALID",
	UnaryOpNOT:     "NOT",
}

// String 返回 NOT 或 INVALID，供日志与错误信息格式化。
// String returns the string representation of op. If op is out of bounds, it
// returns "INVALID."
func (op UnaryOp) String() string {
	if op < 0 || int(op) >= len(unaryOpStrings) {
		return "INVALID"
	}
	return unaryOpStrings[op]
}
// 扩展新一元算子时需同步更新 unaryOpStrings 与 evaluateUnary 分支。
