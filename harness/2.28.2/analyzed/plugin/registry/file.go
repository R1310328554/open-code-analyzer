// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// registry 包（非 OSS 构建）实现从 .docker/config.json 文件读取镜像仓库凭据。
package registry

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/plugin/registry/auths"

	"github.com/sirupsen/logrus"
)

// FileSource 从 .docker/config.json 文件读取镜像仓库凭据。
func FileSource(path string) core.RegistryService {
	return &registryConfig{
		path: path,
	}
}

// registryConfig 持有 Docker 配置文件路径。
type registryConfig struct {
	path string
}

// List 解析指定路径的 Docker 配置；路径为空时跳过。
func (r *registryConfig) List(ctx context.Context, req *core.RegistryArgs) ([]*core.Registry, error) {
	// .docker/config.json 路径为可选配置，空字符串时忽略。
	if r.path == "" {
		return nil, nil
	}

	logger := logrus.WithField("config", r.path)
	logger.Traceln("registry: parsing docker config.json file")

	regs, err := auths.ParseFile(r.path)
	if err != nil {
		logger.WithError(err).Errorln("registry: cannot parse docker config.json file")
		return nil, err
	}

	return regs, err
}
