// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// cache 包为非 OSS 构建提供带内存 LRU 缓存的文件内容服务包装层。
package cache

import (
	"context"
	"fmt"

	"github.com/drone/drone/core"

	"github.com/hashicorp/golang-lru"
)

// contentKey 缓存键格式：仓库 slug、提交 SHA 与文件路径。
const contentKey = "%s/%s/%s"

// Contents 返回包装了内存 LRU 缓存的 FileService 实例。
func Contents(base core.FileService) core.FileService {
	// 简单 LRU 缓存避免短时间内重复请求同一 YAML 文件。
	cache, _ := lru.New(25)
	return &service{
		service: base,
		cache:   cache,
	}
}

// service 在底层 FileService 之上叠加 LRU 缓存。
type service struct {
	cache   *lru.Cache
	service core.FileService
	user    *core.User
}

// Find 按仓库、提交与路径查找文件，命中缓存则直接返回。
func (s *service) Find(ctx context.Context, user *core.User, repo, commit, ref, path string) (*core.File, error) {
	key := fmt.Sprintf(contentKey, repo, commit, path)
	cached, ok := s.cache.Get(key)
	if ok {
		return cached.(*core.File), nil
	}
	file, err := s.service.Find(ctx, user, repo, commit, ref, path)
	if err != nil {
		return nil, err
	}
	s.cache.Add(key, file)
	return file, nil
}
