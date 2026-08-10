package executor

// pipeline_utils：BufferedPipeline 将固定 RecordBatch 序列作为只读 Pipeline 供测试注入。

import (
	"context"

	"github.com/apache/arrow-go/v18/arrow"
)

// BufferedPipeline 按序返回 records，current 初值为 -1 以便首次 Read 读到 index 0。
// BufferedPipeline is a pipeline implementation that reads from a fixed set of Arrow records.
// It implements the Pipeline interface and serves as a simple source for testing and data injection.
type BufferedPipeline struct {
	records []arrow.RecordBatch
	current int
}

// NewBufferedPipeline 接受可变参数 record 列表，Open 为空操作。
// NewBufferedPipeline creates a new BufferedPipeline from a set of Arrow records.
// The pipeline will return these records in sequence.
func NewBufferedPipeline(records ...arrow.RecordBatch) *BufferedPipeline {
	return &BufferedPipeline{
		records: records,
		current: -1, // Start before the first record
	}
}

// Open implements Pipeline.
func (p *BufferedPipeline) Open(_ context.Context) error { return nil }

// Read 递增 current，超出长度返回 EOF；调用方负责释放已读 batch。
// Read implements Pipeline.
// It advances to the next record and returns EOF when all records have been read.
func (p *BufferedPipeline) Read(_ context.Context) (arrow.RecordBatch, error) {
	p.current++
	if p.current >= len(p.records) {
		return nil, EOF
	}

	// Get the next record. The caller is responsible for releasing it.
	return p.records[p.current], nil
}

// Close 将 records 置 nil 以释放未读 batch 的引用。
// Close implements Pipeline. It releases all unreturned records.
func (p *BufferedPipeline) Close() {
	p.records = nil
}
// BufferedPipeline 不参与 xcap 观测，常用于单元测试与本地 pipeline 调试。
