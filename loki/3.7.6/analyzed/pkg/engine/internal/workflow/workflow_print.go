package workflow

// workflow_print 将 Workflow 的 Task DAG 格式化为可读树形文本，标注各物理节点的 @source/@sink Stream ULID 及任务时间范围。

import (
	"fmt"
	"io"
	"strings"
	"time"

	"github.com/grafana/loki/v3/pkg/engine/internal/planner/physical"
	"github.com/grafana/loki/v3/pkg/engine/internal/util/dag"
	"github.com/grafana/loki/v3/pkg/engine/internal/util/tree"
)

// Sprint 将 Fprint 输出写入 strings.Builder 并返回完整字符串。
// Sprint returns a string representation of the workflow.
func Sprint(wf *Workflow) string {
	var sb strings.Builder
	_ = Fprint(&sb, wf)
	return sb.String()
}

// Fprint 对每个根 Task 前序遍历，打印 Task 框线、MaxTimeRange 及 Fragment 计划树。
// Fprint prints a string representation of the workflow to the given writer.
func Fprint(w io.Writer, wf *Workflow) error {
	visited := make(map[*Task]struct{}, wf.graph.Len())

	roots := wf.graph.Roots()
	for _, root := range roots {
		err := wf.graph.Walk(root, func(n *Task) error {
			if _, seen := visited[n]; seen {
				return nil
			}
			visited[n] = struct{}{}

			fmt.Fprintf(w, "┌ Task %s\n", n.ID())
			fmt.Fprintf(w, "│ @max_time_range start=%s end=%s\n", n.MaxTimeRange.Start.Format(time.RFC3339Nano), n.MaxTimeRange.End.Format(time.RFC3339Nano))
			fmt.Fprintln(w, "│")

			var sb strings.Builder
			for _, root := range n.Fragment.Roots() {
				printer := tree.NewPrinter(&sb)

				planTree := physical.BuildTree(n.Fragment, root)

				for node, streams := range n.Sources {
					treeNode := findTreeNode(planTree, func(n *tree.Node) bool { return n.Context == node })
					if treeNode == nil {
						continue
					}

					for _, stream := range streams {
						treeNode.AddComment("@source", "", []tree.Property{tree.NewProperty("stream", false, stream.ULID.String())})
					}
				}

				for node, streams := range n.Sinks {
					treeNode := findTreeNode(planTree, func(n *tree.Node) bool { return n.Context == node })
					if treeNode == nil {
						continue
					}

					for _, stream := range streams {
						treeNode.AddComment("@sink", "", []tree.Property{tree.NewProperty("stream", false, stream.ULID.String())})
					}
				}

				printer.Print(planTree)
			}

			for line := range strings.Lines(sb.String()) {
				fmt.Fprintf(w, "│ %s", line)
			}
			fmt.Fprintln(w, "└")
			return nil
		}, dag.PreOrderWalk)
		if err != nil {
			return err
		}
	}

	return nil
}

// findTreeNode 递归搜索 planTree 及其 Comment 子树，匹配 physical.Node 上下文。
// findTreeNode finds the first node in the tree that satisfies the given
// predicate. findTreeNode returns nil if no node is found.
func findTreeNode(root *tree.Node, f func(node *tree.Node) bool) *tree.Node {
	if f(root) {
		return root
	}

	for _, child := range root.Children {
		if node := findTreeNode(child, f); node != nil {
			return node
		}
	}

	for _, comment := range root.Comments {
		if node := findTreeNode(comment, f); node != nil {
			return node
		}
	}

	return nil
}
// 调试输出使用 tree.Printer 渲染物理计划，Sources/Sinks 以 @source/@sink 注释附加。
