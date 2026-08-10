// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// Package ccmenu 生成符合 CCTray/CCMenu 协议的 XML 构建状态摘要。
package ccmenu

import (
	"encoding/xml"
	"fmt"
	"time"

	"github.com/drone/drone/core"
)

// CCProjects 是 CCTray XML 根元素，包含单个项目节点。
type CCProjects struct {
	XMLName xml.Name   `xml:"Projects"`
	Project *CCProject `xml:"Project"`
}

// CCProject 表示 CCTray 协议中的单个项目状态字段。
type CCProject struct {
	XMLName         xml.Name `xml:"Project"`
	Name            string   `xml:"name,attr"`            // 项目标识（仓库 slug）
	Activity        string   `xml:"activity,attr"`        // 当前活动：Building 或 Sleeping
	LastBuildStatus string   `xml:"lastBuildStatus,attr"` // 最近构建结果枚举
	LastBuildLabel  string   `xml:"lastBuildLabel,attr"`  // 最近构建编号标签
	LastBuildTime   string   `xml:"lastBuildTime,attr"`   // 最近构建开始时间（RFC3339）
	WebURL          string   `xml:"webUrl,attr"`          // 构建详情页链接
}

// New 根据仓库与构建信息构造 CCMenu/CCTray 兼容的 XML 项目节点。
func New(r *core.Repository, b *core.Build, link string) *CCProjects {
	proj := &CCProject{
		Name:            r.Slug,
		WebURL:          link,
		Activity:        "Building",
		LastBuildStatus: "Unknown",
		LastBuildLabel:  "Unknown",
	}

	// 若构建已结束（非 pending/running/blocked），填充最近构建时间与编号
	if b.Status != core.StatusPending &&
		b.Status != core.StatusRunning &&
		b.Status != core.StatusBlocked {
		proj.Activity = "Sleeping"
		proj.LastBuildTime = time.Unix(b.Started, 0).Format(time.RFC3339)
		proj.LastBuildLabel = fmt.Sprint(b.Number)
	}

	// 将 Drone 构建状态映射为 CCTray 允许的枚举值
	switch b.Status {
	case core.StatusError, core.StatusKilled, core.StatusDeclined:
		proj.LastBuildStatus = "Exception"
	case core.StatusPassing:
		proj.LastBuildStatus = "Success"
	case core.StatusFailing:
		proj.LastBuildStatus = "Failure"
	}

	return &CCProjects{Project: proj}
}
