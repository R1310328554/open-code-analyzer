package chunk

// dummyChunk 是 Encoding(0) 占位实现，用于测试场景下不关心 chunk 实际内容的场合。

import (
	"io"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util/filter"
)

func newDummyChunk() *dummyChunk {
	return &dummyChunk{}
}

// dummyChunk 实现 chunk.Data 接口，所有方法为空操作或返回零值。
// dummyChunk implements chunk.Data
// It is a placeholder chunk with Encoding(0)
// It can be used in tests where the content of a chunk is irrelevant.
type dummyChunk struct{}

func (chk *dummyChunk) Add(sample model.SamplePair) (Data, error) {
	return nil, nil
}

func (chk *dummyChunk) Marshal(io.Writer) error {
	return nil
}

func (chk *dummyChunk) UnmarshalFromBuf([]byte) error {
	return nil
}

// Encoding 返回 Dummy 编码类型，与 factory.go 中注册的 dummy 编码一致。
func (chk *dummyChunk) Encoding() Encoding {
	return Dummy
}

func (chk *dummyChunk) Rewrite(filter.Func) (Data, error) {
	return nil, nil
}

func (chk *dummyChunk) Size() int {
	return 0
}

func (chk *dummyChunk) UncompressedSize() int {
	return 0
}

func (chk *dummyChunk) Entries() int {
	return 0
}

func (chk *dummyChunk) Utilization() float64 {
	return 0
}
// Rewrite/Add 均返回 nil 数据，Size 与 Entries 恒为 0，Utilization 为 0。
