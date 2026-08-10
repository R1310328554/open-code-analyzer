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

// api 包提供 Drone REST API 路由与 HTTP 处理器装配。
package api

import (
	"net/http"
	"os"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/acl"
	"github.com/drone/drone/handler/api/auth"
	"github.com/drone/drone/handler/api/badge"
	globalbuilds "github.com/drone/drone/handler/api/builds"
	"github.com/drone/drone/handler/api/card"
	"github.com/drone/drone/handler/api/ccmenu"
	"github.com/drone/drone/handler/api/events"
	"github.com/drone/drone/handler/api/queue"
	"github.com/drone/drone/handler/api/repos"
	"github.com/drone/drone/handler/api/repos/builds"
	"github.com/drone/drone/handler/api/repos/builds/branches"
	"github.com/drone/drone/handler/api/repos/builds/deploys"
	"github.com/drone/drone/handler/api/repos/builds/logs"
	"github.com/drone/drone/handler/api/repos/builds/pulls"
	"github.com/drone/drone/handler/api/repos/builds/stages"
	"github.com/drone/drone/handler/api/repos/collabs"
	"github.com/drone/drone/handler/api/repos/crons"
	"github.com/drone/drone/handler/api/repos/encrypt"
	"github.com/drone/drone/handler/api/repos/secrets"
	"github.com/drone/drone/handler/api/repos/sign"
	globalsecrets "github.com/drone/drone/handler/api/secrets"
	"github.com/drone/drone/handler/api/system"
	"github.com/drone/drone/handler/api/template"
	"github.com/drone/drone/handler/api/user"
	"github.com/drone/drone/handler/api/user/remote"
	"github.com/drone/drone/handler/api/users"
	"github.com/drone/drone/logger"

	"github.com/go-chi/chi"
	"github.com/go-chi/chi/middleware"
	"github.com/go-chi/cors"
)

// corsOpts 定义 API 跨域资源共享（CORS）策略。
var corsOpts = cors.Options{
	AllowedOrigins:   []string{"*"},
	AllowedMethods:   []string{"GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"},
	AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type", "X-CSRF-Token"},
	ExposedHeaders:   []string{"Link"},
	AllowCredentials: true,
	MaxAge:           300,
}

// New 注入全部依赖并构造 API Server 实例。
func New(
	builds core.BuildStore,
	commits core.CommitService,
	card core.CardStore,
	cron core.CronStore,
	events core.Pubsub,
	globals core.GlobalSecretStore,
	hooks core.HookService,
	logs core.LogStore,
	license *core.License,
	licenses core.LicenseService,
	orgs core.OrganizationService,
	perms core.PermStore,
	repos core.RepositoryStore,
	repoz core.RepositoryService,
	scheduler core.Scheduler,
	secrets core.SecretStore,
	stages core.StageStore,
	steps core.StepStore,
	status core.StatusService,
	session core.Session,
	stream core.LogStream,
	syncer core.Syncer,
	system *core.System,
	template core.TemplateStore,
	transferer core.Transferer,
	triggerer core.Triggerer,
	users core.UserStore,
	userz core.UserService,
	webhook core.WebhookSender,
) Server {
	return Server{
		Builds:     builds,
		Card:       card,
		Cron:       cron,
		Commits:    commits,
		Events:     events,
		Globals:    globals,
		Hooks:      hooks,
		Logs:       logs,
		License:    license,
		Licenses:   licenses,
		Orgs:       orgs,
		Perms:      perms,
		Repos:      repos,
		Repoz:      repoz,
		Scheduler:  scheduler,
		Secrets:    secrets,
		Stages:     stages,
		Steps:      steps,
		Status:     status,
		Session:    session,
		Stream:     stream,
		Syncer:     syncer,
		System:     system,
		Template:   template,
		Transferer: transferer,
		Triggerer:  triggerer,
		Users:      users,
		Userz:      userz,
		Webhook:    webhook,
	}
}

