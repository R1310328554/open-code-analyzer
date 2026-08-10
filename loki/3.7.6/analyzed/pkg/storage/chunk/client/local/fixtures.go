package local

// fixtures 为单元测试注册 boltdb 本地存储 fixture：临时目录内同时搭建 BoltDB 索引、FS 对象存储与 TableClient，并配置 10 分钟周期的 schema。

import (
	"io"
	"os"
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/testutils"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

// fixture 实现 testutils.Fixture 接口，Name 返回 "boltdb"。
type fixture struct {
	name    string
	dirname string
}

func (f *fixture) Name() string {
	return f.name
}

// Clients 在 TempDir 下创建索引/块/表三套客户端，Closer 负责 RemoveAll 清理。
func (f *fixture) Clients() (
	indexClient index.Client, chunkClient client.Client, tableClient index.TableClient,
	schemaConfig config.SchemaConfig, closer io.Closer, err error,
) {
	f.dirname, err = os.MkdirTemp(os.TempDir(), "boltdb")
	if err != nil {
		return
	}

	indexClient, err = NewBoltDBIndexClient(BoltDBConfig{
		Directory: f.dirname,
	})
	if err != nil {
		return
	}

	oClient, err := NewFSObjectClient(FSConfig{Directory: f.dirname})
	if err != nil {
		return
	}

	chunkClient = client.NewClient(oClient, client.FSEncoder, config.SchemaConfig{})

	tableClient, err = NewTableClient(f.dirname)
	if err != nil {
		return
	}

	schemaConfig = config.SchemaConfig{
		Configs: []config.PeriodConfig{{
			IndexType: "boltdb",
			From:      config.DayTime{Time: model.Now()},
			ChunkTables: config.PeriodicTableConfig{
				Prefix: "chunks",
				Period: 10 * time.Minute,
			},
			IndexTables: config.IndexPeriodicTableConfig{
				PeriodicTableConfig: config.PeriodicTableConfig{
					Prefix: "index",
					Period: 10 * time.Minute,
				}},
		}},
	}

	closer = testutils.CloserFunc(func() error {
		return os.RemoveAll(f.dirname)
	})

	return
}

// Fixtures for unit testing GCP storage.
// Fixtures 导出 boltdb fixture 供跨后端一致性测试套件注册使用。
var Fixtures = []testutils.Fixture{
	&fixture{
		name: "boltdb",
	},
}
// chunkClient 使用 FSEncoder 编码键；IndexType 设为 boltdb 以匹配本地索引实现。
