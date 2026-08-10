// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

/*

/rpc/v2/stage                       POST  (request)
/rpc/v2/stage/{stage}?machine=      POST  (accept, details)
/rpc/v2/stage/{stage}               PUT   (beforeAll, afterAll)
/rpc/v2/stage/{stage}/steps/{step}  PUT   (before, after)
/rpc/v2/build/{build}/watch         POST  (watch)
/rpc/v2/stage/{stage}/logs/batch    POST  (batch)
/rpc/v2/stage/{stage}/logs/upload   POST  (upload)

*/

// rpc2 包实现基于 chi 路由的 v2 RESTful RPC HTTP 处理器（非 OSS 构建）。
package rpc2

import (
	"context"
	"encoding/json"
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/go-chi/chi"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/operator/manager"
	"github.com/drone/drone/store/shared/db"
)

// defaultTimeout 为 v2 RPC HTTP 请求默认超时。
var defaultTimeout = time.Second * 30

var noContext = context.Background()

// HandleJoin 处理 Agent 加入集群的占位请求（当前为 no-op）。
//
// POST /rpc/v2/nodes/:machine
func HandleJoin() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		writeOK(w) // this is a no-op
	}
}

// HandleLeave 处理 Agent 离开集群的占位请求（当前为 no-op）。
//
// DELETE /rpc/v2/nodes/:machine
func HandleLeave() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		writeOK(w) // this is a no-op
	}
}

// HandlePing 处理连通性探测请求，返回 200 表示服务可达。
//
// GET /rpc/v2/ping
func HandlePing() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		writeOK(w) // this is a no-op
	}
}

// HandleRequest 从调度队列长轮询请求下一个可执行阶段。
//
// POST /rpc/v2/stage
func HandleRequest(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		ctx, cancel := context.WithTimeout(ctx, defaultTimeout)
		defer cancel()

		req := new(manager.Request)
		err := json.NewDecoder(r.Body).Decode(req)
		if err != nil {
			writeError(w, err)
			return
		}
		stage, err := m.Request(ctx, req)
		if err != nil {
			writeError(w, err)
		} else {
			writeJSON(w, stage)
		}
	}
}

// HandleAccept 接受指定阶段的执行权并绑定 Agent 机器名。
//
// POST /rpc/v2/stage/{stage}?machine=
func HandleAccept(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		stage, _ := strconv.ParseInt(
			chi.URLParam(r, "stage"), 10, 64)

		out, err := m.Accept(noContext, stage, r.FormValue("machine"))
		if err != nil {
			writeError(w, err)
		} else {
			writeJSON(w, out)
		}
	}
}

// HandleInfo 返回阶段构建详情、netrc 凭据及含密钥的仓库信息。
//
// POST /rpc/v2/build/{build}
func HandleInfo(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		stage, _ := strconv.ParseInt(
			chi.URLParam(r, "stage"), 10, 64)

		res, err := m.Details(noContext, stage)
		if err != nil {
			writeError(w, err)
			return
		}

		netrc, err := m.Netrc(noContext, res.Repo.ID)
		if err != nil {
			writeError(w, err)
			return
		}

		writeJSON(w, &details{
			Context: res,
			Netrc:   netrc,
			Repo: &repository{
				Repository: res.Repo,
				Secret:     res.Repo.Secret,
			},
		})
	}
}

// HandleUpdateStage 根据阶段状态调用 BeforeAll 或 AfterAll 更新阶段。
//
// PUT /rpc/v2/stage/{stage}
func HandleUpdateStage(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		dst := new(core.Stage)
		err := json.NewDecoder(r.Body).Decode(dst)
		if err != nil {
			writeError(w, err)
			return
		}

		if dst.Status == core.StatusPending ||
			dst.Status == core.StatusRunning {
			err = m.BeforeAll(noContext, dst)
		} else {
			err = m.AfterAll(noContext, dst)
		}

		if err != nil {
			writeError(w, err)
		} else {
			writeJSON(w, dst)
		}
	}
}

// HandleUpdateStep 根据步骤状态调用 Before 或 After 更新步骤。
//
// POST /rpc/v2/step/{step}
func HandleUpdateStep(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		dst := new(core.Step)
		err := json.NewDecoder(r.Body).Decode(dst)
		if err != nil {
			writeError(w, err)
			return
		}

		if dst.Status == core.StatusPending ||
			dst.Status == core.StatusRunning {
			err = m.Before(noContext, dst)
		} else {
			err = m.After(noContext, dst)
		}

		if err != nil {
			writeError(w, err)
		} else {
			writeJSON(w, dst)
		}
	}
}

// HandleWatch 阻塞监听构建取消事件，超时返回 204 供客户端重试。
//
// GET /rpc/v2/build/{build}/watch
func HandleWatch(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		ctx, cancel := context.WithTimeout(ctx, defaultTimeout)
		defer cancel()

		build, _ := strconv.ParseInt(
			chi.URLParamFromCtx(ctx, "build"), 10, 64)

		_, err := m.Watch(ctx, build)
		if err != nil {
			writeError(w, err)
		} else {
			writeOK(w)
		}
	}
}

// HandleLogBatch 批量提交步骤的多行实时日志。
//
// POST /rpc/v2/step/{step}/logs/batch
func HandleLogBatch(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		step, _ := strconv.ParseInt(
			chi.URLParam(r, "step"), 10, 64)

		lines := []*core.Line{}
		err := json.NewDecoder(r.Body).Decode(&lines)
		if err != nil {
			writeError(w, err)
			return
		}

		// TODO(bradrydzewski) modify the write function to
		// accept a slice of lines.
		for _, line := range lines {
			err := m.Write(noContext, step, line)
			if err != nil {
				writeError(w, err)
				return
			}
		}

		writeOK(w)
	}
}

// HandleLogUpload 上传并持久化步骤完整日志。
//
// POST /rpc/v2/step/{step}/logs/upload
func HandleLogUpload(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		step, _ := strconv.ParseInt(
			chi.URLParam(r, "step"), 10, 64)

		err := m.Upload(noContext, step, r.Body)
		if err != nil {
			writeError(w, err)
		} else {
			writeOK(w)
		}
	}
}

// HandleCardUpload 上传并持久化步骤可视化卡片。
//
// POST /rpc/v2/step/{step}/card
func HandleCardUpload(m manager.BuildManager) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		step, _ := strconv.ParseInt(
			chi.URLParam(r, "step"), 10, 64)

		in := new(core.CardInput)
		err := json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = m.UploadCard(noContext, step, in)
		if err != nil {
			writeError(w, err)
		} else {
			writeOK(w)
		}
	}
}

// writeJSON 将对象序列化为 JSON 写入响应。
func writeJSON(w http.ResponseWriter, v interface{}) {
	json.NewEncoder(w).Encode(v)
}

// writeOK 返回 HTTP 200 空响应体。
func writeOK(w http.ResponseWriter) {
	w.WriteHeader(http.StatusOK)
}

// writeError 将错误映射为 204/409/500 等状态码并写入消息。
func writeError(w http.ResponseWriter, err error) {
	if err == context.DeadlineExceeded {
		w.WriteHeader(204) // should retry
	} else if err == context.Canceled {
		w.WriteHeader(204) // should retry
	} else if err == db.ErrOptimisticLock {
		w.WriteHeader(409) // should abort
	} else {
		w.WriteHeader(500) // should fail
	}
	io.WriteString(w, err.Error())
}
