// token ID 解码：BPE 字节级与 SentencePiece ▁/字节 token 还原为文本。
package tokenizer

import (
	"strconv"
	"strings"
)

// Decode 将 token ID 序列解码为 UTF-8 文本。
// Decode converts token IDs back to text
func (t *Tokenizer) Decode(ids []int32) string {
	var sb strings.Builder

	for _, id := range ids {
		if int(id) >= len(t.vocab.Values) {
			continue
		}

		token := t.vocab.Values[id]

		switch t.typ {
		case TokenizerSentencePiece:
			// SentencePiece：▁→空格，并解析 <0xNN> 字节 token。
			// SentencePiece style: replace ▁ with space, decode byte tokens
			token = strings.ReplaceAll(token, "▁", " ")
			// 处理 <0x0D> 等十六进制字节占位符。
			// Handle byte fallback tokens like <0x0D>
			if len(token) == 6 && token[0] == '<' && token[1] == '0' && token[2] == 'x' && token[5] == '>' {
				if v, err := strconv.ParseUint(token[3:5], 16, 8); err == nil {
					sb.WriteByte(byte(v))
					continue
				}
			}
			sb.WriteString(token)
		default:
			// GPT-2 BPE：按 byteToRune 逆映射写原始字节。
			// GPT-2 BPE style: decode byte-level encoding
			for _, r := range token {
				switch {
				case r == 0x0100:
					// 与 GGML 一致：NULL 字节解码时省略。
					// Mirror GGML tokenizer behavior for NULL byte.
					// 0x00 is omitted during decode.
					continue
				case r == 0x0143:
					r = 0x00ad
				case r > 0x0100 && r <= 0x0120:
					r = r - 0x0100
				case r > 0x0120 && r <= 0x0142:
					r = r - 0x00a2
				}

				// 按单字节写入，非 UTF-8 编码 rune。
				// Write as byte, not UTF-8 encoded rune
				sb.WriteByte(byte(r))
			}
		}
	}

	return sb.String()
}
