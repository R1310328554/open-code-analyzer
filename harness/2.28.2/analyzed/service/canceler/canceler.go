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

// canceler 包实现构建取消服务，协调存储、调度器、状态与 webhook 通知。
package canceler

import (
	"context"
	"encoding/json"
	"runtime/debug"
	"time"

	"github.com/drone/drone/core"

	"github.com/hashicorp/go-multierror"
	"github.com/sirupsen/logrus"
)

var noContext = context.Background()

// service 封装取消所需的全部依赖存储与服务。
type service struct {
	builds    core.BuildStore
	events    core.Pubsub
	repos     core.RepositoryStore
	scheduler core.Scheduler
	stages    core.StageStore
	status    core.StatusService
	steps     core.StepStore
	users     core.UserStore
	webhooks  core.WebhookSender
}

// New 创建实现 core.Canceler 的取消服务实例。
func New(
	builds core.BuildStore,
	events core.Pubsub,
	repos core.RepositoryStore,
	scheduler core.Scheduler,
	stages core.StageStore,
	status core.StatusService,
	steps core.StepStore,
	users core.UserStore,
	webhooks core.WebhookSender,
) core.Canceler {
	return &service{
		builds:    builds,
		events:    events,
		repos:     repos,
		scheduler: scheduler,
		stages:    stages,
		status:    status,
		steps:     steps,
		users:     users,
		webhooks:  webhooks,
	}
}

// Cancel 取消单个构建，将其状态设为 killed。
func (s *service) Cancel(ctx context.Context, repo *core.Repository, build *core.Build) error {
	return s.cancel(ctx, repo, build, core.StatusKilled)
}

// CancelPending 取消同一事件与引用下编号更低的待处理/运行中构建。
func (s *service) CancelPending(ctx context.Context, repo *core.Repository, build *core.Build) error {
	defer func() {
		if err := recover(); err != nil {
			debug.PrintStack()
		}
	}()

	// switch {
	// case repo.CancelPulls && build.Event == core.EventPullRequest:
	// case repo.CancelPush && build.Event == core.EventPush:
	// default:
	// 	return nil
	// }

	switch build.Event {
	// push 与 pull_request 事件支持系统自动取消旧构建
	case core.EventPush, core.EventPullRequest:
	default:
		return nil
	}

	// 从数据库获取所有仓库的未完成构建，再按规则过滤
	incomplete, err := s.repos.ListIncomplete(ctx)
	if err != nil {
		return err
	}

	var result error
	for _, item := range incomplete {
		if !match(build, item) {
			continue
		}

		err := s.cancel(ctx, repo, item.Build, core.StatusSkipped)
		if err != nil {
			result = multierror.Append(result, err)
		}
	}

	return result
}

// cancel 执行单次构建取消：更新状态、通知调度器、同步 SCM 状态并发布事件。
func (s *service) cancel(ctx context.Context, repo *core.Repository, build *core.Build, status string) error {
	logger := logrus.WithFields(
		logrus.Fields{
			"repo":   repo.Slug,
			"ref":    build.Ref,
			"build":  build.Number,
			"event":  build.Event,
			"status": build.Status,
		},
	)

	// 仅 pending/running 状态的构建可被取消
	switch build.Status {
	case core.StatusPending, core.StatusRunning:
	default:
		return nil
	}

	build.Status = status
	build.Finished = time.Now().Unix()
	if build.Started == 0 {
		build.Started = time.Now().Unix()
	}

	err := s.builds.Update(ctx, build)
	if err != nil {
		logger.WithError(err).
			Warnln("canceler: cannot update build status to cancelled")
		return err
	}

	// 通知调度器取消构建，Runner 订阅后将停止执行
	err = s.scheduler.Cancel(ctx, build.ID)
	if err != nil {
		logger.WithError(err).
			Warnln("canceler: cannot signal cancelled build is complete")
	}

	// 向远程 SCM 更新 commit 状态
	user, err := s.users.Find(ctx, repo.UserID)
	if err == nil {
		err := s.status.Send(ctx, user, &core.StatusInput{
			Repo:  repo,
			Build: build,
		})
		if err != nil {
			logger.WithError(err).
				Debugln("canceler: cannot set status")
		}
	}

	stages, err := s.stages.ListSteps(ctx, build.ID)
	if err != nil {
		logger.WithError(err).
			Debugln("canceler: cannot list build stages")
	}

	// 将所有未完成阶段与步骤标记为 killed 或 skipped
	for _, stage := range stages {
		if stage.IsDone() {
			continue
		}
		if stage.Started != 0 {
			stage.Status = core.StatusKilled
		} else {
			stage.Status = core.StatusSkipped
			stage.Started = time.Now().Unix()
		}
		stage.Stopped = time.Now().Unix()
		err := s.stages.Update(ctx, stage)
		if err != nil {
			logger.WithError(err).
				WithField("stage", stage.Number).
				Debugln("canceler: cannot update stage status")
		}

		for _, step := range stage.Steps {
			if step.IsDone() {
				continue
			}
			if step.Started != 0 {
				step.Status = core.StatusKilled
			} else {
				step.Status = core.StatusSkipped
				step.Started = time.Now().Unix()
			}
			step.Stopped = time.Now().Unix()
			step.ExitCode = 130
			err := s.steps.Update(ctx, step)
			if err != nil {
				logger.WithError(err).
					WithField("stage", stage.Number).
					WithField("step", step.Number).
					Debugln("canceler: cannot update step status")
			}
		}
	}

	logger.WithError(err).
		Debugln("canceler: successfully cancelled build")

	build.Stages = stages

	// 发布 pubsub 事件以实时刷新 UI
	repoCopy := new(core.Repository)
	*repoCopy = *repo
	repoCopy.Build = build
	repoCopy.Build.Stages = stages
	data, _ := json.Marshal(repoCopy)
	err = s.events.Publish(noContext, &core.Message{
		Repository: repo.Slug,
		Visibility: repo.Visibility,
		Data:       data,
	})
	if err != nil {
		logger.WithError(err).
			Warnln("canceler: cannot publish cancel event")
	}

	// 发送 webhook 通知外部系统
	payload := &core.WebhookData{
		Event:  core.WebhookEventBuild,
		Action: core.WebhookActionUpdated,
		Repo:   repo,
		Build:  build,
	}
	err = s.webhooks.Send(ctx, payload)
	if err != nil {
		logger.WithError(err).
			Warnln("manager: cannot send global webhook")
	}

	return nil
}
