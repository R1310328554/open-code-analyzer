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

// auths 包解析 Docker 客户端配置文件（通常为 ~/.docker/config.json）中的镜像仓库凭据。
package auths

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"io"
	"os"
	"strings"

	"github.com/drone/drone/core"
)

// config 表示 Docker 客户端配置结构，通常位于 ~/.docker/config.json。
type config struct {
	Auths map[string]struct {
		Auth string `json:"auth"`
	} `json:"auths"`
}

// Parse 从 Reader 解析镜像仓库凭据列表。
func Parse(r io.Reader) ([]*core.Registry, error) {
	c := new(config)
	err := json.NewDecoder(r).Decode(c)
	if err != nil {
		return nil, err
	}
	var auths []*core.Registry
	for k, v := range c.Auths {
		username, password := decode(v.Auth)
		auths = append(auths, &core.Registry{
			Address:  k,
			Username: username,
			Password: password,
		})
	}
	return auths, nil
}

// ParseFile 从文件路径解析镜像仓库凭据。
func ParseFile(filepath string) ([]*core.Registry, error) {
	f, err := os.Open(filepath)
	if err != nil {
		return nil, err
	}
	defer f.Close()
	return Parse(f)
}

// ParseString 从字符串解析镜像仓库凭据。
func ParseString(s string) ([]*core.Registry, error) {
	return Parse(strings.NewReader(s))
}

// ParseBytes 从字节切片解析镜像仓库凭据。
func ParseBytes(b []byte) ([]*core.Registry, error) {
	return Parse(bytes.NewReader(b))
}

// encode 将用户名与密码编码为 base64 认证字符串。
func encode(username, password string) string {
	return base64.StdEncoding.EncodeToString(
		[]byte(username + ":" + password),
	)
}

// decode 解码 base64 认证字符串，返回用户名与密码。
func decode(s string) (username, password string) {
	d, err := base64.StdEncoding.DecodeString(s)
	if err != nil {
		return
	}
	parts := strings.SplitN(string(d), ":", 2)
	if len(parts) > 0 {
		username = parts[0]
	}
	if len(parts) > 1 {
		password = parts[1]
	}
	return
}
