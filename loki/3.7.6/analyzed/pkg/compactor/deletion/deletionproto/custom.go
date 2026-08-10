package deletionproto

// deletionproto 自定义扩展：删除请求状态常量与 Chunk 访问器，
// 桥接 protobuf 生成类型与 Prometheus model.Time 等内部类型。

import "github.com/prometheus/common/model"

const (
	StatusReceived  DeleteRequestStatus = "received"
	StatusProcessed DeleteRequestStatus = "processed"
)

type (
	DeleteRequestStatus string
)

func (s DeleteRequestStatus) Equal(status DeleteRequestStatus) bool {
	return s == status
}

// GetFrom 返回 chunk 起始时间戳（Prometheus model.Time）。
func (c *Chunk) GetFrom() model.Time {
	return c.From
}

func (c *Chunk) GetThrough() model.Time {
	return c.Through
}

func (c *Chunk) GetSize() uint32 {
	return c.KB
}

func (c *Chunk) GetEntriesCount() uint32 {
	return c.Entries
}
