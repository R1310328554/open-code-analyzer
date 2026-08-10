package tsdb

// store 将 TSDB index shipper 与 HeadManager 组合为 index.ReaderWriter：写路径追加 chunk 元数据，读路径经 MultiIndex 查询本地 head 与远端索引。

import (
	"context"
	"fmt"
	"math"
	"sync"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/storage/chunk"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/fetcher"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/downloads"
	tsdbindex "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// IndexWriter 定义向 TSDB head 追加 series/chunk 元数据的写入接口。
type IndexWriter interface {
	Append(userID string, ls labels.Labels, fprint uint64, chks tsdbindex.ChunkMetas) error
}

type store struct {
	index.Reader
	indexShipper indexshipper.IndexShipper
	indexWriter  IndexWriter
	logger       log.Logger
	stopOnce     sync.Once
}

// NewStore 按 shipper 模式初始化 HeadManager 或 noop/failing writer，并返回 Stop 清理函数。
// NewStore creates a new tsdb index ReaderWriter.
func NewStore(
	name, prefix string,
	indexShipperCfg indexshipper.Config,
	schemaCfg config.SchemaConfig,
	_ *fetcher.Fetcher,
	objectClient client.ObjectClient,
	limits downloads.Limits,
	tableRange config.TableRange,
	reg prometheus.Registerer,
	logger log.Logger,
) (
	index.ReaderWriter,
	func(),
	error,
) {

	storeInstance := &store{
		logger: logger,
	}

	if err := storeInstance.init(name, prefix, indexShipperCfg, schemaCfg, objectClient, limits, tableRange, reg); err != nil {
		return nil, nil, err
	}

	return storeInstance, storeInstance.Stop, nil
}

func (s *store) init(name, prefix string, indexShipperCfg indexshipper.Config, schemaCfg config.SchemaConfig, objectClient client.ObjectClient,
	limits downloads.Limits, tableRange config.TableRange, reg prometheus.Registerer) error {

	var err error
	s.indexShipper, err = indexshipper.NewIndexShipper(
		prefix,
		indexShipperCfg,
		objectClient,
		limits,
		nil,
		OpenShippableTSDB,
		tableRange,
		prometheus.WrapRegistererWithPrefix("loki_tsdb_shipper_", reg),
		s.logger,
	)
	if err != nil {
		return err
	}

	var indices []Index
	opts := DefaultIndexClientOptions()

	// early return in case index shipper is disabled.
	if indexShipperCfg.Mode == indexshipper.ModeDisabled {
		s.indexWriter = noopIndexWriter{}
		s.Reader = NewIndexClient(NoopIndex{}, opts, limits)
		return nil
	}

	if indexShipperCfg.Mode == indexshipper.ModeWriteOnly {
		// We disable bloom filters on write nodes
		// for the Stats() methods as it's of relatively little
		// benefit when compared to the memory cost. The bloom filters
		// help detect duplicates with some probability, but this
		// is only relevant across index bucket boundaries
		// & pre-compacted indices (replication, not valid on a single ingester).
		opts.UseBloomFilters = false
	}

	if indexShipperCfg.Mode != indexshipper.ModeReadOnly {
		nodeName, err := indexShipperCfg.GetUniqueUploaderName()
		if err != nil {
			return err
		}

		tsdbMetrics := NewMetrics(reg)
		tsdbManager := NewTSDBManager(
			name,
			nodeName,
			indexShipperCfg.ActiveIndexDirectory,
			s.indexShipper,
			tableRange,
			schemaCfg,
			s.logger,
			tsdbMetrics,
		)

		headManager := NewHeadManager(
			name,
			s.logger,
			indexShipperCfg.ActiveIndexDirectory,
			tsdbMetrics,
			tsdbManager,
		)
		if err := headManager.Start(); err != nil {
			return err
		}

		s.indexWriter = headManager
		indices = append(indices, headManager)
	} else {
		s.indexWriter = failingIndexWriter{}
	}

	indices = append(indices, newIndexShipperQuerier(s.indexShipper, tableRange))
	multiIndex := NewMultiIndex(IndexSlice(indices))

	s.Reader = NewIndexClient(multiIndex, opts, limits)

	return nil
}

func (s *store) Stop() {
	s.stopOnce.Do(func() {
		if hm, ok := s.indexWriter.(*HeadManager); ok {
			if err := hm.Stop(); err != nil {
				level.Error(s.logger).Log("msg", "failed to stop head manager", "err", err)
			}
		}
		s.indexShipper.Stop()
	})
}

// IndexChunk 将 chunk 指纹、时间 bounds 与大小估算写入 TSDB 索引以提升持久性。
func (s *store) IndexChunk(_ context.Context, _ model.Time, _ model.Time, chk chunk.Chunk) error {
	// Always write the index to benefit durability via replication factor.
	approxKB := math.Round(float64(chk.Data.UncompressedSize()) / float64(1<<10))
	metas := tsdbindex.ChunkMetas{
		{
			Checksum: chk.Checksum,
			MinTime:  int64(chk.From),
			MaxTime:  int64(chk.Through),
			KB:       uint32(approxKB),
			Entries:  uint32(chk.Data.Entries()),
		},
	}
	if err := s.indexWriter.Append(chk.UserID, chk.Metric, chk.Fingerprint, metas); err != nil {
		return errors.Wrap(err, "writing index entry")
	}
	return nil
}

// failingIndexWriter 在只读模式下拒绝 Append，提示 index writer 未初始化。
type failingIndexWriter struct{}

func (f failingIndexWriter) Append(_ string, _ labels.Labels, _ uint64, _ tsdbindex.ChunkMetas) error {
	return fmt.Errorf("index writer is not initialized due to tsdb store being initialized in read-only mode")
}

type noopIndexWriter struct{}

func (f noopIndexWriter) Append(_ string, _ labels.Labels, _ uint64, _ tsdbindex.ChunkMetas) error {
	return nil
}
// WriteOnly 节点关闭 bloom filter 以降低 Stats 内存开销，读节点仍可使用完整索引。
