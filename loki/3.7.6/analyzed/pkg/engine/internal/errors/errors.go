package errors

// 引擎内部 sentinel 错误：索引、键、类型不匹配及未实现分支的统一标识。

import "errors"

var (
	ErrIndex          = errors.New("index error")
	ErrKey            = errors.New("key error")
	ErrType           = errors.New("type error")
	ErrNotImplemented = errors.New("not implemented")
)
// 调用方应优先 errors.Is 匹配这些哨兵值，而非依赖错误字符串内容。
