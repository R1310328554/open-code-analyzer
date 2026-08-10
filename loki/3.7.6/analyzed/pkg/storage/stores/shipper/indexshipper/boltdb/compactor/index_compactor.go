package compactor

// indexCompactor 实现 compactor.IndexCompactor：创建 TableCompactor 并打开已压缩 boltdb 索引文件。

import (
	"context"

	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/pkg/compactor"
	"github.com/grafana/loki/v3/pkg/storage/config"
)

const (
	batchSize = 1000
)

type indexEntry struct {
	k, v []byte
}

// indexCompactor 为 boltdb-shipper 索引压缩的工厂与文件打开入口。
type indexCompactor struct{}

func NewIndexCompactor() compactor.IndexCompactor {
	return indexCompactor{}
}

func (i indexCompactor) NewTableCompactor(ctx context.Context, commonIndexSet compactor.IndexSet, existingUserIndexSet map[string]compactor.IndexSet, userIndexSetFactoryFunc compactor.MakeEmptyUserIndexSetFunc, periodConfig config.PeriodConfig) compactor.TableCompactor {
	return newTableCompactor(ctx, commonIndexSet, existingUserIndexSet, userIndexSetFactoryFunc, periodConfig)
}

func (i indexCompactor) OpenCompactedIndexFile(_ context.Context, path, tableName, _, workingDir string, periodConfig config.PeriodConfig, logger log.Logger) (compactor.CompactedIndex, error) {
	boltdb, err := openBoltdbFileWithNoSync(path)
	if err != nil {
		return nil, err
	}

	return newCompactedIndex(boltdb, tableName, workingDir, periodConfig, logger), nil
}
// OpenCompactedIndexFile 以 NoSync 模式打开路径上的 boltdb 并包装为 CompactedIndex。
