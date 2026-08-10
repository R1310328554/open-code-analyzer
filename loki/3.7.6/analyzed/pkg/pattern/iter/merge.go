package iter

// merge 使用 loser.Tree 按时间戳合并多个 Pattern 迭代器，相同 timestamp/pattern/level 的样本 Value 累加。

import (
	"math"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/util/constants"
	"github.com/grafana/loki/v3/pkg/util/loser"
)

type mergeIterator struct {
	tree        *loser.Tree[patternSample, Iterator]
	current     patternSample
	initialized bool
	done        bool
}

type patternSample struct {
	pattern string
	level   string
	sample  logproto.PatternSample
}

var maxSample = patternSample{
	pattern: "",
	level:   constants.LogLevelUnknown,
	sample:  logproto.PatternSample{Timestamp: math.MaxInt64},
}

// NewMerge 构造按 Timestamp→Pattern 排序的多路归并迭代器，耗尽时关闭全部输入。
func NewMerge(iters ...Iterator) Iterator {
	tree := loser.New(iters, maxSample, func(s Iterator) patternSample {
		return patternSample{
			pattern: s.Pattern(),
			level:   s.Level(),
			sample:  s.At(),
		}
	}, func(e1, e2 patternSample) bool {
		if e1.sample.Timestamp == e2.sample.Timestamp {
			return e1.pattern < e2.pattern
		}
		return e1.sample.Timestamp < e2.sample.Timestamp
	}, func(s Iterator) {
		s.Close()
	})
	return &mergeIterator{
		tree: tree,
	}
}

// Next 在相同时间戳与模式键上累加 Value，直到遇到不同键或树耗尽。
func (m *mergeIterator) Next() bool {
	if m.done {
		return false
	}

	if !m.initialized {
		m.initialized = true
		if !m.tree.Next() {
			m.done = true
			return false
		}
	}

	m.current.pattern = m.tree.Winner().Pattern()
	m.current.level = m.tree.Winner().Level()
	m.current.sample = m.tree.Winner().At()

	for m.tree.Next() {
		if m.current.sample.Timestamp != m.tree.Winner().At().Timestamp ||
			m.current.pattern != m.tree.Winner().Pattern() ||
			m.current.level != m.tree.Winner().Level() {
			return true
		}
		m.current.sample.Value += m.tree.Winner().At().Value
	}

	m.done = true
	return true
}

func (m *mergeIterator) Pattern() string {
	return m.current.pattern
}

func (m *mergeIterator) Level() string {
	return m.current.level
}

func (m *mergeIterator) At() logproto.PatternSample {
	return m.current.sample
}

func (m *mergeIterator) Err() error {
	return nil
}

func (m *mergeIterator) Close() error {
	m.tree.Close()
	return nil
}
// maxSample 哨兵 Timestamp 为 MaxInt64，保证 loser 树空时比较逻辑正确终止。
