package logql

// LogQL 表达式抽象语法树与行过滤器。
// 定义 Expr 接口及标签选择器、管道过滤器的组合求值逻辑。

import (
	"bytes"
	"fmt"
	"regexp"

	"github.com/prometheus/prometheus/model/labels"
)

// 行级过滤函数：对单条日志字节流返回是否保留。
// Filter is a line filter sent to a querier to filter out log line.
type Filter func([]byte) bool

// LogQL 表达式根接口：提供 Matchers 与 Filter 两种求值视图。
// Expr is a LogQL expression.
type Expr interface {
	Filter() (Filter, error)
	Matchers() []*labels.Matcher
}

type matchersExpr struct {
	matchers []*labels.Matcher
}

func (e *matchersExpr) Matchers() []*labels.Matcher {
	return e.matchers
}

func (e *matchersExpr) Filter() (Filter, error) {
	return nil, nil
}

type filterExpr struct {
	left  Expr
	ty    labels.MatchType
	match string
}

func (e *filterExpr) Matchers() []*labels.Matcher {
	return e.left.Matchers()
}

// 在已有表达式上叠加管道过滤器（|=、|~、!=、!~）。
// NewFilterExpr wraps an existing Expr with a next filter expression.
func NewFilterExpr(left Expr, ty labels.MatchType, match string) Expr {
	return &filterExpr{
		left:  left,
		ty:    ty,
		match: match,
	}
}

func (e *filterExpr) Filter() (Filter, error) {
	var f func([]byte) bool
	switch e.ty {
	case labels.MatchRegexp:
		re, err := regexp.Compile(e.match)
		if err != nil {
			return nil, err
		}
		f = re.Match

	case labels.MatchNotRegexp:
		re, err := regexp.Compile(e.match)
		if err != nil {
			return nil, err
		}
		f = func(line []byte) bool {
			return !re.Match(line)
		}

	case labels.MatchEqual:
		f = func(line []byte) bool {
			return bytes.Contains(line, []byte(e.match))
		}

	case labels.MatchNotEqual:
		f = func(line []byte) bool {
			return !bytes.Contains(line, []byte(e.match))
		}

	default:
		return nil, fmt.Errorf("unknow matcher: %v", e.match)
	}
	next, ok := e.left.(*filterExpr)
	if ok {
		nextFilter, err := next.Filter()
		if err != nil {
			return nil, err
		}
		return func(line []byte) bool {
			return nextFilter(line) && f(line)
		}, nil
	}
	return f, nil
}

// 构造 labels.Matcher，解析失败时 panic（供 yacc 动作使用）。
func mustNewMatcher(t labels.MatchType, n, v string) *labels.Matcher {
	m, err := labels.NewMatcher(t, n, v)
	if err != nil {
		panic(err)
	}
	return m
}
