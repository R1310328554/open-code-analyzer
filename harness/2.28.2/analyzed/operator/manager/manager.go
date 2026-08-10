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

// manager 包封装 Drone 构建运行器与服务器之间的复杂构建操作，提供简化的 BuildManager 接口。
package manager

import (
	"bytes"
	"context"
	"io"
	"io/ioutil"
	"time"

	"github.com/drone/drone-yaml/yaml/converter"
	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"

	"github.com/hashicorp/go-multierror"
	"github.com/sirupsen/logrus"
)

// noContext 为不携带取消/超时的后台上下文，用于部分数据库操作。
var noContext = context.Background()

var _ BuildManager = (*Manager)(nil)

type (
	// Context 包含运行器执行一次构建所需的最小上下文信息。
	Context struct {
		Repo    *core.Repository `json:"repository"`
		Build   *core.Build      `json:"build"`
		Stage   *core.Stage      `json:"stage"`
		Config  *core.File       `json:"config"`
		Secrets []*core.Secret   `json:"secrets"`
		System  *core.System     `json:"system"`
	}

	// BuildManager 封装复杂构建操作，为构建运行器提供简化接口。
	BuildManager interface {
		// Request 从调度队列请求下一个可执行的构建阶段。
		Request(ctx context.Context, args *Request) (*core.Stage, error)

		// Accept 接受指定构建阶段并在给定机器上锁定执行权。
		Accept(ctx context.Context, stage int64, machine string) (*core.Stage, error)

		// Netrc 返回用于克隆仓库的有效 netrc 凭据。
		Netrc(ctx context.Context, repo int64) (*core.Netrc, error)

		// Details 获取指定阶段的完整构建上下文（仓库、配置、密钥等）。
		Details(ctx context.Context, stage int64) (*Context, error)

		// Before 在构建步骤开始执行前更新状态并创建日志流。
		Before(ctx context.Context, step *core.Step) error

		// After 在构建步骤完成后更新状态并清理日志流。
		After(ctx context.Context, step *core.Step) error

		// BeforeAll 在构建阶段开始执行前进行初始化（持久化步骤、发布事件等）。
		BeforeAll(ctx context.Context, stage *core.Stage) error

		// AfterAll 在构建阶段完成后执行收尾（调度下游、更新构建状态等）。
		AfterAll(ctx context.Context, stage *core.Stage) error

		// Watch 监听构建取消请求，返回是否应停止执行。
		Watch(ctx context.Context, stage int64) (bool, error)

		// Write 向构建日志流写入一行实时日志。
		Write(ctx context.Context, step int64, line *core.Line) error

		// Upload 上传步骤的完整日志内容。
		Upload(ctx context.Context, step int64, r io.Reader) error

		// UploadBytes 以字节切片形式上传步骤完整日志。
		UploadBytes(ctx context.Context, step int64, b []byte) error

		// UploadCard 为指定步骤创建可视化卡片。
		UploadCard(ctx context.Context, step int64, input *core.CardInput) error
	}

	// Request 定义从队列请求待执行构建时的过滤条件（架构、内核、标签等）。
	Request struct {
		Kind    string            `json:"kind"`
		Type    string            `json:"type"`
		OS      string            `json:"os"`
		Arch    string            `json:"arch"`
		Variant string            `json:"variant"`
		Kernel  string            `json:"kernel"`
		Labels  map[string]string `json:"labels,omitempty"`
	}
)

// New 构造并返回实现 BuildManager 接口的 Manager 实例。
func New(
	builds core.BuildStore,
	cards core.CardStore,
	config core.ConfigService,
	converter core.ConvertService,
	events core.Pubsub,
	logs core.LogStore,
	logz core.LogStream,
	netrcs core.NetrcService,
	repos core.RepositoryStore,
	scheduler core.Scheduler,
	secrets core.SecretStore,
	globals core.GlobalSecretStore,
	status core.StatusService,
	stages core.StageStore,
	steps core.StepStore,
	system *core.System,
	users core.UserStore,
	webhook core.WebhookSender,
) BuildManager {
	return &Manager{
		Builds:    builds,
		Cards:     cards,
		Config:    config,
		Converter: converter,
		Events:    events,
		Globals:   globals,
		Logs:      logs,
		Logz:      logz,
		Netrcs:    netrcs,
		Repos:     repos,
		Scheduler: scheduler,
		Secrets:   secrets,
		Status:    status,
		Stages:    stages,
		Steps:     steps,
		System:    system,
		Users:     users,
		Webhook:   webhook,
	}
}

