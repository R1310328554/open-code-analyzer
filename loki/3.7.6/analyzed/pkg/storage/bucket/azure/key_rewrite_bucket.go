package azure

// key_rewrite_bucket 包装 objstore.Bucket，在所有对象键上将冒号替换为配置的 delimiter，因 Azure 键名不支持冒号字符。

import (
	"context"
	"io"
	"strings"

	"github.com/thanos-io/objstore"
)

// keyRewriteBucket 嵌入 Bucket 接口，rewriteKey 对 Get/Upload/Delete 等路径统一改写。
// keyRewriteBucket wraps a bucket and replaces ":" with a configured delimiter in all object keys.
type keyRewriteBucket struct {
	objstore.Bucket
	delimiter string
}

func (b *keyRewriteBucket) rewriteKey(key string) string {
	return strings.ReplaceAll(key, ":", b.delimiter)
}

func (b *keyRewriteBucket) Get(ctx context.Context, name string) (io.ReadCloser, error) {
	return b.Bucket.Get(ctx, b.rewriteKey(name))
}

func (b *keyRewriteBucket) GetRange(ctx context.Context, name string, off, length int64) (io.ReadCloser, error) {
	return b.Bucket.GetRange(ctx, b.rewriteKey(name), off, length)
}

func (b *keyRewriteBucket) Exists(ctx context.Context, name string) (bool, error) {
	return b.Bucket.Exists(ctx, b.rewriteKey(name))
}

func (b *keyRewriteBucket) Attributes(ctx context.Context, name string) (objstore.ObjectAttributes, error) {
	return b.Bucket.Attributes(ctx, b.rewriteKey(name))
}

func (b *keyRewriteBucket) Upload(ctx context.Context, name string, r io.Reader) error {
	return b.Bucket.Upload(ctx, b.rewriteKey(name), r)
}

func (b *keyRewriteBucket) Delete(ctx context.Context, name string) error {
	return b.Bucket.Delete(ctx, b.rewriteKey(name))
}
// GetRange、Exists、Attributes 与 Upload 均在调用底层前改写对象名保持一致性。
