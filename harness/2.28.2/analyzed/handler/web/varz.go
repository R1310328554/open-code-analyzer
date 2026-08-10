// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package web

import (
	"net/http"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// varz 聚合 SCM 与许可证等内部运行状态，供 /varz 端点 JSON 输出。
type varz struct {
	SCM     *scmInfo     `json:"scm"`
	License *licenseInfo `json:"license"`
}

// scmInfo 描述源码管理客户端的基础 URL 与 API 速率限制。
type scmInfo struct {
	URL  string    `json:"url"`
	Rate *rateInfo `json:"rate"`
}

// rateInfo 表示 SCM API 的速率配额与重置时间。
type rateInfo struct {
	Limit     int   `json:"limit"`
	Remaining int   `json:"remaining"`
	Reset     int64 `json:"reset"`
}

// licenseInfo 序列化许可证类型、席位/仓库配额及过期时间。
type licenseInfo struct {
	Kind       string    `json:"kind"`
	Seats      int64     `json:"seats"`
	SeatsUsed  int64     `json:"seats_used,omitempty"`
	SeatsAvail int64     `json:"seats_available,omitempty"`
	Repos      int64     `json:"repos"`
	ReposUsed  int64     `json:"repos_used,omitempty"`
	ReposAvail int64     `json:"repos_available,omitempty"`
	Expires    time.Time `json:"expire_at,omitempty"`
}

// HandleVarz 创建 HTTP 处理器，以 JSON 形式暴露 SCM 速率与许可证信息。
func HandleVarz(client *scm.Client, license *core.License) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		rate := client.Rate()
		v := &varz{
			License: &licenseInfo{
				Kind:    license.Kind,
				Seats:   license.Users,
				Repos:   license.Repos,
				Expires: license.Expires,
			},
			SCM: &scmInfo{
				URL: client.BaseURL.String(),
				Rate: &rateInfo{
					Limit:     rate.Limit,
					Remaining: rate.Remaining,
					Reset:     rate.Reset,
				},
			},
		}
		writeJSON(w, v, 200)
	}
}
