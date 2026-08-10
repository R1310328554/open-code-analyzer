package bucket

// object_client_adapter 将 Thanos objstore.Bucket 适配为 Loki chunk client 的 ObjectClient：支持命名存储、hedged 读取、后端类型感知及 GCS/S3 可重试错误判定。

import (
	"context"
	"fmt"
	"io"
	"slices"
	"strings"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/thanos-io/objstore"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/aws"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/gcp"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/hedging"
)

// ObjectClientAdapter 持有主桶与 hedged 桶双客户端，并记录是否支持 UpdatedAt 迭代选项。
type ObjectClientAdapter struct {
	bucket, hedgedBucket objstore.Bucket
	logger               log.Logger
	supportsUpdatedAt    bool
	isRetryableErr       func(err error) bool
	// storeType is the resolved backend type (e.g. filesystem, s3, gcs), used by
	// callers that need backend-specific behaviour such as chunk key encoding.
	storeType string
}

// NewObjectClient 解析命名存储、可选禁用重试、创建桶并在 hedging 启用时构建第二客户端。
func NewObjectClient(ctx context.Context, backend string, cfg ConfigWithNamedStores, component string, hedgingCfg hedging.Config, disableRetries bool, logger log.Logger) (*ObjectClientAdapter, error) {
	var (
		storeType = backend
		storeCfg  = cfg.Config
	)

	if st, ok := cfg.NamedStores.LookupStoreType(backend); ok {
		storeType = st
		// override config with values from named store config
		if err := cfg.NamedStores.OverrideConfig(&storeCfg, backend); err != nil {
			return nil, err
		}
	}

	if disableRetries {
		if err := storeCfg.disableRetries(storeType); err != nil {
			return nil, fmt.Errorf("create bucket: %w", err)
		}
	}

	bucket, err := NewClient(ctx, storeType, storeCfg, component, logger)
	if err != nil {
		return nil, fmt.Errorf("create bucket: %w", err)
	}

	hedgedBucket := bucket
	if hedgingCfg.At != 0 {
		hedgedTrasport, err := hedgingCfg.RoundTripperWithRegisterer(nil, prometheus.WrapRegistererWithPrefix("loki_", prometheus.DefaultRegisterer))
		if err != nil {
			return nil, fmt.Errorf("create hedged transport: %w", err)
		}

		if err := storeCfg.configureTransport(storeType, hedgedTrasport); err != nil {
			return nil, fmt.Errorf("create hedged bucket: %w", err)
		}

		hedgedBucket, err = NewClient(ctx, storeType, storeCfg, component, logger)
		if err != nil {
			return nil, fmt.Errorf("create hedged bucket: %w", err)
		}
	}

	o := &ObjectClientAdapter{
		bucket:            bucket,
		hedgedBucket:      hedgedBucket,
		logger:            log.With(logger, "component", "bucket_to_object_client_adapter"),
		supportsUpdatedAt: slices.Contains(bucket.SupportedIterOptions(), objstore.UpdatedAt),
		storeType:         storeType,
		// default to no retryable errors. Override with WithRetryableErrFunc
		isRetryableErr: func(_ error) bool {
			return false
		},
	}

	switch storeType {
	case GCS:
		o.isRetryableErr = gcp.IsRetryableErr
	case S3:
		o.isRetryableErr = aws.IsRetryableErr
	}

	return o, nil
}

// Stop 为 ObjectClient 生命周期钩子，当前 filesystem/S3 桶无需显式关闭。
func (o *ObjectClientAdapter) Stop() {
}

// IsBackendFilesystem 供上层按本地文件系统后端选择 chunk key 编码策略。
// IsBackendFilesystem reports whether the underlying object storage backend is
// the local filesystem.
func (o *ObjectClientAdapter) IsBackendFilesystem() bool {
	return o.storeType == Filesystem
}

// ObjectExists 委托底层 bucket.Exists 检查对象键是否存在。
// ObjectExists checks if a given objectKey exists in the bucket
func (o *ObjectClientAdapter) ObjectExists(ctx context.Context, objectKey string) (bool, error) {
	return o.bucket.Exists(ctx, objectKey)
}

// GetAttributes 经 hedgedBucket 读取对象属性，目前仅填充 Size 字段。
// GetAttributes returns the attributes of the specified object key from the configured bucket.
func (o *ObjectClientAdapter) GetAttributes(ctx context.Context, objectKey string) (client.ObjectAttributes, error) {
	attr := client.ObjectAttributes{}
	thanosAttr, err := o.hedgedBucket.Attributes(ctx, objectKey)
	if err != nil {
		return attr, err
	}

	attr.Size = thanosAttr.Size
	return attr, nil
}

