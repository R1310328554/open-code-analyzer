// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package admission

import (
	"context"
	"time"

	"github.com/drone/drone-go/drone"
	"github.com/drone/drone-go/plugin/admission"
	"github.com/drone/drone/core"
)

// External 创建通过 HTTP 调用外部插件的准入控制器。
func External(endpoint, secret string, skipVerify bool) core.AdmissionService {
	return &external{
		endpoint:   endpoint,
		secret:     secret,
		skipVerify: skipVerify,
	}
}

// external 将登录/注册事件转发至配置的 Drone 准入插件端点。
type external struct {
	endpoint   string
	secret     string
	skipVerify bool
}

// Admit 向外部服务发送登录或注册事件，并根据响应更新 user.Admin 等字段。
func (c *external) Admit(ctx context.Context, user *core.User) error {
	if c.endpoint == "" {
		return nil
	}

	// include a timeout to prevent an API call from
	// hanging the build process indefinitely. The
	// external service must return a request within
	// one minute.
	ctx, cancel := context.WithTimeout(ctx, time.Minute)
	defer cancel()

	req := &admission.Request{
		Event: admission.EventLogin,
		User:  toUser(user),
	}
	if user.ID == 0 {
		req.Event = admission.EventRegister
	}
	client := admission.Client(c.endpoint, c.secret, c.skipVerify)
	result, err := client.Admit(ctx, req)
	if result != nil {
		user.Admin = result.Admin
	}
	return err
}

// toUser 将 core.User 转换为 drone-go 插件 API 使用的 User 结构。
func toUser(from *core.User) drone.User {
	return drone.User{
		ID:        from.ID,
		Login:     from.Login,
		Email:     from.Email,
		Avatar:    from.Avatar,
		Active:    from.Active,
		Admin:     from.Admin,
		Machine:   from.Machine,
		Syncing:   from.Syncing,
		Synced:    from.Synced,
		Created:   from.Created,
		Updated:   from.Updated,
		LastLogin: from.LastLogin,
	}
}
