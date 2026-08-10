package v2

// ordering 定义可比较类型 Orderable 与 UnlessIterator：对两个已排序迭代器做集合差集（A 中不在 B 的元素）。

type Ord byte

const (
	Less Ord = iota
	Eq
	Greater
)

// Orderable[T 类型封装该模块的状态与行为。
type Orderable[T any] interface {
	// Return the caller's position relative to the target
	Compare(T) Ord
}

// OrderedImpl[T 类型封装该模块的状态与行为。
type OrderedImpl[T any] struct {
	val T
	cmp func(T, T) Ord
}

// 比较两个 Orderable 元素的相对顺序。
func (o OrderedImpl[T]) Compare(other OrderedImpl[T]) Ord {
	return o.cmp(o.val, other.val)
}

// Unwrap 实现该路径上的核心处理逻辑。
func (o OrderedImpl[T]) Unwrap() T {
	return o.val
}

// convenience method for creating an Orderable implementation
// for a type dynamically by passing in a value and a comparison function
// This is useful for types that are not under our control, such as built-in types
// and for reducing boilerplate in testware/etc.
// Hot-path code should use a statically defined Orderable implementation for performance
// NewOrderable[T any] 创建组件实例并完成必要初始化。
func NewOrderable[T any](val T, cmp func(T, T) Ord) OrderedImpl[T] {
	return OrderedImpl[T]{val, cmp}
}

// UnlessIterator 对两个已排序流做 A\B：输出 a 中不在 b 的元素。
type UnlessIterator[T Orderable[T]] struct {
	a, b PeekIterator[T]
}

// NewUnlessIterator 要求 a、b 均已排序；非 Peek 迭代器会自动包装 PeekIter。
// Iterators _must_ be sorted. Defers to underlying `PeekingIterator` implementation
// for both iterators if they implement it.
func NewUnlessIterator[T Orderable[T]](a, b Iterator[T]) *UnlessIterator[T] {
	var peekA, peekB PeekIterator[T]
	var ok bool

	if peekA, ok = a.(PeekIterator[T]); !ok {
		peekA = NewPeekIter(a)
	}

	if peekB, ok = b.(PeekIterator[T]); !ok {
		peekB = NewPeekIter(b)
	}

	return &UnlessIterator[T]{
		a: peekA,
		b: peekB,
	}
}

// 推进迭代器；返回是否还有下一元素。
func (it *UnlessIterator[T]) Next() bool {
outer:
	for it.a.Next() {
		a := it.a.At()

		// advance b until it is greater than or equal to a
		for {
			b, ok := it.b.Peek()
			if !ok {
				// b is empty, so a is not in b
				return true
			}

			switch a.Compare(b) {
			case Less:
				// a is not in b
				return true
			case Eq:
				// a is in b, so continue looking through a
				continue outer
			case Greater:
				// keep advancing b until it is greater than or equal to a
				// no need to check b/c peek ensures we have another
				_ = it.b.Next()
				continue

			}

		}
	}
	return false
}

// At 实现该路径上的核心处理逻辑。
func (it *UnlessIterator[T]) At() T {
	return it.a.At()
}

// Err 实现该路径上的核心处理逻辑。
func (it *UnlessIterator[T]) Err() error {
	if err := it.a.Err(); err != nil {
		return err
	}
	return it.b.Err()
}
// UnlessIterator 要求输入已排序；Greater 时推进 b 直至对齐或 b 耗尽。
