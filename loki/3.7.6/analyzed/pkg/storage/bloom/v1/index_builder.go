package v1

// index_builder 顺序写入 series 到 series 块：首写 BlockOptions，按 SeriesPageSize 切页并 delta 编码，Close 写入页头表与尾部元数据。

import (
	"fmt"
	"io"

	"github.com/pkg/errors"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util/encoding"
)

type IndexBuilder struct {
	opts   BlockOptions
	writer io.WriteCloser

	offset        int // track the offset of the file
	writtenSchema bool
	pages         []SeriesPageHeaderWithOffset
	page          PageWriter
	scratch       *encoding.Encbuf

	previousFp        model.Fingerprint
	previousOffset    BloomOffset
	fromFp            model.Fingerprint
	fromTs, throughTs model.Time
}

func NewIndexBuilder(opts BlockOptions, writer io.WriteCloser) *IndexBuilder {
	return &IndexBuilder{
		opts:    opts,
		writer:  writer,
		page:    NewPageWriter(int(opts.SeriesPageSize)),
		scratch: &encoding.Encbuf{},
	}
}

func (b *IndexBuilder) UnflushedSize() int {
	return b.scratch.Len() + b.page.UnflushedSize()
}

func (b *IndexBuilder) WriteOpts() error {
	b.scratch.Reset()
	b.opts.Encode(b.scratch)
	if _, err := b.writer.Write(b.scratch.Get()); err != nil {
		return errors.Wrap(err, "writing opts+schema")
	}
	b.writtenSchema = true
	b.offset += b.scratch.Len()
	return nil
}

// Append 要求指纹单调递增；页首 series 记录 fromFp 与时间边界。
func (b *IndexBuilder) Append(series SeriesWithMeta) error {
	if !b.writtenSchema {
		if err := b.WriteOpts(); err != nil {
			return errors.Wrap(err, "appending series")
		}
	}

	version := b.opts.Schema.version

	b.scratch.Reset()
	// we don't want to update the previous pointers yet in case
	// we need to flush the page first which would
	// be passed the incorrect final fp/offset
	lastOffset := series.Encode(b.scratch, version, b.previousFp, b.previousOffset)

	if !b.page.SpaceFor(b.scratch.Len()) && b.page.Count() > 0 {
		if err := b.flushPage(); err != nil {
			return errors.Wrap(err, "flushing series page")
		}

		// re-encode now that a new page has been cut and we use delta-encoding
		b.scratch.Reset()
		lastOffset = series.Encode(b.scratch, version, b.previousFp, b.previousOffset)
	}

	switch {
	case b.page.Count() == 0:
		// Special case: this is the first series in a page
		if len(series.Chunks) < 1 {
			return fmt.Errorf("series with zero chunks for fingerprint %v", series.Fingerprint)
		}
		b.fromFp = series.Fingerprint
		b.fromTs, b.throughTs = chkBounds(series.Chunks)
	case b.previousFp > series.Fingerprint:
		return fmt.Errorf("out of order series fingerprint for series %v", series.Fingerprint)
	default:
		from, through := chkBounds(series.Chunks)
		if b.fromTs.After(from) {
			b.fromTs = from
		}
		if b.throughTs.Before(through) {
			b.throughTs = through
		}
	}

	_ = b.page.Add(b.scratch.Get())
	b.previousFp = series.Fingerprint
	b.previousOffset = lastOffset
	return nil
}

// chkBounds 扫描 chunk 列表求最小 From 与最大 Through 作为页时间范围。
// must be > 1
func chkBounds(chks []ChunkRef) (from, through model.Time) {
	from, through = chks[0].From, chks[0].Through
	for _, chk := range chks[1:] {
		if chk.From.Before(from) {
			from = chk.From
		}

		if chk.Through.After(through) {
			through = chk.Through
		}
	}
	return
}

// flushPage 压缩页、生成 SeriesPageHeader（NumSeries/Bounds/FromTs/ThroughTs）。
func (b *IndexBuilder) flushPage() error {
	crc32Hash := Crc32HashPool.Get()
	defer Crc32HashPool.Put(crc32Hash)

	decompressedLen, compressedLen, err := b.page.writePage(
		b.writer,
		b.opts.Schema.CompressorPool(),
		crc32Hash,
	)
	if err != nil {
		return errors.Wrap(err, "writing series page")
	}

	header := SeriesPageHeaderWithOffset{
		Offset:          b.offset,
		Len:             compressedLen,
		DecompressedLen: decompressedLen,
		SeriesHeader: SeriesHeader{
			NumSeries: b.page.Count(),
			Bounds:    NewBounds(b.fromFp, b.previousFp),
			FromTs:    b.fromTs,
			ThroughTs: b.throughTs,
		},
	}

	b.pages = append(b.pages, header)
	b.offset += compressedLen

	b.fromFp = 0
	b.fromTs = 0
	b.throughTs = 0
	b.previousFp = 0
	b.previousOffset = BloomOffset{}
	b.page.Reset()

	return nil
}

// Close 写入全部 SeriesPageHeaderWithOffset、BE64 头偏移与 CRC32 校验和。
func (b *IndexBuilder) Close() (uint32, error) {
	if b.page.Count() > 0 {
		if err := b.flushPage(); err != nil {
			return 0, errors.Wrap(err, "flushing final series page")
		}
	}

	b.scratch.Reset()
	b.scratch.PutUvarint(len(b.pages))
	for _, h := range b.pages {
		h.Encode(b.scratch)
	}

	// put offset to beginning of header section
	// cannot be varint encoded because it's offset will be calculated as
	// the 8 bytes prior to the checksum
	b.scratch.PutBE64(uint64(b.offset))
	crc32Hash := Crc32HashPool.Get()
	defer Crc32HashPool.Put(crc32Hash)
	// wrap with final checksum
	b.scratch.PutHash(crc32Hash)
	_, err := b.writer.Write(b.scratch.Get())
	if err != nil {
		return 0, errors.Wrap(err, "writing series page headers")
	}
	return crc32Hash.Sum32(), errors.Wrap(b.writer.Close(), "closing series writer")
}
// 页切换后需用重置的 previousFp/Offset 重新 Encode series，保证 delta 基准正确。
