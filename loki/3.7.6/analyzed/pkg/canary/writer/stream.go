package writer

// Canary 流式写入器：将日志条目直接写入 io.Writer（如 stdout），
// 用于本地调试或不依赖 Loki 推送的测试场景。

import (
	"fmt"
	"io"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
)

type StreamWriter struct {
	w      io.Writer
	logger log.Logger
}

func NewStreamWriter(w io.Writer, logger log.Logger) *StreamWriter {
	return &StreamWriter{
		w:      w,
		logger: logger,
	}
}

// WriteEntry 同步写入日志行，失败时记录错误日志。
func (s *StreamWriter) WriteEntry(ts time.Time, entry string) {
	_, err := fmt.Fprint(s.w, entry)
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to write log entry", "entry", ts, "error", err)
	}
}

func (s *StreamWriter) Stop() {}
