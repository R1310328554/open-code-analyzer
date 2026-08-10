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
// llm.go — 全局 LLM 模型与厂商工厂 DAO：查询系统预置的大语言模型目录及有效状态的厂商列表。

//

package dao

import (
	"ragflow/internal/entity"
)

// LLMDAO 全局 LLM 模型表的数据访问对象。
type LLMDAO struct{}

// NewLLMDAO 创建 LLMDAO 实例。
func NewLLMDAO() *LLMDAO {
	return &LLMDAO{}
}

// GetAll 返回全部 LLM 模型记录。
func (dao *LLMDAO) GetAll() ([]*entity.LLM, error) {
	var llms []*entity.LLM
	err := DB.Find(&llms).Error
	if err != nil {
		return nil, err
	}
	return llms, nil
}

// GetAllValid 返回 status=1 的有效 LLM 模型。
func (dao *LLMDAO) GetAllValid() ([]*entity.LLM, error) {
	var llms []*entity.LLM
	err := DB.Where("status = ?", "1").Find(&llms).Error
	if err != nil {
		return nil, err
	}
	return llms, nil
}

// GetByFactory 按厂商 ID（fid）列出模型。
func (dao *LLMDAO) GetByFactory(factory string) ([]*entity.LLM, error) {
	var llms []*entity.LLM
	err := DB.Where("fid = ?", factory).Find(&llms).Error
	if err != nil {
		return nil, err
	}
	return llms, nil
}

// GetByFactoryAndName 按厂商与模型名精确查询。
func (dao *LLMDAO) GetByFactoryAndName(factory, name string) (*entity.LLM, error) {
	var llm entity.LLM
	err := DB.Where("fid = ? AND llm_name = ?", factory, name).First(&llm).Error
	if err != nil {
		return nil, err
	}
	return &llm, nil
}

// LLMFactoryDAO LLM 厂商工厂表的数据访问对象。
type LLMFactoryDAO struct{}

// NewLLMFactoryDAO 创建 LLMFactoryDAO 实例。
func NewLLMFactoryDAO() *LLMFactoryDAO {
	return &LLMFactoryDAO{}
}

// GetAllValid 返回 status=1 的有效厂商列表。
func (dao *LLMFactoryDAO) GetAllValid() ([]*entity.LLMFactories, error) {
	var factories []*entity.LLMFactories
	err := DB.Where("status = ?", "1").Find(&factories).Error
	if err != nil {
		return nil, err
	}
	return factories, nil
}

// GetByName 按厂商名称查询工厂记录。
func (dao *LLMFactoryDAO) GetByName(name string) (*entity.LLMFactories, error) {
	var factory entity.LLMFactories
	err := DB.Where("name = ?", name).First(&factory).Error
	if err != nil {
		return nil, err
	}
	return &factory, nil
}
