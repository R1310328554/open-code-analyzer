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
// license.go — 许可证持久化 DAO：存储与读取系统许可证字符串，供启动时校验模块使用。

//

package dao

import (
	"ragflow/internal/entity"
	"time"
)

// LicenseDAO 许可证表的数据访问对象。
type LicenseDAO struct{}

// NewLicenseDAO 创建 LicenseDAO 实例。
func NewLicenseDAO() *LicenseDAO {
	return &LicenseDAO{}
}

// Create 写入新的许可证记录（含创建时间）。
func (dao *LicenseDAO) Create(licenseID, licenseStr string) error {
	license := entity.License{
		ID:        licenseID,
		License:   licenseStr,
		CreatedAt: time.Now(),
	}
	return DB.Create(license).Error
}

// GetLatest 按 created_at 降序取最新一条许可证。
func (dao *LicenseDAO) GetLatest() (*entity.License, error) {
	var license entity.License
	err := DB.Order("created_at DESC").First(&license).Error
	if err != nil {
		return nil, err
	}
	return &license, nil
}
