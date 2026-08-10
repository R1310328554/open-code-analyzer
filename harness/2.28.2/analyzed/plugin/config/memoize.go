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

// +build !oss

package config

import (
	"context"
	"fmt"

	"github.com/drone/drone/core"

	lru "github.com/hashicorp/golang-lru"
	"github.com/sirupsen/logrus"
)

// keyf 是 LRU 缓存键格式，由仓库 ID、构建元数据与配置路径等字段拼接而成。
const keyf = "%d|%d|%s|%s|%s|%s|%s"

// Memoize 为底层 ConfigService 增加 LRU 缓存，避免多流水线项目重复拉取与转换同一配置。
func Memoize(base core.ConfigService) core.ConfigService {
	// 容量 10 的简单 LRU，防止短时间内对同一 YAML 重复请求。
	cache, _ := lru.New(10)
	return &memoize{base: base, cache: cache}
}

// memoize 包装基础配置服务并在命中缓存时直接返回已解析结果。
type memoize struct {
	base  core.ConfigService
	cache *lru.Cache
}

// Find 生成缓存键并查 LRU；未命中时委托 base 查找，成功且 commit SHA 非空则写入缓存。
func (c *memoize) Find(ctx context.Context, req *core.ConfigArgs) (*core.Config, error) {
	// 若底层为已禁用的 global 服务（client 为 nil），跳过缓存直接返回。
	if global, ok := c.base.(*global); ok == true && global.client == nil {
		return nil, nil
	}

	// 根据仓库与构建上下文生成唯一缓存键。
	key := fmt.Sprintf(keyf,
		req.Repo.ID,
		req.Build.Created,
		req.Build.Event,
		req.Build.Action,
		req.Build.Ref,
		req.Build.After,
		req.Repo.Config,
	)

	logger := logrus.WithField("repo", req.Repo.Slug).
		WithField("build", req.Build.Event).
		WithField("action", req.Build.Action).
		WithField("ref", req.Build.Ref).
		WithField("rev", req.Build.After).
		WithField("config", req.Repo.Config)

	logger.Trace("extension: configuration: check cache")

	// 缓存命中则直接返回已存储的配置对象。
	cached, ok := c.cache.Get(key)
	if ok {
		logger.Trace("extension: configuration: cache hit")
		return cached.(*core.Config), nil
	}

	logger.Trace("extension: configuration: cache miss")

	// 缓存未命中，委托底层服务获取配置。
	config, err := c.base.Find(ctx, req)
	if err != nil {
		return nil, err
	}

	if config == nil {
		return nil, nil
	}
	if config.Data == "" {
		return nil, nil
	}

	// 成功获取配置后写入缓存；commit SHA 为空（如 Gogs）时不缓存。
	if req.Build.After != "" {
		c.cache.Add(key, config)
	}

	return config, nil
}
