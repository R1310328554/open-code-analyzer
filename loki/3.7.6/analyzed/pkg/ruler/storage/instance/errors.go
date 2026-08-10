// This directory was copied and adapted from https://github.com/grafana/agent/tree/main/pkg/metrics.
// We cannot vendor the agent in since the agent vendors loki in, which would cause a cyclic dependency.
// NOTE: many changes have been made to the original code for our use-case.
package instance

// errors 定义实例热更新失败类型 ErrInvalidUpdate，不可变字段变更需停止并重建实例。

import "fmt"

// ErrInvalidUpdate 包装 Inner 错误，支持 errors.Is/As 识别动态更新不可行场景。
// ErrInvalidUpdate is returned whenever Update is called against an instance
// but an invalid field is changed between configs. If ErrInvalidUpdate is
// returned, the instance must be fully stopped and replaced with a new one
// with the new config.
type ErrInvalidUpdate struct {
	Inner error
}

// Error implements the error interface.
func (e ErrInvalidUpdate) Error() string { return e.Inner.Error() }

// Is returns true if err is an ErrInvalidUpdate.
func (e ErrInvalidUpdate) Is(err error) bool {
	switch err.(type) {
	case ErrInvalidUpdate, *ErrInvalidUpdate:
		return true
	default:
		return false
	}
}

// As will set the err object to ErrInvalidUpdate provided err
// is a pointer to ErrInvalidUpdate.
func (e ErrInvalidUpdate) As(err interface{}) bool {
	switch v := err.(type) {
	case *ErrInvalidUpdate:
		*v = e
	default:
		return false
	}
	return true
}

// errImmutableField 描述 name、wal_truncate_frequency 等不可热变更的配置项。
// errImmutableField is the error describing a field that cannot be changed. It
// is wrapped inside of a ErrInvalidUpdate.
type errImmutableField struct{ Field string }

func (e errImmutableField) Error() string {
	return fmt.Sprintf("%s cannot be changed dynamically", e.Field)
}
// 收到 ErrInvalidUpdate 时 BasicManager 会 Stop 旧进程并 spawn 新实例完成配置切换。
