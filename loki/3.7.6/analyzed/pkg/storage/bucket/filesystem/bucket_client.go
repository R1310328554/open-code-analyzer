package filesystem

// bucket_client 提供本地文件系统对象存储适配：将 Config.Directory 传给 Thanos filesystem provider 作为块数据根目录。

import (
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/filesystem"
)

// NewBucketClient 调用 filesystem.NewBucket，无需 logger，适合开发与小规模部署。
// NewBucketClient creates a new filesystem bucket client
func NewBucketClient(cfg Config) (objstore.Bucket, error) {
	return filesystem.NewBucket(cfg.Directory)
}
// filesystem 后端无 HTTP 传输层，bucket 包中 configureTransport 对该后端为空操作。