// Manager 为构建运行器提供简化接口，便于与 Drone 服务器交互。
type Manager struct {
	Builds    core.BuildStore
	Cards     core.CardStore
	Config    core.ConfigService
	Converter core.ConvertService
	Events    core.Pubsub
	Globals   core.GlobalSecretStore
	Logs      core.LogStore
	Logz      core.LogStream
	Netrcs    core.NetrcService
	Repos     core.RepositoryStore
	Scheduler core.Scheduler
	Secrets   core.SecretStore
	Status    core.StatusService
	Stages    core.StageStore
	Steps     core.StepStore
	System    *core.System
	Users     core.UserStore
	Webhook   core.WebhookSender
}

// Request 从调度器请求与过滤条件匹配的下一个可用构建阶段。
func (m *Manager) Request(ctx context.Context, args *Request) (*core.Stage, error) {
	logger := logrus.WithFields(
		logrus.Fields{
			"kind":    args.Kind,
			"type":    args.Type,
			"os":      args.OS,
			"arch":    args.Arch,
			"kernel":  args.Kernel,
			"variant": args.Variant,
		},
	)
	logger.Debugln("manager: request queue item")

	stage, err := m.Scheduler.Request(ctx, core.Filter{
		Kind:    args.Kind,
		Type:    args.Type,
		OS:      args.OS,
		Arch:    args.Arch,
		Kernel:  args.Kernel,
		Variant: args.Variant,
		Labels:  args.Labels,
	})
	if err != nil && ctx.Err() != nil {
		logger.Debugln("manager: context canceled")
		return nil, err
	}
	if err != nil {
		logger = logrus.WithError(err)
		logger.Warnln("manager: request queue item error")
		return nil, err
	}
	return stage, nil
}

// Accept 接受构建阶段执行权；多 Agent 竞争时通过数据库乐观锁保证唯一执行。
func (m *Manager) Accept(ctx context.Context, id int64, machine string) (*core.Stage, error) {
	logger := logrus.WithFields(
		logrus.Fields{
			"stage-id": id,
			"machine":  machine,
		},
	)
	logger.Debugln("manager: accept stage")

	stage, err := m.Stages.Find(noContext, id)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find stage")
		return nil, err
	}
	if stage.Machine != "" {
		logger.Debugln("manager: stage already assigned. abort.")
		return nil, db.ErrOptimisticLock
	}

	stage.Machine = machine
	stage.Status = core.StatusPending
	stage.Updated = time.Now().Unix()

	err = m.Stages.Update(noContext, stage)
	if err == db.ErrOptimisticLock {
		logger = logger.WithError(err)
		logger.Debugln("manager: stage processed by another agent")
	} else if err != nil {
		logger = logger.WithError(err)
		logger.Debugln("manager: cannot update stage")
	} else {
		logger.Debugln("manager: stage accepted")
	}
	return stage, err
}

// handleDetailsError 在 Details 失败时将阶段标记为错误并触发收尾，确保状态持久化。
func (m *Manager) handleDetailsError(ctx context.Context, stage *core.Stage, err error) (*Context, error) {
	logrus.WithFields(logrus.Fields{
		"stage.id":      stage.ID,
		"stage.version": stage.Version,
		"error":         err,
	}).Warnln("manager: details failed, marking stage as error")

	stage.Status = core.StatusError
	stage.Error = err.Error()
	stage.Stopped = time.Now().Unix()
	stage.Updated = time.Now().Unix()
	if len(stage.Error) > 500 {
		stage.Error = stage.Error[:500]
	}

	if dbErr := m.Stages.Update(noContext, stage); dbErr != nil {
		logrus.WithError(dbErr).
			WithField("stage.id", stage.ID).
			WithField("stage.version", stage.Version).
			Warnln("manager: failed to mark stage as error after details failure")
	}

	if afterErr := m.AfterAll(noContext, stage); afterErr != nil {
		logrus.WithError(afterErr).
			WithField("stage.id", stage.ID).
			Warnln("manager: failed teardown after details error")
	}

	return nil, err
}

