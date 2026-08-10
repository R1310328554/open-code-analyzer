package logical

// printer 将 logical Plan 导出为 Mermaid 格式，便于在文档或调试工具中可视化计划图。

import (
	"fmt"
	"io"

	"github.com/grafana/loki/v3/pkg/engine/internal/util/tree"
)

func WriteMermaidFormat(w io.Writer, p *Plan) {
	var t treeFormatter
	for _, inst := range p.Instructions {
		switch inst := inst.(type) {
		case *Return:
			node := t.convert(inst.Value)
			printer := tree.NewMermaid(w)
			_ = printer.Write(node)

			fmt.Fprint(w, "\n\n")
		}
	}
}
// 每个 Return 输出一段 Mermaid 子图，多 Return 时以空行分隔便于粘贴到 Markdown。
