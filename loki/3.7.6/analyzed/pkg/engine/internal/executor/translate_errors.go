package executor

// translate_errors 在 executor.EOF 与标准 io.EOF 之间转换，统一 pipeline 边界错误语义。

import (
	"context"
	"errors"
	"io"

	"github.com/apache/arrow-go/v18/arrow"
)

func TranslateEOF(pipeline Pipeline) Pipeline {
	return translateEOFPipeline{pipeline}
}

type translateEOFPipeline struct {
	pipeline Pipeline
}

func (p translateEOFPipeline) Open(ctx context.Context) error {
	return p.pipeline.Open(ctx)
}

func (p translateEOFPipeline) Close() {
	p.pipeline.Close()
}

func (p translateEOFPipeline) Read(ctx context.Context) (arrow.RecordBatch, error) {
	rec, err := p.pipeline.Read(ctx)
	return rec, translateEOF(err, false)
}

// translateEOF 双向映射：toExecutor 为 true 时 io.EOF→executor.EOF，否则反向。
func translateEOF(err error, toExecutor bool) error {
	if toExecutor {
		// io.EOF to executor.EOF
		if errors.Is(err, io.EOF) {
			err = EOF
		}
	}
	if !toExecutor {
		// executor.EOF to io.EOF
		if errors.Is(err, EOF) {
			err = io.EOF
		}
	}

	return err
}
// translateEOFPipeline 透传 Open/Close，仅拦截 Read 返回的错误类型。
