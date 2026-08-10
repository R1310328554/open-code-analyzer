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

package main

import (
	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/go-login/login"
	"github.com/drone/go-login/login/bitbucket"
	"github.com/drone/go-login/login/gitea"
	"github.com/drone/go-login/login/gitee"
	"github.com/drone/go-login/login/github"
	"github.com/drone/go-login/login/gitlab"
	"github.com/drone/go-login/login/gogs"
	"github.com/drone/go-login/login/stash"
	"github.com/drone/go-scm/scm/transport/oauth2"
	"strings"

	"github.com/google/wire"
	"github.com/sirupsen/logrus"
)

// wire set for loading the authenticator.
// loginSet 定义 OAuth 登录认证中间件与令牌刷新器的 Wire 提供者集合。
var loginSet = wire.NewSet(
	provideLogin,
	provideRefresher,
)

// provideLogin 根据环境配置返回对应 SCM 平台的 OAuth 登录中间件。
func provideLogin(config config.Config) login.Middleware {
	switch {
	case config.Bitbucket.ClientID != "":
		return provideBitbucketLogin(config)
	case config.Github.ClientID != "":
		return provideGithubLogin(config)
	case config.Gitee.ClientID != "":
		return provideGiteeLogin(config)
	case config.Gitea.Server != "":
		return provideGiteaLogin(config)
	case config.GitLab.ClientID != "":
		return provideGitlabLogin(config)
	case config.Gogs.Server != "":
		return provideGogsLogin(config)
	case config.Stash.ConsumerKey != "":
		return provideStashLogin(config)
	}
	logrus.Fatalln("main: source code management system not configured")
	return nil
}

// provideBitbucketLogin 根据环境配置返回 Bitbucket Cloud OAuth 登录中间件。
func provideBitbucketLogin(config config.Config) login.Middleware {
	if config.Bitbucket.ClientID == "" {
		return nil
	}
	return &bitbucket.Config{
		ClientID:     config.Bitbucket.ClientID,
		ClientSecret: config.Bitbucket.ClientSecret,
		RedirectURL:  config.Server.Addr + "/login",
	}
}

// provideGithubLogin 根据环境配置返回 GitHub OAuth 登录中间件。
func provideGithubLogin(config config.Config) login.Middleware {
	if config.Github.ClientID == "" {
		return nil
	}
	return &github.Config{
		ClientID:     config.Github.ClientID,
		ClientSecret: config.Github.ClientSecret,
		Scope:        config.Github.Scope,
		Server:       config.Github.Server,
		Client:       defaultClient(config.Github.SkipVerify),
		Logger:       logrus.StandardLogger(),
	}
}

// provideGiteeLogin 根据环境配置返回 Gitee OAuth 登录中间件。
func provideGiteeLogin(config config.Config) login.Middleware {
	if config.Gitee.ClientID == "" {
		return nil
	}
	redirectURL := config.Gitee.RedirectURL
	if redirectURL == "" {
		redirectURL = config.Server.Addr + "/login"
	}
	return &gitee.Config{
		ClientID:     config.Gitee.ClientID,
		ClientSecret: config.Gitee.ClientSecret,
		RedirectURL:  redirectURL,
		Server:       config.Gitee.Server,
		Scope:        config.Gitee.Scope,
		Client:       defaultClient(config.Gitee.SkipVerify),
	}
}

// provideGiteaLogin 根据环境配置返回 Gitea OAuth 登录中间件。
func provideGiteaLogin(config config.Config) login.Middleware {
	if config.Gitea.Server == "" {
		return nil
	}
	redirectURL := config.Gitea.RedirectURL
	if redirectURL == "" {
		redirectURL = config.Server.Addr + "/login"
	}
	return &gitea.Config{
		ClientID:     config.Gitea.ClientID,
		ClientSecret: config.Gitea.ClientSecret,
		Server:       config.Gitea.Server,
		Client:       defaultClient(config.Gitea.SkipVerify),
		Logger:       logrus.StandardLogger(),
		RedirectURL:  redirectURL,
		Scope:        config.Gitea.Scope,
	}
}

// provideGitlabLogin 根据环境配置返回 GitLab OAuth 登录中间件。
func provideGitlabLogin(config config.Config) login.Middleware {
	if config.GitLab.ClientID == "" {
		return nil
	}
	return &gitlab.Config{
		ClientID:     config.GitLab.ClientID,
		ClientSecret: config.GitLab.ClientSecret,
		RedirectURL:  config.Server.Addr + "/login",
		Server:       config.GitLab.Server,
		Client:       defaultClient(config.GitLab.SkipVerify),
	}
}

// provideGogsLogin 根据环境配置返回 Gogs OAuth 登录中间件。
func provideGogsLogin(config config.Config) login.Middleware {
	if config.Gogs.Server == "" {
		return nil
	}
	return &gogs.Config{
		Label:  "drone",
		Login:  "/login/form",
		Server: config.Gogs.Server,
		Client: defaultClient(config.Gogs.SkipVerify),
	}
}

// provideStashLogin 根据环境配置返回 Stash OAuth 登录中间件。
func provideStashLogin(config config.Config) login.Middleware {
	if config.Stash.ConsumerKey == "" {
		return nil
	}
	privateKey, err := stash.ParsePrivateKeyFile(config.Stash.PrivateKey)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot parse Private Key file")
	}
	return &stash.Config{
		Address:        config.Stash.Server,
		ConsumerKey:    config.Stash.ConsumerKey,
		ConsumerSecret: config.Stash.ConsumerSecret,
		PrivateKey:     privateKey,
		CallbackURL:    config.Server.Addr + "/login",
		Client:         defaultClient(config.Stash.SkipVerify),
	}
}

// provideRefresher 为 Bitbucket、Gitea 等平台返回 OAuth 令牌刷新器。
func provideRefresher(config config.Config) *oauth2.Refresher {
	switch {
	case config.Bitbucket.ClientID != "":
		return &oauth2.Refresher{
			ClientID:     config.Bitbucket.ClientID,
			ClientSecret: config.Bitbucket.ClientSecret,
			Endpoint:     "https://bitbucket.org/site/oauth2/access_token",
			Source:       oauth2.ContextTokenSource(),
			Client:       defaultClient(config.Bitbucket.SkipVerify),
		}
	case config.Gitea.ClientID != "":
		return &oauth2.Refresher{
			ClientID:     config.Gitea.ClientID,
			ClientSecret: config.Gitea.ClientSecret,
			Endpoint:     strings.TrimSuffix(config.Gitea.Server, "/") + "/login/oauth/access_token",
			Source:       oauth2.ContextTokenSource(),
			Client:       defaultClient(config.Gitea.SkipVerify),
		}
	case config.GitLab.ClientID != "":
		return &oauth2.Refresher{
			ClientID:     config.GitLab.ClientID,
			ClientSecret: config.GitLab.ClientSecret,
			Endpoint:     strings.TrimSuffix(config.GitLab.Server, "/") + "/oauth/token",
			Source:       oauth2.ContextTokenSource(),
			Client:       defaultClient(config.GitLab.SkipVerify),
		}
	case config.Gitee.ClientID != "":
		return &oauth2.Refresher{
			ClientID:     config.Gitee.ClientID,
			ClientSecret: config.Gitee.ClientSecret,
			Endpoint:     strings.TrimSuffix(config.Gitee.Server, "/") + "/oauth/token",
			Source:       oauth2.ContextTokenSource(),
			Client:       defaultClient(config.Gitee.SkipVerify),
		}

	}
	return nil
}
