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

package main

import (
	"time"

	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/drone/core"
	"github.com/drone/drone/livelog"
	"github.com/drone/drone/metric/sink"
	"github.com/drone/drone/pubsub"
	"github.com/drone/drone/service/canceler"
	"github.com/drone/drone/service/canceler/reaper"
	"github.com/drone/drone/service/commit"
	contents "github.com/drone/drone/service/content"
	"github.com/drone/drone/service/content/cache"
	"github.com/drone/drone/service/hook"
	"github.com/drone/drone/service/hook/parser"
	"github.com/drone/drone/service/linker"
	"github.com/drone/drone/service/netrc"
	orgs "github.com/drone/drone/service/org"
	"github.com/drone/drone/service/repo"
	"github.com/drone/drone/service/status"
	"github.com/drone/drone/service/syncer"
	"github.com/drone/drone/service/token"
	"github.com/drone/drone/service/transfer"
	"github.com/drone/drone/service/user"
	"github.com/drone/drone/session"
	"github.com/drone/drone/trigger"
	"github.com/drone/drone/trigger/cron"
	"github.com/drone/drone/version"
	"github.com/drone/go-scm/scm"

	"github.com/google/wire"
)

// wire set for loading the services.
// serviceSet 定义业务服务层（提交、触发、同步等）的 Wire 提供者集合。
var serviceSet = wire.NewSet(
	canceler.New,
	commit.New,
	cron.New,
	livelog.New,
	linker.New,
	parser.New,
	pubsub.New,
	token.Renewer,
	transfer.New,
	trigger.New,
	user.New,

	provideRepositoryService,
	provideContentService,
	provideDatadog,
	provideHookService,
	provideNetrcService,
	provideOrgService,
	provideReaper,
	provideSession,
	provideStatusService,
	provideSyncer,
	provideSystem,
)

// provideContentService 返回带 LRU 缓存的文件内容服务。
func provideContentService(client *scm.Client, renewer core.Renewer) core.FileService {
	return cache.Contents(
		contents.New(client, renewer),
	)
}

// provideHookService 根据环境配置返回 Webhook 钩子管理服务。
func provideHookService(client *scm.Client, renewer core.Renewer, config config.Config) core.HookService {
	return hook.New(client, config.Proxy.Addr, renewer, config.IncomingWebhook.Events)
}

// provideNetrcService 根据环境配置返回 Git 克隆凭证（.netrc）服务。
func provideNetrcService(client *scm.Client, renewer core.Renewer, config config.Config) core.NetrcService {
	return netrc.New(
		client,
		renewer,
		config.Cloning.AlwaysAuth,
		config.Cloning.Username,
		config.Cloning.Password,
	)
}

// provideOrgService 返回带缓存的组织查询服务。
func provideOrgService(client *scm.Client, renewer core.Renewer) core.OrganizationService {
	return orgs.NewCache(orgs.New(client, renewer), 10, time.Minute*5)
}

// provideRepositoryService 根据环境配置返回仓库管理服务。
func provideRepositoryService(client *scm.Client, renewer core.Renewer, config config.Config) core.RepositoryService {
	return repo.New(
		client,
		renewer,
		config.Repository.Visibility,
		config.Repository.Trusted,
	)
}

// provideSession 根据环境配置创建并返回用户会话管理器。
func provideSession(store core.UserStore, config config.Config) (core.Session, error) {
	return session.New(store, session.NewConfig(
		config.Session.Secret,
		config.Session.Timeout,
		config.Session.Secure),
	), nil
}

// provideStatusService 根据环境配置返回 CI 状态回写服务。
func provideStatusService(client *scm.Client, renewer core.Renewer, config config.Config) core.StatusService {
	return status.New(client, renewer, status.Config{
		Base:     config.Server.Addr,
		Name:     config.Status.Name,
		Disabled: config.Status.Disabled,
	})
}

// provideSyncer 返回仓库同步器，可按命名空间过滤器限制同步范围。
func provideSyncer(repoz core.RepositoryService,
	repos core.RepositoryStore,
	users core.UserStore,
	batch core.Batcher,
	config config.Config) core.Syncer {
	sync := syncer.New(repoz, repos, users, batch)
	// 用户可配置过滤器，限制哪些仓库允许同步并写入数据库
	if filter := config.Repository.Filter; len(filter) > 0 {
		sync.SetFilter(syncer.NamespaceFilter(filter))
	}
	return sync
}

// provideSystem 返回包含服务器地址与版本信息的系统详情结构。
func provideSystem(config config.Config) *core.System {
	return &core.System{
		Proto:   config.Server.Proto,
		Host:    config.Server.Host,
		Link:    config.Server.Addr,
		Version: version.Version.String(),
	}
}

// provideReaper 返回僵尸构建清理器，用于终止超时未完成的构建。
func provideReaper(
	repos core.RepositoryStore,
	builds core.BuildStore,
	stages core.StageStore,
	canceler core.Canceler,
	config config.Config,
) *reaper.Reaper {
	return reaper.New(
		repos,
		builds,
		stages,
		canceler,
		config.Cleanup.Running,
		config.Cleanup.Pending,
		config.Cleanup.Buffer,
	)
}

// provideDatadog 返回 Datadog 指标上报 sink。
func provideDatadog(
	users core.UserStore,
	repos core.RepositoryStore,
	builds core.BuildStore,
	system *core.System,
	license *core.License,
	config config.Config,
) *sink.Datadog {
	return sink.New(
		users,
		repos,
		builds,
		*system,
		sink.Config{
			Endpoint:        config.Datadog.Endpoint,
			Token:           config.Datadog.Token,
			License:         license.Kind,
			Licensor:        license.Licensor,
			Subscription:    license.Subscription,
			EnableGithub:    config.IsGitHub(),
			EnableGithubEnt: config.IsGitHubEnterprise(),
			EnableGitlab:    config.IsGitLab(),
			EnableBitbucket: config.IsBitbucket(),
			EnableStash:     config.IsStash(),
			EnableGogs:      config.IsGogs(),
			EnableGitea:     config.IsGitea(),
			EnableGitee:     config.IsGitee(),
			EnableAgents:    !config.Agent.Disabled,
		},
	)
}