// Details 加载并组装指定阶段的完整构建上下文（仓库、配置、密钥、YAML 转换等）。
func (m *Manager) Details(ctx context.Context, id int64) (*Context, error) {
	logger := logrus.WithField("step-id", id)
	logger.Debugln("manager: fetching stage details")

	stage, err := m.Stages.Find(noContext, id)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find stage")
		return nil, err
	}
	build, err := m.Builds.Find(noContext, stage.BuildID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find build")
		return m.handleDetailsError(ctx, stage, err)
	}
	stages, err := m.Stages.List(ctx, stage.BuildID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot list stages")
		return m.handleDetailsError(ctx, stage, err)
	}
	build.Stages = stages
	repo, err := m.Repos.Find(noContext, build.RepoID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find repository")
		return m.handleDetailsError(ctx, stage, err)
	}
	logger = logger.WithFields(
		logrus.Fields{
			"build": build.Number,
			"repo":  repo.Slug,
		},
	)
	user, err := m.Users.Find(noContext, repo.UserID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find repository owner")
		return m.handleDetailsError(ctx, stage, err)
	}
	config, err := m.Config.Find(noContext, &core.ConfigArgs{
		User:  user,
		Repo:  repo,
		Build: build,
	})
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find configuration")
		return m.handleDetailsError(ctx, stage, err)
	}

	// this code is temporarily in place to detect and convert
	// the legacy yaml configuration file to the new format.
	config.Data, _ = converter.ConvertString(config.Data, converter.Metadata{
		Filename: repo.Config,
		URL:      repo.Link,
		Ref:      build.Ref,
	})

	config, err = m.Converter.Convert(noContext, &core.ConvertArgs{
		Build:  build,
		Config: config,
		Repo:   repo,
		User:   user,
	})
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot convert configuration")
		return m.handleDetailsError(ctx, stage, err)
	}
	var secrets []*core.Secret
	tmpSecrets, err := m.Secrets.List(noContext, repo.ID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot list secrets")
		return m.handleDetailsError(ctx, stage, err)
	}
	tmpGlobalSecrets, err := m.Globals.List(noContext, repo.Namespace)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot list global secrets")
		return m.handleDetailsError(ctx, stage, err)
	}
	// TODO(bradrydzewski) can we delegate filtering
	// secrets to the agent? If not, we should add
	// unit tests.
	for _, secret := range tmpSecrets {
		if secret.PullRequest == false &&
			build.Event == core.EventPullRequest {
			continue
		}
		secrets = append(secrets, secret)
	}
	for _, secret := range tmpGlobalSecrets {
		if secret.PullRequest == false &&
			build.Event == core.EventPullRequest {
			continue
		}
		secrets = append(secrets, secret)
	}
	return &Context{
		Repo:    repo,
		Build:   build,
		Stage:   stage,
		Secrets: secrets,
		System:  m.System,
		Config:  &core.File{Data: []byte(config.Data)},
	}, nil
}

// Before 在步骤开始前创建日志流并通过 updater 持久化步骤状态。
func (m *Manager) Before(ctx context.Context, step *core.Step) error {
	logger := logrus.WithFields(
		logrus.Fields{
			"step.status": step.Status,
			"step.name":   step.Name,
			"step.id":     step.ID,
		},
	)
	logger.Debugln("manager: updating step status")

	err := m.Logz.Create(noContext, step.ID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot create log stream")
		return err
	}
	updater := &updater{
		Builds:  m.Builds,
		Events:  m.Events,
		Repos:   m.Repos,
		Steps:   m.Steps,
		Stages:  m.Stages,
		Webhook: m.Webhook,
	}
	return updater.do(ctx, step)
}

// After 在步骤完成后更新状态并删除临时日志流。
func (m *Manager) After(ctx context.Context, step *core.Step) error {
	logger := logrus.WithFields(
		logrus.Fields{
			"step.status": step.Status,
			"step.name":   step.Name,
			"step.id":     step.ID,
		},
	)
	logger.Debugln("manager: updating step status")

	var errs error
	updater := &updater{
		Builds:  m.Builds,
		Events:  m.Events,
		Repos:   m.Repos,
		Steps:   m.Steps,
		Stages:  m.Stages,
		Webhook: m.Webhook,
	}

	if err := updater.do(ctx, step); err != nil {
		errs = multierror.Append(errs, err)
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot update step")
	}

	if err := m.Logz.Delete(noContext, step.ID); err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot teardown log stream")
	}
	return errs
}

