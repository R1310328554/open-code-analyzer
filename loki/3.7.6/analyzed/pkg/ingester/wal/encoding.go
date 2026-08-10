package wal

// wal 包定义 WAL/checkpoint 记录的二进制编解码：支持多版本 entries 格式（V1/V2 计数器/V3 结构化元数据）。

import (
	"errors"
	"fmt"
	"time"

	"github.com/prometheus/prometheus/tsdb/chunks"
	"github.com/prometheus/prometheus/tsdb/record"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/util/encoding"
)

// RecordType 标识 WAL 记录类型：series、entries 各版本或 checkpoint。
// RecordType represents the type of the WAL/Checkpoint record.
type RecordType byte

const (
	_ = iota // ignore first value so the zero value doesn't look like a record type.
	// WALRecordSeries is the type for the WAL record for series.
	WALRecordSeries RecordType = iota
	// WALRecordEntriesV1 is the type for the WAL record for samples.
	WALRecordEntriesV1
	// CheckpointRecord is the type for the Checkpoint record based on protos.
	CheckpointRecord
	// WALRecordEntriesV2 is the type for the WAL record for samples with an
	// additional counter value for use in replaying without the ordering constraint.
	WALRecordEntriesV2
	// WALRecordEntriesV3 is the type for the WAL record for samples with structured metadata.
	WALRecordEntriesV3
)

// CurrentEntriesRec 指定当前写入的 entries 版本（V3 含 structured metadata）。
// The current type of Entries that this distribution writes.
// Loki can read in a backwards compatible manner, but will write the newest variant.
// TODO: Change to WALRecordEntriesV3?
const CurrentEntriesRec = WALRecordEntriesV3

// Record is a struct combining the series and samples record.
// Record 聚合 tenant、series 引用与 RefEntries；entryIndexMap 加速 ingestion 期 fingerprint 查找。
type Record struct {
	UserID string
	Series []record.RefSeries

	// entryIndexMap coordinates the RefEntries index associated with a particular fingerprint.
	// This is helpful for constant time lookups during ingestion and is ignored when restoring
	// from the WAL.
	entryIndexMap map[uint64]int
	RefEntries    []RefEntries
}

func (r *Record) IsEmpty() bool {
	return len(r.Series) == 0 && len(r.RefEntries) == 0
}

func (r *Record) Reset() {
	r.UserID = ""
	if len(r.Series) > 0 {
		r.Series = r.Series[:0]
	}

	r.RefEntries = r.RefEntries[:0]
	r.entryIndexMap = make(map[uint64]int)
}

// AddEntries 按 fingerprint 合并条目到同一 RefEntries，并更新 replay 计数器。
func (r *Record) AddEntries(fp uint64, counter int64, entries ...logproto.Entry) {
	if idx, ok := r.entryIndexMap[fp]; ok {
		r.RefEntries[idx].Entries = append(r.RefEntries[idx].Entries, entries...)
		r.RefEntries[idx].Counter = counter
		return
	}

	r.entryIndexMap[fp] = len(r.RefEntries)
	r.RefEntries = append(r.RefEntries, RefEntries{
		Counter: counter,
		Ref:     chunks.HeadSeriesRef(fp),
		Entries: entries,
	})
}

// RefEntries 绑定 series ref、WAL 回放计数器与一批 logproto.Entry。
type RefEntries struct {
	Counter int64
	Ref     chunks.HeadSeriesRef
	Entries []logproto.Entry
}

func (r *Record) EncodeSeries(b []byte) []byte {
	buf := encoding.EncWith(b)
	buf.PutByte(byte(WALRecordSeries))
	buf.PutUvarintStr(r.UserID)

	var enc record.Encoder
	// The 'encoded' already has the type header and userID here, hence re-using
	// the remaining part of the slice (i.e. encoded[len(encoded):])) to encode the series.
	encoded := buf.Get()
	encoded = append(encoded, enc.Series(r.Series, encoded[len(encoded):])...)

	return encoded
}

