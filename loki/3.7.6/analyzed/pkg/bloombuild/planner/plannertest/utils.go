package plannertest

// Planner 单元测试辅助：构造固定日表、TSDB 标识、bloom meta/block
// 及合成 series，便于策略与 planner 逻辑在无真实对象存储时验证。

import (
	"bytes"
	"context"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/compression"
	v2 "github.com/grafana/loki/v3/pkg/iter/v2"
	v1 "github.com/grafana/loki/v3/pkg/storage/bloom/v1"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/bloomshipper"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
)

// TestDay/TestTable 为测试提供统一的固定日表与 index 前缀上下文。
var TestDay = ParseDayTime("2023-09-01")
var TestTable = config.NewDayTable(TestDay, "index_")

func TsdbID(n int) tsdb.SingleTenantTSDBIdentifier {
	return tsdb.SingleTenantTSDBIdentifier{
		TS: time.Unix(int64(n), 0),
	}
}

// GenMeta 生成带 fakeTenant、指纹边界、来源 TSDB 与 block 引用的测试 meta。
func GenMeta(minVal, maxVal model.Fingerprint, sources []int, blocks []bloomshipper.BlockRef) bloomshipper.Meta {
	m := bloomshipper.Meta{
		MetaRef: bloomshipper.MetaRef{
			Ref: bloomshipper.Ref{
				TenantID:  "fakeTenant",
				TableName: TestTable.Addr(),
				Bounds:    v1.NewBounds(minVal, maxVal),
			},
		},
		Blocks: blocks,
	}
	for _, source := range sources {
		m.Sources = append(m.Sources, TsdbID(source))
	}
	return m
}

func GenBlockRef(minVal, maxVal model.Fingerprint) bloomshipper.BlockRef {
	startTS, endTS := TestDay.Bounds()
	return bloomshipper.BlockRef{
		Ref: bloomshipper.Ref{
			TenantID:       "fakeTenant",
			TableName:      TestTable.Addr(),
			Bounds:         v1.NewBounds(minVal, maxVal),
			StartTimestamp: startTS,
			EndTimestamp:   endTS,
			Checksum:       0,
		},
	}
}

// GenBlock 用内存 writer 构建空 bloom block 并 tar 压缩为可读 seeker。
func GenBlock(ref bloomshipper.BlockRef) (bloomshipper.Block, error) {
	indexBuf := bytes.NewBuffer(nil)
	bloomsBuf := bytes.NewBuffer(nil)
	writer := v1.NewMemoryBlockWriter(indexBuf, bloomsBuf)
	reader := v1.NewByteReader(indexBuf, bloomsBuf)

	blockOpts := v1.NewBlockOptions(compression.None, 0, 0)

	builder, err := v1.NewBlockBuilder(blockOpts, writer)
	if err != nil {
		return bloomshipper.Block{}, err
	}

	if _, err = builder.BuildFrom(v2.NewEmptyIter[v1.SeriesWithBlooms]()); err != nil {
		return bloomshipper.Block{}, err
	}

	block := v1.NewBlock(reader, v1.NewMetrics(nil))

	buf := bytes.NewBuffer(nil)
	if err := v1.TarCompress(ref.Codec, buf, block.Reader()); err != nil {
		return bloomshipper.Block{}, err
	}

	tarReader := bytes.NewReader(buf.Bytes())

	return bloomshipper.Block{
		BlockRef: ref,
		Data:     bloomshipper.ClosableReadSeekerAdapter{ReadSeeker: tarReader},
	}, nil
}

func GenSeries(bounds v1.FingerprintBounds) []*v1.Series {
	return GenSeriesWithStep(bounds, 1)
}

// GenSeriesWithStep 在指纹范围内按步长生成带占位 chunk 引用的 v1.Series 切片。
func GenSeriesWithStep(bounds v1.FingerprintBounds, step int) []*v1.Series {
	series := make([]*v1.Series, 0, int(bounds.Max-bounds.Min+1)/step)
	for i := bounds.Min; i <= bounds.Max; i += model.Fingerprint(step) {
		series = append(series, &v1.Series{
			Fingerprint: i,
			Chunks: v1.ChunkRefs{
				{
					From:     0,
					Through:  1,
					Checksum: 1,
				},
			},
		})
	}
	return series
}

// PutMetas 将 meta 及其关联 block 写入 mock bloom 客户端，供集成测试种子数据。
func PutMetas(bloomClient bloomshipper.Client, metas []bloomshipper.Meta) error {
	for _, meta := range metas {
		err := bloomClient.PutMeta(context.Background(), meta)
		if err != nil {
			return err
		}

		for _, block := range meta.Blocks {
			writtenBlock, err := GenBlock(block)
			if err != nil {
				return err
			}

			err = bloomClient.PutBlock(context.Background(), writtenBlock)
			if err != nil {
				return err
			}
		}
	}
	return nil
}

func ParseDayTime(s string) config.DayTime {
	t, err := time.Parse("2006-01-02", s)
	if err != nil {
		panic(err)
	}
	return config.DayTime{
		Time: model.TimeFromUnix(t.Unix()),
	}
}
