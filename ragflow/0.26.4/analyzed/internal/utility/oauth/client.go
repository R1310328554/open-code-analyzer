//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

// Package oauth 将 Python api/apps/auth 认证客户端移植到 Go。
// 统一 oauth2 / oidc / github 三种 OAuth 风格于 Client 接口。
// 当前不校验 id_token 签名，仅通过 access_token 调用 /userinfo。
package oauth

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// Config 为 OAuth 渠道配置，独立于 server 包以避免循环依赖。
type Config struct {
	Type             string
	ClientID         string
	ClientSecret     string
	AuthorizationURL string
	TokenURL         string
	UserinfoURL      string
	RedirectURI      string
	Scope            string
	Issuer           string
}

// UserInfo 为归一化用户资料；Email 为回调必需字段，其余尽力填充。
type UserInfo struct {
	Email     string `json:"email"`
	Username  string `json:"username"`
	Nickname  string `json:"nickname"`
	AvatarURL string `json:"avatar_url"`
}

// Client 为登录与回调 Handler 使用的认证客户端接口。
type Client interface {
	AuthorizationURL(state string) (string, error)
	ExchangeCodeForToken(ctx context.Context, code string) (*TokenResponse, error)
	FetchUserInfo(ctx context.Context, accessToken, idToken string) (*UserInfo, error)
}

// TokenResponse 为令牌端点响应中使用的字段子集。
type TokenResponse struct {
	AccessToken string `json:"access_token"`
	TokenType   string `json:"token_type,omitempty"`
	IDToken     string `json:"id_token,omitempty"`
	ExpiresIn   int    `json:"expires_in,omitempty"`
	Scope       string `json:"scope,omitempty"`
}

// HTTPRequestTimeout 为 token/userinfo 请求超时，对齐 Python 7 秒。
const HTTPRequestTimeout = 7 * time.Second

// NewClient 按 cfg.Type 返回对应 Client；空类型时 Issuer 存在则选 OIDC。
func NewClient(cfg Config) (Client, error) {
	t := strings.ToLower(strings.TrimSpace(cfg.Type))
	if t == "" {
		if cfg.Issuer != "" {
			t = "oidc"
		} else {
			t = "oauth2"
		}
	}
	switch t {
	case "oauth2":
		return newOAuthClient(cfg)
	case "oidc":
		return newOIDCClient(cfg)
	case "github":
		return newGitHubClient(cfg)
	default:
		return nil, fmt.Errorf("unsupported type: %s", t)
	}
}

// oauthClient 为基础 OAuth 2.0 实现；OIDC/GitHub 嵌入并覆盖 FetchUserInfo。
type oauthClient struct {
	cfg        Config
	httpClient *http.Client
}

func newOAuthClient(cfg Config) (*oauthClient, error) {
	if cfg.ClientID == "" {
		return nil, fmt.Errorf("oauth: client_id is required")
	}
	if cfg.AuthorizationURL == "" {
		return nil, fmt.Errorf("oauth: authorization_url is required")
	}
	if cfg.TokenURL == "" {
		return nil, fmt.Errorf("oauth: token_url is required")
	}
	if cfg.RedirectURI == "" {
		return nil, fmt.Errorf("oauth: redirect_uri is required")
	}
	return &oauthClient{
		cfg:        cfg,
		httpClient: &http.Client{Timeout: HTTPRequestTimeout},
	}, nil
}

// AuthorizationURL 构建浏览器跳转的授权 URL。
func (c *oauthClient) AuthorizationURL(state string) (string, error) {
	params := url.Values{}
	params.Set("client_id", c.cfg.ClientID)
	params.Set("redirect_uri", c.cfg.RedirectURI)
	params.Set("response_type", "code")
	if c.cfg.Scope != "" {
		params.Set("scope", c.cfg.Scope)
	}
	if state != "" {
		params.Set("state", state)
	}
	sep := "?"
	if strings.Contains(c.cfg.AuthorizationURL, "?") {
		sep = "&"
	}
	return c.cfg.AuthorizationURL + sep + params.Encode(), nil
}

