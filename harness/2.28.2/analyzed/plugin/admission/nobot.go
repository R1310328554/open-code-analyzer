// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// admission 包（非 OSS 构建）实现用户注册准入策略，包括反机器人与开放/关闭注册等。
package admission

import (
	"context"
	"errors"
	"time"

	"github.com/drone/drone/core"
)

// ErrCannotVerify 在无法确认用户为真实人类账户时返回。
var ErrCannotVerify = errors.New("Cannot verify user authenticity")

// Nobot 创建反机器人准入策略：拒绝账号创建时间过短、疑似机器人的新用户。
// 策略假定 SCM 会在账户达到最小年龄前识别并移除机器人账号。
func Nobot(service core.UserService, age time.Duration) core.AdmissionService {
	return &nobot{service: service, age: age}
}

// nobot 实现基于账号创建时间的准入校验。
type nobot struct {
	age     time.Duration
	service core.UserService
}

// Admit 对新用户校验 SCM 账号创建时间是否超过配置的最小年龄；已有用户直接放行。
func (s *nobot) Admit(ctx context.Context, user *core.User) error {
	// 准入策略仅对新注册用户生效，已有用户始终允许通过。
	if user.ID != 0 {
		return nil
	}

	// 未配置最小年龄时跳过校验。
	if s.age == 0 {
		return nil
	}
	account, err := s.service.Find(ctx, user.Token, user.Refresh)
	if err != nil {
		return err
	}
	if account.Created == 0 {
		return nil
	}
	now := time.Now()
	if time.Unix(account.Created, 0).Add(s.age).After(now) {
		return ErrCannotVerify
	}
	return nil
}