// Server 实现 http.Handler，通过 HTTP 暴露 Drone 全部 REST API 功能。
type Server struct {
	Builds     core.BuildStore           // 构建数据存储
	Card       core.CardStore            // 构建卡片存储
	Cron       core.CronStore            // 定时任务存储
	Commits    core.CommitService        // 提交信息服务
	Events     core.Pubsub               // 事件发布订阅
	Globals    core.GlobalSecretStore    // 全局密钥存储
	Hooks      core.HookService          // Webhook 钩子服务
	Logs       core.LogStore             // 构建日志存储
	License    *core.License             // 许可证信息
	Licenses   core.LicenseService       // 许可证校验服务
	Orgs       core.OrganizationService  // 组织查询服务
	Perms      core.PermStore            // 仓库权限存储
	Repos      core.RepositoryStore      // 仓库数据存储
	Repoz      core.RepositoryService    // 远程仓库服务
	Scheduler  core.Scheduler            // 构建阶段调度器
	Secrets    core.SecretStore          // 仓库密钥存储
	Stages     core.StageStore           // 阶段数据存储
	Steps      core.StepStore            // 步骤数据存储
	Status     core.StatusService        // CI 状态回写服务
	Session    core.Session              // 用户会话管理
	Stream     core.LogStream            // 实时日志流
	Syncer     core.Syncer               // 仓库同步器
	System     *core.System              // 系统元信息
	Template   core.TemplateStore        // 流水线模板存储
	Transferer core.Transferer           // 仓库所有权转移
	Triggerer  core.Triggerer            // 构建触发器
	Users      core.UserStore            // 用户数据存储
	Userz      core.UserService          // 远程用户服务
	Webhook    core.WebhookSender        // 出站 Webhook 发送器
	Private    bool                      // 是否启用私有模式
}

