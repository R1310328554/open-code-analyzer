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

// file.go — 租户文件树与文档映射：文件夹层级 file 表及 file2document 关联。

package entity

// File 租户文件/文件夹节点（parent_id 树形结构）
type File struct {
	ID         string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	ParentID   string  `gorm:"column:parent_id;size:32;not null;index" json:"parent_id"`
	TenantID   string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	CreatedBy  string  `gorm:"column:created_by;size:32;not null;index" json:"created_by"`
	Name       string  `gorm:"column:name;size:255;not null;index" json:"name"`
	Location   *string `gorm:"column:location;size:255;index" json:"location,omitempty"`
	Size       int64   `gorm:"column:size;default:0;index" json:"size"`
	Type       string  `gorm:"column:type;size:32;not null;index" json:"type"`
	SourceType string  `gorm:"column:source_type;size:128;not null;default:'';index" json:"source_type"`
	BaseModel
}

// TableName specify table name
func (File) TableName() string {
	return "file"
}

// File2Document 文件与知识库 document 的可选双向关联
type File2Document struct {
	ID         string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	FileID     *string `gorm:"column:file_id;size:32;index" json:"file_id,omitempty"`
	DocumentID *string `gorm:"column:document_id;size:32;index" json:"document_id,omitempty"`
	BaseModel
}

// TableName specify table name
func (File2Document) TableName() string {
	return "file2document"
}

// File.type 区分文件与目录；source_type 标记上传来源。File2Document 允许 file_id/document_id 可空以支持渐进绑定。
