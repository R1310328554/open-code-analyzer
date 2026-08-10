package util //nolint:revive

// util 包 errors 子模块提供 defer Close 错误日志、MultiError 聚合及 gRPC 连接取消与 deadline 判定等通用错误工具。

import (
	"bytes"
	"context"
	"errors"
	"fmt"

	"github.com/go-kit/log/level"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"

	"github.com/grafana/loki/v3/pkg/util/log"
)

// LogError 在 f 返回非 nil 时以 error 级别记录 message 与 error 字段。
// LogError logs any error returned by f; useful when deferring Close etc.
func LogError(message string, f func() error) {
	if err := f(); err != nil {
		level.Error(log.Logger).Log("message", message, "error", err)
	}
}

// LogError logs any error returned by f; useful when deferring Close etc.
func LogErrorWithContext(ctx context.Context, message string, f func() error) {
	if err := f(); err != nil {
		level.Error(log.WithContext(ctx, log.Logger)).Log("message", message, "error", err)
	}
}

// MultiError 切片实现 error 接口，可累积多条独立错误并在 Error() 中拼接。
// The MultiError type implements the error interface, and contains the
// Errors used to construct it.
type MultiError []error

// Returns a concatenated string of the contained errors
func (es MultiError) Error() string {
	var buf bytes.Buffer

	if len(es) > 1 {
		_, _ = fmt.Fprintf(&buf, "%d errors: ", len(es))
	}

	for i, err := range es {
		if i != 0 {
			buf.WriteString("; ")
		}
		buf.WriteString(err.Error())
	}

	return buf.String()
}

// Add adds the error to the error list if it is not nil.
func (es *MultiError) Add(err error) {
	if err == nil {
		return
	}
	if merr, ok := err.(MultiError); ok {
		*es = append(*es, merr...)
	} else {
		*es = append(*es, err)
	}
}

// Err returns the error list as an error or nil if it is empty.
func (es MultiError) Err() error {
	if len(es) == 0 {
		return nil
	}
	return es
}

// Is tells if all errors are the same as the target error.
func (es MultiError) Is(target error) bool {
	if len(es) == 0 {
		return false
	}
	for _, err := range es {
		if !errors.Is(err, target) {
			return false
		}
	}
	return true
}

// IsDeadlineExceeded tells if all errors are either context.DeadlineExceeded or grpc codes.DeadlineExceeded.
// IsDeadlineExceeded 当且仅当所有子错误均为 context 或 gRPC deadline 超时时返回 true。
func (es MultiError) IsDeadlineExceeded() bool {
	if len(es) == 0 {
		return false
	}
	for _, err := range es {
		if errors.Is(err, context.DeadlineExceeded) {
			continue
		}
		s, ok := status.FromError(err)
		if ok && s.Code() == codes.DeadlineExceeded {
			continue
		}
		return false
	}
	return true
}

// GroupedErrors 在 MultiError 基础上按错误文本分组并统计重复次数。
// GroupedErrors implements the error interface, and it contains the errors used to construct it
// grouped by the error message.
type GroupedErrors struct {
	MultiError
}

// Error Returns a concatenated string of the errors grouped by the error message along with the number of occurrences
// of each error message.
func (es GroupedErrors) Error() string {
	mapErrs := make(map[string]int, len(es.MultiError))
	for _, err := range es.MultiError {
		mapErrs[err.Error()]++
	}

	var idx int
	var buf bytes.Buffer
	uniqueErrs := len(mapErrs)
	for err, n := range mapErrs {
		if idx != 0 {
			buf.WriteString("; ")
		}
		if uniqueErrs > 1 || n > 1 {
			_, _ = fmt.Fprintf(&buf, "%d errors like: ", n)
		}
		buf.WriteString(err)
		idx++
	}

	return buf.String()
}

// IsConnCanceled 识别 codes.Canceled 或 transport is closing 等连接关闭场景。
// IsConnCanceled returns true, if error is from a closed gRPC connection.
// copied from https://github.com/etcd-io/etcd/blob/7f47de84146bdc9225d2080ec8678ca8189a2d2b/clientv3/client.go#L646
func IsConnCanceled(err error) bool {
	if err == nil {
		return false
	}

	// >= gRPC v1.23.x
	s, ok := status.FromError(err)
	if ok {
		// connection is canceled or server has already closed the connection
		return s.Code() == codes.Canceled || s.Message() == "transport is closing"
	}

	return false
}
// MultiError.Add 会展开嵌套 MultiError，避免多层包装导致重复计数。
