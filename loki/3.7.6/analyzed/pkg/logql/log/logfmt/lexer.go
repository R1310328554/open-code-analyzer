package logfmt

// lexer 为 logfmt 路径表达式提供手写词法分析器，产出 KEY/STRING token 供 goyacc 生成的 LogfmtExpr 解析器使用。

import (
	"bufio"
	"fmt"
	"io"
	"text/scanner"
)

type Scanner struct {
	buf   *bufio.Reader
	data  []interface{}
	err   error
	debug bool
}

func NewScanner(r io.Reader, debug bool) *Scanner {
	return &Scanner{
		buf:   bufio.NewReader(r),
		debug: debug,
	}
}

func (sc *Scanner) Error(s string) {
	sc.err = fmt.Errorf("%s", s)
	fmt.Printf("syntax error: %s\n", s)
}

func (sc *Scanner) Reduced(rule, state int, lval *LogfmtExprSymType) bool {
	if sc.debug {
		fmt.Printf("rule: %v; state %v; lval: %v\n", rule, state, lval)
	}
	return false
}

func (sc *Scanner) Lex(lval *LogfmtExprSymType) int {
	return sc.lex(lval)
}

// lex 跳过空白后识别标识符键名或双引号字符串字面量。
func (sc *Scanner) lex(lval *LogfmtExprSymType) int {
	for {
		r := sc.read()

		if r == 0 {
			return 0
		}
		if isWhitespace(r) {
			continue
		}

		switch true {
		case isStartIdentifier(r):
			sc.unread()
			lval.key = sc.scanField()
			return KEY
		case r == '"':
			sc.unread()
			lval.str = sc.scanStr()
			return STRING
		default:
			sc.err = fmt.Errorf("unexpected char %c", r)
			return 0
		}
	}
}

// 标识符以字母或下划线开头，后续可含数字。
func isStartIdentifier(r rune) bool {
	return (r >= 'a' && r <= 'z') || (r >= 'A' && r <= 'Z') || r == '_'
}

func isIdentifier(r rune) bool {
	return isStartIdentifier(r) || (r >= '0' && r <= '9')
}

func (sc *Scanner) scanField() string {
	var str []rune

	for {
		r := sc.read()
		if !isIdentifier(r) || isEndOfInput(r) {
			sc.unread()
			break
		}

		str = append(str, r)
	}
	return string(str)
}

// input is either terminated by EOF or null byte
func isEndOfInput(r rune) bool {
	return r == scanner.EOF || r == rune(0)
}

func (sc *Scanner) read() rune {
	ch, _, _ := sc.buf.ReadRune()
	return ch
}

func (sc *Scanner) scanStr() string {
	var str []rune
	//begin with ", end with "
	r := sc.read()
	if r != '"' {
		sc.err = fmt.Errorf("unexpected char %c", r)
		return ""
	}

	for {
		r := sc.read()
		if isEndOfInput(r) {
			break
		}

		if r == '"' || r == ']' {
			break
		}
		str = append(str, r)
	}
	return string(str)
}

func (sc *Scanner) unread() { _ = sc.buf.UnreadRune() }

func isWhitespace(ch rune) bool { return ch == ' ' || ch == '\t' || ch == '\n' }
// scanStr 以 " 为界读取路径段；EOF 或 null 字节终止输入。
