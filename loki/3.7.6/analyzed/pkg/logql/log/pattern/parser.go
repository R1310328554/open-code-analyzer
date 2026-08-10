package pattern

// parser 封装 pattern 表达式的 yacc 解析入口与 sync.Pool 复用，将输入字符串/字节编译为 expr AST。

import (
	"fmt"
	"sync"
)

const underscore = "_"

var tokens = map[int]string{
	LESS_THAN:  "<",
	MORE_THAN:  ">",
	UNDERSCORE: underscore,
}

type parser struct {
	p *exprParserImpl
}

var parserPool = sync.Pool{
	New: func() interface{} {
		p := &parser{
			p: &exprParserImpl{},
		}
		return p
	},
}

func init() {
	// Improve the error messages coming out of yacc.
	exprErrorVerbose = true
	for tok, str := range tokens {
		exprToknames[tok-exprPrivate+1] = str
	}
}

// parseExpr 解析完整 pattern 字符串并校验捕获约束。
func parseExpr(input string) (expr, error) {
	return parseExprBytes([]byte(input))
}

func parseExprBytes(input []byte) (expr, error) {
	p := parserPool.Get().(*parser)
	defer parserPool.Put(p)

	l := newLexer()
	l.setData(input)

	e := p.p.Parse(l)
	if e != 0 || len(l.errs) > 0 {
		return nil, l.errs[0]
	}
	return l.expr, nil
}

// parseError is what is returned when we failed to parse.
// parseError 携带行列号与消息，供 LogQL 报错展示。
type parseError struct {
	msg       string
	line, col int
}

func (p parseError) Error() string {
	if p.col == 0 && p.line == 0 {
		return p.msg
	}
	return fmt.Sprintf("parse error at line %d, col %d: %s", p.line, p.col, p.msg)
}

func newParseError(msg string, line, col int) parseError {
	return parseError{
		msg:  msg,
		line: line,
		col:  col,
	}
}
// init 注册 token 可读名并启用 exprErrorVerbose 改善语法错误输出。
