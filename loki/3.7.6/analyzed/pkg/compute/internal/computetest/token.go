package computetest

// compute 测试 DSL 词法 token 枚举与 String 实现。

// token 表示 DSL 中所有词法单元类型。
// token is the set of lexical tokens for the compute DSL.
type token int

const (
	tokenIllegal token = iota
	tokenEOF
	tokenTerm // \n (statement terminator)

	tokenIdent   // foo
	tokenInteger // 12345 (also used for unsigned)
	tokenString  // "quoted string"
	tokenSelect  // "select"

	tokenSub // - (used for negative integers)

	tokenColon  // ":"
	tokenArrow  // "->"
	tokenLBrack // "["
	tokenRBrack // "]"
)

// tokenNames 提供 token 到调试字符串的映射表。
var tokenNames = [...]string{
	tokenIllegal: "ILLEGAL",
	tokenEOF:     "EOF",
	tokenTerm:    "TERMINATOR",

	tokenIdent:   "IDENT",
	tokenInteger: "INTEGER",
	tokenString:  "STRING",
	tokenSelect:  "select",

	tokenSub: "-",

	tokenColon:  ":",
	tokenArrow:  "->",
	tokenLBrack: "[",
	tokenRBrack: "]",
}

// String 返回 token 的可读名称，越界时回退 ILLEGAL。
func (t token) String() string {
	if int(t) < len(tokenNames) {
		return tokenNames[t]
	}
	return "ILLEGAL"
}
// token 类型为扫描器与解析器共享的词法契约。
