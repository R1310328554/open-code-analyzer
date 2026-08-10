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

// time_record.go — 通用时间序列记录：append-only 表，存储任意 JSON/文本快照供调试或审计。
//

package entity

import (
	"time"
)

// TimeRecord 时间记录 GORM 实体（表 time_records）
type TimeRecord struct {
	// ID 自增主键
	ID        int64     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	// Data 记录载荷（通常为 JSON 字符串）
	Data      string    `gorm:"column:data;type:longtext;not null" json:"data"`
	// CreatedAt 写入时间（DB 默认 CURRENT_TIMESTAMP）
	CreatedAt time.Time `gorm:"column:created_at;type:timestamp;default:CURRENT_TIMESTAMP" json:"created_at"`
}

// TableName 返回 GORM 表名 time_records
func (TimeRecord) TableName() string {
	return "time_records"
}

// 未嵌入 BaseModel：仅 created_at，无 update_time。适用于性能采样、临时指标 dump 等低频查询场景。
