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

// base.go — GORM 实体基类与时间戳钩子：对齐 Python Peewee 可空时间字段；JSONMap/JSONSlice 数据库 JSON 映射。

package entity

import (
	"database/sql/driver"
	"encoding/json"
	"time"

	"gorm.io/gorm"
)

// BaseModel 嵌入基类；时间字段可空以对齐 Python Peewee null=True
type BaseModel struct {
	CreateTime *int64     `gorm:"column:create_time;index" json:"create_time,omitempty"`
	CreateDate *time.Time `gorm:"column:create_date;index" json:"create_date,omitempty"`
	UpdateTime *int64     `gorm:"column:update_time;index" json:"update_time,omitempty"`
	UpdateDate *time.Time `gorm:"column:update_date;index" json:"update_date,omitempty"`
}

func autoModelTime() (int64, time.Time) {
	now := time.Now().Local()
	return now.UnixMilli(), now.Truncate(time.Second)
}

func statementHasTimeField(tx *gorm.DB, fieldNames ...string) bool {
	if tx == nil || tx.Statement == nil {
		return false
	}

	switch dest := tx.Statement.Dest.(type) {
	case map[string]interface{}:
		for _, fieldName := range fieldNames {
			if _, ok := dest[fieldName]; ok {
				return true
			}
		}
	case []map[string]interface{}:
		for _, item := range dest {
			for _, fieldName := range fieldNames {
				if _, ok := item[fieldName]; ok {
					return true
				}
			}
		}
	}

	return false
}

// BeforeCreate 创建前自动注入 create/update 时间与日期
func (m *BaseModel) BeforeCreate(tx *gorm.DB) error {
	timestamp, dateTime := autoModelTime()

	if m.CreateTime == nil {
		m.CreateTime = &timestamp
	}
	if m.CreateDate == nil {
		m.CreateDate = &dateTime
	}
	if m.UpdateTime == nil {
		m.UpdateTime = &timestamp
	}
	if m.UpdateDate == nil {
		m.UpdateDate = &dateTime
	}

	if tx != nil && tx.Statement != nil {
		if !statementHasTimeField(tx, "create_time", "CreateTime") && m.CreateTime != nil {
			tx.Statement.SetColumn("CreateTime", *m.CreateTime)
		}
		if !statementHasTimeField(tx, "create_date", "CreateDate") && m.CreateDate != nil {
			tx.Statement.SetColumn("CreateDate", *m.CreateDate)
		}
		if !statementHasTimeField(tx, "update_time", "UpdateTime") && m.UpdateTime != nil {
			tx.Statement.SetColumn("UpdateTime", *m.UpdateTime)
		}
		if !statementHasTimeField(tx, "update_date", "UpdateDate") && m.UpdateDate != nil {
			tx.Statement.SetColumn("UpdateDate", *m.UpdateDate)
		}
	}
	return nil
}

// BeforeUpdate 更新前刷新 update_time/update_date
func (m *BaseModel) BeforeUpdate(tx *gorm.DB) error {
	timestamp, dateTime := autoModelTime()

	if !statementHasTimeField(tx, "update_time", "UpdateTime") {
		m.UpdateTime = &timestamp
	}
	if !statementHasTimeField(tx, "update_date", "UpdateDate") {
		m.UpdateDate = &dateTime
	}

	if tx != nil && tx.Statement != nil {
		if !statementHasTimeField(tx, "update_time", "UpdateTime") && m.UpdateTime != nil {
			tx.Statement.SetColumn("UpdateTime", *m.UpdateTime)
		}
		if !statementHasTimeField(tx, "update_date", "UpdateDate") && m.UpdateDate != nil {
			tx.Statement.SetColumn("UpdateDate", *m.UpdateDate)
		}
	}
	return nil
}

func (m *BaseModel) UpdateCreateDateAndTime() error {
	timestamp, dateTime := autoModelTime()
	m.CreateTime = &timestamp
	m.UpdateDate = &dateTime
	return nil
}

func (m *BaseModel) UpdateUpdateDateAndTime() error {
	timestamp, dateTime := autoModelTime()
	m.UpdateTime = &timestamp
	m.UpdateDate = &dateTime
	return nil
}

// JSONMap 可序列化为 JSON 的 map，实现 driver.Valuer/sql.Scanner
type JSONMap map[string]interface{}

// Value 序列化为 JSON 写入数据库
func (j JSONMap) Value() (driver.Value, error) {
	if j == nil {
		return nil, nil
	}
	return json.Marshal(j)
}

// Scan 从数据库字节反序列化
func (j *JSONMap) Scan(value interface{}) error {
	if value == nil {
		*j = nil
		return nil
	}
	b, ok := value.([]byte)
	if !ok {
		return json.Unmarshal([]byte(value.(string)), j)
	}
	return json.Unmarshal(b, j)
}

// JSONSlice JSON 数组类型，同样实现 Valuer/Scanner
type JSONSlice []interface{}

// Value implements driver.Valuer interface
func (j JSONSlice) Value() (driver.Value, error) {
	if j == nil {
		return nil, nil
	}
	return json.Marshal(j)
}

// Scan implements sql.Scanner interface
func (j *JSONSlice) Scan(value interface{}) error {
	if value == nil {
		*j = nil
		return nil
	}
	b, ok := value.([]byte)
	if !ok {
		return json.Unmarshal([]byte(value.(string)), j)
	}
	return json.Unmarshal(b, j)
}

// autoModelTime 使用本地时区毫秒时间戳与截断秒级日期。statementHasTimeField 避免 map 批量更新时覆盖显式传入的时间列。
