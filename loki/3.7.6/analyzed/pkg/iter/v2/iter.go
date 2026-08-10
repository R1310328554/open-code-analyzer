package v2

// iter 提供常用迭代器适配器：PeekIter、SliceIter、MapIter、FilterIter、CancellableIter 等组合与装饰实现。

import (
	"context"
	"io"
)

// PeekIter[T 类型封装该模块的状态与行为。
type PeekIter[T any] struct {
	itr Iterator[T]

	// the first call to Next() will populate cur & next
	init      bool
	zero      T // zero value of T for returning empty Peek's
	cur, next *T
}

// NewPeekIter 将任意 Iterator 包装为支持 Peek 的实现。
func NewPeekIter[T any](itr Iterator[T]) *PeekIter[T] {
	return &PeekIter[T]{itr: itr}
}

// populates the first element so Peek can be used and subsequent Next()
// calls will work as expected
// ensureInit 实现该路径上的核心处理逻辑。
func (it *PeekIter[T]) ensureInit() {
	if it.init {
		return
	}
	if it.itr.Next() {
		at := it.itr.At()
		it.next = &at
	}
	it.init = true
}

// load the next element and return the cached one
// cacheNext 实现该路径上的核心处理逻辑。
func (it *PeekIter[T]) cacheNext() {
	it.cur = it.next
	if it.cur != nil && it.itr.Next() {
		at := it.itr.At()
		it.next = &at
	} else {
		it.next = nil
	}
}

// 推进迭代器；返回是否还有下一元素。
func (it *PeekIter[T]) Next() bool {
	it.ensureInit()
	it.cacheNext()
	return it.cur != nil
}

// 预览下一个元素而不消费当前项。
func (it *PeekIter[T]) Peek() (T, bool) {
	it.ensureInit()
	if it.next == nil {
		return it.zero, false
	}
	return *it.next, true
}

// Err 实现该路径上的核心处理逻辑。
func (it *PeekIter[T]) Err() error {
	return it.itr.Err()
}

// At 实现该路径上的核心处理逻辑。
func (it *PeekIter[T]) At() T {
	return *it.cur
}

// SliceIter 将内存切片适配为 SizedIterator，cur 从 -1 起递增。
type SliceIter[T any] struct {
	cur int
	xs  []T
}

// NewSliceIter[T any] 创建组件实例并完成必要初始化。
func NewSliceIter[T any](xs []T) *SliceIter[T] {
	return &SliceIter[T]{xs: xs, cur: -1}
}

// 返回尚未遍历的元素数量。
func (it *SliceIter[T]) Remaining() int {
	return max(0, len(it.xs)-(it.cur+1))
}

// 推进迭代器；返回是否还有下一元素。
func (it *SliceIter[T]) Next() bool {
	it.cur++
	return it.cur < len(it.xs)
}

// Err 实现该路径上的核心处理逻辑。
func (it *SliceIter[T]) Err() error {
	return nil
}

// At 实现该路径上的核心处理逻辑。
func (it *SliceIter[T]) At() T {
	return it.xs[it.cur]
}

// MapIter[A 类型封装该模块的状态与行为。
type MapIter[A any, B any] struct {
	Iterator[A]
	f func(A) B
}

// NewMapIter[A any, B any] 创建组件实例并完成必要初始化。
func NewMapIter[A any, B any](src Iterator[A], f func(A) B) *MapIter[A, B] {
	return &MapIter[A, B]{Iterator: src, f: f}
}

// At 实现该路径上的核心处理逻辑。
func (it *MapIter[A, B]) At() B {
	return it.f(it.Iterator.At())
}

// EmptyIter[T 类型封装该模块的状态与行为。
type EmptyIter[T any] struct {
	zero T
}

// 推进迭代器；返回是否还有下一元素。
func (it *EmptyIter[T]) Next() bool {
	return false
}

// Err 实现该路径上的核心处理逻辑。
func (it *EmptyIter[T]) Err() error {
	return nil
}

// At 实现该路径上的核心处理逻辑。
func (it *EmptyIter[T]) At() T {
	return it.zero
}

// 预览下一个元素而不消费当前项。
func (it *EmptyIter[T]) Peek() (T, bool) {
	return it.zero, false
}

// 返回尚未遍历的元素数量。
func (it *EmptyIter[T]) Remaining() int {
	return 0
}

