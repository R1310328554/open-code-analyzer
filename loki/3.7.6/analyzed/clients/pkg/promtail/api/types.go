package api

// Promtail 日志条目 API 抽象：Entry、EntryHandler 通道接口与 EntryMiddleware 装饰链。
// 贯穿 scrape → pipeline → client 的数据流，支持标签合并与条目变换。

import (
	"sync"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// 带 Prometheus 标签集的日志条目，内嵌 logproto.Entry 行与时间戳。
// Entry is a log entry with labels.
type Entry struct {
	Labels model.LabelSet
	logproto.Entry
}

type InstrumentedEntryHandler interface {
	EntryHandler
	UnregisterLatencyMetric(prometheus.Labels)
}

// 通过 channel 接收 Entry 的处理器；Stop 需调用以优雅关闭 goroutine。
// EntryHandler is something that can "handle" entries via a channel.
// Stop must be called to gracefully shutdown the EntryHandler
type EntryHandler interface {
	Chan() chan<- Entry
	Stop()
}

// 中间件包装 EntryHandler，新实例需独立 Stop，典型用于限流或标签注入。
// EntryMiddleware takes an EntryHandler and returns another one that will intercept and forward entries.
// The newly created EntryHandler should be Stopped independently from the original one.
type EntryMiddleware interface {
	Wrap(EntryHandler) EntryHandler
}

// 函数式 EntryMiddleware，Wrap 直接调用包装函数。
// EntryMiddlewareFunc allows to create EntryMiddleware via a function.
type EntryMiddlewareFunc func(EntryHandler) EntryHandler

func (e EntryMiddlewareFunc) Wrap(next EntryHandler) EntryHandler {
	return e(next)
}

// 单条 Entry 变换函数，用于 NewEntryMutatorHandler 链式修改。
// EntryMutatorFunc is a function that can mutate an entry
type EntryMutatorFunc func(Entry) Entry

type entryHandler struct {
	stop    func()
	entries chan<- Entry
}

func (e entryHandler) Chan() chan<- Entry {
	return e.entries
}

func (e entryHandler) Stop() {
	e.stop()
}

// 用已有 channel 与 stop 回调构造 EntryHandler 适配器。
// NewEntryHandler creates a new EntryHandler using a input channel and a stop function.
func NewEntryHandler(entries chan<- Entry, stop func()) EntryHandler {
	return entryHandler{
		stop:    stop,
		entries: entries,
	}
}

// 启动 goroutine 对下游 Entry 应用 f 后再转发，Stop 时关闭输入并等待。
// NewEntryMutatorHandler creates a EntryHandler that mutates incoming entries from another EntryHandler.
func NewEntryMutatorHandler(next EntryHandler, f EntryMutatorFunc) EntryHandler {
	in, wg, once := make(chan Entry), sync.WaitGroup{}, sync.Once{}
	nextChan := next.Chan()
	wg.Add(1)
	go func() {
		defer wg.Done()
		for e := range in {
			nextChan <- f(e)
		}
	}()
	return NewEntryHandler(in, func() {
		once.Do(func() { close(in) })
		wg.Wait()
	})
}

// 为每条 Entry 合并 additionalLabels 的中间件工厂。
// AddLabelsMiddleware is an EntryMiddleware that adds some labels.
func AddLabelsMiddleware(additionalLabels model.LabelSet) EntryMiddleware {
	return EntryMiddlewareFunc(func(eh EntryHandler) EntryHandler {
		return NewEntryMutatorHandler(eh, func(e Entry) Entry {
			e.Labels = additionalLabels.Merge(e.Labels)
			return e
		})
	})
}
