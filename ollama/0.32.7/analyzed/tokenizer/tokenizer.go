// Tokenizer 接口与 token 类型常量。
package tokenizer

// token 类型枚举（与 SentencePiece 词表类型一致）。
const (
	TOKEN_TYPE_NORMAL = iota + 1
	TOKEN_TYPE_UNKNOWN
	TOKEN_TYPE_CONTROL
	TOKEN_TYPE_USER_DEFINED
	TOKEN_TYPE_UNUSED
	TOKEN_TYPE_BYTE
)

// Tokenizer 定义 Encode/Decode/Is/Vocabulary 统一分词接口。
type Tokenizer interface {
	Encode(s string, addSpecial bool) ([]int32, error)
	Decode([]int32) (string, error)
	Is(int32, Special) bool
	Vocabulary() *Vocabulary
}
