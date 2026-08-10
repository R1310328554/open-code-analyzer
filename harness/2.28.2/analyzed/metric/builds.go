// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// metric 包（非 OSS 构建）向 Prometheus 注册 Drone 运行时 Gauge 指标。
package metric

import (
	"github.com/drone/drone/core"

	"github.com/prometheus/client_golang/prometheus"
)

// BuildCount 注册 drone_build_count Gauge，实时反映系统中构建总数。
func BuildCount(builds core.BuildStore) {
	prometheus.MustRegister(
		prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "drone_build_count",
			Help: "Total number of builds.",
		}, func() float64 {
			i, _ := builds.Count(noContext)
			return float64(i)
		}),
	)
}

// PendingBuildCount 注册 drone_pending_builds Gauge，反映排队等待执行的构建数量。
func PendingBuildCount(builds core.BuildStore) {
	prometheus.MustRegister(
		prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "drone_pending_builds",
			Help: "Total number of pending builds.",
		}, func() float64 {
			list, _ := builds.Pending(noContext)
			return float64(len(list))
		}),
	)
}

// RunningBuildCount 注册 drone_running_builds Gauge，反映当前正在执行的构建数量。
func RunningBuildCount(builds core.BuildStore) {
	prometheus.MustRegister(
		prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "drone_running_builds",
			Help: "Total number of running builds.",
		}, func() float64 {
			list, _ := builds.Running(noContext)
			return float64(len(list))
		}),
	)
}
