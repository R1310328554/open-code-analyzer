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

// chat_channel.go — 外部聊天渠道绑定：Slack/Teams 等 channel 与 Dialog 关联及列表 API 投影。

package entity

// ChatChannel 租户聊天渠道配置（channel 类型 + JSON config）
type ChatChannel struct {
	ID       string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	TenantID string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	Name     string  `gorm:"column:name;size:128;not null" json:"name"`
	Channel  string  `gorm:"column:channel;size:128;not null;index" json:"channel"`
	Config   JSONMap `gorm:"column:config;type:longtext;not null" json:"config"`
	ChatID   *string `gorm:"column:chat_id;size:32;index" json:"chat_id,omitempty"`
	Status   int     `gorm:"column:status;index;default:1" json:"status,omitempty"`
	BaseModel
}

// TableName specify table name
func (ChatChannel) TableName() string {
	return "chat_channel"
}

type ChatChannelListResponse struct {
	ID         string  `json:"id"`
	Name       string  `json:"name"`
	Channel    string  `json:"channel"`
	ChatID     *string `json:"chat_id"`
	Status     int     `json:"status"`
	DialogName *string `json:"dialog_name"` // 关联 Dialog 名称（列表 JOIN 别名）
}

// status 默认 1 表示启用；chat_id 可空表示尚未绑定具体 Dialog。
