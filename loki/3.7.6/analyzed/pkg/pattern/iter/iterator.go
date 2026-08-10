package iter

// iter 包定义 PatternSample 迭代器抽象：扩展 CloseIterator，并提供切片、非重叠链式组合等实现。

import (
	iter "github.com/grafana/loki/v3/pkg/iter/v2"
	"github.com/grafana/loki/v3/pkg/logproto"
)

type Iterator interface {
	iter.CloseIterator[logproto.PatternSample]

	Pattern() string
	Level() string
}

// NewSlice 用内存切片构造单模式迭代器，Close 时无额外清理。
func NewSlice(pattern, lvl string, s []logproto.PatternSample) *PatternIter {
	return &PatternIter{
		CloseIterator: iter.WithClose(iter.NewSliceIter(s), nil),
		pattern:       pattern,
		level:         lvl,
	}
}

// NewEmpty 返回永不产生样本的空迭代器，用于占位或边界情况。
func NewEmpty(pattern string) *PatternIter {
	return &PatternIter{
		CloseIterator: iter.WithClose(iter.NewEmptyIter[logproto.PatternSample](), nil),
		pattern:       pattern,
	}
}

type PatternIter struct {
	iter.CloseIterator[logproto.PatternSample]
	pattern string
	level   string
}

func (s *PatternIter) Pattern() string {
	return s.pattern
}

func (s *PatternIter) Level() string {
	return s.level
}

// nonOverlappingIterator 维护待消费迭代器队列，切换时关闭已耗尽子迭代器。
type nonOverlappingIterator struct {
	iterators []Iterator
	curr      Iterator
	pattern   string
	level     string
}

// NewNonOverlappingIterator 顺序耗尽多个子迭代器，时间区间互不重叠时保证有序输出。
// NewNonOverlappingIterator gives a chained iterator over a list of iterators.
func NewNonOverlappingIterator(pattern, lvl string, iterators []Iterator) Iterator {
	return &nonOverlappingIterator{
		iterators: iterators,
		pattern:   pattern,
		level:     lvl,
	}
}

func (i *nonOverlappingIterator) Next() bool {
	for i.curr == nil || !i.curr.Next() {
		if len(i.iterators) == 0 {
			if i.curr != nil {
				i.curr.Close()
			}
			return false
		}
		if i.curr != nil {
			i.curr.Close()
		}
		i.curr, i.iterators = i.iterators[0], i.iterators[1:]
	}

	return true
}

func (i *nonOverlappingIterator) At() logproto.PatternSample {
	return i.curr.At()
}

func (i *nonOverlappingIterator) Pattern() string {
	return i.pattern
}

func (i *nonOverlappingIterator) Level() string {
	return i.level
}

func (i *nonOverlappingIterator) Err() error {
	if i.curr != nil {
		return i.curr.Err()
	}
	return nil
}

func (i *nonOverlappingIterator) Close() error {
	if i.curr != nil {
		i.curr.Close()
	}
	for _, iter := range i.iterators {
		iter.Close()
	}
	i.iterators = nil
	return nil
}
// Close 会关闭当前迭代器及队列中剩余子迭代器，防止 gRPC 流资源泄漏。
