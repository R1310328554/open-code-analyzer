// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// webhook 包（非 OSS 构建）实现带 HTTP 签名的全局 Webhook 事件推送。
package webhook

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"net/http"
	"path/filepath"
	"time"

	"github.com/drone/drone/core"

	"github.com/99designs/httpsignatures-go"
)

// headers 定义 HTTP 签名所需的请求头。
var headers = []string{
	"date",
	"digest",
}

// signer 使用 HMAC-SHA256 算法对 Webhook 请求进行签名。
var signer = httpsignatures.NewSigner(
	httpsignatures.AlgorithmHmacSha256,
	headers...,
)

// New 根据配置创建 Webhook 发送器。
func New(config Config) core.WebhookSender {
	return &sender{
		Events:    config.Events,
		Endpoints: config.Endpoint,
		Secret:    config.Secret,
		System:    config.System,
	}
}

// payload 包装 Webhook 数据与系统信息，用于 JSON 序列化。
type payload struct {
	*core.WebhookData
	System *core.System `json:"system,omitempty"`
}

// sender 向配置的 HTTP 端点发送签名 Webhook 请求。
type sender struct {
	Client    *http.Client
	Events    []string
	Endpoints []string
	Secret    string
	System    *core.System
}

// Send 将 JSON 编码的 Webhook 推送至所有配置的全局 HTTP 端点。
func (s *sender) Send(ctx context.Context, in *core.WebhookData) error {
	if len(s.Endpoints) == 0 {
		return nil
	}
	if s.match(in.Event, in.Action) == false {
		return nil
	}
	wrapper := payload{
		WebhookData: in,
		System:      s.System,
	}
	data, _ := json.Marshal(wrapper)
	for _, endpoint := range s.Endpoints {
		err := s.send(endpoint, s.Secret, in.Event, data)
		if err != nil {
			return err
		}
	}
	return nil
}

// send 向单个端点发送带 Digest 与 HTTP 签名的 POST 请求。
func (s *sender) send(endpoint, secret, event string, data []byte) error {
	ctx := context.Background()
	ctx, cancel := context.WithTimeout(ctx, time.Minute)
	defer cancel()

	buf := bytes.NewBuffer(data)
	req, err := http.NewRequest("POST", endpoint, buf)
	if err != nil {
		return err
	}

	req = req.WithContext(ctx)
	req.Header.Add("X-Drone-Event", event)
	req.Header.Add("Content-Type", "application/json")
	req.Header.Add("Digest", "SHA-256="+digest(data))
	req.Header.Add("Date", time.Now().UTC().Format(http.TimeFormat))
	err = signer.SignRequest("hmac-key", s.Secret, req)
	if err != nil {
		return err
	}
	res, err := s.client().Do(req)
	if res != nil {
		res.Body.Close()
	}
	return err
}

// match 按 event 或 event:action 通配符判断当前事件是否应推送。
func (s *sender) match(event, action string) bool {
	if len(s.Events) == 0 {
		return true
	}
	var name string
	switch {
	case action == "":
		name = event
	case action != "":
		name = event + ":" + action
	}
	for _, pattern := range s.Events {
		if ok, _ := filepath.Match(pattern, name); ok {
			return true
		}
	}
	return false
}

// client 返回 HTTP 客户端，未配置时使用 http.DefaultClient。
func (s *sender) client() *http.Client {
	if s.Client == nil {
		return http.DefaultClient
	}
	return s.Client
}

// digest 计算请求体的 SHA-256 摘要并 Base64 编码。
func digest(data []byte) string {
	h := sha256.New()
	h.Write(data)
	return base64.StdEncoding.EncodeToString(h.Sum(nil))
}
