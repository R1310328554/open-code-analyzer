// GGUF 惰性读取：按计数延迟解析张量或键值列表。
package gguf

import (
	"encoding/binary"
	"fmt"
	"iter"
)

// lazy 在首次迭代前只读取元素个数，按需拉取后续项。
type lazy[T any] struct {
	count  uint64
	next   func() (T, bool)
	stop   func()
	values []T
	err    error

	// successFunc 全部元素读完后可选回调（如校验尾部对齐）。
	// successFunc is called when all values have been successfully read.
	successFunc func() error
}

// newLazy 读取 GGUF 项计数并构造 iter.Pull 惰性迭代器。
func newLazy[T any](f *File, fn func() (T, error)) (*lazy[T], error) {
	it := lazy[T]{}
	if err := binary.Read(f.reader, binary.LittleEndian, &it.count); err != nil {
		return nil, err
	}
	if it.count > uint64(maxInt()) {
		return nil, fmt.Errorf("GGUF item count %d exceeds maximum %d", it.count, maxInt())
	}

	it.values = make([]T, 0)
	it.next, it.stop = iter.Pull(func(yield func(T) bool) {
		for i := range it.count {
			t, err := fn()
			if err != nil {
				it.err = fmt.Errorf("error reading GGUF item %d: %w", i, err)
				return
			}

			it.values = append(it.values, t)
			if !yield(t) {
				break
			}
		}

		if it.successFunc != nil {
			if err := it.successFunc(); err != nil {
				it.err = err
				return
			}
		}
	})

	return &it, nil
}

// Values 返回仅值的迭代序列（忽略索引）。
func (g *lazy[T]) Values() iter.Seq[T] {
	return func(yield func(T) bool) {
		for _, v := range g.All() {
			if !yield(v) {
				break
			}
		}
	}
}

// All 返回带索引的惰性序列，已缓存项直接复用。
func (g *lazy[T]) All() iter.Seq2[int, T] {
	return func(yield func(int, T) bool) {
		for i := range g.count {
			n := int(i)
			if n < len(g.values) {
				if !yield(n, g.values[n]) {
					break
				}
			} else {
				t, ok := g.next()
				if !ok {
					break
				}

				if !yield(n, t) {
					break
				}
			}
		}
	}
}

// rest 排空剩余迭代项（用于提前关闭时收集错误）。
func (g *lazy[T]) rest() (collected bool) {
	for {
		_, ok := g.next()
		collected = collected || ok
		if !ok {
			break
		}
	}

	return collected
}

// Err 返回读取过程中遇到的第一个错误。
func (g *lazy[T]) Err() error {
	return g.err
}
