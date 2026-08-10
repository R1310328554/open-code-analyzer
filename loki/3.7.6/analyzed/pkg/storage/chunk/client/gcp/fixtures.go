package gcp

// fixtures 为 GCP 存储集成测试提供 bttest 内存 Bigtable 与 fake-gcs-server，组合 column-key/v1 索引、GCS/Bigtable chunk 后端及 hash 前缀等变体。

import (
	"context"
	"fmt"
	"io"

	"cloud.google.com/go/bigtable"
	"cloud.google.com/go/bigtable/bttest"
	"cloud.google.com/go/storage"
	"github.com/fsouza/fake-gcs-server/fakestorage"
	"google.golang.org/api/option"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/hedging"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/testutils"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

const (
	proj, instance = "proj", "instance"
)

// fixture 记录测试矩阵维度并在 Clients() 中启动本地 Bigtable/GCS 服务。
type fixture struct {
	btsrv  *bttest.Server
	gcssrv *fakestorage.Server

	name string

	gcsObjectClient bool
	columnKeyClient bool
	hashPrefix      bool
}

func (f *fixture) Name() string {
	return f.name
}

// Clients 启动 bttest 与 fake GCS，返回 index/chunk/table 客户端及 closer。
func (f *fixture) Clients() (
	iClient index.Client, cClient client.Client, tClient index.TableClient,
	schemaConfig config.SchemaConfig, closer io.Closer, err error,
) {
	f.btsrv, err = bttest.NewServer("localhost:0")
	if err != nil {
		return
	}

	f.gcssrv = fakestorage.NewServer(nil)

	opts := fakestorage.CreateBucketOpts{
		Name: "chunks",
	}
	f.gcssrv.CreateBucketWithOpts(opts)

	conn, err := grpc.NewClient(f.btsrv.Addr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return
	}

	ctx := context.Background()
	adminClient, err := bigtable.NewAdminClient(ctx, proj, instance, option.WithGRPCConn(conn))
	if err != nil {
		return
	}

	schemaConfig = testutils.DefaultSchemaConfig("gcp-columnkey")
	tClient = &tableClient{
		client: adminClient,
	}

	bigTableClientConfig := bigtable.ClientConfig{
		MetricsProvider: bigtable.NoopMetricsProvider{},
	}
	c, err := bigtable.NewClientWithConfig(ctx, proj, instance, bigTableClientConfig, option.WithGRPCConn(conn))
	if err != nil {
		return
	}

	cfg := Config{
		DistributeKeys: f.hashPrefix,
	}
	if f.columnKeyClient {
		iClient = newStorageClientColumnKey(cfg, schemaConfig, c)
	} else {
		iClient = newStorageClientV1(cfg, schemaConfig, c)
	}

	if f.gcsObjectClient {
		var c *GCSObjectClient
		c, err = newGCSObjectClient(ctx, GCSConfig{
			BucketName: "chunks",
			Insecure:   true,
		}, hedging.Config{}, func(_ context.Context, _ ...option.ClientOption) (*storage.Client, error) {
			return f.gcssrv.Client(), nil
		})
		if err != nil {
			return
		}
		cClient = client.NewClient(c, nil, config.SchemaConfig{})
	} else {
		cClient = newBigtableObjectClient(Config{}, schemaConfig, c)
	}

	closer = testutils.CloserFunc(func() error {
		conn.Close()
		return nil
	})

	return
}

// Fixtures 枚举 gcsObjectClient×columnKeyClient×hashPrefix 共 8 种配置供 testutils 驱动。
// Fixtures for unit testing GCP storage.
var Fixtures = func() []testutils.Fixture {
	fixtures := []testutils.Fixture{}
	for _, gcsObjectClient := range []bool{true, false} {
		for _, columnKeyClient := range []bool{true, false} {
			for _, hashPrefix := range []bool{true, false} {
				fixtures = append(fixtures, &fixture{
					name:            fmt.Sprintf("bigtable-columnkey:%v-gcsObjectClient:%v-hashPrefix:%v", columnKeyClient, gcsObjectClient, hashPrefix),
					columnKeyClient: columnKeyClient,
					gcsObjectClient: gcsObjectClient,
					hashPrefix:      hashPrefix,
				})
			}
		}
	}
	return fixtures
}()
// gcsObjectClient 为 true 时用 fake GCS 存 chunk；否则 chunk 也写入 Bigtable 表。
