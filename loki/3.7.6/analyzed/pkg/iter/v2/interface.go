package v2

// interface 定义 v2 迭代器接口族：Next/At/Err 基础协议，以及 Peek、Seek、Close、Count、Reset 等扩展能力。

// Iterator 是 v2 迭代器基础接口：Next 推进、At 取当前值、Err 报告错误。
// Iterator is the basic iterator type with the common functions for advancing
// and retrieving the current value.
//
// General usage of the iterator:
//
//	for it.Next() {
//	    curr := it.At()
//	    // do something
//	}
//	if it.Err() != nil {
//	    // do something
//	}
// Iterator[T 类型封装该模块的状态与行为。
type Iterator[T any] interface {
	Next() bool
	Err() error
	At() T
}

// Iterators with one single added functionality.

// SizedIterator 在基础迭代器上增加 Remaining，返回尚未消费的元素个数。
type SizedIterator[T any] interface {
	Iterator[T]
	Remaining() int // remaining
}

// PeekIterator[T 类型封装该模块的状态与行为。
type PeekIterator[T any] interface {
	Iterator[T]
	Peek() (T, bool)
}

// SeekIterator[K, 类型封装该模块的状态与行为。
type SeekIterator[K, V any] interface {
	Iterator[V]
	Seek(K) error
}

// CloseIterator[T 类型封装该模块的状态与行为。
type CloseIterator[T any] interface {
	Iterator[T]
	Close() error
}

// CountIterator[T 类型封装该模块的状态与行为。
type CountIterator[T any] interface {
	Iterator[T]
	Count() int
}

// ResetIterator[T 类型封装该模块的状态与行为。
type ResetIterator[T any] interface {
	Reset() error
	Iterator[T]
}

// Iterators which are an intersection type of two or more iterators with a
// single added functionality.

// PeekCloseIterator[T 类型封装该模块的状态与行为。
type PeekCloseIterator[T any] interface {
	PeekIterator[T]
	CloseIterator[T]
}

// CloseResetIterator[T 类型封装该模块的状态与行为。
type CloseResetIterator[T any] interface {
	CloseIterator[T]
	ResetIterator[T]
}
// 组合接口（如 PeekCloseIterator）通过嵌入多个能力减少调用方类型断言。