// BeforeAll 委托 setup 执行阶段开始前的初始化流程。
func (m *Manager) BeforeAll(ctx context.Context, stage *core.Stage) error {
	s := &setup{
		Builds: m.Builds,
		Events: m.Events,
		Repos:  m.Repos,
		Steps:  m.Steps,
		Stages: m.Stages,
		Status: m.Status,
		Users:  m.Users,
	}
	return s.do(ctx, stage)
}

// AfterAll 委托 teardown 执行阶段完成后的收尾流程。
func (m *Manager) AfterAll(ctx context.Context, stage *core.Stage) error {
	t := &teardown{
		Builds:    m.Builds,
		Events:    m.Events,
		Logs:      m.Logz,
		Repos:     m.Repos,
		Scheduler: m.Scheduler,
		Steps:     m.Steps,
		Stages:    m.Stages,
		Status:    m.Status,
		Users:     m.Users,
		Webhook:   m.Webhook,
	}
	return t.do(ctx, stage)
}

// Netrc 生成包含有效未过期令牌、可用于克隆仓库的 netrc 凭据。
func (m *Manager) Netrc(ctx context.Context, id int64) (*core.Netrc, error) {
	logger := logrus.WithField("repo.id", id)

	repo, err := m.Repos.Find(ctx, id)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find repository")
		return nil, err
	}

	user, err := m.Users.Find(ctx, repo.UserID)
	if err != nil {
		logger = logger.WithError(err)
		logger.Warnln("manager: cannot find repository owner")
		return nil, err
	}

	netrc, err := m.Netrcs.Create(ctx, user, repo)
	if err != nil {
		logger = logger.WithError(err)
		logger = logger.WithField("repo.name", repo.Slug)
		logger.Warnln("manager: cannot generate netrc")
	}
	return netrc, err
}

// Watch 轮询调度器与数据库，检测构建是否已被取消或完成。
func (m *Manager) Watch(ctx context.Context, id int64) (bool, error) {
	ok, err := m.Scheduler.Cancelled(ctx, id)
	// we expect a context cancel error here which
	// indicates a polling timeout. The subscribing
	// client should look for the context cancel error
	// and resume polling.
	if err != nil {
		return ok, err
	}

	// // TODO (bradrydzewski) we should be able to return
	// // immediately if Cancelled returns true. This requires
	// // some more testing but would avoid the extra database
	// // call.
	// if ok {
	// 	return ok, err
	// }

	// if no error is returned we should check
	// the database to see if the build is complete. If
	// complete, return true.
	build, err := m.Builds.Find(ctx, id)
	if err != nil {
		logger := logrus.WithError(err)
		logger = logger.WithField("build-id", id)
		logger.Warnln("manager: cannot find build")
		return ok, err
	}
	return build.IsDone(), nil
}

// Write 向指定步骤的实时日志流写入一行。
func (m *Manager) Write(ctx context.Context, step int64, line *core.Line) error {
	err := m.Logz.Write(ctx, step, line)
	if err != nil {
		logger := logrus.WithError(err)
		logger = logger.WithField("step-id", step)
		logger.Warnln("manager: cannot write to log stream")
	}
	return err
}

// Upload 将步骤完整日志持久化到日志存储。
func (m *Manager) Upload(ctx context.Context, step int64, r io.Reader) error {
	err := m.Logs.Create(ctx, step, r)
	if err != nil {
		logger := logrus.WithError(err)
		logger = logger.WithField("step-id", step)
		logger.Warnln("manager: cannot upload complete logs")
	}
	return err
}

// UploadBytes 以字节形式上传并持久化步骤完整日志。
func (m *Manager) UploadBytes(ctx context.Context, step int64, data []byte) error {
	buf := bytes.NewBuffer(data)
	err := m.Logs.Create(ctx, step, buf)
	if err != nil {
		logger := logrus.WithError(err)
		logger = logger.WithField("step-id", step)
		logger.Warnln("manager: cannot upload complete logs")
	}
	return err
}

// UploadCard 为指定步骤创建并持久化可视化卡片。
func (m *Manager) UploadCard(ctx context.Context, stepId int64, input *core.CardInput) error {
	data := ioutil.NopCloser(
		bytes.NewBuffer(input.Data),
	)
	err := m.Cards.Create(ctx, stepId, data)
	if err != nil {
		logger := logrus.WithError(err)
		logger.Warnln("manager: cannot create card")
	}
	return nil
}
