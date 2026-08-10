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

// contents 包通过 SCM 客户端从远程仓库获取文件内容。
package contents

import (
	"context"
	"strings"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// attempts 获取文件失败时的默认重试次数。
var attempts = 3

// wait 每次重试之间的默认等待时长。
var wait = time.Second * 15

// New 构造基于 SCM 客户端的 FileService 实例。
func New(client *scm.Client, renewer core.Renewer) core.FileService {
	return &service{
		client:   client,
		renewer:  renewer,
		attempts: attempts,
		wait:     wait,
	}
}

// service 封装 SCM 客户端与令牌续期逻辑，支持带退避的重试。
type service struct {
	renewer  core.Renewer
	client   *scm.Client
	attempts int
	wait     time.Duration
}

// Find 从 SCM 获取指定路径文件内容，含 Gogs 平台兼容处理。
func (s *service) Find(ctx context.Context, user *core.User, repo, commit, ref, path string) (*core.File, error) {
	// TODO(gogs) 暂不支持通过 Pull Request ref 获取 YAML。
	// TODO(gogs) ability to fetch a yaml by pull request ref.
	// it is not currently possible to fetch the yaml
	// configuration file from a pull request sha. This
	// workaround defaults to master.
	if s.client.Driver == scm.DriverGogs &&
		strings.HasPrefix(ref, "refs/pull") {
		commit = "master"
	}
	// TODO(gogs) 暂不支持通过 commit SHA 获取 tag 下文件。
	// TODO(gogs) ability to fetch a file in tag from commit sha.
	// this is a workaround for gogs which does not allow
	// fetching a file by commit sha for a tag. This forces
	// fetching a file by reference instead.
	if s.client.Driver == scm.DriverGogs &&
		strings.HasPrefix(ref, "refs/tag") {
		commit = ref
	}
	err := s.renewer.Renew(ctx, user, false)
	if err != nil {
		return nil, err
	}
	ctx = context.WithValue(ctx, scm.TokenKey{}, &scm.Token{
		Token:   user.Token,
		Refresh: user.Refresh,
	})
	content, err := s.findRetry(ctx, repo, path, commit)
	if err != nil {
		return nil, err
	}
	return &core.File{
		Data: content.Data,
		Hash: []byte{},
	}, nil
}

// findRetry 带退避重试获取 YAML 配置，应对 GitHub 数据存储的最终一致性延迟。
func (s *service) findRetry(ctx context.Context, repo, path, commit string) (content *scm.Content, err error) {
	for i := 0; i < s.attempts; i++ {
		content, _, err = s.client.Contents.Find(ctx, repo, path, commit)
		// 成功则立即返回，无需继续重试。
		if err == nil {
			return
		}
		// 失败后等待再重试；GitHub 建议总计约 30 秒，此处 3×15 秒共 45 秒。
		time.Sleep(s.wait)
	}
	return
}
