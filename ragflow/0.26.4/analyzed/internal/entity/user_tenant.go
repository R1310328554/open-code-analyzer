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

// user_tenant.go — 用户-租户成员关系：记录用户在租户内的角色、邀请来源与成员状态。
//

package entity

// UserTenant 用户-租户关系 GORM 实体（表 user_tenant）
type UserTenant struct {
	// ID 关系主键
	ID        string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	// UserID 用户 ID
	UserID    string  `gorm:"column:user_id;size:32;not null;index" json:"user_id"`
	// TenantID 租户 ID
	TenantID  string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	// Role 租户内角色（owner/normal 等）
	Role      string  `gorm:"column:role;size:32;not null;index" json:"role"`
	// InvitedBy 邀请人用户 ID
	InvitedBy string  `gorm:"column:invited_by;size:32;not null;index" json:"invited_by"`
	// Status 成员状态（待接受/已加入/已移除）
	Status    *string `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 user_tenant
func (UserTenant) TableName() string {
	return "user_tenant"
}

// 同一 user 可加入多个 tenant；权限中间件按 tenant_id+role 判定。创建租户时自动生成 owner 关系，invited_by 可为创建者自身。
