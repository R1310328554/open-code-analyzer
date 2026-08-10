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
	"crypto/rsa"
	"crypto/tls"
	"crypto/x509"
	"encoding/pem"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
	"strings"

	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/go-scm/scm"
	"github.com/drone/go-scm/scm/driver/bitbucket"
	"github.com/drone/go-scm/scm/driver/gitea"
	"github.com/drone/go-scm/scm/driver/gitee"
	"github.com/drone/go-scm/scm/driver/github"
	"github.com/drone/go-scm/scm/driver/gitlab"
	"github.com/drone/go-scm/scm/driver/gogs"
	"github.com/drone/go-scm/scm/driver/stash"
	"github.com/drone/go-scm/scm/transport/oauth1"
	"github.com/drone/go-scm/scm/transport/oauth2"

	"github.com/google/wire"
	"github.com/sirupsen/logrus"
)

// wire set for loading the scm client.
// clientSet 定义 SCM 客户端相关的 Wire 提供者集合。
var clientSet = wire.NewSet(
	provideClient,
)

// provideClient 根据环境配置返回对应的源代码管理（SCM）客户端。
func provideClient(config config.Config) *scm.Client {
	switch {
	case config.Bitbucket.ClientID != "":
		return provideBitbucketClient(config)
	case config.Github.ClientID != "":
		return provideGithubClient(config)
	case config.Gitee.ClientID != "":
		return provideGiteeClient(config)
	case config.Gitea.Server != "":
		return provideGiteaClient(config)
	case config.GitLab.ClientID != "":
		return provideGitlabClient(config)
	case config.Gogs.Server != "":
		return provideGogsClient(config)
	case config.Stash.ConsumerKey != "":
		return provideStashClient(config)
	}
	logrus.Fatalln("main: source code management system not configured")
	return nil
}

// provideBitbucketClient 根据环境配置返回 Bitbucket Cloud SCM 客户端。
func provideBitbucketClient(config config.Config) *scm.Client {
	client := bitbucket.NewDefault()
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Source: &oauth2.Refresher{
				ClientID:     config.Bitbucket.ClientID,
				ClientSecret: config.Bitbucket.ClientSecret,
				Endpoint:     "https://bitbucket.org/site/oauth2/access_token",
				Source:       oauth2.ContextTokenSource(),
			},
		},
	}
	if config.Bitbucket.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	return client
}

// provideGithubClient 根据环境配置返回 GitHub SCM 客户端。
func provideGithubClient(config config.Config) *scm.Client {
	client, err := github.New(config.Github.APIServer)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the GitHub client")
	}
	if config.Github.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Source: oauth2.ContextTokenSource(),
			Base:   defaultTransport(config.Github.SkipVerify),
		},
	}
	return client
}

// provideGiteeClient 根据环境配置返回 Gitee SCM 客户端。
func provideGiteeClient(config config.Config) *scm.Client {
	client, err := gitee.New(config.Gitee.APIServer)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the Gitee client")
	}
	if config.Gitee.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Scheme: oauth2.SchemeBearer,
			Source: &oauth2.Refresher{
				ClientID:     config.Gitee.ClientID,
				ClientSecret: config.Gitee.ClientSecret,
				Endpoint:     strings.TrimSuffix(config.Gitee.Server, "/") + "/oauth/token",
				Source:       oauth2.ContextTokenSource(),
			},
			Base: defaultTransport(config.Gitee.SkipVerify),
		},
	}
	return client
}

// provideGiteaClient 根据环境配置返回 Gitea SCM 客户端。
func provideGiteaClient(config config.Config) *scm.Client {
	client, err := gitea.New(config.Gitea.Server)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the Gitea client")
	}
	if config.Gitea.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Scheme: oauth2.SchemeBearer,
			Source: &oauth2.Refresher{
				ClientID:     config.Gitea.ClientID,
				ClientSecret: config.Gitea.ClientSecret,
				Endpoint:     strings.TrimSuffix(config.Gitea.Server, "/") + "/login/oauth/access_token",
				Source:       oauth2.ContextTokenSource(),
			},
			Base: defaultTransport(config.Gitea.SkipVerify),
		},
	}
	return client
}