// ExchangeCodeForToken 用授权码换取 access_token。
func (c *oauthClient) ExchangeCodeForToken(ctx context.Context, code string) (*TokenResponse, error) {
	form := url.Values{}
	form.Set("client_id", c.cfg.ClientID)
	form.Set("client_secret", c.cfg.ClientSecret)
	form.Set("code", code)
	form.Set("redirect_uri", c.cfg.RedirectURI)
	form.Set("grant_type", "authorization_code")

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.cfg.TokenURL, strings.NewReader(form.Encode()))
	if err != nil {
		return nil, fmt.Errorf("failed to exchange authorization code for token: %w", err)
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Accept", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to exchange authorization code for token: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if err != nil {
		return nil, fmt.Errorf("failed to exchange authorization code for token: %w", err)
	}
	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("failed to exchange authorization code for token: HTTP %d: %s", resp.StatusCode, strings.TrimSpace(string(body)))
	}

	token := &TokenResponse{}
	if jerr := json.Unmarshal(body, token); jerr != nil {
		// 部分提供商（如 GitHub）返回 form-urlencoded 而非 JSON。
		if values, perr := url.ParseQuery(string(body)); perr == nil {
			token.AccessToken = values.Get("access_token")
			token.TokenType = values.Get("token_type")
			token.IDToken = values.Get("id_token")
			token.Scope = values.Get("scope")
		} else {
			return nil, fmt.Errorf("failed to exchange authorization code for token: parse response: %w", jerr)
		}
	}
	if token.AccessToken == "" {
		return nil, fmt.Errorf("failed to exchange authorization code for token: empty access_token")
	}
	return token, nil
}

// FetchUserInfo 用 access_token 拉取用户信息并归一化。
func (c *oauthClient) FetchUserInfo(ctx context.Context, accessToken, idToken string) (*UserInfo, error) {
	if c.cfg.UserinfoURL == "" {
		return nil, fmt.Errorf("failed to fetch user info: userinfo_url is required")
	}
	raw, err := c.fetchUserinfoRaw(ctx, c.cfg.UserinfoURL, accessToken)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch user info: %w", err)
	}
	return normalizeUserInfo(raw), nil
}

func (c *oauthClient) fetchUserinfoRaw(ctx context.Context, endpoint, accessToken string) (map[string]interface{}, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("Accept", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(io.LimitReader(resp.Body, 4<<20))
	if err != nil {
		return nil, err
	}
	if resp.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d: %s", resp.StatusCode, strings.TrimSpace(string(body)))
	}
	var out map[string]interface{}
	if err := json.Unmarshal(body, &out); err != nil {
		return nil, fmt.Errorf("parse userinfo response: %w", err)
	}
	return out, nil
}

// normalizeUserInfo 归一化用户资料：username/nickname/avatar 按 Python 默认回退。
func normalizeUserInfo(raw map[string]interface{}) *UserInfo {
	ui := &UserInfo{}
	if v, ok := raw["email"].(string); ok {
		ui.Email = v
	}
	if v, ok := raw["username"].(string); ok && v != "" {
		ui.Username = v
	} else if ui.Email != "" {
		if at := strings.IndexByte(ui.Email, '@'); at >= 0 {
			ui.Username = ui.Email[:at]
		} else {
			ui.Username = ui.Email
		}
	}
	if v, ok := raw["nickname"].(string); ok && v != "" {
		ui.Nickname = v
	} else {
		ui.Nickname = ui.Username
	}
	if v, ok := raw["avatar_url"].(string); ok && v != "" {
		ui.AvatarURL = v
	} else if v, ok := raw["picture"].(string); ok {
		ui.AvatarURL = v
	}
	return ui
}
// client.go — OAuth/OIDC/GitHub 统一认证客户端，移植自 Python api/apps/auth。
