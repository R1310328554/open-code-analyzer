// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

//go:build !oss
// +build !oss

// config 包（非 OSS 构建）实现从远程 HTTP 插件端点拉取流水线 YAML 配置。
package config

import (
	"context"
	"time"

	"github.com/drone/drone-go/drone"
	"github.com/drone/drone-go/plugin/config"

	"github.com/drone/drone/core"
)

// Global 创建从远程 endpoint 获取 YAML 配置的 ConfigService；endpoint 为空时返回禁用实例。
func Global(endpoint, signer string, skipVerify bool, timeout time.Duration) core.ConfigService {
	if endpoint == "" {
		return new(global)
	}
	return &global{
		client: config.Client(
			endpoint,
			signer,
			skipVerify,
		),
		timeout: timeout,
	}
}

// global 通过 HTTP 插件客户端向外部服务请求构建配置。
type global struct {
	client  config.Plugin
	timeout time.Duration
}

// Find 向远程配置插件发起请求，将仓库与构建上下文转换为 drone 请求格式并返回配置内容。
func (g *global) Find(ctx context.Context, in *core.ConfigArgs) (*core.Config, error) {
	if g.client == nil {
		return nil, nil
	}
	// 为 API 调用设置超时，防止外部服务无响应时阻塞构建流程（默认 1 分钟）。
	ctx, cancel := context.WithTimeout(ctx, g.timeout)
	defer cancel()

	req := &config.Request{
		Repo:  toRepo(in.Repo),
		Build: toBuild(in.Build),
		Token: drone.Token{
			Access:  in.User.Token,
			Refresh: in.User.Refresh,
		},
	}

	res, err := g.client.Find(ctx, req)
	if err != nil {
		return nil, err
	}

	// 无错误但 Data 为空表示远端返回 204 No Content，应视为无配置而非错误。
	if res.Data == "" {
		return nil, nil
	}

	return &core.Config{
		Kind: res.Kind,
		Data: res.Data,
	}, nil
}

// toRepo 将 core.Repository 转换为 drone-go 插件请求所需的 Repo 结构。
func toRepo(from *core.Repository) drone.Repo {
	return drone.Repo{
		ID:         from.ID,
		UID:        from.UID,
		UserID:     from.UserID,
		Namespace:  from.Namespace,
		Name:       from.Name,
		Slug:       from.Slug,
		SCM:        from.SCM,
		HTTPURL:    from.HTTPURL,
		SSHURL:     from.SSHURL,
		Link:       from.Link,
		Branch:     from.Branch,
		Private:    from.Private,
		Visibility: from.Visibility,
		Active:     from.Active,
		Config:     from.Config,
		Trusted:    from.Trusted,
		Protected:  from.Protected,
		Timeout:    from.Timeout,
	}
}

// toBuild 将 core.Build 转换为 drone-go 插件请求所需的 Build 结构。
func toBuild(from *core.Build) drone.Build {
	return drone.Build{
		ID:           from.ID,
		RepoID:       from.RepoID,
		Trigger:      from.Trigger,
		Number:       from.Number,
		Parent:       from.Parent,
		Status:       from.Status,
		Error:        from.Error,
		Event:        from.Event,
		Action:       from.Action,
		Link:         from.Link,
		Timestamp:    from.Timestamp,
		Title:        from.Title,
		Message:      from.Message,
		Before:       from.Before,
		After:        from.After,
		Ref:          from.Ref,
		Fork:         from.Fork,
		Source:       from.Source,
		Target:       from.Target,
		Author:       from.Author,
		AuthorName:   from.AuthorName,
		AuthorEmail:  from.AuthorEmail,
		AuthorAvatar: from.AuthorAvatar,
		Sender:       from.Sender,
		Params:       from.Params,
		Deploy:       from.Deploy,
		Started:      from.Started,
		Finished:     from.Finished,
		Created:      from.Created,
		Updated:      from.Updated,
		Version:      from.Version,
	}
}
