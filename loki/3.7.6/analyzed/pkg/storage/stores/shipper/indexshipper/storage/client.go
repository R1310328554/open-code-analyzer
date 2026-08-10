package storage

// storage client 封装 boltdb-shipper 索引在对象存储上的读写：区分公共索引与租户索引，并通过 cachedObjectClient 缓存列举结果。

import (
	"context"
	"io"
	"path"
	"strings"
	"time"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
)

const delimiter = "/"

// UserIndexClient allows doing operations on the object store for user specific index.
// UserIndexClient 定义租户专属索引文件在对象存储上的 CRUD 与列举接口。
type UserIndexClient interface {
	ListUserFiles(ctx context.Context, tableName, userID string, bypassCache bool) ([]IndexFile, error)
	GetUserFile(ctx context.Context, tableName, userID, fileName string) (io.ReadCloser, error)
	PutUserFile(ctx context.Context, tableName, userID, fileName string, file io.Reader) error
	DeleteUserFile(ctx context.Context, tableName, userID, fileName string) error
}

// CommonIndexClient allows doing operations on the object store for common index.
type CommonIndexClient interface {
	ListFiles(ctx context.Context, tableName string, bypassCache bool) ([]IndexFile, []string, error)
	GetFile(ctx context.Context, tableName, fileName string) (io.ReadCloser, error)
	PutFile(ctx context.Context, tableName, fileName string, file io.Reader) error
	DeleteFile(ctx context.Context, tableName, fileName string) error
}

// Client 组合 CommonIndexClient 与 UserIndexClient，并暴露表级缓存刷新与 NotFound 判定。
// Client is used to manage boltdb index files in object storage, when using boltdb-shipper.
type Client interface {
	CommonIndexClient
	UserIndexClient

	RefreshIndexTableNamesCache(ctx context.Context)
	ListTables(ctx context.Context) ([]string, error)
	RefreshIndexTableCache(ctx context.Context, tableName string)
	IsFileNotFoundErr(err error) bool
	Stop()
}

type indexStorageClient struct {
	objectClient *cachedObjectClient
}

type IndexFile struct {
	Name       string
	ModifiedAt time.Time
}

// NewIndexStorageClient 用 storagePrefix 包装原始 ObjectClient 并注入 cachedObjectClient。
func NewIndexStorageClient(origObjectClient client.ObjectClient, storagePrefix string) Client {
	objectClient := newCachedObjectClient(client.NewPrefixedObjectClient(origObjectClient, storagePrefix))
	return &indexStorageClient{objectClient: objectClient}
}

func (s *indexStorageClient) RefreshIndexTableNamesCache(ctx context.Context) {
	s.objectClient.RefreshIndexTableNamesCache(ctx)
}

func (s *indexStorageClient) RefreshIndexTableCache(ctx context.Context, tableName string) {
	s.objectClient.RefreshIndexTableCache(ctx, tableName)
}

func (s *indexStorageClient) ListTables(ctx context.Context) ([]string, error) {
	_, tables, err := s.objectClient.List(ctx, "", delimiter, false)
	if err != nil {
		return nil, err
	}

	tableNames := make([]string, 0, len(tables))
	for _, table := range tables {
		tableNames = append(tableNames, path.Base(string(table)))
	}

	return tableNames, nil
}

// ListFiles 列举表根目录下公共索引文件，并返回租户子目录名列表。
func (s *indexStorageClient) ListFiles(ctx context.Context, tableName string, bypassCache bool) ([]IndexFile, []string, error) {
	// The forward slash here needs to stay because we are trying to list contents of a directory without which
	// we will get the name of the same directory back with hosted object stores.
	// This is due to the object stores not having a concept of directories.
	objects, users, err := s.objectClient.List(ctx, tableName+delimiter, delimiter, bypassCache)
	if err != nil {
		return nil, nil, err
	}

	files := make([]IndexFile, 0, len(objects))
	for _, object := range objects {
		// The s3 client can also return the directory itself in the ListObjects.
		if strings.HasSuffix(object.Key, delimiter) {
			continue
		}
		files = append(files, IndexFile{
			Name:       path.Base(object.Key),
			ModifiedAt: object.ModifiedAt,
		})
	}

	userIDs := make([]string, 0, len(users))
	for _, user := range users {
		userIDs = append(userIDs, path.Base(string(user)))
	}

	return files, userIDs, nil
}

func (s *indexStorageClient) ListUserFiles(ctx context.Context, tableName, userID string, bypassCache bool) ([]IndexFile, error) {
	// The forward slash here needs to stay because we are trying to list contents of a directory without which
	// we will get the name of the same directory back with hosted object stores.
	// This is due to the object stores not having a concept of directories.
	objects, _, err := s.objectClient.List(ctx, path.Join(tableName, userID)+delimiter, delimiter, bypassCache)
	if err != nil {
		return nil, err
	}

	files := make([]IndexFile, 0, len(objects))
	for _, object := range objects {
		// The s3 client can also return the directory itself in the ListObjects.
		if strings.HasSuffix(object.Key, delimiter) {
			continue
		}
		files = append(files, IndexFile{
			Name:       path.Base(object.Key),
			ModifiedAt: object.ModifiedAt,
		})
	}

	return files, nil
}

func (s *indexStorageClient) GetFile(ctx context.Context, tableName, fileName string) (io.ReadCloser, error) {
	reader, _, err := s.objectClient.GetObject(ctx, path.Join(tableName, fileName))
	return reader, err
}

func (s *indexStorageClient) GetUserFile(ctx context.Context, tableName, userID, fileName string) (io.ReadCloser, error) {
	readCloser, _, err := s.objectClient.GetObject(ctx, path.Join(tableName, userID, fileName))
	return readCloser, err
}

func (s *indexStorageClient) PutFile(ctx context.Context, tableName, fileName string, file io.Reader) error {
	return s.objectClient.PutObject(ctx, path.Join(tableName, fileName), file)
}

func (s *indexStorageClient) PutUserFile(ctx context.Context, tableName, userID, fileName string, file io.Reader) error {
	return s.objectClient.PutObject(ctx, path.Join(tableName, userID, fileName), file)
}

func (s *indexStorageClient) DeleteFile(ctx context.Context, tableName, fileName string) error {
	return s.objectClient.DeleteObject(ctx, path.Join(tableName, fileName))
}

func (s *indexStorageClient) DeleteUserFile(ctx context.Context, tableName, userID, fileName string) error {
	return s.objectClient.DeleteObject(ctx, path.Join(tableName, userID, fileName))
}

func (s *indexStorageClient) IsFileNotFoundErr(err error) bool {
	return s.objectClient.IsObjectNotFoundErr(err)
}

func (s *indexStorageClient) Stop() {
	s.objectClient.Stop()
}
// Stop 关闭底层 cachedObjectClient，释放表名与对象列表的内存缓存。
