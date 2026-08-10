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

// file_commit.go — 工作区版本控制：类 Git 提交、变更项、树快照及 diff/版本历史 API 结构。

package entity

// FileCommit 工作区文件夹快照提交（类 git commit）
type FileCommit struct {
	ID        string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	FolderID  string  `gorm:"column:folder_id;size:32;not null;index" json:"folder_id"`
	ParentID  *string `gorm:"column:parent_id;size:32;index" json:"parent_id,omitempty"`
	Message   string  `gorm:"column:message;size:512;not null;default:''" json:"message"`
	AuthorID  string  `gorm:"column:author_id;size:32;not null;index" json:"author_id"`
	FileCount int     `gorm:"column:file_count;default:0" json:"file_count"`
	TreeState *string `gorm:"column:tree_state;type:longtext" json:"tree_state,omitempty"`
	BaseModel
}

// TableName 返回表名 file_commit
func (FileCommit) TableName() string {
	return "file_commit"
}

// FileCommitItem 提交内单文件变更（add/modify/delete/rename）
type FileCommitItem struct {
	ID          string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	CommitID    string  `gorm:"column:commit_id;size:32;not null;uniqueIndex:idx_commit_file" json:"commit_id"`
	FileID      string  `gorm:"column:file_id;size:32;not null;uniqueIndex:idx_commit_file" json:"file_id"`
	Operation   string  `gorm:"column:operation;size:16;not null;index" json:"operation"`
	OldHash     *string `gorm:"column:old_hash;size:64;index" json:"old_hash,omitempty"`
	NewHash     *string `gorm:"column:new_hash;size:64;index" json:"new_hash,omitempty"`
	OldLocation *string `gorm:"column:old_location;size:255" json:"old_location,omitempty"`
	NewLocation *string `gorm:"column:new_location;size:255" json:"new_location,omitempty"`
	OldName     *string `gorm:"column:old_name;size:255" json:"old_name,omitempty"`
	NewName     *string `gorm:"column:new_name;size:255" json:"new_name,omitempty"`
	BaseModel
}

// TableName returns the table name for FileCommitItem model.
func (FileCommitItem) TableName() string {
	return "file_commit_item"
}

// TreeNode 提交 tree_state JSON 中的文件树节点
type TreeNode struct {
	ID       string      `json:"id"`
	Name     string      `json:"name"`
	Type     string      `json:"type"` // 节点类型：file 或 folder
	Hash     string      `json:"hash,omitempty"`
	Location string      `json:"location,omitempty"`
	Size     int64       `json:"size,omitempty"`
	Status   string      `json:"status"` // 状态：1 有效，0 已删除
	Children []*TreeNode `json:"children,omitempty"`
}

// FileChange 创建提交 API 请求中的单文件变更
type FileChange struct {
	FileID    string `json:"file_id"`
	FileName  string `json:"file_name"`
	Operation string `json:"operation"` // 操作：add/modify/delete/rename
	Content   string `json:"content,omitempty"`
	OldName   string `json:"old_name,omitempty"`
	NewName   string `json:"new_name,omitempty"`
}

// CommitResponse 提交 API 响应体
type CommitResponse struct {
	ID         string  `json:"id"`
	FolderID   string  `json:"folder_id"`
	ParentID   *string `json:"parent_id,omitempty"`
	Message    string  `json:"message"`
	AuthorID   string  `json:"author_id"`
	FileCount  int     `json:"file_count"`
	TreeState  *string `json:"tree_state,omitempty"`
	CreateTime *int64  `json:"create_time,omitempty"`
}

// DiffEntry 两提交间单文件 diff 项
type DiffEntry struct {
	FileID      string  `json:"file_id"`
	FileName    string  `json:"file_name"`
	Operation   string  `json:"operation"`
	OldHash     *string `json:"old_hash,omitempty"`
	NewHash     *string `json:"new_hash,omitempty"`
	OldLocation *string `json:"old_location,omitempty"`
	NewLocation *string `json:"new_location,omitempty"`
}

// VersionEntry 文件版本历史中的单条记录
type VersionEntry struct {
	CommitID   string `json:"commit_id"`
	Operation  string `json:"operation"`
	Hash       string `json:"hash"`
	CreateTime *int64 `json:"create_time,omitempty"`
	Message    string `json:"message"`
}

// tree_state 存储完整目录树 JSON；FileCommitItem 通过 idx_commit_file 保证 commit+file 唯一。DiffEntry/VersionEntry 为纯 API DTO，无 GORM 映射。
