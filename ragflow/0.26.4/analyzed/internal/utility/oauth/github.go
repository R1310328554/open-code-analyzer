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

package oauth

// github.go 实现 GitHub OAuth，补全隐藏邮箱用户的主邮箱。

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// gitHubClient 使用 GitHub 固定端点，并从 /user/emails 获取主邮箱。
type gitHubClient struct {
	*oauthClient
}

func newGitHubClient(cfg Config) (*gitHubClient, error) {
	cfg.AuthorizationURL = "https://github.com/login/oauth/authorize"
	cfg.TokenURL = "https://github.com/login/oauth/access_token"
	cfg.UserinfoURL = "https://api.github.com/user"
	if cfg.Scope == "" {
		cfg.Scope = "user:email"
	}
	base, err := newOAuthClient(cfg)
	if err != nil {
		return nil, err
	}
	return &gitHubClient{oauthClient: base}, nil
}

// FetchUserInfo 合并 /user 与 /user/emails 的主邮箱。
func (c *gitHubClient) FetchUserInfo(ctx context.Context, accessToken, idToken string) (*UserInfo, error) {
	raw, err := c.fetchUserinfoRaw(ctx, c.cfg.UserinfoURL, accessToken)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch github user info: %w", err)
	}
	email, err := c.fetchPrimaryEmail(ctx, accessToken)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch github user info: %w", err)
	}
	if email != "" {
		raw["email"] = email
	}
	return normalizeGitHubUserInfo(raw), nil
}

func (c *gitHubClient) fetchPrimaryEmail(ctx context.Context, accessToken string) (string, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, c.cfg.UserinfoURL+"/emails", nil)
	if err != nil {
		return "", err
	}
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("Accept", "application/json")
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()
	body, err := io.ReadAll(io.LimitReader(resp.Body, 1<<20))
	if err != nil {
		return "", err
	}
	if resp.StatusCode >= 400 {
		return "", fmt.Errorf("HTTP %d: %s", resp.StatusCode, strings.TrimSpace(string(body)))
	}
	var emails []map[string]interface{}
	if err := json.Unmarshal(body, &emails); err != nil {
		return "", fmt.Errorf("parse emails response: %w", err)
	}
	for _, e := range emails {
		primary, _ := e["primary"].(bool)
		addr, _ := e["email"].(string)
		if primary && addr != "" {
			return addr, nil
		}
	}
	// 无 primary 标记时回退到首个已验证邮箱。
	for _, e := range emails {
		verified, _ := e["verified"].(bool)
		addr, _ := e["email"].(string)
		if verified && addr != "" {
			return addr, nil
		}
	}
	return "", nil
}

// normalizeGitHubUserInfo 按 GitHub 字段映射：login/name/avatar_url。
func normalizeGitHubUserInfo(raw map[string]interface{}) *UserInfo {
	ui := &UserInfo{}
	if v, ok := raw["email"].(string); ok {
		ui.Email = v
	}
	if v, ok := raw["login"].(string); ok && v != "" {
		ui.Username = v
	} else if ui.Email != "" {
		if at := strings.IndexByte(ui.Email, '@'); at >= 0 {
			ui.Username = ui.Email[:at]
		} else {
			ui.Username = ui.Email
		}
	}
	if v, ok := raw["name"].(string); ok && v != "" {
		ui.Nickname = v
	} else {
		ui.Nickname = ui.Username
	}
	if v, ok := raw["avatar_url"].(string); ok {
		ui.AvatarURL = v
	}
	return ui
}
// github.go — GitHub OAuth 客户端，从 /user/emails 补全主邮箱。
