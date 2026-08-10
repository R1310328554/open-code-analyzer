// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package rpc2

// Copyright 2019 Drone.IO Inc. All rights reserved.
import (
	"github.com/drone/drone/core"
	"github.com/drone/drone/operator/manager"
)

// details 向运行器提供完整构建上下文、netrc 及含密钥的仓库信息。
type details struct {
	*manager.Context
	Netrc *core.Netrc `json:"netrc"`
	Repo  *repository `json:"repository"`
}

// repository 包装仓库对象，在 JSON 序列化时显式包含 secret 字段。
type repository struct {
	*core.Repository
	Secret string `json:"secret"`
}
