package log

// label_extraction_expr 表示从日志解析出的单条标签提取配置：目标标识符与源表达式字符串。

type LabelExtractionExpr struct {
	Identifier string
	Expression string
}

func NewLabelExtractionExpr(identifier, expression string) LabelExtractionExpr {
	return LabelExtractionExpr{
		Identifier: identifier,
		Expression: expression,
	}
}
// Identifier 为输出标签名，Expression 保留原始提取语法供调试与计划打印。
