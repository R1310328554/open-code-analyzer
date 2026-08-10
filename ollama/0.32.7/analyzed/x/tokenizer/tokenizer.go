// tokenizer.go — HuggingFace 模型的 BPE 与 SentencePiece 分词器实现。
// tokenizer.go - BPE and SentencePiece tokenizer for HuggingFace models
//
// 基于标准 BPE（Sennrich 2015），并支持：
// Based on standard BPE algorithm (Sennrich et al. 2015) with:
// - GPT-2 byte-level encoding (OpenAI tiktoken)
// - HuggingFace tokenizer.json pretokenizer patterns
// - SentencePiece ▁-style space handling

package tokenizer

import "regexp"

// TokenizerType 标识分词算法类型。
// TokenizerType identifies the tokenization algorithm
type TokenizerType int

const (
	TokenizerBPE           TokenizerType = iota // GPT-2 字节级 BPE
	// GPT-2 style byte-level BPE
	TokenizerSentencePiece                      // SentencePiece，空格用 ▁
	// SentencePiece with ▁ for spaces
)

// Vocabulary 保存词表、merge 规则与特殊 token 配置。
// Vocabulary holds the tokenizer vocabulary and merges
type Vocabulary struct {
	Values  []string
	Reverse map[string]int32
	Merges  map[string]int

	BOS    int32
	EOS    []int32 // 支持多个 EOS（如 Gemma 的 <eos> 与 <end_of_turn>）
	// Multiple EOS tokens supported (e.g., Gemma has <eos> and <end_of_turn>)
	PAD    int32   // 填充 token
	// Padding token (often <|endoftext|> or <pad>)
	AddBOS bool
	AddEOS bool

	// 预计算 <0xNN> 字节回退 token ID（256 项，无则 -1）。
	// Precomputed byte token IDs for <0xNN> fallback (256 entries, -1 if not found)
	byteTokens [256]int32
}

// Tokenizer 封装词表、预分词正则与特殊 token 查找。
// Tokenizer handles BPE and SentencePiece tokenization
type Tokenizer struct {
	vocab                              *Vocabulary
	pretokenizer                       *regexp.Regexp
	pretokenizerSpaceBeforePunctuation bool
	specialTokens                      map[string]int32 // 特殊 token 字符串→ID
	// Special tokens for direct lookup
	sortedSpecialTokens                []string         // 按长度降序，最长优先匹配
	// Special tokens sorted by length, longest first
	typ                                TokenizerType    // 当前算法类型
	// Algorithm type
}

// 预计算 GPT-2 字节级编码表：字节→编码 rune。
// Precomputed GPT-2 byte-level encoding table
// Maps byte values to their encoded rune equivalents
var byteToRune [256]rune

// init 填充 byteToRune 映射（tiktoken/GGML 兼容规则）。
func init() {
	for b := range 256 {
		r := rune(b)
		switch {
		case r == 0x00ad:
			r = 0x0143
		case r <= 0x0020:
			r = r + 0x0100
		case r >= 0x007f && r <= 0x00a0:
			r = r + 0x00a2
		}
		byteToRune[b] = r
	}
}

// VocabSize 返回词表大小。
// VocabSize returns the vocabulary size
func (t *Tokenizer) VocabSize() int {
	return len(t.vocab.Values)
}

// BOS 返回句首 token ID。
// BOS returns the beginning of sequence token ID
func (t *Tokenizer) BOS() int32 {
	return t.vocab.BOS
}

// AddBOS 表示编码时是否自动 prepend BOS。
// AddBOS returns whether a BOS token should be prepended during encoding.
func (t *Tokenizer) AddBOS() bool {
	return t.vocab.AddBOS
}

// EOS 返回第一个 EOS ID（兼容旧接口）。
// EOS returns the first end of sequence token ID (for backwards compatibility)
func (t *Tokenizer) EOS() int32 {
	if len(t.vocab.EOS) > 0 {
		return t.vocab.EOS[0]
	}
	return -1
}

// EOSTokens 返回全部 EOS token ID。
// EOSTokens returns all end of sequence token IDs
func (t *Tokenizer) EOSTokens() []int32 {
	return t.vocab.EOS
}

// PAD 返回填充 token ID，未设置则为 -1。
// PAD returns the padding token ID, or -1 if not set
func (t *Tokenizer) PAD() int32 {
	return t.vocab.PAD
}

// IsEOS 判断 id 是否为任一 EOS。
// IsEOS returns true if the token ID is an end of sequence token
func (t *Tokenizer) IsEOS(id int32) bool {
	for _, eos := range t.vocab.EOS {
		if id == eos {
			return true
		}
	}
	return false
}

// GetSpecialToken 按字符串查特殊 token ID。
// GetSpecialToken returns the token ID for a special token string
func (t *Tokenizer) GetSpecialToken(name string) (int32, bool) {
	id, ok := t.specialTokens[name]
	return id, ok
}
