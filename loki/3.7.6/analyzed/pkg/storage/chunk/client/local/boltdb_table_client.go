package local

// TableClient 提供本地 BoltDB 索引表的文件级管理：列举目录下 .boltdb 文件、创建空表文件、删除表及返回静态 TableDesc。

import (
	"context"
	"os"
	"path/filepath"

	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

type TableClient struct {
	directory string
}

// NewTableClient returns a new TableClient.
// NewTableClient 返回指向指定目录的轻量表管理客户端。
func NewTableClient(directory string) (index.TableClient, error) {
	return &TableClient{directory: directory}, nil
}

// ListTables 递归 Walk 目录，收集所有非目录文件名作为表名。
func (c *TableClient) ListTables(_ context.Context) ([]string, error) {
	boltDbFiles := []string{}
	err := filepath.Walk(c.directory, func(_ string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		if !info.IsDir() {
			boltDbFiles = append(boltDbFiles, info.Name())
		}
		return nil
	})
	if err != nil {
		return nil, err
	}
	return boltDbFiles, nil
}

// CreateTable 以 O_CREATE|O_RDONLY 创建空文件，若已存在则不报错。
func (c *TableClient) CreateTable(_ context.Context, desc config.TableDesc) error {
	file, err := os.OpenFile(filepath.Join(c.directory, desc.Name), os.O_CREATE|os.O_RDONLY, 0o666)
	if err != nil {
		return err
	}

	return file.Close()
}

func (c *TableClient) DeleteTable(_ context.Context, name string) error {
	return os.Remove(filepath.Join(c.directory, name))
}

func (c *TableClient) DescribeTable(_ context.Context, name string) (desc config.TableDesc, isActive bool, err error) {
	return config.TableDesc{
		Name: name,
	}, true, nil
}

// UpdateTable 本地实现为空操作，BoltDB 无动态容量概念。
func (c *TableClient) UpdateTable(_ context.Context, _, _ config.TableDesc) error {
	return nil
}

func (*TableClient) Stop() {}
// DescribeTable 始终返回 isActive=true，ProvisionedRead/Write 由索引客户端侧忽略。
