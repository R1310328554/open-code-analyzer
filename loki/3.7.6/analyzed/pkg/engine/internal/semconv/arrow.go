package semconv

// semconv/arrow 将语义列标识 Identifier 转换为 Apache Arrow Field，供引擎列式存储与交换使用。

import (
	"github.com/apache/arrow-go/v18/arrow"
)

// FieldFromIdent 用 FQN 作字段名，类型来自 Identifier 的 Loki DataType。
func FieldFromIdent(ident *Identifier, nullable bool) arrow.Field {
	return arrow.Field{
		Name:     ident.FQN(),
		Type:     ident.dataType.ArrowType(),
		Nullable: nullable,
	}
}

// FieldFromFQN 解析完全限定名后委托 FieldFromIdent 构建 Arrow 字段。
func FieldFromFQN(fqn string, nullable bool) arrow.Field {
	ident := MustParseFQN(fqn)
	return FieldFromIdent(ident, nullable)
}
// nullable 参数控制 Arrow 字段是否允许空值，与 SQL 语义一致。