// provideGitlabClient 根据环境配置返回 GitLab SCM 客户端。
func provideGitlabClient(config config.Config) *scm.Client {
	logrus.WithField("server", config.GitLab.Server).
		WithField("client", config.GitLab.ClientID).
		WithField("skip_verify", config.GitLab.SkipVerify).
		Debugln("main: creating the GitLab client")

	client, err := gitlab.New(config.GitLab.Server)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the GitLab client")
	}
	if config.GitLab.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Scheme: oauth2.SchemeBearer,
			Source: &oauth2.Refresher{
				ClientID:     config.GitLab.ClientID,
				ClientSecret: config.GitLab.ClientSecret,
				Endpoint:     strings.TrimSuffix(config.GitLab.Server, "/") + "/oauth/token",
				Source:       oauth2.ContextTokenSource(),
			},
			Base:   defaultTransport(config.GitLab.SkipVerify),
		},
	}
	return client
}

// provideGogsClient 根据环境配置返回 Gogs SCM 客户端。
func provideGogsClient(config config.Config) *scm.Client {
	logrus.WithField("server", config.Gogs.Server).
		WithField("skip_verify", config.Gogs.SkipVerify).
		Debugln("main: creating the Gogs client")

	client, err := gogs.New(config.Gogs.Server)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the Gogs client")
	}
	if config.Gogs.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth2.Transport{
			Scheme: oauth2.SchemeToken,
			Source: oauth2.ContextTokenSource(),
			Base:   defaultTransport(config.Gogs.SkipVerify),
		},
	}
	return client
}

// provideStashClient 根据环境配置返回 Atlassian Stash SCM 客户端。
func provideStashClient(config config.Config) *scm.Client {
	logrus.WithField("server", config.Stash.Server).
		WithField("skip_verify", config.Stash.SkipVerify).
		Debugln("main: creating the Stash client")

	privateKey, err := parsePrivateKeyFile(config.Stash.PrivateKey)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot parse the Stash Private Key")
	}
	client, err := stash.New(config.Stash.Server)
	if err != nil {
		logrus.WithError(err).
			Fatalln("main: cannot create the Stash client")
	}
	if config.Stash.Debug {
		client.DumpResponse = httputil.DumpResponse
	}
	client.Client = &http.Client{
		Transport: &oauth1.Transport{
			ConsumerKey: config.Stash.ConsumerKey,
			PrivateKey:  privateKey,
			Source:      oauth1.ContextTokenSource(),
			Base:        defaultTransport(config.Stash.SkipVerify),
		},
	}
	return client
}

// defaultClient 返回默认 HTTP 客户端；skipverify 为 true 时跳过 SSL 验证。
func defaultClient(skipverify bool) *http.Client {
	client := &http.Client{}
	client.Transport = defaultTransport(skipverify)
	return client
}

// defaultTransport 返回默认 HTTP 传输层；skipverify 为 true 时跳过 SSL 验证。
func defaultTransport(skipverify bool) http.RoundTripper {
	return &http.Transport{
		Proxy: http.ProxyFromEnvironment,
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: skipverify,
		},
	}
}

// parsePrivateKeyFile 解析 PEM 格式编码的 RSA 私钥文件。
func parsePrivateKeyFile(path string) (*rsa.PrivateKey, error) {
	d, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}
	return parsePrivateKey(d)
}

// parsePrivateKey 解析 PEM 格式编码的 RSA 私钥字节数据。
func parsePrivateKey(data []byte) (*rsa.PrivateKey, error) {
	p, _ := pem.Decode(data)
	return x509.ParsePKCS1PrivateKey(p.Bytes)
}
