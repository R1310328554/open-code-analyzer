// 文本编码：特殊 token 切分、预分词、并行 chunk 与 BPE merge。
package tokenizer

import (
	"runtime"
	"sort"
	"strings"
	"sync"
	"unicode"
	"unicode/utf8"
)

// 并行编码阈值：输入与每 worker chunk 数。
const (
	encodeParallelMinInputBytes      = 4 * 1024
	encodeParallelMinChunksPerWorker = 8
)

// tokenMatch 预分词正则匹配区间。
type tokenMatch struct {
	start int
	end   int
}

// encodeChunk 编码单元：普通文本或特殊 token。
type encodeChunk struct {
	text      string
	isSpecial bool
}

// isNonNewlineWhitespace 判断 s 是否仅含非换行空白。
// isNonNewlineWhitespace returns true if s contains only whitespace characters (no newlines)
func isNonNewlineWhitespace(s string) bool {
	if s == "" {
		return false
	}
	for _, r := range s {
		if r == '\n' || r == '\r' {
			return false
		}
		if !unicode.IsSpace(r) {
			return false
		}
	}
	return true
}

// splitBySpecialTokens 按最长特殊 token 前缀切分文本。
// splitBySpecialTokens splits text into parts, keeping special tokens as separate elements
func (t *Tokenizer) splitBySpecialTokens(s string) []string {
	if len(t.specialTokens) == 0 {
		return []string{s}
	}

	tokens := t.sortedSpecialTokens
	if len(tokens) == 0 {
		// 非 loader 构造时分词器时的 sorted 列表回退。
		// Fallback for tokenizers constructed outside the loaders.
		tokens = make([]string, 0, len(t.specialTokens))
		for tok := range t.specialTokens {
			tokens = append(tokens, tok)
		}
		sort.Slice(tokens, func(i, j int) bool {
			return len(tokens[i]) > len(tokens[j])
		})
	}

	var result []string
	remaining := s

	for len(remaining) > 0 {
		found := false
		for _, tok := range tokens {
			if strings.HasPrefix(remaining, tok) {
				result = append(result, tok)
				remaining = remaining[len(tok):]
				found = true
				break
			}
		}
		if !found {
			// 找下一处特殊 token 起始位置。
			// Find next special token position
			nextPos := len(remaining)
			for _, tok := range tokens {
				if idx := strings.Index(remaining, tok); idx != -1 && idx < nextPos {
					nextPos = idx
				}
			}
			if nextPos > 0 {
				result = append(result, remaining[:nextPos])
			}
			remaining = remaining[nextPos:]
		}
	}

	return result
}

// adjustWhitespaceBoundary 调整预分词边界：空白与标点间的空格归属。
func adjustWhitespaceBoundary(part string, curr, next *tokenMatch, spaceBeforePunct bool) {
	m := part[curr.start:curr.end]
	nextText := part[next.start:next.end]

	if !isNonNewlineWhitespace(m) || len(nextText) == 0 {
		return
	}

	firstRune, _ := utf8.DecodeRuneInString(nextText)
	shiftASCIIOnly := !unicode.IsLetter(firstRune)
	if shiftASCIIOnly && (!spaceBeforePunct || unicode.IsNumber(firstRune) || unicode.IsSpace(firstRune)) {
		return
	}

	lastSpaceStart := curr.end
	for j := curr.end; j > curr.start; {
		r, size := utf8.DecodeLastRuneInString(part[curr.start:j])
		if unicode.IsSpace(r) {
			if shiftASCIIOnly && r != ' ' {
				return
			}
			lastSpaceStart = j - size
			break
		}
		j -= size
	}
	if lastSpaceStart > curr.start {
		curr.end = lastSpaceStart
		next.start = lastSpaceStart
	} else {
		next.start = curr.start
		curr.end = curr.start
	}
}

// forEachPartChunk 对片段做特殊 token 或预分词 chunk 回调。
func (t *Tokenizer) forEachPartChunk(part string, fn func(encodeChunk)) {
	if _, ok := t.specialTokens[part]; ok {
		fn(encodeChunk{text: part, isSpecial: true})
		return
	}

	if t.pretokenizer == nil {
		fn(encodeChunk{text: part, isSpecial: false})
		return
	}

	re := t.pretokenizer
	offset := 0
	loc := re.FindStringIndex(part[offset:])
	if loc == nil {
		return
	}

	curr := tokenMatch{start: offset + loc[0], end: offset + loc[1]}
	offset += loc[1]

	for {
		loc = re.FindStringIndex(part[offset:])
		if loc == nil {
			if curr.end > curr.start {
				fn(encodeChunk{text: part[curr.start:curr.end], isSpecial: false})
			}
			return
		}

		next := tokenMatch{start: offset + loc[0], end: offset + loc[1]}
		offset += loc[1]

		adjustWhitespaceBoundary(part, &curr, &next, t.pretokenizerSpaceBeforePunctuation)

		if curr.end > curr.start {
			fn(encodeChunk{text: part[curr.start:curr.end], isSpecial: false})
		}
		curr = next
	}
}

