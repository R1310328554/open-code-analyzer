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

// license.go — 企业版许可证存储：encrypted_data 字段持久化加密许可载荷。

package entity

import (
	"time"
)

// License 许可证记录（id + encrypted_data + created_at）
type License struct {
	ID        string    `gorm:"column:id;size:128;not null;primaryKey" json:"id"`
	License   string    `gorm:"column:encrypted_data;type:longtext;not null" json:"encrypted_data"`
	CreatedAt time.Time `gorm:"column:created_at;type:timestamp;default:CURRENT_TIMESTAMP" json:"created_at"`
}

// TableName 返回表名 license
func (License) TableName() string {
	return "license"
}

// License 不含 BaseModel；created_at 由数据库 DEFAULT CURRENT_TIMESTAMP 维护。解密与校验逻辑在上层 admin/enterprise 服务中完成。
