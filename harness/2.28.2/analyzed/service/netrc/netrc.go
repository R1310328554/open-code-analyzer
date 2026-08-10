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

// netrc 包为 Git 克隆生成 .netrc 认证凭证。
package netrc

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

var _ core.NetrcService = (*Service)(nil)

// Service 实现 core.NetrcService，按 SCM 平台生成克隆认证信息。
type Service struct {
	client   *scm.Client
	renewer  core.Renewer
	private  bool
	username string
	password string
}

// New 构造 Netrc 服务，可配置全局用户名密码或 OAuth 令牌模式。
func New(
	client *scm.Client,
	renewer core.Renewer,
	private bool,
	username string,
	password string,
) core.NetrcService {
	return &Service{
		client:   client,
		renewer:  renewer,
		private:  private,
		username: username,
		password: password,
	}
}

// Create 为指定用户与仓库生成 .netrc 条目；公开仓库且未强制认证时返回 nil。
func (s *Service) Create(ctx context.Context, user *core.User, repo *core.Repository) (*core.Netrc, error) {
	// 公开仓库且未启用 always-auth 时无需提供凭证。
	if repo.Private == false && s.private == false {
		return nil, nil
	}

	netrc := new(core.Netrc)
	err := netrc.SetMachine(repo.HTTPURL)
	if err != nil {
		return nil, err
	}

	if s.username != "" && s.password != "" {
		netrc.Password = s.password
		netrc.Login = s.username
		return netrc, nil
	}

	// 强制刷新 OAuth 令牌，避免流水线执行期间令牌过期。
	err = s.renewer.Renew(ctx, user, true)
	if err != nil {
		return nil, err
	}

	switch s.client.Driver {
	case scm.DriverGitlab, scm.DriverGitee:
		netrc.Login = "oauth2"
		netrc.Password = user.Token
	case scm.DriverBitbucket:
		netrc.Login = "x-token-auth"
		netrc.Password = user.Token
	case scm.DriverGithub, scm.DriverGogs, scm.DriverGitea:
		netrc.Password = "x-oauth-basic"
		netrc.Login = user.Token
	}
	return netrc, nil
}
