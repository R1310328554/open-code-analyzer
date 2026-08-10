package jsonexpr

// parser 暴露 Parse 入口：对 JSON 路径表达式字符串词法+语法分析，返回嵌套 list 表示的路径 AST。

import (
	"strings"
)

func init() {
	JSONExprErrorVerbose = true
}

func Parse(expr string, debug bool) ([]interface{}, error) {
	s := NewScanner(strings.NewReader(expr), debug)
	JSONExprParse(s)

	if s.err != nil {
		return nil, s.err
	}
	return s.data, nil
}
// 路径 AST 元素类型为 string（字段/键）或 int（数组下标），由 JSON 提取 Stage 逐步下钻 JSON。
