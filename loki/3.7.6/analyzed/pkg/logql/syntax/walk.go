package syntax

// walk 提供轻量 AST 遍历回调：Walkable 节点调用 WalkFn，返回 false 可剪枝子树。

// WalkFn 在每个 Expr 节点访问时调用；返回 false 则不再深入子节点。
// WalkFn is the callback function that gets called whenever a node of the AST is visited.
// The return value indicates whether the traversal should continue with the child nodes.
type WalkFn = func(e Expr) bool

// Walkable 由实现 Walk 方法的 Expr 类型满足，用于通用树遍历工具。
// Walkable denotes a node of the AST that can be traversed.
type Walkable interface {
	Walk(f WalkFn)
}
// 各 Expr 的 Walk 实现负责按语法结构递归调用 f 并尊重 WalkFn 的继续/停止信号。
