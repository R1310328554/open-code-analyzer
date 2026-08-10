package client

// Promtail 推送批次：按标签聚合 logproto.Stream，控制字节上限与 stream 数量。
// 多租户模式下每租户独立 batch，编码为 snappy 压缩的 PushRequest。

import (
	"fmt"
	"slices"
	"strconv"
	"strings"
	"time"

	"github.com/gogo/protobuf/proto"
	"github.com/golang/snappy"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"

	"github.com/grafana/loki/v3/pkg/logproto"
)

const (
	errMaxStreamsLimitExceeded = "streams limit exceeded, streams: %d exceeds limit: %d, stream: '%s'"
)

// 待发送 Loki 的日志流缓存，合并多条 stream 以减少 HTTP 推送次数。
// batch holds pending log streams waiting to be sent to Loki, and it's used
// to reduce the number of push requests to Loki aggregating multiple log streams
// and entries in a single batch request. In case of multi-tenant Promtail, log
// streams for each tenant are stored in a dedicated batch.
type batch struct {
	streams   map[string]*logproto.Stream
	bytes     int
	createdAt time.Time

	maxStreams int
}

// 创建空 batch 并可选预填条目，记录创建时间与 maxStreams 上限。
func newBatch(maxStreams int, entries ...api.Entry) *batch {
	b := &batch{
		streams:    map[string]*logproto.Stream{},
		bytes:      0,
		createdAt:  time.Now(),
		maxStreams: maxStreams,
	}

	// Add entries to the batch
	for _, entry := range entries {
		//never error here
		_ = b.add(entry)
	}

	return b
}

// 向 batch 追加一条日志条目
// add an entry to the batch
func (b *batch) add(entry api.Entry) error {
	b.bytes += entrySize(entry)

	// Append the entry to an already existing stream (if any)
	labels := labelsMapToString(entry.Labels, ReservedLabelTenantID)
	if stream, ok := b.streams[labels]; ok {
		stream.Entries = append(stream.Entries, entry.Entry)
		return nil
	}

	streams := len(b.streams)
	if b.maxStreams > 0 && streams >= b.maxStreams {
		return fmt.Errorf(errMaxStreamsLimitExceeded, streams, b.maxStreams, labels)
	}
	// Add the entry as a new stream
	b.streams[labels] = &logproto.Stream{
		Labels:  labels,
		Entries: []logproto.Entry{entry.Entry},
	}
	return nil
}

// 将 LabelSet 序列化为 {k="v", ...} 字符串，可排除 __tenant_id__ 等保留标签。
func labelsMapToString(ls model.LabelSet, without model.LabelName) string {
	var b strings.Builder
	totalSize := 2
	lstrs := make([]model.LabelName, 0, len(ls))

	for l, v := range ls {
		if l == without {
			continue
		}

		lstrs = append(lstrs, l)
		// guess size increase: 2 for `, ` between labels and 3 for the `=` and quotes around label value
		totalSize += len(l) + 2 + len(v) + 3
	}

	b.Grow(totalSize)
	b.WriteByte('{')
	slices.Sort(lstrs)
	for i, l := range lstrs {
		if i > 0 {
			b.WriteString(", ")
		}

		b.WriteString(string(l))
		b.WriteString(`=`)
		b.WriteString(strconv.Quote(string(ls[l])))
	}
	b.WriteByte('}')

	return b.String()
}

// 当前 batch 累计字节数（各 entry 行长与 structured metadata 之和）。
// sizeBytes returns the current batch size in bytes
func (b *batch) sizeBytes() int {
	return b.bytes
}

// 预估加入 entry 后的 batch 字节数，用于触发 BatchSize 阈值发送。
// sizeBytesAfter returns the size of the batch after the input entry
// will be added to the batch itself
func (b *batch) sizeBytesAfter(entry api.Entry) int {
	return b.bytes + entrySize(entry)
}

// 自 batch 创建起的存活时长，配合 BatchWait 定时 flush。
// age of the batch since its creation
func (b *batch) age() time.Duration {
	return time.Since(b.createdAt)
}

// 构建 PushRequest、proto 序列化并 snappy 压缩，返回载荷与条目数。
// encode the batch as snappy-compressed push request, and returns
// the encoded bytes and the number of encoded entries
func (b *batch) encode() ([]byte, int, error) {
	req, entriesCount := b.createPushRequest()
	buf, err := proto.Marshal(req)
	if err != nil {
		return nil, 0, err
	}
	buf = snappy.Encode(nil, buf)
	return buf, entriesCount, nil
}

// 将 streams map 转为 PushRequest 切片并统计 entries 总数。
// creates push request and returns it, together with number of entries
func (b *batch) createPushRequest() (*logproto.PushRequest, int) {
	req := logproto.PushRequest{
		Streams: make([]logproto.Stream, 0, len(b.streams)),
	}

	entriesCount := 0
	for _, stream := range b.streams {
		req.Streams = append(req.Streams, *stream)
		entriesCount += len(stream.Entries)
	}
	return &req, entriesCount
}

// 计算单条 entry 占用字节：行文本加 structured metadata 各 label 大小。
func entrySize(entry api.Entry) int {
	structuredMetadataSize := 0
	for _, label := range entry.StructuredMetadata {
		structuredMetadataSize += label.Size()
	}
	return len(entry.Line) + structuredMetadataSize
}
