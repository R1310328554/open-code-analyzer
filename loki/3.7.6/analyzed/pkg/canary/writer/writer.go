package writer

// Canary 日志生成器：按固定间隔产生带纳秒时间戳的测试日志，
// 可配置乱序比例与行大小，并通过 EntryWriter 接口输出。

import (
	"fmt"
	"math/rand"
	"strconv"
	"strings"
	"time"

	"github.com/go-kit/log"
)

const (
	LogEntry = "%s %s\n"
)

type EntryWriter interface {
// WriteEntry 须非阻塞，以保证定时器驱动的写入节奏稳定。
	// WriteEntry handles sending the log to the output
	// To maintain consistent log timing, Write is expected to be non-blocking
	WriteEntry(ts time.Time, entry string)
	Stop()
}

// Writer 定时生成日志并通过 sent 通道通知 Comparator。
type Writer struct {
	w                    EntryWriter
	sent                 chan time.Time
	interval             time.Duration
	outOfOrderPercentage int
	outOfOrderMin        time.Duration
	outOfOrderMax        time.Duration
	size                 int
	prevTsLen            int
	pad                  string
	quit                 chan struct{}
	done                 chan struct{}

	logger log.Logger
}

func NewWriter(
	writer EntryWriter,
	sentChan chan time.Time,
	entryInterval, outOfOrderMin, outOfOrderMax time.Duration,
	outOfOrderPercentage, entrySize int,
	logger log.Logger,
) *Writer {
	w := &Writer{
		w:                    writer,
		sent:                 sentChan,
		interval:             entryInterval,
		outOfOrderPercentage: outOfOrderPercentage,
		outOfOrderMin:        outOfOrderMin,
		outOfOrderMax:        outOfOrderMax,
		size:                 entrySize,
		prevTsLen:            0,
		quit:                 make(chan struct{}),
		done:                 make(chan struct{}),
		logger:               logger,
	}

	go w.run()

	return w
}

func (w *Writer) Stop() {
	if w.quit != nil {
		close(w.quit)
		<-w.done
		w.quit = nil
	}
}

// run 按 interval 触发写入，随机注入乱序时间戳并填充固定长度。
func (w *Writer) run() {
	t := time.NewTicker(w.interval)
	defer func() {
		t.Stop()
		close(w.done)
	}()
	for {
		select {
		case <-t.C:
			t := time.Now()
			if i := rand.Intn(100); i < w.outOfOrderPercentage { //#nosec G404 -- Random sampling for testing purposes, does not require secure random. -- nosemgrep: math-random-used
				n := rand.Intn(int(w.outOfOrderMax.Seconds()-w.outOfOrderMin.Seconds())) + int(w.outOfOrderMin.Seconds()) //#nosec G404 -- Random sampling for testing purposes, does not require secure random. -- nosemgrep: math-random-used
				t = t.Add(-time.Duration(n) * time.Second)
			}
			ts := strconv.FormatInt(t.UnixNano(), 10)
			tsLen := len(ts)

			// I guess some day this could happen????
			if w.prevTsLen != tsLen {
				var str strings.Builder
				// Total line length includes timestamp, white space separator, new line char.  Subtract those out
				for str.Len() < w.size-tsLen-2 {
					str.WriteString("p")
				}
				w.pad = str.String()
				w.prevTsLen = tsLen
			}

			w.w.WriteEntry(t, fmt.Sprintf(LogEntry, ts, w.pad))
			w.sent <- t
		case <-w.quit:
			return
		}
	}
}
