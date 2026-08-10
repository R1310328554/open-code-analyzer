package stages

import (
	"github.com/grafana/loki/v3/pkg/logql/log"
)

// decolorizeStage 去除日志行中的 ANSI 终端颜色转义序列。
type decolorizeStage struct{}

// newDecolorizeStage 创建 decolorize 阶段（无额外配置）。
func newDecolorizeStage(_ interface{}) (Stage, error) {
	return &decolorizeStage{}, nil
}

// Run 对每条日志行调用 Decolorizer，将净化后的文本写回 Entry.Line。
func (m *decolorizeStage) Run(in chan Entry) chan Entry {
	decolorizer, _ := log.NewDecolorizer()
	out := make(chan Entry)
	go func() {
		defer close(out)
		for e := range in {
			decolorizedLine, _ := decolorizer.Process(
				e.Timestamp.Unix(),
				[]byte(e.Line),
				nil,
			)
			e.Line = string(decolorizedLine)
			out <- e
		}
	}()
	return out
}

// Name 返回阶段类型标识 StageTypeDecolorize。
func (m *decolorizeStage) Name() string {
	return StageTypeDecolorize
}

// Cleanup implements Stage.
func (*decolorizeStage) Cleanup() {
	// no-op
}
