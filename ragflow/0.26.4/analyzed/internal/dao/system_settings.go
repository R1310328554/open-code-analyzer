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
// system_settings.go — 系统设置数据访问层：读写全局键值配置，支持按名称/前缀/来源查询及事务内 upsert。

//

package dao

import (
	"errors"
	"ragflow/internal/entity"

	"gorm.io/gorm"
)

// SystemSettingsDAO 系统设置表的数据访问对象。
type SystemSettingsDAO struct{}

// NewSystemSettingsDAO 创建系统设置 DAO 实例。
func NewSystemSettingsDAO() *SystemSettingsDAO {
	return &SystemSettingsDAO{}
}

// GetAll 按 name 升序返回全部系统设置记录。
func (d *SystemSettingsDAO) GetAll() ([]entity.SystemSettings, error) {
	var settings []entity.SystemSettings
	err := DB.Order("name ASC").Find(&settings).Error
	if err != nil {
		return nil, err
	}
	return settings, nil
}

// GetByName 按配置名精确查询（可能返回多条同名记录）。
func (d *SystemSettingsDAO) GetByName(name string) ([]entity.SystemSettings, error) {
	var settings []entity.SystemSettings
	err := DB.Where("name = ?", name).Order("name ASC").Find(&settings).Error
	if err != nil {
		return nil, err
	}
	return settings, nil
}

// GetByNamePrefix 按名称前缀 LIKE 查询配置项。
func (d *SystemSettingsDAO) GetByNamePrefix(namePrefix string) ([]entity.SystemSettings, error) {
	var settings []entity.SystemSettings
	err := DB.Where("name LIKE ?", namePrefix+"%").Order("name ASC").Find(&settings).Error
	if err != nil {
		return nil, err
	}
	return settings, nil
}

// UpdateByName 按名称更新 value、source、data_type 字段。
func (d *SystemSettingsDAO) UpdateByName(name string, setting *entity.SystemSettings) error {
	return DB.Model(&entity.SystemSettings{}).
		Where("name = ?", name).
		Updates(map[string]interface{}{
			"value":     setting.Value,
			"source":    setting.Source,
			"data_type": setting.DataType,
		}).Error
}

// Create 插入新系统设置记录。
func (d *SystemSettingsDAO) Create(setting *entity.SystemSettings) error {
	return DB.Create(setting).Error
}

// SaveOrCreate 存在唯一同名记录则更新，否则新建；同名多于一条则报错。
func (d *SystemSettingsDAO) SaveOrCreate(name string, value string, source string, dataType string) error {
	settings, err := d.GetByName(name)
	if err != nil {
		return err
	}

	if len(settings) == 1 {
		setting := &settings[0]
		setting.Value = value
		setting.Source = source
		setting.DataType = dataType
		return d.UpdateByName(name, setting)
	} else if len(settings) > 1 {
		return errors.New("can't update more than 1 setting: " + name)
	}

	newSetting := &entity.SystemSettings{
		Name:     name,
		Value:    value,
		Source:   source,
		DataType: dataType,
	}
	return d.Create(newSetting)
}

// Count 返回系统设置总条数。
func (d *SystemSettingsDAO) Count() (int64, error) {
	var count int64
	err := DB.Model(&entity.SystemSettings{}).Count(&count).Error
	return count, err
}

// DeleteByName 按名称硬删除配置。
func (d *SystemSettingsDAO) DeleteByName(name string) error {
	return DB.Where("name = ?", name).Delete(&entity.SystemSettings{}).Error
}

// Exists 判断指定名称的配置是否存在。
func (d *SystemSettingsDAO) Exists(name string) (bool, error) {
	var count int64
	err := DB.Model(&entity.SystemSettings{}).Where("name = ?", name).Count(&count).Error
	if err != nil {
		return false, err
	}
	return count > 0, nil
}

// GetBySource 按配置来源字段筛选。
func (d *SystemSettingsDAO) GetBySource(source string) ([]entity.SystemSettings, error) {
	var settings []entity.SystemSettings
	err := DB.Where("source = ?", source).Find(&settings).Error
	if err != nil {
		return nil, err
	}
	return settings, nil
}

// GetByDataType 按数据类型字段筛选。
func (d *SystemSettingsDAO) GetByDataType(dataType string) ([]entity.SystemSettings, error) {
	var settings []entity.SystemSettings
	err := DB.Where("data_type = ?", dataType).Find(&settings).Error
	if err != nil {
		return nil, err
	}
	return settings, nil
}

// Transaction 在数据库事务中执行回调。
func (d *SystemSettingsDAO) Transaction(fn func(tx *gorm.DB) error) error {
	return DB.Transaction(fn)
}

// CreateWithTx 在事务内插入配置。
func (d *SystemSettingsDAO) CreateWithTx(tx *gorm.DB, setting *entity.SystemSettings) error {
	return tx.Create(setting).Error
}

// UpdateByNameWithTx 在事务内按名称更新配置。
func (d *SystemSettingsDAO) UpdateByNameWithTx(tx *gorm.DB, name string, setting *entity.SystemSettings) error {
	return tx.Model(&entity.SystemSettings{}).
		Where("name = ?", name).
		Updates(map[string]interface{}{
			"value":     setting.Value,
			"source":    setting.Source,
			"data_type": setting.DataType,
		}).Error
}
