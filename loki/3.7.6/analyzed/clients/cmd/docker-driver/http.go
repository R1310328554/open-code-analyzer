package main

// Docker 插件 HTTP 处理器。
// 实现 LogDriver.StartLogging/StopLogging/Capabilities/ReadLogs RPC 端点。

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"

	"github.com/docker/docker/daemon/logger"
	"github.com/docker/docker/pkg/ioutils"
	"github.com/docker/go-plugins-helpers/sdk"
)

type StartLoggingRequest struct {
	File string
	Info logger.Info
}

type StopLoggingRequest struct {
	File string
}

type CapabilitiesResponse struct {
	Err string
	Cap logger.Capability
}

type ReadLogsRequest struct {
	Info   logger.Info
	Config logger.ReadConfig
}

// 注册 Docker 日志驱动插件的全部 HTTP 路由处理器。
func handlers(h *sdk.Handler, d *driver) {
	h.HandleFunc("/LogDriver.StartLogging", func(w http.ResponseWriter, r *http.Request) {
		var req StartLoggingRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		if req.Info.ContainerID == "" {
			respond(errors.New("must provide container id in log context"), w)
			return
		}

		err := d.StartLogging(req.File, req.Info)
		respond(err, w)
	})

	h.HandleFunc("/LogDriver.StopLogging", func(w http.ResponseWriter, r *http.Request) {
		var req StopLoggingRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		d.StopLogging(req.File)
		respond(nil, w)
	})

	h.HandleFunc("/LogDriver.Capabilities", func(w http.ResponseWriter, _ *http.Request) {
		_ = json.NewEncoder(w).Encode(&CapabilitiesResponse{
			Cap: logger.Capability{ReadLogs: true},
		})
	})

	h.HandleFunc("/LogDriver.ReadLogs", func(w http.ResponseWriter, r *http.Request) {
		var req ReadLogsRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		stream, err := d.ReadLogs(req.Info, req.Config)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer stream.Close()

		w.Header().Set("Content-Type", "application/x-json-stream")
		wf := ioutils.NewWriteFlusher(w)
		_, _ = io.Copy(wf, stream)
	})
}

type response struct {
	Err string
}

// 将错误封装为 JSON 响应写回 Docker 引擎。
func respond(err error, w io.Writer) {
	var res response
	if err != nil {
		res.Err = err.Error()
	}
	_ = json.NewEncoder(w).Encode(&res)
}
