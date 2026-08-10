package bucket

// prefixed_bucket_client 在底层 objstore.Bucket 上透明添加路径前缀：实现多租户按 userID 隔离对象键，对外暴露无前缀的相对路径 API。

import (
	"context"
	"io"
	"strings"

	"github.com/thanos-io/objstore"
)

type PrefixedBucketClient struct {
	bucket objstore.Bucket
	prefix string
}

// NewPrefixedBucketClient 构造带前缀的桶装饰器，不改变底层桶的生命周期。
// NewPrefixedBucketClient returns a new PrefixedBucketClient.
func NewPrefixedBucketClient(bucket objstore.Bucket, prefix string) *PrefixedBucketClient {
	return &PrefixedBucketClient{
		bucket: bucket,
		prefix: prefix,
	}
}

// fullName 将逻辑对象名转为 prefix/DirDelim/name 形式的存储键。
func (b *PrefixedBucketClient) fullName(name string) string {
	return b.prefix + objstore.DirDelim + name
}

// Close 关闭底层 bucket 连接。
// Close implements io.Closer
func (b *PrefixedBucketClient) Close() error {
	return b.bucket.Close()
}

// Upload 在加前缀后的键上写入对象内容。
// Upload the contents of the reader as an object into the bucket.
func (b *PrefixedBucketClient) Upload(ctx context.Context, name string, r io.Reader) (err error) {
	err = b.bucket.Upload(ctx, b.fullName(name), r)
	return
}

// GetAndReplace 对前缀键执行读-改-写，委托底层 bucket 原子替换逻辑。
// GetAndReplace is a helper function that gets an object from the bucket and replaces it with a new reader.
func (b *PrefixedBucketClient) GetAndReplace(ctx context.Context, name string, fn func(existing io.ReadCloser) (io.ReadCloser, error)) error {
	return b.bucket.GetAndReplace(ctx, b.fullName(name), fn)
}

// Delete 删除前缀命名空间下的指定对象。
// Delete removes the object with the given name.
func (b *PrefixedBucketClient) Delete(ctx context.Context, name string) error {
	return b.bucket.Delete(ctx, b.fullName(name))
}

// Name 返回底层 provider 的桶名，不含租户前缀。
// Name returns the bucket name for the provider.
func (b *PrefixedBucketClient) Name() string { return b.bucket.Name() }

// SupportedIterOptions 透传底层桶支持的迭代选项类型列表。
// SupportedIterOptions returns a list of supported IterOptions by the underlying provider.
func (b *PrefixedBucketClient) SupportedIterOptions() []objstore.IterOptionType {
	return b.bucket.SupportedIterOptions()
}

// Iter 列举目录时剥离 prefix+DirDelim，回调 f 收到相对路径名。
// Iter calls f for each entry in the given directory (not recursive.). The argument to f is the full
// object name including the prefix of the inspected directory. The configured prefix will be stripped
// before supplied function is applied.
func (b *PrefixedBucketClient) Iter(ctx context.Context, dir string, f func(string) error, options ...objstore.IterOption) error {
	return b.bucket.Iter(ctx, b.fullName(dir), func(s string) error {
		return f(strings.TrimPrefix(s, b.prefix+objstore.DirDelim))
	}, options...)
}

// IterWithAttributes 同 Iter，额外传递 IterObjectAttributes 并在回调前修正 Name 前缀。
// IterWithAttributes calls f for each entry in the given directory similar to Iter.
// In addition to Name, it also includes requested object attributes in the argument to f.
//
// Attributes can be requested using IterOption.
// Not all IterOptions are supported by all providers, requesting for an unsupported option will fail with ErrOptionNotSupported.
func (b *PrefixedBucketClient) IterWithAttributes(ctx context.Context, dir string, f func(attrs objstore.IterObjectAttributes) error, options ...objstore.IterOption) error {
	return b.bucket.IterWithAttributes(ctx, b.fullName(dir), func(attrs objstore.IterObjectAttributes) error {
		attrs.Name = strings.TrimPrefix(attrs.Name, b.prefix+objstore.DirDelim)
		return f(attrs)
	}, options...)
}

// Get 按相对名读取对象，内部转换为 fullName。
// Get returns a reader for the given object name.
func (b *PrefixedBucketClient) Get(ctx context.Context, name string) (io.ReadCloser, error) {
	return b.bucket.Get(ctx, b.fullName(name))
}

// GetRange 支持字节范围读取，键名同样经 fullName 转换。
// GetRange returns a new range reader for the given object name and range.
func (b *PrefixedBucketClient) GetRange(ctx context.Context, name string, off, length int64) (io.ReadCloser, error) {
	return b.bucket.GetRange(ctx, b.fullName(name), off, length)
}

// Exists 检查前缀命名空间内对象是否存在。
// Exists checks if the given object exists in the bucket.
func (b *PrefixedBucketClient) Exists(ctx context.Context, name string) (bool, error) {
	return b.bucket.Exists(ctx, b.fullName(name))
}

// IsObjNotFoundErr 透传底层对象不存在错误判定。
// IsObjNotFoundErr returns true if error means that object is not found. Relevant to Get operations.
func (b *PrefixedBucketClient) IsObjNotFoundErr(err error) bool {
	return b.bucket.IsObjNotFoundErr(err)
}

// Attributes 返回前缀键对应的对象元数据。
// Attributes returns attributes of the specified object.
func (b *PrefixedBucketClient) Attributes(ctx context.Context, name string) (objstore.ObjectAttributes, error) {
	return b.bucket.Attributes(ctx, b.fullName(name))
}

// WithExpectedErrs 若底层实现 InstrumentedBucket 则包装预期错误过滤器并保留 prefix。
// WithExpectedErrs allows to specify a filter that marks certain errors as expected, so it will not increment
// thanos_objstore_bucket_operation_failures_total metric.
func (b *PrefixedBucketClient) ReaderWithExpectedErrs(fn objstore.IsOpFailureExpectedFunc) objstore.BucketReader {
	return b.WithExpectedErrs(fn)
}

// IsAccessDeniedErr 透传访问拒绝错误判定。
// IsAccessDeniedErr returns true if access to object is denied.
func (b *PrefixedBucketClient) IsAccessDeniedErr(err error) bool {
	return b.bucket.IsAccessDeniedErr(err)
}

// Provider 返回底层 objstore 供应商标识。
// Provider returns the provider of the bucket.
func (b *PrefixedBucketClient) Provider() objstore.ObjProvider {
	return b.bucket.Provider()
}

// ReaderWithExpectedErrs allows to specify a filter that marks certain errors as expected, so it will not increment
// thanos_objstore_bucket_operation_failures_total metric.
func (b *PrefixedBucketClient) WithExpectedErrs(fn objstore.IsOpFailureExpectedFunc) objstore.Bucket {
	if ib, ok := b.bucket.(objstore.InstrumentedBucket); ok {
		return &PrefixedBucketClient{
			bucket: ib.WithExpectedErrs(fn),
			prefix: b.prefix,
		}
	}
	return b
}
// ReaderWithExpectedErrs 与 WithExpectedErrs 成对出现，前者为 objstore 接口别名方法。
