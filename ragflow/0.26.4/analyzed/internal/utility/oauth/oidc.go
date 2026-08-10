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

// oidc.go 实现 OIDC 客户端，自动发现授权/令牌/userinfo 端点。

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// oidcClient 从 Issuer 发现文档解析端点后委托 oauthClient；暂不校验 id_token。
type oidcClient struct {
	*oauthClient
	issuer string
}

func newOIDCClient(cfg Config) (*oidcClient, error) {
	if cfg.Issuer == "" {
		return nil, fmt.Errorf("missing issuer in configuration")
	}
	meta, err := loadOIDCMetadata(cfg.Issuer)
	if err != nil {
		return nil, fmt.Errorf("failed to fetch OIDC metadata: %w", err)
	}
	if meta.Issuer != "" {
		cfg.Issuer = meta.Issuer
	}
	if cfg.AuthorizationURL == "" {
		cfg.AuthorizationURL = meta.AuthorizationEndpoint
	}
	if cfg.TokenURL == "" {
		cfg.TokenURL = meta.TokenEndpoint
	}
	if cfg.UserinfoURL == "" {
		cfg.UserinfoURL = meta.UserinfoEndpoint
	}
	base, err := newOAuthClient(cfg)
	if err != nil {
		return nil, err
	}
	return &oidcClient{oauthClient: base, issuer: cfg.Issuer}, nil
}

// oidcMetadata 为发现文档中使用的字段子集。
type oidcMetadata struct {
	Issuer                string `json:"issuer"`
	AuthorizationEndpoint string `json:"authorization_endpoint"`
	TokenEndpoint         string `json:"token_endpoint"`
	UserinfoEndpoint      string `json:"userinfo_endpoint"`
	JWKSURI               string `json:"jwks_uri"`
}

// loadOIDCMetadata 获取 OIDC 发现文档，测试可覆盖。
var loadOIDCMetadata = func(issuer string) (*oidcMetadata, error) {
	metadataURL := strings.TrimRight(issuer, "/") + "/.well-known/openid-configuration"
	ctx, cancel := context.WithTimeout(context.Background(), HTTPRequestTimeout)
	defer cancel()
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, metadataURL, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Accept", "application/json")
	resp, err := http.DefaultClient.Do(req)
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
	meta := &oidcMetadata{}
	if err := json.Unmarshal(body, meta); err != nil {
		return nil, fmt.Errorf("parse OIDC metadata: %w", err)
	}
	return meta, nil
}
// oidc.go — OIDC 客户端，通过 .well-known/openid-configuration 自动发现端点。
