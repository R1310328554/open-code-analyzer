// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// registry 包（非 OSS 构建）实现通过外部 Secret 插件获取 image_pull_secrets。
package registry

import (
	"context"
	"time"

	"github.com/drone/drone-go/plugin/secret"
	"github.com/drone/drone-yaml/yaml"
	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"
	"github.com/drone/drone/plugin/registry/auths"

	droneapi "github.com/drone/drone-go/drone"
)

// External 返回通过外部 Secret 插件获取 image_pull_secrets 的 RegistryService。
func External(endpoint, secret string, skipVerify bool) core.RegistryService {
	return &externalController{
		endpoint:   endpoint,
		secret:     secret,
		skipVerify: skipVerify,
	}
}

// externalController 按 YAML 中 secret.get 配置调用外部服务。
type externalController struct {
	endpoint   string
	secret     string
	skipVerify bool
}

// List 遍历 PullSecrets，从外部 API 获取并解析 Docker auths 格式凭据。
func (c *externalController) List(ctx context.Context, in *core.RegistryArgs) ([]*core.Registry, error) {
	var results []*core.Registry

	for _, match := range in.Pipeline.PullSecrets {
		logger := logger.FromContext(ctx).
			WithField("name", match).
			WithField("kind", "secret").
			WithField("secret", c.endpoint)
		logger.Trace("image_pull_secrets: find secret")

		// 在 manifest 中查找命名 Secret；未找到则返回 nil，交由链中下一控制器处理。
		path, name, ok := getExternal(in.Conf, match)
		if !ok {
			logger.Trace("image_pull_secrets: no matching secret resource in yaml")
			return nil, nil
		}

		logger = logger.
			WithField("get.path", path).
			WithField("get.name", name)

		// 为 API 调用设置超时，防止外部服务无响应时阻塞构建流程（1 分钟）。
		ctx, cancel := context.WithTimeout(ctx, time.Minute)
		defer cancel()

		req := &secret.Request{
			Name:  name,
			Path:  path,
			Repo:  toRepo(in.Repo),
			Build: toBuild(in.Build),
		}
		client := secret.Client(c.endpoint, c.secret, c.skipVerify)
		res, err := client.Find(ctx, req)
		if err != nil {
			logger.WithError(err).Trace("image_pull_secrets: cannot get secret")
			return nil, err
		}

		// 无错误但 Data 为空表示远端返回 204 No Content，应视为无凭据而非错误。
		if res.Data == "" {
			return nil, nil
		}

		// 密钥可限制为非 Pull Request 事件可用；PR 构建时返回空结果。
		if (res.Pull == false && res.PullRequest == false) &&
			in.Build.Event == core.EventPullRequest {
			logger.WithError(err).Trace("image_pull_secrets: pull_request access denied")
			return nil, nil
		}

		parsed, err := auths.ParseString(res.Data)
		if err != nil {
			return nil, err
		}

		logger.Trace("image_pull_secrets: found secret")
		results = append(results, parsed...)
	}

	return results, nil
}

// getExternal 在 manifest 中查找带 get.path/get.name 的 Secret 资源。
func getExternal(manifest *yaml.Manifest, match string) (path, name string, ok bool) {
	for _, resource := range manifest.Resources {
		secret, ok := resource.(*yaml.Secret)
		if !ok {
			continue
		}
		if secret.Name != match {
			continue
		}
		if secret.Get.Name == "" && secret.Get.Path == "" {
			continue
		}
		return secret.Get.Path, secret.Get.Name, true
	}
	return
}

// toRepo 将 core.Repository 转换为 drone-go API 类型。
func toRepo(from *core.Repository) droneapi.Repo {
	return droneapi.Repo{
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

// toBuild 将 core.Build 转换为 drone-go API 类型。
func toBuild(from *core.Build) droneapi.Build {
	return droneapi.Build{
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
