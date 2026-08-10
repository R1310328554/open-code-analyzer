package utils //nolint:revive

// Promtail 日志条目分发工具：FanoutEntryHandler 将单路 api.Entry 并发
// 扇出到多个下游 EntryHandler，避免单个阻塞通道拖慢整体 pipeline。

import (
	"context"
	"sync"
	"time"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
)

// 维护 entries channel 与多个 handler，Stop 时支持优雅超时或硬取消。
// FanoutEntryHandler implements api.EntryHandler, fanning out received entries to one or multiple channels.
type FanoutEntryHandler struct {
	entries  chan api.Entry
	handlers []api.EntryHandler

	// using a single channel instead of a 1-valued wait group, to be able to wait on it
	mainRoutineDone   chan struct{}
	once              sync.Once
	sendOnStopTimeout time.Duration
	hardStop          context.CancelFunc
}

// 创建带 cancel context 的 handler 并立即启动后台 fanout goroutine。
// NewFanoutEntryHandler creates a new FanoutEntryHandler.
func NewFanoutEntryHandler(sendTimeoutOnStop time.Duration, handlers ...api.EntryHandler) *FanoutEntryHandler {
	ctx, cancel := context.WithCancel(context.Background())
	eh := &FanoutEntryHandler{
		entries:           make(chan api.Entry),
		handlers:          handlers,
		mainRoutineDone:   make(chan struct{}),
		sendOnStopTimeout: sendTimeoutOnStop,
		hardStop:          cancel,
	}
	eh.start(ctx)
	return eh
}

// 主循环从 entries 读取，为每个 handler 启动短生命周期 goroutine 并发发送。
func (eh *FanoutEntryHandler) start(ctx context.Context) {
	go func() {
		defer func() {
			close(eh.mainRoutineDone)
		}()

		for e := range eh.entries {
			// To prevent a single channel from blocking all others, we run each channel send in a separate go routine.
			// This cause each entry to be sent in |eh.handlers| routines concurrently. When all finish, we know the entry
			// has been fanned out properly, and we can read the next received entry.
			var entryWG sync.WaitGroup
			entryWG.Add(len(eh.handlers))
			for _, handler := range eh.handlers {
				go func(ctx context.Context, eh api.EntryHandler) {
					defer entryWG.Done()
					// just doing a single select block here, since these routines are short-lived
					select {
					case <-ctx.Done():
					case eh.Chan() <- e:
					}
				}(ctx, handler)
			}
			entryWG.Wait()
		}
	}()
}

func (eh *FanoutEntryHandler) Chan() chan<- api.Entry {
	return eh.entries
}

// 关闭 entries 后等待当前条目发送完成；超时则 cancel 所有发送 goroutine。
// Stop only stops the channel FanoutEntryHandler exposes. It then waits for the entry being processed to be sent successfully.
// If it times out, it hard stops all sending routines.
func (eh *FanoutEntryHandler) Stop() {
	eh.once.Do(func() {
		close(eh.entries)
	})
	// after closing channel, if the main go-routine was sending entries, wait for them to be sent, or a timeout fires
	select {
	case <-eh.mainRoutineDone:
		// graceful stop
		return
	case <-time.After(eh.sendOnStopTimeout):
	}
	// sending pending entries timed out, hard stop
	eh.hardStop()
	<-eh.mainRoutineDone
}
