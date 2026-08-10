package testutil

// testutil 为 TSDB 测试提供本地 filesystem bucket：创建临时目录并包装 metrics，模拟对象存储读写环境。

import (
	"testing"

	"github.com/stretchr/testify/require"
	"github.com/thanos-io/objstore"

	"github.com/grafana/loki/v3/pkg/storage/bucket/filesystem"
)

// PrepareFilesystemBucket 返回带 metrics 的 Bucket 与临时存储目录路径。
func PrepareFilesystemBucket(t testing.TB) (objstore.Bucket, string) {
	storageDir := t.TempDir()

	bkt, err := filesystem.NewBucketClient(filesystem.Config{Directory: storageDir})
	require.NoError(t, err)

	return objstore.WrapWithMetrics(bkt, nil, "test"), storageDir
}
// 测试结束由 t.TempDir 自动清理目录，无需手动删除 bucket 文件。
