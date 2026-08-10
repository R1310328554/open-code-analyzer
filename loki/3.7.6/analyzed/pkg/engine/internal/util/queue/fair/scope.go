package fair

// Scope 用字符串切片表示队列内的分层路径，如 tenant/worker/stream。

import "strings"

// Scope 每一层对应树中一级 scope 名称，RegisterScope 按路径建树。
// A Scope denotes a named area within a [Queue] where items may be queued.
type Scope []string

// String 以斜杠连接全路径，便于日志与调试输出。
// String returns the full name of the scope, with each element separated by a
// slash.
func (s Scope) String() string {
	return strings.Join(s, "/")
}

// Name 返回路径末段，作为父 pqueue 中 scopeLookup 的键。
// Name returns the local name of the scope (last element).
func (s Scope) Name() string {
	return s[len(s)-1]
}
// 空 Scope 在 RegisterScope 时会触发 ErrEmptyScope。
