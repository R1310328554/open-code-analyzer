// SPDX-License-Identifier: AGPL-3.0-only

package schedulerpb

// schedulerpb 定义 query-scheduler 与 frontend/querier 间的 gRPC 协议扩展。

import "github.com/pkg/errors"

var (
	ErrSchedulerIsNotRunning = errors.New("scheduler is not running")
)
// custom.go 补充 protobuf 生成代码之外的共享错误常量。
