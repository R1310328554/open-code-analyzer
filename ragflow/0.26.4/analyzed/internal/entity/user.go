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

// user.go — 平台用户实体：账号、认证状态、偏好设置与角色绑定。
//

package entity

import "time"

// User 用户 GORM 实体（表 user）
type User struct {
	// ID 用户主键
	ID              string     `gorm:"column:id;size:32;primaryKey" json:"id"`
	// AccessToken 会话/API 访问令牌
	AccessToken     *string    `gorm:"column:access_token;size:255;index" json:"access_token,omitempty"`
	// Nickname 显示昵称
	Nickname        string     `gorm:"column:nickname;size:100;not null;index" json:"nickname"`
	// Password 密码哈希（JSON 序列化时省略）
	Password        *string    `gorm:"column:password;size:255;index" json:"-"`
	// Email 登录邮箱（唯一）
	Email           string     `gorm:"column:email;size:255;not null;unique" json:"email"`
	Avatar          *string    `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	// Language 界面语言偏好
	Language        *string    `gorm:"column:language;size:32;index" json:"language,omitempty"`
	// ColorSchema 主题配色
	ColorSchema     *string    `gorm:"column:color_schema;size:32;index" json:"color_schema,omitempty"`
	// Timezone 时区
	Timezone        *string    `gorm:"column:timezone;size:64;index" json:"timezone,omitempty"`
	// LastLoginTime 最近登录时间
	LastLoginTime   *time.Time `gorm:"column:last_login_time;index" json:"last_login_time,omitempty"`
	// IsAuthenticated 是否已完成身份验证
	IsAuthenticated string     `gorm:"column:is_authenticated;size:1;not null;default:1;index" json:"is_authenticated"`
	// IsActive 账号是否激活
	IsActive        string     `gorm:"column:is_active;size:1;not null;default:1;index" json:"is_active"`
	// IsAnonymous 是否为匿名访客
	IsAnonymous     string     `gorm:"column:is_anonymous;size:1;not null;default:0;index" json:"is_anonymous"`
	// LoginChannel 登录渠道（password/oauth 等）
	LoginChannel    *string    `gorm:"column:login_channel;index" json:"login_channel,omitempty"`
	// Status 用户状态
	Status          *string    `gorm:"column:status;size:1;default:1;index" json:"status"`
	// IsSuperuser 是否平台超级管理员
	IsSuperuser     *bool      `gorm:"column:is_superuser;index" json:"is_superuser,omitempty"`
	// RoleID 关联 RBAC 角色
	RoleID          int64      `gorm:"column:role_id;index;default:1;not null;" json:"role_id,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 user
func (User) TableName() string {
	return "user"
}

// 与 user_tenant 多对多关联租户；is_superuser 绕过租户级权限检查。OAuth 用户可能无 password；access_token 用于 API 鉴权。
