// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// registry 包（非 OSS 构建）实现从 HTTP 端点获取镜像仓库凭据。
package registry

import (
	"context"

	"github.com/drone/drone-go/plugin/registry"
	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"
)

// EndpointSource 创建从 HTTP 端点获取镜像仓库凭据的 RegistryService。
func EndpointSource(endpoint, secret string, skipVerify bool) core.RegistryService {
	return &service{
		endpoint:   endpoint,
		secret:     secret,
		skipVerify: skipVerify,
	}
}

// service 通过 drone-go registry 插件客户端拉取凭据。
type service struct {
	endpoint   string
	secret     string
	skipVerify bool
}

// List 调用外部端点 API 并映射为 core.Registry 列表；endpoint 为空时跳过。
func (c *service) List(ctx context.Context, in *core.RegistryArgs) ([]*core.Registry, error) {
	if c.endpoint == "" {
		return nil, nil
	}
	logger := logger.FromContext(ctx)
	logger.Trace("registry: plugin: get credentials")

	req := &registry.Request{
		Repo:  toRepo(in.Repo),
		Build: toBuild(in.Build),
	}
	client := registry.Client(c.endpoint, c.secret, c.skipVerify)
	res, err := client.List(ctx, req)
	if err != nil {
		logger.WithError(err).Warn("registry: plugin: cannot get credentials")
		return nil, err
	}

	var registries []*core.Registry
	for _, registry := range res {
		registries = append(registries, &core.Registry{
			Address:  registry.Address,
			Username: registry.Username,
			Password: registry.Password,
		})
		logger.WithField("address", registry.Address).
			Trace("registry: plugin: found credentials")
	}
	return registries, nil
}
