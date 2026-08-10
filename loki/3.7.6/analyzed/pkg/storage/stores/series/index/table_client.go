package index

// table_client 定义 DynamoDB 等后端索引表的 DDL 操作接口：列举、创建、描述、更新与删除表。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/storage/config"
)

// TableClient 抽象周期性索引表的 provisioned/on-demand 容量管理。
// TableClient is a client for telling Dynamo what to do with tables.
type TableClient interface {
	ListTables(ctx context.Context) ([]string, error)
	CreateTable(ctx context.Context, desc config.TableDesc) error
	DeleteTable(ctx context.Context, name string) error
	DescribeTable(ctx context.Context, name string) (desc config.TableDesc, isActive bool, err error)
	UpdateTable(ctx context.Context, current, expected config.TableDesc) error
	Stop()
}

// byName 按表名字典序排序 TableDesc 切片，供 TableManager 期望表列表对齐。
type byName []config.TableDesc

func (a byName) Len() int           { return len(a) }
func (a byName) Swap(i, j int)      { a[i], a[j] = a[j], a[i] }
func (a byName) Less(i, j int) bool { return a[i].Name < a[j].Name }
// DescribeTable 返回当前表描述与 ACTIVE 状态，UpdateTable 在期望与现状不一致时调整吞吐。
