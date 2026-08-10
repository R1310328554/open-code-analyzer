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
// canvas_template.go — Agent 画布模板 DAO：读取 canvas_template 表，供「从模板创建 Agent」功能展示内置模板列表。

//

package dao

import (
	"ragflow/internal/entity"
)

// CanvasTemplateDAO canvas_template 表的数据访问对象。
type CanvasTemplateDAO struct{}

// NewCanvasTemplateDAO 构造 CanvasTemplateDAO 实例。
func NewCanvasTemplateDAO() *CanvasTemplateDAO {
	return &CanvasTemplateDAO{}
}

// GetAll 按 create_time 降序返回全部模板，UI 最新优先；对齐 Python get_all()。
func (dao *CanvasTemplateDAO) GetAll() ([]*entity.CanvasTemplate, error) {
	var templates []*entity.CanvasTemplate
	if err := DB.Order("create_time desc").Find(&templates).Error; err != nil {
		return nil, err
	}
	return templates, nil
}
