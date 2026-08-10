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
// file2document.go — 文件与文档映射 DAO：维护 file_id 与 document_id 的多对多关联及级联删除。

//

package dao

import (
	"ragflow/internal/entity"
)

// File2DocumentDAO 文件-文档映射表的数据访问对象。
type File2DocumentDAO struct{}

// NewFile2DocumentDAO 创建映射 DAO 实例。
func NewFile2DocumentDAO() *File2DocumentDAO {
	return &File2DocumentDAO{}
}

// GetKBInfoByFileID 联表查询文件关联的知识库 ID、名称与文档 ID。
func (dao *File2DocumentDAO) GetKBInfoByFileID(fileID string) ([]map[string]interface{}, error) {
	var results []map[string]interface{}

	rows, err := DB.Model(&entity.File{}).
		Select("knowledgebase.id, knowledgebase.name, file2document.document_id").
		Joins("JOIN file2document ON file2document.file_id = ?", fileID).
		Joins("JOIN document ON document.id = file2document.document_id").
		Joins("JOIN knowledgebase ON knowledgebase.id = document.kb_id").
		Where("file.id = ?", fileID).
		Rows()
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		var kbID, kbName, docID string
		if err := rows.Scan(&kbID, &kbName, &docID); err != nil {
			continue
		}
		results = append(results, map[string]interface{}{
			"kb_id":       kbID,
			"kb_name":     kbName,
			"document_id": docID,
		})
	}

	return results, nil
}

// GetByFileID 按文件 ID 查询全部映射记录。
func (dao *File2DocumentDAO) GetByFileID(fileID string) ([]*entity.File2Document, error) {
	var mappings []*entity.File2Document
	err := DB.Where("file_id = ?", fileID).Find(&mappings).Error
	return mappings, err
}

// DeleteByFileID 按文件 ID 硬删除全部映射。
func (dao *File2DocumentDAO) DeleteByFileID(fileID string) error {
	return DB.Unscoped().Where("file_id = ?", fileID).Delete(&entity.File2Document{}).Error
}

// GetByDocumentID 按文档 ID 查询映射记录。
func (dao *File2DocumentDAO) GetByDocumentID(docID string) ([]*entity.File2Document, error) {
	var mappings []*entity.File2Document
	err := DB.Where("document_id = ?", docID).Find(&mappings).Error
	return mappings, err
}

// DeleteByDocumentID 按文档 ID 硬删除映射。
func (dao *File2DocumentDAO) DeleteByDocumentID(docID string) error {
	return DB.Unscoped().Where("document_id = ?", docID).Delete(&entity.File2Document{}).Error
}

// Create 插入新的文件-文档映射记录。
func (dao *File2DocumentDAO) Create(mapping *entity.File2Document) error {
	return DB.Create(mapping).Error
}
