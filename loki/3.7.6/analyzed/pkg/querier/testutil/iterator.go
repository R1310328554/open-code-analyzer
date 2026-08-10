package testutil

// testutil 提供构造假 logproto.Stream 与 EntryIterator 的测试辅助，时间戳与 line 为从 from 起的连续整数序列。

import (
	"fmt"
	"time"

	"github.com/grafana/loki/v3/pkg/iter"
	"github.com/grafana/loki/v3/pkg/logproto"
)

// NewFakeStreamIterator 返回单 stream、quantity 条顺序 entry 的 EntryIterator。
// mockStreamIterator returns an iterator with 1 stream and quantity entries,
// where entries timestamp and line string are constructed as sequential numbers
// starting at from
func NewFakeStreamIterator(from int, quantity int) iter.EntryIterator {
	return iter.NewStreamIterator(NewFakeStream(from, quantity))
}

// NewFakeStream 默认标签 {type="test"}，NewFakeStreamWithLabels 可自定义 labels。
// mockStream return a stream with quantity entries, where entries timestamp and
// line string are constructed as sequential numbers starting at from
func NewFakeStream(from int, quantity int) logproto.Stream {
	return NewFakeStreamWithLabels(from, quantity, `{type="test"}`)
}

func NewFakeStreamWithLabels(from int, quantity int, labels string) logproto.Stream {
	entries := make([]logproto.Entry, 0, quantity)

	for i := from; i < from+quantity; i++ {
		entries = append(entries, logproto.Entry{
			Timestamp: time.Unix(int64(i), 0),
			Line:      fmt.Sprintf("line %d", i),
		})
	}

	return logproto.Stream{
		Entries: entries,
		Labels:  labels,
	}
}
// 供 querier/store 单测构造可预测的日志流，无需真实 chunk 数据。
