// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package metric

import (
	"github.com/drone/drone/core"

	"github.com/prometheus/client_golang/prometheus"
)

// RunningJobCount 注册 drone_running_jobs Gauge，反映当前运行中的阶段/作业数量。
func RunningJobCount(stages core.StageStore) {
	prometheus.MustRegister(
		prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "drone_running_jobs",
			Help: "Total number of running jobs.",
		}, func() float64 {
			list, _ := stages.ListState(noContext, core.StatusRunning)
			return float64(len(list))
		}),
	)
}

// PendingJobCount 注册 drone_pending_jobs Gauge，反映排队等待执行的阶段/作业数量。
func PendingJobCount(stages core.StageStore) {
	prometheus.MustRegister(
		prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "drone_pending_jobs",
			Help: "Total number of pending jobs.",
		}, func() float64 {
			list, _ := stages.ListState(noContext, core.StatusPending)
			return float64(len(list))
		}),
	)
}