// appendEncodedChunk 编码单个 chunk 并追加 ID。
func (t *Tokenizer) appendEncodedChunk(ids []int32, c encodeChunk) []int32 {
	if c.isSpecial {
		if id, ok := t.specialTokens[c.text]; ok {
			return append(ids, id)
		}
		return ids
	}

	return t.encodeChunkInto(c.text, ids)
}

// Encode 将文本转为 token ID；大输入且 chunk 足够多时使用并行。
// Encode tokenizes text to token IDs.
// Parallel encoding is used only for very large inputs with enough chunks per worker.
func (t *Tokenizer) Encode(s string, addBOS bool) []int32 {
	// 先按特殊 token 切分。
	// First: split by special tokens
	parts := t.splitBySpecialTokens(s)

	// 小输入快路径：顺序编码，不物化 chunk 切片。
	// Fast path: encode sequentially without materializing chunk slices.
	if len(s) < encodeParallelMinInputBytes {
		var ids []int32
		for _, part := range parts {
			t.forEachPartChunk(part, func(c encodeChunk) {
				ids = t.appendEncodedChunk(ids, c)
			})
		}

		if addBOS && t.vocab.BOS >= 0 {
			ids = append([]int32{t.vocab.BOS}, ids...)
		}
		return ids
	}

	// 大输入收集全部 chunk 以启用并行。
	// For large inputs collect chunks to enable parallel processing.
	var allChunks []encodeChunk
	for _, part := range parts {
		t.forEachPartChunk(part, func(c encodeChunk) {
			allChunks = append(allChunks, c)
		})
	}

	// 仅当 chunk 数足以摊销 goroutine 开销时才并行。
	// Encode chunks. Use the parallel path only when the chunk count is
	// large enough to amortize goroutine/synchronization overhead.
	useParallel := true
	numWorkers := runtime.GOMAXPROCS(0)
	if numWorkers > len(allChunks) {
		numWorkers = len(allChunks)
	}
	if numWorkers < 2 || len(allChunks) < numWorkers*encodeParallelMinChunksPerWorker {
		useParallel = false
	}

	var ids []int32
	if !useParallel {
		for _, c := range allChunks {
			ids = t.appendEncodedChunk(ids, c)
		}
	} else {
		chunksPer := (len(allChunks) + numWorkers - 1) / numWorkers
		results := make([][]int32, numWorkers)
		var wg sync.WaitGroup

		for i := range numWorkers {
			start := i * chunksPer
			end := start + chunksPer
			if end > len(allChunks) {
				end = len(allChunks)
			}
			if start >= end {
				continue
			}

			wg.Add(1)
			go func(i int, chunks []encodeChunk) {
				defer wg.Done()
				var r []int32
				for _, c := range chunks {
					r = t.appendEncodedChunk(r, c)
				}
				results[i] = r
			}(i, allChunks[start:end])
		}
		wg.Wait()

		for _, r := range results {
			ids = append(ids, r...)
		}
	}

	if addBOS && t.vocab.BOS >= 0 {
		ids = append([]int32{t.vocab.BOS}, ids...)
	}
	return ids
}

// encodeChunkInto 对单 chunk 做 SP/BPE 变换后 BPE merge。
// encodeChunkInto appends encoded tokens to ids and returns the extended slice.
// Uses BPE merge algorithm for both BPE and SentencePiece tokenization.
func (t *Tokenizer) encodeChunkInto(s string, ids []int32) []int32 {
	if s == "" {
		return ids
	}

	// SP：空格→▁；BPE：byteToRune 字节级编码。
	// Apply encoding transformation
	// SentencePiece: replace space with ▁
	// BPE: convert bytes using precomputed table (GPT-2 byte-level encoding)
	var encoded string
	if t.typ == TokenizerSentencePiece {
		encoded = strings.ReplaceAll(s, " ", "▁")
	} else {
		var sb strings.Builder
		sb.Grow(len(s) * 2)
		for i := range len(s) {
			sb.WriteRune(byteToRune[s[i]])
		}
		encoded = sb.String()
	}

	// 整 chunk 命中单一词表项则直接 append。
	// Fast path: check if entire chunk is a single token
	if id, ok := t.vocab.Reverse[encoded]; ok {
		return append(ids, id)
	}

	return t.encodeBPEMerge(encoded, ids)
}
