package noop

// noop 包提供空实现 ObjectClient，用于禁用对象存储或测试场景：写操作静默成功，读操作返回错误，List 恒为空。

import (
	"context"
	"io"
	"time"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
)

// ObjectClient holds config for filesystem as object store
type ObjectClient struct {
}

// NewNoopObjectClient makes a chunk.Client which stores chunks as files in the local filesystem.
// NewNoopObjectClient 返回可用但无实际存储能力的客户端实例。
func NewNoopObjectClient() (*ObjectClient, error) {
	return &ObjectClient{}, nil
}

// Stop implements ObjectClient
func (ObjectClient) Stop() {}

// GetObject from the store
// GetObject 始终失败并提示 noop storage 无法返回对象。
func (f *ObjectClient) GetObject(_ context.Context, _ string) (io.ReadCloser, int64, error) {
	return nil, 0, errors.New("noop storage cannot return any objects")
}

func (f *ObjectClient) GetObjectRange(_ context.Context, _ string, _, _ int64) (io.ReadCloser, error) {
	return nil, errors.New("noop storage cannot return any objects")
}

// PutObject into the store
func (f *ObjectClient) PutObject(_ context.Context, _ string, _ io.Reader) error {
	return nil
}

// List implements chunk.ObjectClient.
// ObjectClient assumes that prefix is a directory, and only supports "" and "/" delimiters.
func (f *ObjectClient) List(_ context.Context, _, _ string) ([]client.StorageObject, []client.StorageCommonPrefix, error) {
	return []client.StorageObject{}, []client.StorageCommonPrefix{}, nil
}

func (f *ObjectClient) DeleteObject(_ context.Context, _ string) error {
	return nil
}

// DeleteChunksBefore implements BucketClient
func (f *ObjectClient) DeleteChunksBefore(_ context.Context, _ time.Time) error {
	return nil
}

// IsObjectNotFoundErr returns true if error means that object is not found. Relevant to GetObject and DeleteObject operations.
// IsObjectNotFoundErr 恒为 true，使上层将任意读错误视为对象不存在。
func (f *ObjectClient) IsObjectNotFoundErr(_ error) bool {
	return true
}

func (f *ObjectClient) ObjectExists(_ context.Context, _ string) (bool, error) {
	return false, nil
}

func (f *ObjectClient) IsRetryableErr(_ error) bool {
	return false
}

// GetAttributes implements ObjectClient
func (f *ObjectClient) GetAttributes(_ context.Context, _ string) (client.ObjectAttributes, error) {
	return client.ObjectAttributes{}, nil
}
// PutObject/DeleteObject 等写路径返回 nil，便于在配置缺失后端时启动流程不中断。
