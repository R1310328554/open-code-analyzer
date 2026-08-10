package computetest

// compute 测试 DSL 词法扫描器：识别标识符、整数、字符串、
// 关键字 select 及符号 token，并自动插入语句终止符。

import (
	"bufio"
	"io"
	"strconv"
)

const (
	bom = 0xFEFF // byte order mark, permitted as very first character
	eof = -1     // end of file
)

type scanner struct {
	reader *bufio.Reader

	line       int
	col        int
	ch         rune
	insertTerm bool // flag to insert terminator token
}

// position 记录 token 在源文件中的行号与列号。
type position struct{ Line, Col int }

func newScanner(r io.Reader) *scanner {
	s := &scanner{
		reader: bufio.NewReader(r),

		line: 1,
	}

	s.next() // Preload first character.
	if s.ch == bom {
		s.next() // Ignore BOM if it's the first character.
	}
	return s
}

// Scan 返回下一 token 的位置、类型与字面量，EOF 以 tokenEOF 表示。
// Scan scans the next token and returns the token position, the token, and its
// literal string if applicable. The source end is indicated by [tokenEOF].
//
// If the returned token is a literal, the lit string has the corresponding
// value. Otherwise, if the token is a keyword or symbol, the literal string is
// that keyword or symbol.
//
// Scan will skip whitespace and comments.
func (s *scanner) Scan() (pos position, tok token, lit string) {
scanAgain:
	s.skipWhitespace()

	pos = position{Line: s.line, Col: s.col}

	switch ch := s.ch; {
	case isIdentStart(ch):
		pos, tok, lit = s.scanIdent(pos)
	case isDigit(ch):
		pos, tok, lit = s.scanNumber(pos)

	default:
		s.next() // Make progress.

		// ch is now the first character in a sequence, and s.ch is the second
		// character.

		switch ch {
		case eof:
			if s.insertTerm {
				s.insertTerm = false
				return pos, tokenTerm, "TERMINATOR"
			}
			tok, lit = tokenEOF, "EOF"
		case '\n':
			// Only reachable when insertTerm is true.
			s.insertTerm = false
			tok, lit = tokenTerm, "\n"
		case '#':
			s.skipComment()
			goto scanAgain
		case ':':
			tok, lit = tokenColon, ":"
		case '[':
			tok, lit = tokenLBrack, "["
		case ']':
			tok, lit = tokenRBrack, "]"
		case '"':
			pos, tok, lit = s.scanString(pos)
		case '-': // - or ->
			if s.ch == '>' {
				s.next() // consume >
				tok, lit = tokenArrow, "->"
			} else {
				tok, lit = tokenSub, "-"
			}
		}
	}

	if s.canTerminate(tok) {
		s.insertTerm = true
	}
	return pos, tok, lit
}

func (s *scanner) next() {
	if s.ch == '\n' {
		s.line++
		s.col = 1
	} else {
		s.col++
	}

	ch, _, err := s.reader.ReadRune()
	if err != nil {
		s.ch = eof
		return
	}
	s.ch = ch
}

func (s *scanner) skipWhitespace() {
	for s.ch == ' ' || s.ch == '\t' || s.ch == '\r' || (s.ch == '\n' && !s.insertTerm) {
		s.next()
	}
}

func (s *scanner) skipComment() {
	for s.ch != '\n' && s.ch != eof {
		s.next()
	}
}

// canTerminate 判断当前 token 后是否应插入隐式 TERMINATOR。
// canTerminate returns true if the given token can end a statement
func (s *scanner) canTerminate(tok token) bool {
	switch tok {
	case tokenIdent, tokenInteger, tokenString, tokenRBrack:
		return true
	default:
		return false
	}
}

// scanIdent 读取标识符或关键字 select。
func (s *scanner) scanIdent(pos position) (position, token, string) {
	lit := string(s.ch)
	s.next()
	for isIdentContinue(s.ch) {
		lit += string(s.ch)
		s.next()
	}
	switch lit {
	case "select":
		return pos, tokenSelect, lit
	default:
		return pos, tokenIdent, lit
	}
}

func (s *scanner) scanNumber(pos position) (position, token, string) {
	lit := string(s.ch)
	s.next()
	for isDigit(s.ch) {
		lit += string(s.ch)
		s.next()
	}
	return pos, tokenInteger, lit
}

// scanString 解析双引号字符串并经由 strconv.Unquote 处理转义。
func (s *scanner) scanString(pos position) (position, token, string) {
	// Build the raw string including quotes for strconv.Unquote
	// The opening quote has already been consumed
	raw := "\""
	for {
		if s.ch == eof {
			// Unterminated string
			return pos, tokenIllegal, ""
		}
		if s.ch == '"' {
			// End of string
			raw += "\""
			s.next() // consume closing quote
			break
		}
		if s.ch == '\\' {
			// Include escape sequence as-is
			raw += "\\"
			s.next()
			if s.ch == eof {
				return pos, tokenIllegal, ""
			}
			raw += string(s.ch)
			s.next()
		} else {
			raw += string(s.ch)
			s.next()
		}
	}

	// Use strconv.Unquote to handle escape sequences
	lit, err := strconv.Unquote(raw)
	if err != nil {
		return pos, tokenIllegal, ""
	}
	return pos, tokenString, lit
}

func isDigit(ch rune) bool {
	return ch >= '0' && ch <= '9'
}

func isIdentStart(ch rune) bool {
	return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_'
}

func isIdentContinue(ch rune) bool {
	return isIdentStart(ch) || isDigit(ch)
}
// 扫描器为解析器提供带位置信息的词法单元序列。
