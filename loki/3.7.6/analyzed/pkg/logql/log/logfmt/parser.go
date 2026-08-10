package logfmt

// parser 封装 logfmt 路径表达式的词法/语法分析入口，供 LogfmtExpressionParser 将表达式编译为嵌套路径树。

import (
	"strings"
)

func init() {
	LogfmtExprErrorVerbose = true
}

func Parse(expr string, debug bool) ([]interface{}, error) {
	s := NewScanner(strings.NewReader(expr), debug)
	LogfmtExprParse(s)

	if s.err != nil {
		return nil, s.err
	}
	return s.data, nil
}
// 解析失败时返回 Scanner 收集的首个词法/语法错误。
