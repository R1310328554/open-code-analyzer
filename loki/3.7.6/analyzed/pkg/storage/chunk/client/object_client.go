package client

// 本文件定义 ObjectClient 接口及基于对象存储的 chunk.Client 实现：KeyEncoder 编码块键，client 结构体并行 Put/Get 并解码 chunk 数据。

import (
	"bytes"
	"context"
	"encoding/base64"
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/storage/chunk"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/util"
	"github.com/grafana/loki/v3/pkg/storage/config"
)

// ObjectAttributes 描述对象元数据，当前仅包含 Size 字节长度。
type ObjectAttributes struct {
	Size int64
}

// ObjectClient 抽象 S3/GCS/Azure 等后端的对象 CRUD、列举与存在性检查。
// ObjectClient is used to store arbitrary data in Object Store (S3/GCS/Azure/...)
type ObjectClient interface {
	ObjectExists(ctx context.Context, objectKey string) (bool, error)
	GetAttributes(ctx context.Context, objectKey string) (ObjectAttributes, error)

	PutObject(ctx context.Context, objectKey string, object io.Reader) error
	// NOTE: The consumer of GetObject should always call the Close method when it is done reading which otherwise could cause a resource leak.
	GetObject(ctx context.Context, objectKey string) (io.ReadCloser, int64, error)
	GetObjectRange(ctx context.Context, objectKey string, off, length int64) (io.ReadCloser, error)

	// List objects with given prefix.
	//
	// If delimiter is empty, all objects are returned, even if they are in nested in "subdirectories".
	// If delimiter is not empty, it is used to compute common prefixes ("subdirectories"),
	// and objects containing delimiter in the name will not be returned in the result.
	//
	// For example, if the prefix is "notes/" and the delimiter is a slash (/) as in "notes/summer/july", the common prefix is "notes/summer/".
	// Common prefixes will always end with passed delimiter.
	//
	// Keys of returned storage objects have given prefix.
	List(ctx context.Context, prefix string, delimiter string) ([]StorageObject, []StorageCommonPrefix, error)
	DeleteObject(ctx context.Context, objectKey string) error
	IsObjectNotFoundErr(err error) bool
	IsRetryableErr(err error) bool
	Stop()
}

// StorageObject represents an object being stored in an Object Store
type StorageObject struct {
	Key        string
	ModifiedAt time.Time
}

// StorageCommonPrefix represents a common prefix aka a synthetic directory in Object Store.
// It is guaranteed to always end with delimiter passed to List method.
type StorageCommonPrefix string

// KeyEncoder is used to encode chunk keys before writing/retrieving chunks
// from the underlying ObjectClient
// Schema/Chunk are passed as arguments to allow this to improve over revisions
// KeyEncoder 按 schema 版本将 chunk 外部键编码为对象存储路径（如 base64 或目录结构）。
type KeyEncoder func(schema config.SchemaConfig, chk chunk.Chunk) string

// base64Encoder is used to encode chunk keys in base64 before storing/retrieving
// them from the ObjectClient
var base64Encoder = func(key string) string {
	return base64.StdEncoding.EncodeToString([]byte(key))
}

// FSEncoder v12+ 保留目录层级仅对末段 base64 编码，改善大规模文件系统性能。
var FSEncoder = func(schema config.SchemaConfig, chk chunk.Chunk) string {
	// Filesystem encoder pre-v12 encodes the chunk as one base64 string.
	// This has the downside of making them opaque and storing all chunks in a single
	// directory, hurting performance at scale and discoverability.
	// Post v12, we respect the directory structure imposed by chunk keys.
	key := schema.ExternalKey(chk.ChunkRef)
	if schema.VersionForChunk(chk.ChunkRef) > 11 {
		split := strings.LastIndexByte(key, '/')
		encodedTail := base64Encoder(key[split+1:])
		return strings.Join([]string{key[:split], encodedTail}, "/")

	}
	return base64Encoder(key)
}

const defaultMaxParallel = 150

// client is used to store chunks in object store backends
// client 内嵌 ObjectClient 与 KeyEncoder，getChunkMaxParallel 控制并发 GetChunks。
type client struct {
	store               ObjectClient
	keyEncoder          KeyEncoder
	getChunkMaxParallel int
	schema              config.SchemaConfig
}

// NewClient wraps the provided ObjectClient with a chunk.Client implementation
func NewClient(store ObjectClient, encoder KeyEncoder, schema config.SchemaConfig) Client {
	return NewClientWithMaxParallel(store, encoder, defaultMaxParallel, schema)
}

