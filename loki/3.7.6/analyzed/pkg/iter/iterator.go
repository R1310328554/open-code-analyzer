package iter

// iterator 定义泛型 StreamIterator 接口与空操作/错误占位迭代器，EntryIterator 与 SampleIterator 均基于此抽象。

import (
	"errors"

	v2 "github.com/grafana/loki/v3/pkg/iter/v2"
	"github.com/grafana/loki/v3/pkg/logproto"
)

type logprotoType interface {
	logproto.Entry | logproto.Sample
}

type StreamIterator[T logprotoType] interface {
	v2.CloseIterator[T]
	// Labels returns the labels for the current entry.
	// The labels can be mutated by the query engine and not reflect the original stream.
	Labels() string
	// StreamHash returns the hash of the original stream for the current entry.
	StreamHash() uint64
}

type EntryIterator StreamIterator[logproto.Entry]
type SampleIterator StreamIterator[logproto.Sample]

// noOpIterator implements StreamIterator
// noOpIterator 立即结束的空迭代器，用于零结果短路路径。
type noOpIterator[T logprotoType] struct{}

func (noOpIterator[T]) Next() bool         { return false }
func (noOpIterator[T]) Err() error         { return nil }
func (noOpIterator[T]) At() (zero T)       { return zero }
func (noOpIterator[T]) Labels() string     { return "" }
func (noOpIterator[T]) StreamHash() uint64 { return 0 }
func (noOpIterator[T]) Close() error       { return nil }

// NoopEntryIterator 与 NoopSampleIterator 为包级单例空迭代器。
var NoopEntryIterator = noOpIterator[logproto.Entry]{}
var NoopSampleIterator = noOpIterator[logproto.Sample]{}

// errorIterator implements StreamIterator
// errorIterator 测试用占位：Next 返回 false 且 Err/Close 返回固定错误。
type errorIterator[T logprotoType] struct{}

func (errorIterator[T]) Next() bool         { return false }
func (errorIterator[T]) Err() error         { return errors.New("error") }
func (errorIterator[T]) At() (zero T)       { return zero }
func (errorIterator[T]) Labels() string     { return "" }
func (errorIterator[T]) StreamHash() uint64 { return 0 }
func (errorIterator[T]) Close() error       { return errors.New("close") }

var ErrorEntryIterator = errorIterator[logproto.Entry]{}
var ErrorSampleIterator = errorIterator[logproto.Sample]{}
// Labels 可能被查询引擎变异，StreamHash 始终指向原始流以便 dedupe 与路由。