// EncodeEntries 以首条时间戳为基准编码相对偏移，V3 额外序列化 structured metadata。
func (r *Record) EncodeEntries(version RecordType, b []byte) []byte {
	buf := encoding.EncWith(b)
	buf.PutByte(byte(version))
	buf.PutUvarintStr(r.UserID)

	// Placeholder for the first timestamp of any sample encountered.
	// All others in this record will store their timestamps as diffs relative to this
	// as a space optimization.
	var first int64

outer:
	for _, ref := range r.RefEntries {
		for _, entry := range ref.Entries {
			first = entry.Timestamp.UnixNano()
			buf.PutBE64int64(first)
			break outer
		}
	}

	for _, ref := range r.RefEntries {
		// ignore refs with 0 entries
		if len(ref.Entries) < 1 {
			continue
		}
		buf.PutBE64(uint64(ref.Ref)) // write fingerprint

		if version >= WALRecordEntriesV2 {
			buf.PutBE64int64(ref.Counter) // write highest counter value
		}

		buf.PutUvarint(len(ref.Entries)) // write number of entries

		for _, s := range ref.Entries {
			buf.PutVarint64(s.Timestamp.UnixNano() - first)
			buf.PutUvarint(len(s.Line))
			buf.PutString(s.Line)

			if version >= WALRecordEntriesV3 {
				// structured metadata
				buf.PutUvarint(len(s.StructuredMetadata))
				for _, l := range s.StructuredMetadata {
					buf.PutUvarint(len(l.Name))
					buf.PutString(l.Name)
					buf.PutUvarint(len(l.Value))
					buf.PutString(l.Value)
				}
			}
		}
	}
	return buf.Get()
}

// DecodeEntries 按版本解析 entries 块，V3 逐条读取 structured metadata 键值对。
func DecodeEntries(b []byte, version RecordType, rec *Record) error {
	if len(b) == 0 {
		return nil
	}

	dec := encoding.DecWith(b)
	baseTime := dec.Be64int64()

	for len(dec.B) > 0 && dec.Err() == nil {
		refEntries := RefEntries{
			Ref: chunks.HeadSeriesRef(dec.Be64()),
		}

		if version >= WALRecordEntriesV2 {
			refEntries.Counter = dec.Be64int64()
		}

		nEntries := dec.Uvarint()
		refEntries.Entries = make([]logproto.Entry, 0, nEntries)
		rem := nEntries
		for ; dec.Err() == nil && rem > 0; rem-- {
			timeOffset := dec.Varint64()
			lineLength := dec.Uvarint()
			line := dec.Bytes(lineLength)

			var structuredMetadata []logproto.LabelAdapter
			if version >= WALRecordEntriesV3 {
				nStructuredMetadata := dec.Uvarint()
				if nStructuredMetadata > 0 {
					structuredMetadata = make([]logproto.LabelAdapter, 0, nStructuredMetadata)
					for i := 0; dec.Err() == nil && i < nStructuredMetadata; i++ {
						nameLength := dec.Uvarint()
						name := dec.Bytes(nameLength)
						valueLength := dec.Uvarint()
						value := dec.Bytes(valueLength)
						structuredMetadata = append(structuredMetadata, logproto.LabelAdapter{
							Name:  string(name),
							Value: string(value),
						})
					}
				}
			}

			refEntries.Entries = append(refEntries.Entries, logproto.Entry{
				Timestamp:          time.Unix(0, baseTime+timeOffset),
				Line:               string(line),
				StructuredMetadata: structuredMetadata,
			})
		}

		if dec.Err() != nil {
			return fmt.Errorf("entry decode error after %d RefEntries: %w", nEntries-rem, dec.Err())
		}

		rec.RefEntries = append(rec.RefEntries, refEntries)
	}

	if dec.Err() != nil {
		return fmt.Errorf("refEntry decode error: %w", dec.Err())
	}

	if len(dec.B) > 0 {
		return fmt.Errorf("unexpected %d bytes left in entry", len(dec.B))
	}
	return nil
}

// DecodeRecord 读取记录类型头，分派到 series 或 entries 解码路径。
func DecodeRecord(b []byte, walRec *Record) (err error) {
	var (
		userID  string
		dec     record.Decoder
		rSeries []record.RefSeries

		decbuf = encoding.DecWith(b)
		t      = RecordType(decbuf.Byte())
	)

	switch t {
	case WALRecordSeries:
		userID = decbuf.UvarintStr()
		rSeries, err = dec.Series(decbuf.B, walRec.Series)
	case WALRecordEntriesV1, WALRecordEntriesV2, WALRecordEntriesV3:
		userID = decbuf.UvarintStr()
		err = DecodeEntries(decbuf.B, t, walRec)
	default:
		return errors.New("unknown record type")
	}

	// We reach here only if its a record with type header.
	if decbuf.Err() != nil {
		return decbuf.Err()
	}

	if err != nil {
		return err
	}

	walRec.UserID = userID
	walRec.Series = rSeries
	return nil
}
// 时间戳差分编码显著压缩 WAL 条目体积，counter 字段支持无序写入场景下的精确回放去重。
