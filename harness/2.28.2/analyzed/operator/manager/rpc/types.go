// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package rpc

import (
	"sync"

	"github.com/drone/drone/core"
	"github.com/drone/drone/operator/manager"
)

// requestRequest 封装从队列请求阶段的过滤参数。
type requestRequest struct {
	Request *manager.Request
}

// acceptRequest 封装 Agent 接受阶段时的阶段 ID 与机器名。
type acceptRequest struct {
	Stage   int64
	Machine string
}

// netrcRequest 封装请求 netrc 凭据的仓库 ID。
type netrcRequest struct {
	Repo int64
}

// detailsRequest 封装请求构建详情的阶段 ID。
type detailsRequest struct {
	Stage int64
}

// stageRequest 封装阶段状态更新请求体。
type stageRequest struct {
	Stage *core.Stage
}

// stepRequest 封装步骤状态更新请求体。
type stepRequest struct {
	Step *core.Step
}

// writeRequest 封装单行日志写入请求（步骤 ID + 日志行）。
type writeRequest struct {
	Step int64
	Line *core.Line
}

// watchRequest 封装监听构建取消的构建 ID。
type watchRequest struct {
	Build int64
}

// watchResponse 返回构建是否已完成或应取消。
type watchResponse struct {
	Done bool
}

// buildContextToken 在 JSON 响应中单独携带仓库密钥与构建上下文。
type buildContextToken struct {
	Secret  string
	Context *manager.Context
}

// errorWrapper 封装 RPC 错误消息的标准 JSON 结构。
type errorWrapper struct {
	Message string
}

// writePool 复用 writeRequest 对象，减少日志写入路径的分配。
var writePool = sync.Pool{
	New: func() interface{} {
		return &writeRequest{}
	},
}
