package log

// vector 定义列式日志批次结构：时间戳与行内容分向量存储，便于批量迭代与向量化处理。

type VectorInt []int64

type VectorString struct {
	Offsets VectorInt
	Lines   []byte
}

// TODO: we might want an interface to support different types of batches. https://github.com/jeschkies/loki/blob/065a34a1afb765e45d15430c143ac522d0308646/pkg/logql/vectorized.go#L54
// Batch 将时间戳列、行内容列与可选 Selection 索引组合为一批日志条目。
type Batch struct {
	Timestamps VectorInt
	Entries    VectorString
	Selection  []int
	// TODO: Add selection
}

// Get 按索引返回时间戳与行切片；索引越界时返回 false。
// Returns the timestamp and line for index i or false
func (b *Batch) Get(i int) (int64, []byte, bool) {
	if i < 0 || i >= len(b.Timestamps) {
		return 0, nil, false
	}

	prevOffset := 0
	if i > 0 {
		prevOffset = int(b.Entries.Offsets[i-1])
	}
	return b.Timestamps[i], b.Entries.Lines[prevOffset:b.Entries.Offsets[i]], true
}

// Iter 按时间顺序遍历批次，yield 返回 false 时提前终止。
func (b *Batch) Iter(yield func(int64, []byte) bool) {
	prevOffset := 0
	for i, ts := range b.Timestamps {
		if i > 0 {
			prevOffset = int(b.Entries.Offsets[i-1])
		}
		line := b.Entries.Lines[prevOffset:b.Entries.Offsets[i]]
		if !yield(ts, line) {
			return
		}
	}
}

// Append 追加一条日志：更新 Offsets 并追加行字节到 Lines 缓冲区。
func (b *Batch) Append(ts int64, line []byte) {
	b.Timestamps = append(b.Timestamps, ts)
	b.Entries.Offsets = append(b.Entries.Offsets, int64(len(b.Entries.Lines)))
	b.Entries.Lines = append(b.Entries.Lines, line...)
}
// Offsets[i] 为第 i 条行在 Lines 中的结束位置，前一条结束位置即当前行起始偏移。
