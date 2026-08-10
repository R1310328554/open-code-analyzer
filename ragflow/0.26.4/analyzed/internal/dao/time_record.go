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
// time_record.go — 时间记录数据访问层：用于插入、查询、裁剪与清理 time_records 表（如性能采样或审计缓冲）。

//

package dao

import (
	"ragflow/internal/entity"
)

// TimeRecordDAO 时间记录表的数据访问对象。
type TimeRecordDAO struct{}

// NewTimeRecordDAO 创建时间记录 DAO 实例。
func NewTimeRecordDAO() *TimeRecordDAO {
	return &TimeRecordDAO{}
}

// Create 插入新时间记录。
func (dao *TimeRecordDAO) Create(record *entity.TimeRecord) error {
	return DB.Create(record).Error
}

// GetRecent 按 id 降序取最近 limit 条记录。
func (dao *TimeRecordDAO) GetRecent(limit int) ([]*entity.TimeRecord, error) {
	var records []*entity.TimeRecord
	err := DB.Order("id DESC").Limit(limit).Find(&records).Error
	if err != nil {
		return nil, err
	}
	return records, nil
}

// GetCount 返回记录总条数。
func (dao *TimeRecordDAO) GetCount() (int64, error) {
	var count int64
	err := DB.Model(&entity.TimeRecord{}).Count(&count).Error
	return count, err
}

// DeleteOldest 按 id 升序删除最旧的 limit 条（原生 SQL LIMIT）。
func (dao *TimeRecordDAO) DeleteOldest(limit int64) error {
	return DB.Exec("DELETE FROM time_records ORDER BY id ASC LIMIT ?", limit).Error
}

// GetByID 按主键查询单条记录。
func (dao *TimeRecordDAO) GetByID(id int64) (*entity.TimeRecord, error) {
	var record entity.TimeRecord
	err := DB.First(&record, id).Error
	if err != nil {
		return nil, err
	}
	return &record, nil
}

// GetAll 返回全部时间记录。
func (dao *TimeRecordDAO) GetAll() ([]*entity.TimeRecord, error) {
	var records []*entity.TimeRecord
	err := DB.Find(&records).Error
	return records, err
}

// KeepLatest 保留最新 count 条（按 id 阈值删除更旧记录）。
func (dao *TimeRecordDAO) KeepLatest(count int64) error {
	// 第一步：查询当前最大 id
	var maxID int64
	if err := DB.Model(&entity.TimeRecord{}).Select("COALESCE(MAX(id), 0)").Scan(&maxID).Error; err != nil {
		return err
	}

	// 无记录或 count<=0 则无需删除
	if maxID == 0 || count <= 0 {
		return nil
	}

	// 第二步：计算保留阈值 id = maxID - count
	thresholdID := maxID - count

	// 阈值<=0 表示记录数不足，全部保留
	if thresholdID <= 0 {
		return nil
	}

	// 第三步：删除 id 小于等于阈值的旧记录
	return DB.Where("id <= ?", thresholdID).Delete(&entity.TimeRecord{}).Error
}

// DeleteAll 清空 time_records 表全部记录。
func (dao *TimeRecordDAO) DeleteAll() error {
	return DB.Where("1=1").Delete(&entity.TimeRecord{}).Error
}
