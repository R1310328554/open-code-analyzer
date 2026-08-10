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

package core

import (
	"context"
	"fmt"
	"net/url"
)

type (
	// Netrc 包含自动登录流程所需的机器名、用户名与密码信息，
	// 用于生成 .netrc 格式的 Git 克隆凭证。
	Netrc struct {
		Machine  string `json:"machine"`
		Login    string `json:"login"`
		Password string `json:"password"`
	}

	// NetrcService 返回可用于认证并克隆私有仓库的有效 .netrc 内容。
	// 若无需认证或未启用认证，则返回 nil Netrc 与 nil 错误。
	NetrcService interface {
		Create(context.Context, *User, *Repository) (*Netrc, error)
	}
)

// SetMachine 从 URL 地址解析并设置 netrc 的机器名（主机名）。
func (n *Netrc) SetMachine(address string) error {
	url, err := url.Parse(address)
	if err != nil {
		return err
	}
	n.Machine = url.Hostname()
	return nil
}

// String 返回 .netrc 文件格式的字符串表示。
func (n *Netrc) String() string {
	return fmt.Sprintf("machine %s login %s password %s",
		n.Machine,
		n.Login,
		n.Password,
	)
}
