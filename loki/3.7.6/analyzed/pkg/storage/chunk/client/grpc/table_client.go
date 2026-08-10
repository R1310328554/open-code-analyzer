package grpc

// grpc 包提供基于 gRPC 的索引表管理客户端，将 TableClient 接口映射到远程 GrpcStore 服务，用于分布式场景下创建、列举、描述、更新与删除索引表。

import (
	"context"

	"github.com/golang/protobuf/ptypes/empty"
	"github.com/pkg/errors"
	"google.golang.org/grpc"

	"github.com/grafana/loki/v3/pkg/storage/config"
)

type TableClient struct {
	client GrpcStoreClient
	conn   *grpc.ClientConn
}

// NewTableClient returns a new TableClient.
// NewTableClient 连接 cfg.Address 指定的 gRPC 服务端并返回可操作的表客户端。
func NewTableClient(cfg Config) (*TableClient, error) {
	grpcClient, conn, err := connectToGrpcServer(cfg.Address)
	if err != nil {
		return nil, err
	}
	client := &TableClient{
		client: grpcClient,
		conn:   conn,
	}
	return client, nil
}

// ListTables 调用远程 ListTables RPC，返回当前所有索引表名称。
func (c *TableClient) ListTables(ctx context.Context) ([]string, error) {
	tables, err := c.client.ListTables(ctx, &empty.Empty{})
	if err != nil {
		return nil, errors.WithStack(err)
	}
	return tables.TableNames, nil
}

func (c *TableClient) DeleteTable(ctx context.Context, name string) error {
	tableName := &DeleteTableRequest{TableName: name}
	_, err := c.client.DeleteTable(ctx, tableName)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

// DescribeTable 获取表配置（读写容量、按需 IO、标签）及是否处于活跃状态。
func (c *TableClient) DescribeTable(ctx context.Context, name string) (desc config.TableDesc, isActive bool, err error) {
	tableName := &DescribeTableRequest{TableName: name}
	tableDesc, err := c.client.DescribeTable(ctx, tableName)
	if err != nil {
		return desc, false, errors.WithStack(err)
	}
	desc.Name = tableDesc.Desc.Name
	desc.ProvisionedRead = tableDesc.Desc.ProvisionedRead
	desc.ProvisionedWrite = tableDesc.Desc.ProvisionedWrite
	desc.UseOnDemandIOMode = tableDesc.Desc.UseOnDemandIOMode
	desc.Tags = tableDesc.Desc.Tags
	return desc, tableDesc.IsActive, nil
}

// UpdateTable 以乐观并发方式提交 current/expected 表描述，防止并发覆盖。
func (c *TableClient) UpdateTable(ctx context.Context, current, expected config.TableDesc) error {
	currentTable := &TableDesc{}
	expectedTable := &TableDesc{}

	currentTable.Name = current.Name
	currentTable.UseOnDemandIOMode = current.UseOnDemandIOMode
	currentTable.ProvisionedWrite = current.ProvisionedWrite
	currentTable.ProvisionedRead = current.ProvisionedRead
	currentTable.Tags = current.Tags

	expectedTable.Name = expected.Name
	expectedTable.UseOnDemandIOMode = expected.UseOnDemandIOMode
	expectedTable.ProvisionedWrite = expected.ProvisionedWrite
	expectedTable.ProvisionedRead = expected.ProvisionedRead
	expectedTable.Tags = expected.Tags

	updateTableRequest := &UpdateTableRequest{
		Current:  currentTable,
		Expected: expectedTable,
	}
	_, err := c.client.UpdateTable(ctx, updateTableRequest)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

func (c *TableClient) CreateTable(ctx context.Context, desc config.TableDesc) error {
	req := &CreateTableRequest{}
	req.Desc = &TableDesc{}
	req.Desc.Name = desc.Name
	req.Desc.ProvisionedRead = desc.ProvisionedRead
	req.Desc.ProvisionedWrite = desc.ProvisionedWrite
	req.Desc.Tags = desc.Tags
	req.Desc.UseOnDemandIOMode = desc.UseOnDemandIOMode

	_, err := c.client.CreateTable(ctx, req)
	if err != nil {
		return errors.WithStack(err)
	}
	return nil
}

// Stop 关闭底层 gRPC 连接，释放网络资源。
func (c *TableClient) Stop() {
	c.conn.Close()
}
// 远程表操作均通过 protobuf 消息传递，错误统一经 errors.WithStack 包装便于追踪。
