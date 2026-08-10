// Copyright 2017 The Cockroach Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied. See the License for the specific language governing
// permissions and limitations under the License.

// Fork of https://raw.githubusercontent.com/cockroachdb/cockroach/065aa74206c9ec9bcd8b9ea2a6c62ddde8aab0a6/pkg/util/treeprinter/tree_printer.go
package logql

// tree_printer 提供 ASCII 树形输出（fork 自 Cockroach treeprinter）：按深度优先预序追加子节点并绘制 ├── / └── 边。

import (
	"bytes"
	"fmt"
	"strings"
)

var (
	edgeLink = []rune(" │")
	edgeMid  = []rune(" ├── ")
	edgeLast = []rune(" └── ")
)

// Node 绑定共享 tree 状态与当前层级，Child/AddLine 在此深度追加输出行。
// Node is a handle associated with a specific depth in a tree. See below for
// sample usage.
type Node struct {
	tree  *tree
	level int
}

// NewTree 返回根级 Node（level=0），后续 Child 调用须保持深度优先预序。
// NewTree creates a tree printer and returns a sentinel node reference which
// should be used to add the root. Sample usage:
//
//	tp := NewTree()
//	root := n.Child("root")
//	root.Child("child-1")
//	root.Child("child-2").Child("grandchild\ngrandchild-more-info")
//	root.Child("child-3")
//
//	fmt.Print(tp.String())
//
// Output:
//
//	root
//	 ├── child-1
//	 ├── child-2
//	 │    └── grandchild
//	 │        grandchild-more-info
//	 └── child-3
//
// Note that the Child calls can't be rearranged arbitrarily; they have
// to be in the order they need to be displayed (depth-first pre-order).
func NewTree() Node {
	return Node{
		tree:  &tree{},
		level: 0,
	}
}

// tree 累积各行 rune 切片，并在 lastNode 记录每层最后一个节点行号以修正 sibling 边。
type tree struct {
	// rows maintains the rows accumulated so far, as rune arrays.
	//
	// When a new child is added (e.g. child2 above), we may have to
	// go back up and fix edges.
	rows [][]rune

	// row index of the last row for a given level. Grows as needed.
	lastNode []int
}

// Childf adds a node as a child of the given node.
func (n Node) Childf(format string, args ...interface{}) Node {
	return n.Child(fmt.Sprintf(format, args...))
}

// Child 支持多行文本：首行带树边，后续行通过 AddLine 缩进对齐。
// Child adds a node as a child of the given node. Multi-line strings are
// supported with appropriate indentation.
func (n Node) Child(text string) Node {
	if strings.ContainsRune(text, '\n') {
		splitLines := strings.Split(text, "\n")
		node := n.childLine(splitLines[0])
		for _, l := range splitLines[1:] {
			n.AddLine(l)
		}
		return node
	}
	return n.childLine(text)
}

// AddLine 在子节点下追加无连接符的缩进行，用于多行节点正文。
// AddLine adds a new line to a child node without an edge.
func (n Node) AddLine(v string) {
	// Each level indents by this much.
	k := len(edgeLast)
	indent := n.level * k
	row := make([]rune, indent+len(v))
	for i := 0; i < indent; i++ {
		row[i] = ' '
	}
	for i, r := range v {
		row[indent+i] = r
	}
	n.tree.rows = append(n.tree.rows, row)
}

// childLine adds a node as a child of the given node.
// childLine 计算缩进、连接 sibling 的 ├── 或 └──，并返回 level+1 的子 Node。
func (n Node) childLine(text string) Node {
	runes := []rune(text)

	// Each level indents by this much.
	k := len(edgeLast)
	indent := n.level * k
	row := make([]rune, indent+len(runes))
	for i := 0; i < indent-k; i++ {
		row[i] = ' '
	}
	if indent >= k {
		// Connect through any empty lines.
		for i := len(n.tree.rows) - 1; i >= 0 && len(n.tree.rows[i]) == 0; i-- {
			n.tree.rows[i] = make([]rune, indent-k+len(edgeLink))
			for j := 0; j < indent-k+len(edgeLink); j++ {
				n.tree.rows[i][j] = ' '
			}
			copy(n.tree.rows[i][indent-k:], edgeLink)
		}
		copy(row[indent-k:], edgeLast)
	}
	copy(row[indent:], runes)

	for len(n.tree.lastNode) <= n.level+1 {
		n.tree.lastNode = append(n.tree.lastNode, -1)
	}
	n.tree.lastNode[n.level+1] = -1

	if last := n.tree.lastNode[n.level]; last != -1 {
		if n.level == 0 {
			panic("multiple root nodes")
		}
		// Connect to the previous sibling.
		copy(n.tree.rows[last][indent-k:], edgeMid)
		for i := last + 1; i < len(n.tree.rows); i++ {
			// Add spaces if necessary.
			for len(n.tree.rows[i]) < indent-k+len(edgeLink) {
				n.tree.rows[i] = append(n.tree.rows[i], ' ')
			}
			copy(n.tree.rows[i][indent-k:], edgeLink)
		}
	}

	n.tree.lastNode[n.level] = len(n.tree.rows)
	n.tree.rows = append(n.tree.rows, row)

	// Return a TreePrinter that can be used for children of this node.
	return Node{
		tree:  n.tree,
		level: n.level + 1,
	}
}

// AddEmptyLine adds an empty line to the output; used to introduce vertical
// spacing as needed.
func (n Node) AddEmptyLine() {
	n.tree.rows = append(n.tree.rows, []rune{})
}

// FormattedRows 仅允许在根 Node 调用，返回各行字符串切片。
// FormattedRows returns the formatted rows. Can only be called on the result of
// treeprinter.New.
func (n Node) FormattedRows() []string {
	if n.level != 0 {
		panic("Only the root can be stringified")
	}
	res := make([]string, len(n.tree.rows))
	for i, r := range n.tree.rows {
		res[i] = string(r)
	}
	return res
}

func (n Node) String() string {
	if n.level != 0 {
		panic("Only the root can be stringified")
	}
	var buf bytes.Buffer
	for _, r := range n.tree.rows {
		buf.WriteString(string(r))
		buf.WriteByte('\n')
	}
	return buf.String()
}
// String 将 rows 拼接为带换行的树形文本，供 LogQL 计划或 AST 调试打印。
