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

package sink

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/drone/drone/core"
)

// payload 是 Datadog API 请求体，包含一组时序指标序列。
type payload struct {
	Series []series `json:"series"`
}

// series 描述单条 Datadog 时序数据点及其标签。
type series struct {
	Metric string    `json:"metric"`
	Points [][]int64 `json:"points"`
	Host   string    `json:"host"`
	Type   string    `json:"type"`
	Tags   []string  `json:"tags,omitempty"`
}

// Datadog 实现向 Datadog 上报用户、仓库与构建数量的定时指标 sink。
type Datadog struct {
	users  core.UserStore
	repos  core.RepositoryStore
	builds core.BuildStore
	system core.System
	config Config
	client *http.Client
}

// New 构造 Datadog sink，注入各数据存储与上报配置。
func New(
	users core.UserStore,
	repos core.RepositoryStore,
	builds core.BuildStore,
	system core.System,
	config Config,
) *Datadog {
	return &Datadog{
		users:  users,
		repos:  repos,
		builds: builds,
		system: system,
		config: config,
	}
}

// Start 启动后台循环，每日午夜触发一次指标采集与上报。
func (d *Datadog) Start(ctx context.Context) error {
	for {
		diff := midnightDiff()
		select {
		case <-time.After(diff):
			d.do(ctx, time.Now().Unix())
		case <-ctx.Done():
			return nil
		}
	}
}

// do 采集当前用户/仓库/构建计数并 POST 至 Datadog API。
func (d *Datadog) do(ctx context.Context, unix int64) error {
	users, err := d.users.Count(ctx)
	if err != nil {
		return err
	}
	repos, err := d.repos.Count(ctx)
	if err != nil {
		return err
	}
	builds, err := d.builds.Count(ctx)
	if err != nil {
		return err
	}
	userList, _ := d.users.ListRange(ctx, core.UserParams{
		Sort: false,
		Page: 0,
		Size: 5,
	})
	tags := createTags(d.config)
	data := new(payload)
	data.Series = []series{
		{
			Metric: "drone.users",
			Points: [][]int64{[]int64{unix, users}},
			Type:   "gauge",
			Host:   d.system.Host,
			Tags:   append(tags, createInstallerTags(userList)...),
		},
		{
			Metric: "drone.repos",
			Points: [][]int64{[]int64{unix, repos}},
			Type:   "gauge",
			Host:   d.system.Host,
			Tags:   tags,
		},
		{
			Metric: "drone.builds",
			Points: [][]int64{[]int64{unix, builds}},
			Type:   "gauge",
			Host:   d.system.Host,
			Tags:   tags,
		},
	}

	buf := new(bytes.Buffer)
	err = json.NewEncoder(buf).Encode(data)
	if err != nil {
		return err
	}

	endpoint := fmt.Sprintf("%s?api_key=%s", d.config.Endpoint, d.config.Token)
	req, err := http.NewRequest("POST", endpoint, buf)
	if err != nil {
		return err
	}
	req.Header.Add("Accept", "application/json")
	req.Header.Add("Content-Type", "application/json")

	res, err := httpClient.Do(req)
	if err != nil {
		return err
	}

	res.Body.Close()
	return nil
}

// Client 返回用于 HTTP 上报的客户端；未自定义时使用带超时的默认客户端。
func (d *Datadog) Client() *http.Client {
	if d.client == nil {
		return httpClient
	}
	return d.client
}

// midnightDiff 计算当前时刻到下一 UTC 本地午夜的时间间隔。
func midnightDiff() time.Duration {
	a := time.Now()
	b := time.Date(a.Year(), a.Month(), a.Day()+1, 0, 0, 0, 0, a.Location())
	return b.Sub(a)
}

// httpClient 是全局 HTTP 客户端，配置超时与 TLS 握手限制以提高上报可靠性。
var httpClient = &http.Client{
	Transport: &http.Transport{
		Proxy:               http.ProxyFromEnvironment,
		TLSHandshakeTimeout: 30 * time.Second,
		DisableKeepAlives:   true,
	},
	Timeout: 1 * time.Minute,
}
