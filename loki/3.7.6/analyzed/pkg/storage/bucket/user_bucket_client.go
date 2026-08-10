package bucket

// user_bucket_client 为指定租户组装最终 objstore 桶客户端：先注入 userID 路径前缀，再叠加 per-tenant S3 SSE 装饰，返回 InstrumentedBucket。

import (
	"github.com/thanos-io/objstore"
)

// NewUserBucketClient 链式调用 NewPrefixedBucketClient 与 NewSSEBucketClient；cfgProvider 可为 nil。
// NewUserBucketClient returns a bucket client to use to access the storage on behalf of the provided user.
// The cfgProvider can be nil.
func NewUserBucketClient(userID string, bucket objstore.Bucket, cfgProvider SSEConfigProvider) objstore.InstrumentedBucket {
// 第一步用 userID 作为对象键前缀，实现多租户存储隔离。
	// Inject the user/tenant prefix.
	bucket = NewPrefixedBucketClient(bucket, userID)

	// Inject the SSE config.
	return NewSSEBucketClient(userID, bucket, cfgProvider)
}
// 返回 InstrumentedBucket 接口以便上层挂载预期错误过滤与 Prometheus 指标。