func NewClientWithMaxParallel(store ObjectClient, encoder KeyEncoder, maxParallel int, schema config.SchemaConfig) Client {
	return &client{
		store:               store,
		keyEncoder:          encoder,
		getChunkMaxParallel: maxParallel,
		schema:              schema,
	}
}

// Stop shuts down the object store and any underlying clients
func (o *client) Stop() {
	o.store.Stop()
}

// PutChunks stores the provided chunks in the configured backend. If multiple errors are
// returned, the last one sequentially will be propagated up.
// PutChunks 并行 goroutine 上传各编码块，仅传播最后一个错误。
func (o *client) PutChunks(ctx context.Context, chunks []chunk.Chunk) error {
	var (
		chunkKeys []string
		chunkBufs [][]byte
	)

	for i := range chunks {
		buf, err := chunks[i].Encoded()
		if err != nil {
			return err
		}

		var key string
		if o.keyEncoder != nil {
			key = o.keyEncoder(o.schema, chunks[i])
		} else {
			key = o.schema.ExternalKey(chunks[i].ChunkRef)
		}

		chunkKeys = append(chunkKeys, key)
		chunkBufs = append(chunkBufs, buf)
	}

	incomingErrors := make(chan error)
	for i := range chunkBufs {
		go func(i int) {
			incomingErrors <- o.store.PutObject(ctx, chunkKeys[i], bytes.NewReader(chunkBufs[i]))
		}(i)
	}

	var lastErr error
	for range chunkKeys {
		err := <-incomingErrors
		if err != nil {
			lastErr = err
		}
	}
	return lastErr
}

// GetChunks retrieves the specified chunks from the configured backend
func (o *client) GetChunks(ctx context.Context, chunks []chunk.Chunk) ([]chunk.Chunk, error) {
	getChunkMaxParallel := o.getChunkMaxParallel
	if getChunkMaxParallel == 0 {
		getChunkMaxParallel = defaultMaxParallel
	}
	return util.GetParallelChunks(ctx, getChunkMaxParallel, chunks, o.getChunk)
}

// getChunk 读取对象字节流并 Decode，调用方须 Close ReadCloser 避免泄漏。
func (o *client) getChunk(ctx context.Context, decodeContext *chunk.DecodeContext, c chunk.Chunk) (chunk.Chunk, error) {
	if ctx.Err() != nil {
		return chunk.Chunk{}, ctx.Err()
	}

	key := o.schema.ExternalKey(c.ChunkRef)
	if o.keyEncoder != nil {
		key = o.keyEncoder(o.schema, c)
	}

	readCloser, size, err := o.store.GetObject(ctx, key)
	if err != nil {
		return chunk.Chunk{}, errors.WithStack(errors.Wrapf(err, "failed to load chunk '%s'", key))
	}

	if readCloser == nil {
		return chunk.Chunk{}, errors.New("object client getChunk fail because object is nil")
	}
	defer readCloser.Close()

	// reset if the size is unknown
	// start with a buf of size bytes.MinRead since we cannot avoid allocations
	if size < 0 {
		size = 0
	}

	// adds bytes.MinRead to avoid allocations when the size is known.
	// This is because ReadFrom reads bytes.MinRead by bytes.MinRead.
	buf := bytes.NewBuffer(make([]byte, 0, size+bytes.MinRead))
	_, err = buf.ReadFrom(readCloser)
	if err != nil {
		return chunk.Chunk{}, errors.WithStack(err)
	}

	if err := c.Decode(decodeContext, buf.Bytes()); err != nil {
		return chunk.Chunk{}, errors.WithStack(
			fmt.Errorf(
				"failed to decode chunk '%s' for tenant `%s`: %w",
				key,
				c.UserID,
				err,
			),
		)
	}
	return c, nil
}

// GetChunks retrieves the specified chunks from the configured backend
func (o *client) DeleteChunk(ctx context.Context, userID, chunkID string) error {
	key := chunkID
	if o.keyEncoder != nil {
		c, err := chunk.ParseExternalKey(userID, key)
		if err != nil {
			return err
		}
		key = o.keyEncoder(o.schema, c)
	}
	return o.store.DeleteObject(ctx, key)
}

func (o *client) IsChunkNotFoundErr(err error) bool {
	return o.store.IsObjectNotFoundErr(err)
}

func (o *client) IsRetryableErr(err error) bool {
	return o.store.IsRetryableErr(err)
}
// defaultMaxParallel 为 150；NewClient 是 NewClientWithMaxParallel 的便捷封装。
