package worker

// errorPipeline 将多个错误合并为 executor.Pipeline，在绑定外部输入失败等场景下替代正常数据管道，避免写入端永久阻塞。

import (
	"context"
	"errors"

	"github.com/apache/arrow-go/v18/arrow"

	"github.com/grafana/loki/v3/pkg/engine/internal/executor"
)

type errorPipeline []error

var _ executor.Pipeline = errorPipeline(nil)

// Open 空实现：错误管道无需初始化底层资源。
func (ep errorPipeline) Open(_ context.Context) error { return nil }

func (ep errorPipeline) Read(_ context.Context) (arrow.RecordBatch, error) {
	return nil, errors.Join(ep...)
}

func (ep errorPipeline) Close() {}
// 典型用法：thread 绑定 streamSource 失败时返回 errorPipeline 而非 nodeSource。
