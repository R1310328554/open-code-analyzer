package client

// PrefixedObjectClient 装饰器为所有 object key 与 List 前缀统一添加固定前缀，便于多租户或路径隔离场景下复用同一底层 ObjectClient。

import (
	"context"
	"io"
	"strings"
)

type PrefixedObjectClient struct {
	downstreamClient ObjectClient
	prefix           string
}

// NewPrefixedObjectClient 返回实现 ObjectClient 的前缀包装客户端。
func NewPrefixedObjectClient(downstreamClient ObjectClient, prefix string) ObjectClient {
	return PrefixedObjectClient{downstreamClient: downstreamClient, prefix: prefix}
}

func (p PrefixedObjectClient) PutObject(ctx context.Context, objectKey string, object io.Reader) error {
	return p.downstreamClient.PutObject(ctx, p.prefix+objectKey, object)
}

func (p PrefixedObjectClient) ObjectExists(ctx context.Context, objectKey string) (bool, error) {
	return p.downstreamClient.ObjectExists(ctx, p.prefix+objectKey)
}

func (p PrefixedObjectClient) GetAttributes(ctx context.Context, objectKey string) (ObjectAttributes, error) {
	return p.downstreamClient.GetAttributes(ctx, p.prefix+objectKey)
}

func (p PrefixedObjectClient) GetObject(ctx context.Context, objectKey string) (io.ReadCloser, int64, error) {
	return p.downstreamClient.GetObject(ctx, p.prefix+objectKey)
}

func (p PrefixedObjectClient) GetObjectRange(ctx context.Context, objectKey string, offset, length int64) (io.ReadCloser, error) {
	return p.downstreamClient.GetObjectRange(ctx, p.prefix+objectKey, offset, length)
}

// List 向下游传递 p.prefix+prefix，返回前剥离包装前缀以保持键语义一致。
func (p PrefixedObjectClient) List(ctx context.Context, prefix, delimiter string) ([]StorageObject, []StorageCommonPrefix, error) {
	objects, commonPrefixes, err := p.downstreamClient.List(ctx, p.prefix+prefix, delimiter)
	if err != nil {
		return nil, nil, err
	}

	for i := range objects {
		objects[i].Key = strings.TrimPrefix(objects[i].Key, p.prefix)
	}

	for i := range commonPrefixes {
		commonPrefixes[i] = StorageCommonPrefix(strings.TrimPrefix(string(commonPrefixes[i]), p.prefix))
	}

	return objects, commonPrefixes, nil
}

func (p PrefixedObjectClient) DeleteObject(ctx context.Context, objectKey string) error {
	return p.downstreamClient.DeleteObject(ctx, p.prefix+objectKey)
}

func (p PrefixedObjectClient) IsObjectNotFoundErr(err error) bool {
	return p.downstreamClient.IsObjectNotFoundErr(err)
}

func (p PrefixedObjectClient) IsRetryableErr(err error) bool {
	return p.downstreamClient.IsRetryableErr(err)
}

func (p PrefixedObjectClient) Stop() {
	p.downstreamClient.Stop()
}

// GetDownstream 暴露内层客户端，供需要绕过前缀的运维或测试场景使用。
func (p PrefixedObjectClient) GetDownstream() ObjectClient {
	return p.downstreamClient
}

func (p PrefixedObjectClient) GetPrefix() string {
	return p.prefix
}
// 所有 CRUD 与 Exists/GetAttributes 均在键前追加 prefix，Stop 委托下游关闭。