// noop
// Reset 实现该路径上的核心处理逻辑。
func (it *EmptyIter[T]) Reset() {}

func NewEmptyIter[T any]() *EmptyIter[T] {
	return &EmptyIter[T]{}
}

// CancellableIter 在 ctx 取消时 Next 立即返回 false 并优先报告 ctx.Err。
type CancellableIter[T any] struct {
	ctx context.Context
	Iterator[T]
}

// 推进迭代器；返回是否还有下一元素。
func (cii *CancellableIter[T]) Next() bool {
	select {
	case <-cii.ctx.Done():
		return false
	default:
		return cii.Iterator.Next()
	}
}

// Err 实现该路径上的核心处理逻辑。
func (cii *CancellableIter[T]) Err() error {
	if err := cii.ctx.Err(); err != nil {
		return err
	}
	return cii.Iterator.Err()
}

// NewCancelableIter[T any] 创建组件实例并完成必要初始化。
func NewCancelableIter[T any](ctx context.Context, itr Iterator[T]) *CancellableIter[T] {
	return &CancellableIter[T]{ctx: ctx, Iterator: itr}
}

// NewCloserIter[T io.Closer] 创建组件实例并完成必要初始化。
func NewCloserIter[T io.Closer](itr Iterator[T]) *CloserIter[T] {
	return &CloserIter[T]{itr}
}

// CloserIter[T 类型封装该模块的状态与行为。
type CloserIter[T io.Closer] struct {
	Iterator[T]
}

// Close 实现该路径上的核心处理逻辑。
func (i *CloserIter[T]) Close() error {
	return i.At().Close()
}

// PeekCloseIter[T 类型封装该模块的状态与行为。
type PeekCloseIter[T any] struct {
	*PeekIter[T]
	close func() error
}

// NewPeekCloseIter[T any] 创建组件实例并完成必要初始化。
func NewPeekCloseIter[T any](itr CloseIterator[T]) *PeekCloseIter[T] {
	return &PeekCloseIter[T]{PeekIter: NewPeekIter[T](itr), close: itr.Close}
}

// Close 实现该路径上的核心处理逻辑。
func (it *PeekCloseIter[T]) Close() error {
	return it.close()
}

// Predicate[T 类型封装该模块的状态与行为。
type Predicate[T any] func(T) bool

func NewFilterIter[T any](it Iterator[T], p Predicate[T]) *FilterIter[T] {
	return &FilterIter[T]{
		Iterator: it,
		match:    p,
	}
}

// FilterIter 跳过不满足谓词 match 的元素，只产出匹配项。
type FilterIter[T any] struct {
	Iterator[T]
	match Predicate[T]
}

// 推进迭代器；返回是否还有下一元素。
func (i *FilterIter[T]) Next() bool {
	hasNext := i.Iterator.Next()
	for hasNext && !i.match(i.At()) {
		hasNext = i.Iterator.Next()
	}
	return hasNext
}

// CounterIter[T 类型封装该模块的状态与行为。
type CounterIter[T any] struct {
	Iterator[T] // the underlying iterator
	count       int
}

// NewCounterIter[T any] 创建组件实例并完成必要初始化。
func NewCounterIter[T any](itr Iterator[T]) *CounterIter[T] {
	return &CounterIter[T]{Iterator: itr}
}

// 推进迭代器；返回是否还有下一元素。
func (it *CounterIter[T]) Next() bool {
	if it.Iterator.Next() {
		it.count++
		return true
	}
	return false
}

// Count 实现该路径上的核心处理逻辑。
func (it *CounterIter[T]) Count() int {
	return it.count
}

// WithClose[T any] 实现该路径上的核心处理逻辑。
func WithClose[T any](itr Iterator[T], closeFunc func() bool) *CloseIter[T] {
	return &CloseIter[T]{
		Iterator: itr,
		close:    closeFunc,
	}
}

// CloseIter[T 类型封装该模块的状态与行为。
type CloseIter[T any] struct {
	Iterator[T]
	close func() bool
}

// Close 实现该路径上的核心处理逻辑。
func (i *CloseIter[T]) Close() error {
	if i.close != nil {
		return i.Close()
	}
	return nil
}
// PeekIter 通过双缓冲 cur/next 实现 O(1) Peek 而不破坏底层迭代状态。
