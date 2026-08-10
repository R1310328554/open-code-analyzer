package wal

// util/wal 包 NewWalReader 封装 Prometheus tsdb wlog：可按起始 segment 读取 WAL 目录，供 ruler/ingester 重放或调试。

import (
	"errors"
	"io"

	"github.com/prometheus/prometheus/tsdb/wlog"
)

// startSegment 小于 0 时使用 NewSegmentsReader 顺序读取全部 segment 文件。
// If startSegment is <0, it means all the segments.
func NewWalReader(dir string, startSegment int) (*wlog.Reader, io.Closer, error) {
	var (
		segmentReader io.ReadCloser
		err           error
	)
	if startSegment < 0 {
		segmentReader, err = wlog.NewSegmentsReader(dir)
		if err != nil {
			return nil, nil, err
		}
	} else {
		first, last, err := wlog.Segments(dir)
		if err != nil {
			return nil, nil, err
		}
		if startSegment > last {
			return nil, nil, errors.New("start segment is beyond the last WAL segment")
		}
		if first > startSegment {
			startSegment = first
		}
		segmentReader, err = wlog.NewSegmentsRangeReader(wlog.SegmentRange{
			Dir:   dir,
			First: startSegment,
			Last:  -1, // Till the end.
		})
		if err != nil {
			return nil, nil, err
		}
	}
	return wlog.NewReader(segmentReader), segmentReader, nil
}
// 指定 segment 时会 clamp 到目录首个 segment，超出 last 则返回错误不读空 WAL。