// Handler 组装 chi 路由器并注册全部 API 路由与中间件。
func (s Server) Handler() http.Handler {
	r := chi.NewRouter()
	r.Use(middleware.Recoverer)
	r.Use(middleware.NoCache)
	r.Use(logger.Middleware)
	r.Use(auth.HandleAuthentication(s.Session))

	cors := cors.New(corsOpts)
	r.Use(cors.Handler)

	// /repos 仓库管理 API
	r.Route("/repos", func(r chi.Router) {
		// 私有模式临时方案：启用 DRONE_SERVER_PRIVATE_MODE 时要求认证。
		if os.Getenv("DRONE_SERVER_PRIVATE_MODE") == "true" {
			r.Use(acl.AuthorizeUser)
		}

		r.With(
			acl.AuthorizeAdmin,
		).Get("/", repos.HandleAll(s.Repos))

		r.Route("/{owner}/{name}", func(r chi.Router) {
			r.Use(acl.InjectRepository(s.Repoz, s.Repos, s.Perms))
			r.Use(acl.CheckReadAccess())

			r.Get("/", repos.HandleFind())
			r.With(
				acl.CheckAdminAccess(),
			).Patch("/", repos.HandleUpdate(s.Repos))
			r.With(
				acl.CheckAdminAccess(),
			).Post("/", repos.HandleEnable(s.Hooks, s.Repos, s.Webhook))
			r.With(
				acl.CheckAdminAccess(),
			).Delete("/", repos.HandleDisable(s.Repos, s.Webhook))
			r.With(
				acl.CheckAdminAccess(),
			).Post("/chown", repos.HandleChown(s.Repos))
			r.With(
				acl.CheckAdminAccess(),
			).Post("/repair", repos.HandleRepair(s.Hooks, s.Repoz, s.Repos, s.Users, s.System.Link))

			// 仓库构建相关路由
			r.Route("/builds", func(r chi.Router) {
				r.Get("/", builds.HandleList(s.Repos, s.Builds))
				r.With(acl.CheckWriteAccess()).Post("/", builds.HandleCreate(s.Users, s.Repos, s.Commits, s.Triggerer))

				r.Get("/branches", branches.HandleList(s.Repos, s.Builds))
				r.With(acl.CheckWriteAccess()).Delete("/branches/*", branches.HandleDelete(s.Repos, s.Builds))

				r.Get("/pulls", pulls.HandleList(s.Repos, s.Builds))
				r.With(acl.CheckWriteAccess()).Delete("/pulls/{pull}", pulls.HandleDelete(s.Repos, s.Builds))

				r.Get("/deployments", deploys.HandleList(s.Repos, s.Builds))
				r.With(acl.CheckWriteAccess()).Delete("/deployments/*", deploys.HandleDelete(s.Repos, s.Builds))

				r.Get("/latest", builds.HandleLast(s.Repos, s.Builds, s.Stages))
				r.Get("/{number}", builds.HandleFind(s.Repos, s.Builds, s.Stages))
				r.Get("/{number}/logs/{stage}/{step}", logs.HandleFind(s.Repos, s.Builds, s.Stages, s.Steps, s.Logs))

				r.With(
					acl.CheckWriteAccess(),
				).Post("/{number}", builds.HandleRetry(s.Repos, s.Builds, s.Triggerer))

				r.With(
					acl.CheckWriteAccess(),
				).Delete("/{number}", builds.HandleCancel(s.Users, s.Repos, s.Builds, s.Stages, s.Steps, s.Status, s.Scheduler, s.Webhook))

				r.With(
					acl.CheckWriteAccess(),
				).Post("/{number}/promote", builds.HandlePromote(s.Repos, s.Builds, s.Triggerer))

				r.With(
					acl.CheckWriteAccess(),
				).Post("/{number}/rollback", builds.HandleRollback(s.Repos, s.Builds, s.Triggerer))

				r.With(
					acl.CheckAdminAccess(),
				).Post("/{number}/decline/{stage}", stages.HandleDecline(s.Repos, s.Builds, s.Stages))

				r.With(
					acl.CheckAdminAccess(),
				).Post("/{number}/decline", stages.HandleDeclineBuild(s.Repos, s.Builds, s.Stages))

				r.With(
					acl.CheckAdminAccess(),
				).Post("/{number}/approve/{stage}", stages.HandleApprove(s.Repos, s.Builds, s.Stages, s.Scheduler))

				r.With(
					acl.CheckAdminAccess(),
				).Delete("/{number}/logs/{stage}/{step}", logs.HandleDelete(s.Repos, s.Builds, s.Stages, s.Steps, s.Logs))

				r.With(
					acl.CheckAdminAccess(),
				).Delete("/", builds.HandlePurge(s.Repos, s.Builds))
			})

			// 仓库密钥管理
			r.Route("/secrets", func(r chi.Router) {
				r.Use(acl.CheckWriteAccess())
				r.Get("/", secrets.HandleList(s.Repos, s.Secrets))
				r.Post("/", secrets.HandleCreate(s.Repos, s.Secrets))
				r.Get("/{secret}", secrets.HandleFind(s.Repos, s.Secrets))
				r.Patch("/{secret}", secrets.HandleUpdate(s.Repos, s.Secrets))
				r.Delete("/{secret}", secrets.HandleDelete(s.Repos, s.Secrets))
			})

			// 流水线签名
			r.Route("/sign", func(r chi.Router) {
				r.Use(acl.CheckWriteAccess())
				r.Post("/", sign.HandleSign(s.Repos))
			})

			// 密钥加密工具
			r.Route("/encrypt", func(r chi.Router) {
				r.Use(acl.CheckWriteAccess())
				r.Post("/", encrypt.Handler(s.Repos))
				r.Post("/secret", encrypt.Handler(s.Repos))
			})

			// 定时任务（Cron）
			r.Route("/cron", func(r chi.Router) {
				r.Use(acl.CheckWriteAccess())
				r.Post("/", crons.HandleCreate(s.Repos, s.Cron))
				r.Get("/", crons.HandleList(s.Repos, s.Cron))
				r.Get("/{cron}", crons.HandleFind(s.Repos, s.Cron))
				r.Post("/{cron}", crons.HandleExec(s.Users, s.Repos, s.Cron, s.Commits, s.Triggerer))
				r.Patch("/{cron}", crons.HandleUpdate(s.Repos, s.Cron))
				r.Delete("/{cron}", crons.HandleDelete(s.Repos, s.Cron))
			})

			// 协作者权限
			r.Route("/collaborators", func(r chi.Router) {
				r.Get("/", collabs.HandleList(s.Repos, s.Perms))
				r.Get("/{member}", collabs.HandleFind(s.Users, s.Repos, s.Perms))
				r.With(
					acl.CheckAdminAccess(),
				).Delete("/{member}", collabs.HandleDelete(s.Users, s.Repos, s.Perms))
			})

			// 构建卡片（可视化输出）
			r.Route("/cards", func(r chi.Router) {
				r.Get("/{build}/{stage}/{step}", card.HandleFind(s.Builds, s.Card, s.Stages, s.Steps, s.Repos))
				r.With(
					acl.CheckAdminAccess(),
				).Post("/{build}/{stage}/{step}", card.HandleCreate(s.Builds, s.Card, s.Stages, s.Steps, s.Repos))
				r.With(
					acl.CheckAdminAccess(),
				).Delete("/{build}/{stage}/{step}", card.HandleDelete(s.Builds, s.Card, s.Stages, s.Steps, s.Repos))
			})
		})
	})

	// 构建状态徽章与 CCMenu 集成
	r.Route("/badges/{owner}/{name}", func(r chi.Router) {
		r.Get("/status.svg", badge.Handler(s.Repos, s.Builds))
		r.With(
			acl.InjectRepository(s.Repoz, s.Repos, s.Perms),
			acl.CheckReadAccess(),
		).Get("/cc.xml", ccmenu.Handler(s.Repos, s.Builds, s.System.Link))
	})

	// 构建队列管理（管理员）
	r.Route("/queue", func(r chi.Router) {
		r.Use(acl.AuthorizeAdmin)
		r.Get("/", queue.HandleItems(s.Stages))
		r.Post("/", queue.HandleResume(s.Scheduler))
		r.Delete("/", queue.HandlePause(s.Scheduler))
	})

	// 当前登录用户 API
	r.Route("/user", func(r chi.Router) {
		r.Use(acl.AuthorizeUser)
		r.Get("/", user.HandleFind())
		r.Patch("/", user.HandleUpdate(s.Users))
		r.Post("/token", user.HandleToken(s.Users))
		r.Get("/repos", user.HandleRepos(s.Repos))
		r.Post("/repos", user.HandleSync(s.Syncer, s.Repos))

		// TODO(bradrydzewski) finalize the name for this endpoint.
		r.Get("/builds", user.HandleRecent(s.Repos))
		r.Get("/builds/recent", user.HandleRecent(s.Repos))

		// 暴露远程 SCM 端点（如 GitHub）
		r.Get("/remote/repos", remote.HandleRepos(s.Repoz))
		r.Get("/remote/repos/{owner}/{name}", remote.HandleRepo(s.Repoz))
	})

	// 用户管理（管理员）
	r.Route("/users", func(r chi.Router) {
		r.Use(acl.AuthorizeAdmin)
		r.Get("/", users.HandleList(s.Users))
		r.Post("/", users.HandleCreate(s.Users, s.Userz, s.Webhook))
		r.Get("/{user}", users.HandleFind(s.Users))
		r.Patch("/{user}", users.HandleUpdate(s.Users, s.Transferer))
		r.Post("/{user}/token/rotate", users.HandleTokenRotation(s.Users))
		r.Delete("/{user}", users.HandleDelete(s.Users, s.Transferer, s.Webhook))
		r.Get("/{user}/repos", users.HandleRepoList(s.Users, s.Repos))
	})

	// 实时事件与日志流
	r.Route("/stream", func(r chi.Router) {
		r.Get("/", events.HandleGlobal(s.Repos, s.Events))

		r.Route("/{owner}/{name}", func(r chi.Router) {
			r.Use(acl.InjectRepository(s.Repoz, s.Repos, s.Perms))
			r.Use(acl.CheckReadAccess())

			r.Get("/", events.HandleEvents(s.Repos, s.Events))
			r.Get("/{number}/{stage}/{step}", events.HandleLogStream(s.Repos, s.Builds, s.Stages, s.Steps, s.Stream))
		})
	})

	// 全局构建查询（管理员）
	r.Route("/builds", func(r chi.Router) {
		r.Use(acl.AuthorizeAdmin)
		r.Get("/incomplete", globalbuilds.HandleIncomplete(s.Repos))
		r.Get("/incomplete/v2", globalbuilds.HandleRunningStatus(s.Repos))
	})

	// 组织级全局密钥
	r.Route("/secrets", func(r chi.Router) {
		r.With(acl.AuthorizeAdmin).Get("/", globalsecrets.HandleAll(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, false)).Get("/{namespace}", globalsecrets.HandleList(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, true)).Post("/{namespace}", globalsecrets.HandleCreate(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, false)).Get("/{namespace}/{name}", globalsecrets.HandleFind(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, true)).Post("/{namespace}/{name}", globalsecrets.HandleUpdate(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, true)).Patch("/{namespace}/{name}", globalsecrets.HandleUpdate(s.Globals))
		r.With(acl.CheckMembership(s.Orgs, true)).Delete("/{namespace}/{name}", globalsecrets.HandleDelete(s.Globals))
	})

	// 组织级流水线模板
	r.Route("/templates", func(r chi.Router) {
		r.With(acl.CheckMembership(s.Orgs, false)).Get("/", template.HandleListAll(s.Template))
		r.With(acl.CheckMembership(s.Orgs, true)).Post("/{namespace}", template.HandleCreate(s.Template))
		r.With(acl.CheckMembership(s.Orgs, false)).Get("/{namespace}", template.HandleList(s.Template))
		r.With(acl.CheckMembership(s.Orgs, false)).Get("/{namespace}/{name}", template.HandleFind(s.Template))
		r.With(acl.CheckMembership(s.Orgs, true)).Put("/{namespace}/{name}", template.HandleUpdate(s.Template))
		r.With(acl.CheckMembership(s.Orgs, true)).Patch("/{namespace}/{name}", template.HandleUpdate(s.Template))
		r.With(acl.CheckMembership(s.Orgs, true)).Delete("/{namespace}/{name}", template.HandleDelete(s.Template))
	})

	// 系统统计（管理员）
	r.Route("/system", func(r chi.Router) {
		r.Use(acl.AuthorizeAdmin)
		// r.Get("/license", system.HandleLicense())
		// r.Get("/limits", system.HandleLimits())
		r.Get("/stats", system.HandleStats(
			s.Builds,
			s.Stages,
			s.Users,
			s.Repos,
			s.Events,
			s.Stream,
		))
	})

	return r
}
