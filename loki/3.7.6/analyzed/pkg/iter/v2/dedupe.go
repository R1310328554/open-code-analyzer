package v2

// v2/dedupe 提供泛型去重迭代器 DedupeIter：连续相等元素通过 merge 合并为单个 B 类型输出。

// DedupeIter 将 Iterator[A] 转为 Iterator[B]，eq 判定相邻元素是否重复。
// DedupeIter is a deduplicating iterator which creates an Iterator[B]
// from a sequence of Iterator[A].
type DedupeIter[A, B any] struct {
	eq    func(A, B) bool // equality check
	from  func(A) B       // convert A to B, used on first element
	merge func(A, B) B    // merge A into B
	itr   PeekIterator[A]

	tmp B
}

// general helper, in this case created for DedupeIter[T,T]
// Identity 泛型恒等函数，用于 A 与 B 类型相同时的 from/merge 参数。
func Identity[A any](a A) A { return a }

func NewDedupingIter[A, B any](
	eq func(A, B) bool,
	from func(A) B,
	merge func(A, B) B,
	itr PeekIterator[A],
) *DedupeIter[A, B] {
	return &DedupeIter[A, B]{
		eq:    eq,
		from:  from,
		merge: merge,
		itr:   itr,
	}
}

// Next 取当前元素为 tmp，Peek 合并所有连续 eq 匹配项后再输出。
func (it *DedupeIter[A, B]) Next() bool {
	if !it.itr.Next() {
		return false
	}
	it.tmp = it.from(it.itr.At())
	for {
		next, ok := it.itr.Peek()
		if !ok || !it.eq(next, it.tmp) {
			break
		}

		it.itr.Next() // ensured via peek
		it.tmp = it.merge(next, it.tmp)
	}
	return true
}

func (it *DedupeIter[A, B]) Err() error {
	return it.itr.Err()
}

func (it *DedupeIter[A, B]) At() B {
	return it.tmp
}

// Collect 将 Iterator 全部元素收集到新切片并返回首个 Err。
// Collect collects an interator into a slice. It uses
// CollectInto with a new slice
func Collect[T any](itr Iterator[T]) ([]T, error) {
	return CollectInto(itr, nil)
}

// CollectInto 复用已有切片 backing array，截断后 append 全部 At() 结果。
// CollectInto collects the elements of an iterator into a provided slice
// which is returned
func CollectInto[T any](itr Iterator[T], into []T) ([]T, error) {
	into = into[:0]

	for itr.Next() {
		into = append(into, itr.At())
	}
	return into, itr.Err()
}
// DedupeIter 依赖 PeekIterator 向前看能力，适合时间序列或标签流上的相邻重复折叠。
