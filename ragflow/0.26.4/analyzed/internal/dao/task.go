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
// task.go — 文档解析任务（Task）数据访问层：管理文档分片解析任务的创建、按文档查询及租户级批量硬删除。

//

package dao

import (
	"ragflow/internal/entity"
)

// TaskDAO 解析任务表的数据访问对象。
type TaskDAO struct{}

// NewTaskDAO 创建任务 DAO 实例。
func NewTaskDAO() *TaskDAO {
	return &TaskDAO{}
}

// Create 插入单条解析任务。
func (dao *TaskDAO) Create(task *entity.Task) error {
	return DB.Create(task).Error
}

// CreateMany 批量插入解析任务，空切片直接返回。
func (dao *TaskDAO) CreateMany(tasks []*entity.Task) error {
	if len(tasks) == 0 {
		return nil
	}
	return DB.Create(&tasks).Error
}

// GetByID 按主键查询任务。
func (dao *TaskDAO) GetByID(id string) (*entity.Task, error) {
	var task entity.Task
	err := DB.Where("id = ?", id).First(&task).Error
	if err != nil {
		return nil, err
	}
	return &task, nil
}

// DeleteByDocIDs 按文档 ID 列表硬删除关联任务。
func (dao *TaskDAO) DeleteByDocIDs(docIDs []string) (int64, error) {
	if len(docIDs) == 0 {
		return 0, nil
	}
	result := DB.Unscoped().Where("doc_id IN ?", docIDs).Delete(&entity.Task{})
	return result.RowsAffected, result.Error
}

// DeleteByTenantID 通过 document 子查询按租户 ID 硬删除全部任务。
func (dao *TaskDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("doc_id IN (SELECT id FROM document WHERE tenant_id = ?)", tenantID).Delete(&entity.Task{})
	return result.RowsAffected, result.Error
}

// GetByDocID 按文档 ID 列出任务，按 from_page 与 create_time 升序。
func (dao *TaskDAO) GetByDocID(docID string) ([]*entity.Task, error) {
	var tasks []*entity.Task
	err := DB.Where("doc_id = ?", docID).Order("from_page ASC, create_time ASC").Find(&tasks).Error
	return tasks, err
}

// GetAllTasks 返回全部任务记录（无筛选，多用于管理或调试）。
func (dao *TaskDAO) GetAllTasks() ([]*entity.Task, error) {
	var tasks []*entity.Task
	err := DB.Find(&tasks).Error
	return tasks, err
}