// PutObject 将 reader 内容上传到主 bucket，不经 hedging 路径。
// PutObject puts the specified bytes into the configured bucket at the provided key
func (o *ObjectClientAdapter) PutObject(ctx context.Context, objectKey string, object io.Reader) error {
	return o.bucket.Upload(ctx, objectKey, object)
}

// GetObject 经 hedgedBucket 拉取对象；无法确定大小时 size 为 -1 并由调用方处理。
// GetObject returns a reader and the size for the specified object key from the configured bucket.
// size is set to -1 if it cannot be succefully determined, it is up to the caller to check this value before using it.
func (o *ObjectClientAdapter) GetObject(ctx context.Context, objectKey string) (io.ReadCloser, int64, error) {
	reader, err := o.hedgedBucket.Get(ctx, objectKey)
	if err != nil {
		return nil, 0, err
	}

	size, err := objstore.TryToGetSize(reader)
	if err != nil {
		size = -1
		level.Warn(o.logger).Log("msg", "failed to get size of object", "err", err)
	}

	return reader, size, err
}

// GetObjectRange 支持范围读取，同样走 hedged 客户端以降低尾延迟。
func (o *ObjectClientAdapter) GetObjectRange(ctx context.Context, objectKey string, offset, length int64) (io.ReadCloser, error) {
	return o.hedgedBucket.GetRange(ctx, objectKey, offset, length)
}

// List 按 prefix 与 delimiter 列举对象；空 delimiter 时递归列出全部子键。
// List objects with given prefix.
func (o *ObjectClientAdapter) List(ctx context.Context, prefix, delimiter string) ([]client.StorageObject, []client.StorageCommonPrefix, error) {
	var storageObjects []client.StorageObject
	var commonPrefixes []client.StorageCommonPrefix
	var iterParams []objstore.IterOption

// delimiter 为空追加 WithRecursiveIter；支持 UpdatedAt 时一并请求最后修改时间。
	// If delimiter is empty we want to list all files
	if delimiter == "" {
		iterParams = append(iterParams, objstore.WithRecursiveIter())
	}

	if o.supportsUpdatedAt {
		iterParams = append(iterParams, objstore.WithUpdatedAt())
	}

	err := o.bucket.IterWithAttributes(ctx, prefix, func(attrs objstore.IterObjectAttributes) error {
// 以 delimiter 结尾的键视为公共前缀目录，否则收集为 StorageObject 并填充 ModifiedAt。
		// CommonPrefixes are keys that have the prefix and have the delimiter
		// as a suffix
		objectKey := attrs.Name
		if delimiter != "" && strings.HasSuffix(objectKey, delimiter) {
			commonPrefixes = append(commonPrefixes, client.StorageCommonPrefix(objectKey))
			return nil
		}

		lastModified, ok := attrs.LastModified()
		if o.supportsUpdatedAt && !ok {
			return errors.Errorf("failed to get lastModified for %s", objectKey)
		}
		// Some providers do not support supports UpdatedAt option. For those we need
		// to make an additional request to get the last modified time.
		if !o.supportsUpdatedAt {
			attr, err := o.bucket.Attributes(ctx, objectKey)
			if err != nil {
				return errors.Wrapf(err, "failed to get attributes for %s", objectKey)
			}
			lastModified = attr.LastModified
		}

		storageObjects = append(storageObjects, client.StorageObject{
			Key:        objectKey,
			ModifiedAt: lastModified,
		})

		return nil
	}, iterParams...)
	if err != nil {
		return nil, nil, err
	}

	return storageObjects, commonPrefixes, nil
}

// DeleteObject 从主 bucket 删除指定对象键。
// DeleteObject deletes the specified object key from the configured bucket.
func (o *ObjectClientAdapter) DeleteObject(ctx context.Context, objectKey string) error {
	return o.bucket.Delete(ctx, objectKey)
}

// IsObjectNotFoundErr 委托底层 IsObjNotFoundErr 判断对象不存在错误。
// IsObjectNotFoundErr returns true if error means that object is not found. Relevant to GetObject and DeleteObject operations.
func (o *ObjectClientAdapter) IsObjectNotFoundErr(err error) bool {
	return o.bucket.IsObjNotFoundErr(err)
}

// IsRetryableErr 对 GCS/S3 使用 provider 专用判定函数，其余后端默认不可重试。
// IsRetryableErr returns true if the request failed due to some retryable server-side scenario
func (o *ObjectClientAdapter) IsRetryableErr(err error) bool {
	return o.isRetryableErr(err)
}
// 不支持 UpdatedAt 的后端在 List 中对每个对象额外调用 Attributes 获取修改时间。
