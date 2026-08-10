// WordPiece 分词器：BERT 风格最长匹配子词，支持 CJK 单字切分。
package tokenizer

import (
	"fmt"
	"iter"
	"strings"
	"unicode"

	"github.com/ollama/ollama/logutil"
)

// WordPiece 持有词表与是否小写化选项。
type WordPiece struct {
	vocab     *Vocabulary
	lowercase bool
}

// ggmlPrefix 为 GGML 词表中表示词边界的 ▁ 前缀（非原始 ## 子词标记）。
// ggmlPrefix is the prefix used by GGML vocabularies to indicate word boundaries.
// this differs from original word piece which uses "##" to indicate subwords.
const ggmlPrefix = "▁"

// wordPieceReplacer 规范化标点与缩写的空格。
var wordPieceReplacer = strings.NewReplacer(
	" .", ".",
	" ?", "?",
	" !", "!",
	" ,", ",",
	" ' ", "'",
	" n't", "n't",
	" 'm", "'m",
	" do not", " don't",
	" 's", "'s",
	" 've", "'ve",
	" 're", "'re",
)

// Decode 将 id 序列拼接为文本，▁ 前缀处插入空格。
// Decode implements Tokenizer.
func (wpm WordPiece) Decode(ids []int32) (string, error) {
	var sb strings.Builder
	for i, id := range ids {
		if id < 0 || int(id) >= len(wpm.vocab.Values) {
			return "", fmt.Errorf("invalid token id: %d", id)
		}

		var separator string
		piece := wpm.vocab.Values[id]
		if i > 0 &&
			(strings.HasPrefix(piece, ggmlPrefix) ||
				(strings.HasPrefix(piece, "[") && strings.HasSuffix(piece, "]"))) {
			separator = " "
		}

		sb.WriteString(wordPieceReplacer.Replace(separator + strings.TrimPrefix(piece, ggmlPrefix)))
	}

	return sb.String(), nil
}

// words 将字符串切分为词，CJK 字符单独成词。
// words splits a string into words, treating CJK characters as separate words.
// TODO: this is specifically for BERT and may need to be adjusted or refactored for other models.
func (wpm WordPiece) words(s string) iter.Seq[string] {
	return func(yield func(string) bool) {
		runes := make([]rune, 0, len(s)*3)
		for _, r := range s {
			switch {
			case r >= 0x4E00 && r <= 0x9FFF,
				r >= 0x3400 && r <= 0x4DBF,
				r >= 0x20000 && r <= 0x2A6DF,
				r >= 0x2A700 && r <= 0x2B73F,
				r >= 0x2B740 && r <= 0x2B81F,
				r >= 0x2B820 && r <= 0x2CEAF,
				r >= 0xF900 && r <= 0xFAFF,
				r >= 0x2F800 && r <= 0x2FA1F:
				runes = append(runes, ' ', r, ' ')
			default:
				runes = append(runes, r)
			}
		}

		for w := range strings.FieldsFuncSeq(string(runes), unicode.IsSpace) {
			// 保留标点并单独成段。
			// split on but keep punctuation
			var start int
			for start < len(w) {
				end := strings.IndexFunc(w[start:], unicode.IsPunct)
				if end < 0 {
					end = len(w) - start
				} else if end == 0 {
					end = 1
				}

				if !yield(w[start : start+end]) {
					return
				}

				start += end
			}
		}
	}
}

// Encode 对每个词做最长前缀 WordPiece 匹配，未知词用 unk。
// Encode implements Tokenizer.
func (wpm WordPiece) Encode(s string, addSpecial bool) ([]int32, error) {
	var ids []int32

	// TODO: use [UNK] from config
	unk := wpm.vocab.Encode("[UNK]")
	for word := range wpm.words(s) {
		var start int
		var pieces []int32
		for start < len(word) {
			end := len(word)

			var piece int32
			for start < end {
				subword := word[start:end]
				if start == 0 {
					subword = ggmlPrefix + subword
				}

				if wpm.lowercase {
					subword = strings.ToLower(subword)
				}
				piece = wpm.vocab.Encode(subword)
				if piece >= 0 {
					break
				}

				end--
			}

			if piece < 0 {
				// 整词无法切分时标记为未知 token。
				// Unknown token
				pieces = pieces[:0]
				break
			}

			pieces = append(pieces, piece)
			start = end
		}

		if len(pieces) > 0 {
			ids = append(ids, pieces...)
		} else {
			ids = append(ids, unk)
		}
	}

	if addSpecial {
		ids = wpm.vocab.addSpecials(ids)
	}

	logutil.Trace("encoded", "string", s, "ids", ids)
	return ids, nil
}

// Is 判断 id 是否为特殊 token。
// Is implements Tokenizer.
func (wpm WordPiece) Is(id int32, special Special) bool {
	return wpm.vocab.Is(id, special)
}

// Vocabulary 返回底层词表。
// Vocabulary implements Tokenizer.
func (wpm WordPiece) Vocabulary() *Vocabulary {
	return wpm.vocab
}

var _ Tokenizer = (*WordPiece)(nil)

// NewWordPiece 构造 WordPiece 分词器。
func NewWordPiece(vocab *Vocabulary, lowercase bool) WordPiece {
	return WordPiece{
		vocab:     vocab,
		lowercase: lowercase,
	}
}
