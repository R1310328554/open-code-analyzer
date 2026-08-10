// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// system 包提供系统级信息（许可证、统计、限额等）的 REST API 处理器。
package system

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/logger"
)

type (
	// users 汇总用户数量统计。
	users struct {
		Total int64 `json:"total"`
	}

	// repos 汇总活跃仓库数量。
	repos struct {
		Active int64 `json:"active"`
	}

	// builds 汇总构建总数及待执行、运行中数量。
	builds struct {
		Pending int   `json:"pending"`
		Running int   `json:"running"`
		Total   int64 `json:"total"`
	}

	// events 汇总事件总线订阅者数量。
	events struct {
		Subscribers int `json:"subscribers"`
	}

	// streams 汇总日志流订阅者与频道数量。
	streams struct {
		Subscribers int `json:"subscribers"`
		Channels    int `json:"channels"`
	}

	// platform 描述单个目标平台（OS/架构/变体）上待执行与运行中的阶段数。
	platform struct {
		Subscribers int    `json:"subscribers"`
		OS          string `json:"os"`
		Arch        string `json:"arch"`
		Variant     string `json:"variant"`
		Kernel      string `json:"kernel"`
		Pending     int    `json:"pending"`
		Running     int    `json:"running"`
	}

	// stats 为 /api/system/stats 响应的顶层聚合结构。
	stats struct {
		Users     users         `json:"users"`
		Repos     repos         `json:"repos"`
		Builds    builds        `json:"builds"`
		Pipelines []*platform   `json:"pipelines"`
		Events    events        `json:"events"`
		Streams   map[int64]int `json:"streams"`
		Watchers  map[int64]int `json:"watchers"`
	}
)

// HandleStats 返回 HTTP 处理器，聚合用户、仓库、构建、队列、事件与日志流等系统指标并以 JSON 返回。
func HandleStats(
	builds core.BuildStore,
	stages core.StageStore,
	users core.UserStore,
	repos core.RepositoryStore,
	bus core.Pubsub,
	streams core.LogStream,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var ctx = r.Context()
		var err error

		//
		// User Stats
		//

		stats := &stats{}
		stats.Users.Total, err = users.Count(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get user count")
			return
		}

		//
		// Repo Stats
		//

		stats.Repos.Active, err = repos.Count(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get repo count")
			return
		}

		//
		// Build Stats
		//

		stats.Builds.Total, err = builds.Count(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get build count")
			return
		}
		buildsPending, err := builds.Pending(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get pending build count")
			return
		}
		buildsRunning, err := builds.Running(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get running build count")
			return
		}
		stats.Builds.Pending = len(buildsPending)
		stats.Builds.Running = len(buildsRunning)

		//
		// Queue Stats
		//

		incomplete, err := stages.ListIncomplete(ctx)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get pending stage count")
			return
		}
		platforms := newPlatformList()
		aggregatePlatformStats(platforms, incomplete)
		stats.Pipelines = platforms

		//
		// Event Stats
		//

		stats.Events.Subscribers, err = bus.Subscribers()
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("stats: cannot get number of subscribers")
			return
		}

		//
		// Stream Stats
		//

		stats.Streams = streams.Info(ctx).Streams

		render.JSON(w, stats, 200)
	}
}

// 平台统计以固定长度数组返回，下列常量对应数组下标。
const (
	linuxArm6 int = iota
	linuxArm7
	linuxArm8
	linuxArm9
	linuxAmd64
	windows1709
	windows1803
	windows1809
)

// newPlatformList 初始化 core 当前支持的全部目标平台槽位。
func newPlatformList() []*platform {
	platforms := [8]*platform{}
	platforms[linuxArm6] = &platform{OS: "linux", Arch: "arm", Variant: "v6"}
	platforms[linuxArm7] = &platform{OS: "linux", Arch: "arm", Variant: "v7"}
	platforms[linuxArm8] = &platform{OS: "linux", Arch: "arm64", Variant: "v8"}
	platforms[linuxArm9] = &platform{OS: "linux", Arch: "arm", Variant: "v9"}
	platforms[linuxAmd64] = &platform{OS: "linux", Arch: "amd64"}
	platforms[windows1709] = &platform{OS: "windows", Arch: "arm64", Kernel: "1709"}
	platforms[windows1803] = &platform{OS: "windows", Arch: "arm64", Kernel: "1803"}
	platforms[windows1809] = &platform{OS: "windows", Arch: "arm64", Kernel: "1809"}
	return platforms[:]
}

// aggregatePlatformStats 按 OS、架构与变体将未完成阶段计数累加到对应平台槽位。
func aggregatePlatformStats(platforms []*platform, stages []*core.Stage) {
	for _, stage := range stages {
		var index int
		switch {
		case stage.OS == "windows" && stage.Kernel == "1709":
			index = windows1709
		case stage.OS == "windows" && stage.Kernel == "1803":
			index = windows1803
		case stage.OS == "windows" && stage.Kernel == "1809":
			index = windows1809
		case stage.OS == "windows":
			// default to 1803 when no variant specified
			index = windows1809
		case stage.Arch == "arm" && stage.Variant == "v6":
			index = linuxArm6
		case stage.Arch == "arm" && stage.Variant == "v7":
			index = linuxArm7
		case stage.Arch == "arm" && stage.Variant == "v9":
			index = linuxArm9
		case stage.Arch == "arm":
			// default to arm7 when no variant specified
			index = linuxArm7
		case stage.Arch == "arm64" && stage.Variant == "v8":
			index = linuxArm8
		case stage.Arch == "arm64":
			// default to arm8 when arm64
			index = linuxArm8
		default:
			index = linuxAmd64
			continue
		}

		switch stage.Status {
		case core.StatusPending:
			platforms[index].Pending++
		case core.StatusRunning:
			platforms[index].Running++
		}
	}
}
