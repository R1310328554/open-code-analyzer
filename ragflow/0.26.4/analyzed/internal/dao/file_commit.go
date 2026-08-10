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
// file_commit.go — 文件版本提交 DAO：记录文件夹快照提交及其包含的文件变更明细，支持分页与排序白名单。

//

package dao

import (
	"ragflow/internal/entity"
)

// FileCommitDAO 文件提交（快照）表的数据访问对象。
type FileCommitDAO struct{}

// NewFileCommitDAO 创建 FileCommitDAO 实例。
func NewFileCommitDAO() *FileCommitDAO {
	return &FileCommitDAO{}
}

// GetByID 按 ID 查询提交记录。
func (dao *FileCommitDAO) GetByID(id string) (*entity.FileCommit, error) {
	var commit entity.FileCommit
	err := DB.Where("id = ?", id).First(&commit).Error
	if err != nil {
		return nil, err
	}
	return &commit, nil
}

// Create 插入新的文件提交记录。
func (dao *FileCommitDAO) Create(commit *entity.FileCommit) error {
	return DB.Create(commit).Error
}

// UpdateTreeState 更新提交对应的目录树状态 JSON。
func (dao *FileCommitDAO) UpdateTreeState(id string, treeState string) error {
	return DB.Model(&entity.FileCommit{}).Where("id = ?", id).Update("tree_state", treeState).Error
}

// GetLatestByFolderID 获取文件夹下最新一条提交（按 create_time 降序）。
func (dao *FileCommitDAO) GetLatestByFolderID(folderID string) (*entity.FileCommit, error) {
	var commit entity.FileCommit
	err := DB.Where("folder_id = ?", folderID).
		Order("create_time DESC").
		First(&commit).Error
	if err != nil {
		return nil, err
	}
	return &commit, nil
}

// allowedFileCommitSorts 为 ListByFolderID 排序字段白名单，防止 SQL 注入。
var allowedFileCommitSorts = map[string]string{
	"create_time": "create_time",
	"create_date": "create_date",
	"update_time": "update_time",
	"update_date": "update_date",
}

// ListByFolderID 分页列出文件夹下的提交历史。
func (dao *FileCommitDAO) ListByFolderID(folderID string, page, pageSize int, orderBy string, desc bool) ([]*entity.FileCommit, int64, error) {
	var commits []*entity.FileCommit
	var total int64

	query := DB.Model(&entity.FileCommit{}).Where("folder_id = ?", folderID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 校验 orderBy 是否在白名单内，非法时回退 create_time
	safeCol, ok := allowedFileCommitSorts[orderBy]
	if !ok {
		safeCol = "create_time"
	}

	orderDirection := "DESC"
	if !desc {
		orderDirection = "ASC"
	}

	orderClause := safeCol + " " + orderDirection

	if page > 0 && pageSize > 0 {
		offset := (page - 1) * pageSize
		if err := query.Order(orderClause).Offset(offset).Limit(pageSize).Find(&commits).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Order(orderClause).Find(&commits).Error; err != nil {
			return nil, 0, err
		}
	}

	return commits, total, nil
}

// FileCommitItemDAO 提交明细（单文件变更）表的数据访问对象。
type FileCommitItemDAO struct{}

// NewFileCommitItemDAO 创建 FileCommitItemDAO 实例。
func NewFileCommitItemDAO() *FileCommitItemDAO {
	return &FileCommitItemDAO{}
}

// Create 插入新的提交明细记录。
func (dao *FileCommitItemDAO) Create(item *entity.FileCommitItem) error {
	return DB.Create(item).Error
}

// ListByCommitID 列出某次提交包含的全部文件变更项。
func (dao *FileCommitItemDAO) ListByCommitID(commitID string) ([]*entity.FileCommitItem, error) {
	var items []*entity.FileCommitItem
	err := DB.Where("commit_id = ?", commitID).Order("create_time ASC").Find(&items).Error
	return items, err
}

// ListByFileID 列出某文件的全部历史提交明细（版本历史）。
func (dao *FileCommitItemDAO) ListByFileID(fileID string) ([]*entity.FileCommitItem, error) {
	var items []*entity.FileCommitItem
	err := DB.Where("file_id = ?", fileID).Order("create_time DESC").Find(&items).Error
	return items, err
}

// GetByCommitIDAndFileID 按提交 ID 与文件 ID 精确查询单条明细。
func (dao *FileCommitItemDAO) GetByCommitIDAndFileID(commitID, fileID string) (*entity.FileCommitItem, error) {
	var item entity.FileCommitItem
	err := DB.Where("commit_id = ? AND file_id = ?", commitID, fileID).First(&item).Error
	if err != nil {
		return nil, err
	}
	return &item, nil
}
