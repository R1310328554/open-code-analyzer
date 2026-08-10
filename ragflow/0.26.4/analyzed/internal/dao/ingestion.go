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
// ingestion.go — 文档摄取任务 DAO：管理摄取任务、子任务（tasklet）及日志的状态机与事务性创建/取消/删除。

//

package dao

import (
	"errors"
	"fmt"
	"ragflow/internal/common"
	"ragflow/internal/entity"
	"ragflow/internal/utility"
)

// IngestionTaskDAO 摄取主任务表的数据访问对象。
type IngestionTaskDAO struct{}

// NewIngestionTaskDAO 创建摄取任务 DAO。
func NewIngestionTaskDAO() *IngestionTaskDAO {
	return &IngestionTaskDAO{}
}

// 以下状态流转说明供 API 服务创建任务时参考
// created → running：摄取器领取任务后设为 running
// running → completed：任务执行成功
// running → failed：执行过程中出错
// created → canceling：摄取器未领取前用户取消
// running → canceling：执行中用户取消
// completed → canceling：已完成任务的清理/回滚取消
// canceling → canceled：取消流程完成
// failed → created：失败后重试，回到 created
// canceled → created：取消后重新执行
// CheckAndCreate 事务内检查 document_id 唯一性并创建或重启失败/已停止任务。
func (dao *IngestionTaskDAO) CheckAndCreate(ingestionTask *entity.IngestionTask) (*entity.IngestionTask, error) {

	tx := DB.Begin()
	if tx.Error != nil {
		return nil, tx.Error
	}

	defer func() {
		if r := recover(); r != nil {
			tx.Rollback()
			panic(r)
		}
	}()

	// 检查是否已有同 document_id 的任务
	var taskRecord *entity.IngestionTask
	err := tx.Where("document_id = ?", ingestionTask.DocumentID).First(&taskRecord).Error
	if err == nil {
		// 已存在记录
		if taskRecord.Status == common.FAILED || taskRecord.Status == common.STOPPED {
			// 失败或已停止则重置为 created 以重启
			err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskRecord.ID).Update("status", common.CREATED).Error
			if err != nil {
				tx.Rollback()
				return nil, err
			}
		} else {
			return nil, fmt.Errorf("document id %s already exists, status: %s, task id: %s", ingestionTask.DocumentID, taskRecord.Status, taskRecord.ID)
		}
	} else {
		// 不存在则新建摄取任务
		ingestionTask.ID = utility.GenerateUUID()
		if err = tx.Create(ingestionTask).Error; err != nil {
			tx.Rollback()
			return nil, err
		}
		taskRecord = ingestionTask
	}

	if err = tx.Commit().Error; err != nil {
		return nil, err
	}

	return taskRecord, nil
}

// UpdateStatus 直接更新摄取任务状态字段。
func (dao *IngestionTaskDAO) UpdateStatus(taskID, status string) error {
	return DB.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Update("status", status).Error
}

// 以下说明供摄取器组件调用 SetRunningByIngestor
// RUNNING/COMPLETED/STOPPED/FAILED 时直接返回不报错
// CREATED 时更新为 RUNNING
// STOPPING 时更新为 STOPPED
// SetRunningByIngestor 摄取器领取任务：CREATED→RUNNING 或 STOPPING→STOPPED。
func (dao *IngestionTaskDAO) SetRunningByIngestor(taskID string) (*entity.IngestionTask, error) {

	tx := DB.Begin()
	if tx.Error != nil {
		return nil, tx.Error
	}
	var committed bool

	defer func() {
		if committed {
			tx.Commit()
		} else {
			tx.Rollback()
			if r := recover(); r != nil {
				panic(r)
			}
		}
	}()

	var tasks []*entity.IngestionTask
	err := tx.Where("id = ?", taskID).Find(&tasks).Error
	if err != nil {
		return nil, err
	}

	if len(tasks) == 0 {
		return nil, common.ErrTaskNotFound
	}

	if len(tasks) != 1 {
		return nil, fmt.Errorf("task %s has multiple records", taskID)
	}

	taskStatus := tasks[0].Status
	switch taskStatus {
	case common.CREATED:
		tasks[0].Status = common.RUNNING
		err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Update("status", common.RUNNING).Error
		if err != nil {
			return nil, err
		}
		committed = true
		return tasks[0], nil
	case common.STOPPING:
		tasks[0].Status = common.STOPPED
		err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Update("status", common.STOPPED).Error
		if err != nil {
			return nil, err
		}
		committed = true
		return tasks[0], nil
	case common.RUNNING:
		// 任务已在运行，幂等返回
		committed = true
		return tasks[0], nil
	default:
		return tasks[0], nil
	}
}

// SetStoppingByAPIServer API 发起取消：CREATED→STOPPED 或 RUNNING→STOPPING。
func (dao *IngestionTaskDAO) SetStoppingByAPIServer(taskID string) (*entity.IngestionTask, error) {

	tx := DB.Begin()
	if tx.Error != nil {
		return nil, tx.Error
	}
	var committed bool

	defer func() {
		if committed {
			tx.Commit()
		} else {
			tx.Rollback()
			if r := recover(); r != nil {
				panic(r)
			}
		}
	}()

	var tasks []*entity.IngestionTask
	err := tx.Where("id = ?", taskID).Find(&tasks).Error
	if err != nil {
		return nil, err
	}

	if len(tasks) == 0 {
		return nil, fmt.Errorf("task %s not found", taskID)
	}

	if len(tasks) != 1 {
		return nil, fmt.Errorf("task %s has multiple records", taskID)
	}

	taskStatus := tasks[0].Status
	switch taskStatus {
	case common.CREATED:
		tasks[0].Status = common.STOPPED
		err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Update("status", common.STOPPED).Error
		if err != nil {
			return nil, err
		}
		committed = true
		return tasks[0], nil
	case common.RUNNING:
		tasks[0].Status = common.STOPPING
		err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Update("status", common.STOPPING).Error
		if err != nil {
			return nil, err
		}
		committed = true
		return tasks[0], nil
	default:
		return tasks[0], nil
	}
}

// TaskletInfo 删除任务时需清理的子任务摘要。
type TaskletInfo struct {
	TaskletID     string   `json:"tasklet_id"`
	FilesToDelete []string `json:"files_to_delete"`
}

// TaskInfo 删除摄取任务时返回的待清理文件与 tasklet 信息。
type TaskInfo struct {
	TaskID        string        `json:"task_id"`
	FilesToDelete []string      `json:"files_to_delete"`
	Tasklets      []TaskletInfo `json:"tasklets"`
}

// RemoveByAPIServerOrAdminServer 在非执行状态下删除任务并汇总 checkpoint 中的待删文件。
func (dao *IngestionTaskDAO) RemoveByAPIServerOrAdminServer(taskID string, userID *string) (*TaskInfo, error) {

	tx := DB.Begin()
	if tx.Error != nil {
		return nil, tx.Error
	}
	var committed bool

	defer func() {
		if committed {
			tx.Commit()
		} else {
			tx.Rollback()
			if r := recover(); r != nil {
				panic(r)
			}
		}
	}()

	var tasks []*entity.IngestionTask
	err := tx.Where("id = ?", taskID).Find(&tasks).Error
	if err != nil {
		return nil, err
	}

	if len(tasks) == 0 {
		return nil, fmt.Errorf("task %s not found", taskID)
	}

	if len(tasks) != 1 {
		return nil, fmt.Errorf("task %s has multiple records", taskID)
	}

	if userID != nil {
		if tasks[0].UserID != *userID {
			return nil, errors.New("task does not belong to the user")
		}
	}

	taskStatus := tasks[0].Status
	switch taskStatus {
	case common.CREATED, common.STOPPED, common.COMPLETED, common.FAILED:
		// 收集全部 tasklet 及其日志中的 files 列表
		var tasklets []*entity.IngestionTasklet
		err = tx.Where("task_id = ?", taskID).Find(&tasklets).Error
		if err != nil {
			return nil, err
		}
		var TaskletInfos []TaskletInfo
		for _, tasklet := range tasklets {
			// get all ingestion tasklet log
			var taskletLogs []*entity.IngestionTaskletLog
			err = tx.Where("tasklet_id = ?", tasklet.ID).Find(&taskletLogs).Error

			fileMap := make(map[string]bool)
			for _, taskletLog := range taskletLogs {
				files, ok := taskletLog.Checkpoint["files"].([]string)
				if ok {
					for _, file := range files {
						fileMap[file] = true
					}
				}
			}
			var filesToDelete []string
			for file := range fileMap {
				filesToDelete = append(filesToDelete, file)
			}
			TaskletInfos = append(TaskletInfos, TaskletInfo{
				TaskletID:     tasklet.ID,
				FilesToDelete: filesToDelete,
			})
		}

		// 收集任务级日志中的待删文件
		var taskLogs []*entity.IngestionTaskLog
		err = tx.Where("task_id = ?", taskID).Find(&taskLogs).Error
		if err != nil {
			return nil, err
		}

		fileMap := make(map[string]bool)
		for _, taskLog := range taskLogs {
			files, ok := taskLog.Checkpoint["files"].([]string)
			if ok {
				for _, file := range files {
					fileMap[file] = true
				}
			}
		}
		var filesToDelete []string
		for file := range fileMap {
			filesToDelete = append(filesToDelete, file)
		}

		err = tx.Model(&entity.IngestionTask{}).Where("id = ?", taskID).Delete(&entity.IngestionTask{}).Error
		if err != nil {
			return nil, err
		}

		taskInfo := &TaskInfo{
			TaskID:        taskID,
			FilesToDelete: filesToDelete,
			Tasklets:      TaskletInfos,
		}
		committed = true
		return taskInfo, nil
	default:
		return nil, fmt.Errorf("task %s is executing, cannot be removed", taskID)
	}
}

// GetAllTasks 管理员分页或全量列出摄取任务。
func (dao *IngestionTaskDAO) GetAllTasks(page, pageSize int) ([]*entity.IngestionTask, error) {
	var tasks []*entity.IngestionTask
	var err error
	if pageSize == 0 {
		err = DB.Find(&tasks).Error
	} else {
		err = DB.Order("create_time DESC").Offset((page - 1) * pageSize).Limit(pageSize).Find(&tasks).Error
	}
	return tasks, err
}

// ListByUserID 按用户 ID 分页列出摄取任务。
func (dao *IngestionTaskDAO) ListByUserID(userID string, page, pageSize int) ([]*entity.IngestionTask, error) {
	var tasks []*entity.IngestionTask
	var err error
	if pageSize == 0 {
		err = DB.Where("user_id = ?", userID).Order("create_time DESC").Find(&tasks).Error
	} else {
		err = DB.Where("user_id = ?", userID).Order("create_time DESC").Offset((page - 1) * pageSize).Limit(pageSize).Find(&tasks).Error
	}

	return tasks, err
}

// ListByUserIDAndDatasetID 按用户与数据集 ID 分页列出任务。
func (dao *IngestionTaskDAO) ListByUserIDAndDatasetID(userID, datasetID string, page, pageSize int) ([]*entity.IngestionTask, error) {
	var tasks []*entity.IngestionTask
	var err error
	if pageSize == 0 {
		err = DB.Where("user_id = ? AND dataset_id = ?", userID, datasetID).Order("create_time DESC").Find(&tasks).Error
	} else {
		err = DB.Where("user_id = ? AND dataset_id = ?", userID, datasetID).Order("create_time DESC").Offset((page - 1) * pageSize).Limit(pageSize).Find(&tasks).Error
	}

	return tasks, err
}

// GetByID 按任务 ID 查询摄取任务。
func (dao *IngestionTaskDAO) GetByID(id string) (*entity.IngestionTask, error) {
	var task *entity.IngestionTask
	err := DB.Where("id = ?", id).First(&task).Error
	return task, err
}

// GetByDocumentID 按文档 ID 查询关联摄取任务。
func (dao *IngestionTaskDAO) GetByDocumentID(documentId string) (*entity.IngestionTask, error) {
	var task *entity.IngestionTask
	err := DB.Where("document_id = ?", documentId).First(&task).Error
	return task, err
}

// IngestionTaskLogDAO 摄取任务级检查点日志 DAO。
type IngestionTaskLogDAO struct{}

// NewIngestionTaskLogDAO 创建任务日志 DAO。
func NewIngestionTaskLogDAO() *IngestionTaskLogDAO {
	return &IngestionTaskLogDAO{}
}

// Create 写入任务级检查点日志。
func (dao *IngestionTaskLogDAO) Create(ingestionLog *entity.IngestionTaskLog) error {
	return DB.Create(ingestionLog).Error
}

// ListLogsByTaskID 按任务 ID 列出全部日志（create_time 降序）。
func (dao *IngestionTaskLogDAO) ListLogsByTaskID(taskID string) ([]*entity.IngestionTaskLog, error) {
	var tasks []*entity.IngestionTaskLog
	err := DB.Where("task_id = ?", taskID).Order("create_time DESC").Find(&tasks).Error
	return tasks, err
}

// LatestLogByTaskID 取最新检查点；按 id 降序以保证秒级 create_time 并列时顺序确定。
func (dao *IngestionTaskLogDAO) LatestLogByTaskID(taskID string) (*entity.IngestionTaskLog, error) {
	var task *entity.IngestionTaskLog
	// 必须按 id 降序而非 create_time：create_time 仅秒级精度，
	// 同一秒内多条 checkpoint 用自增 id 才能反映写入顺序。
	// 流水线恢复算法依赖最新一行，tie-break 必须确定。
	// id 自增单调，始终反映写入先后。
	// 见上：恢复逻辑读取最新日志行。
	// tie-break 必须确定，否则跨 JVM 行为不一致。
	// 因此使用 id DESC 排序。
	err := DB.Where("task_id = ?", taskID).Order("id DESC").First(&task).Error
	return task, err
}

// GetLogByLogID 按日志 ID 查询单条任务日志。
func (dao *IngestionTaskLogDAO) GetLogByLogID(logID string) (*entity.IngestionTaskLog, error) {
	var task *entity.IngestionTaskLog
	err := DB.Where("id = ?", logID).First(&task).Error
	return task, err
}

// DeleteByTaskID 按任务 ID 硬删除全部任务日志。
func (dao *IngestionTaskLogDAO) DeleteByTaskID(taskID string) (int64, error) {
	result := DB.Unscoped().Where("task_id = ?", taskID).Delete(&entity.IngestionTaskLog{})
	return result.RowsAffected, result.Error
}

// IngestionTaskletDAO 摄取子任务（tasklet）DAO。
type IngestionTaskletDAO struct{}

// NewIngestionTaskletDAO 创建 tasklet DAO。
func NewIngestionTaskletDAO() *IngestionTaskletDAO {
	return &IngestionTaskletDAO{}
}

// Create 插入新的 tasklet 记录。
func (dao *IngestionTaskletDAO) Create(ingestionTasklet *entity.IngestionTasklet) error {
	return DB.Create(ingestionTasklet).Error
}

// UpdateStatus 更新 tasklet 状态。
func (dao *IngestionTaskletDAO) UpdateStatus(taskletID, status string) error {
	return DB.Model(&entity.IngestionTasklet{}).Where("id = ?", taskletID).Update("status", status).Error
}
// GetAllTasklets 列出全部 tasklet。
func (dao *IngestionTaskletDAO) GetAllTasklets() ([]*entity.IngestionTasklet, error) {
	var tasks []*entity.IngestionTasklet
	err := DB.Find(&tasks).Error
	return tasks, err
}

// ListByUserID 按用户 ID 列出 tasklet。
func (dao *IngestionTaskletDAO) ListByUserID(userID string) ([]*entity.IngestionTasklet, error) {
	var tasks []*entity.IngestionTasklet
	err := DB.Where("user_id = ?", userID).Find(&tasks).Error
	return tasks, err
}

// GetByID 按 ID 查询 tasklet。
func (dao *IngestionTaskletDAO) GetByID(id string) (*entity.IngestionTasklet, error) {
	var task *entity.IngestionTasklet
	err := DB.Where("id = ?", id).First(&task).Error
	return task, err
}

// IngestionTaskletLogDAO tasklet 级检查点日志 DAO。
type IngestionTaskletLogDAO struct{}

// NewIngestionTaskletLogDAO 创建 tasklet 日志 DAO。
func NewIngestionTaskletLogDAO() *IngestionTaskletLogDAO {
	return &IngestionTaskletLogDAO{}
}

// Create 写入 tasklet 检查点日志。
func (dao *IngestionTaskletLogDAO) Create(ingestionLog *entity.IngestionTaskletLog) error {
	return DB.Create(ingestionLog).Error
}

// ListLogsByTaskletID 按 tasklet ID 列出日志。
func (dao *IngestionTaskletLogDAO) ListLogsByTaskletID(taskID string) ([]*entity.IngestionTaskletLog, error) {
	var tasks []*entity.IngestionTaskletLog
	err := DB.Where("task_id = ?", taskID).Find(&tasks).Error
	return tasks, err
}

// GetLogByLogID 按日志 ID 查询 tasklet 日志。
func (dao *IngestionTaskletLogDAO) GetLogByLogID(logID string) (*entity.IngestionTaskletLog, error) {
	var task *entity.IngestionTaskletLog
	err := DB.Where("id = ?", logID).First(&task).Error
	return task, err
}

// LatestLogByTaskletID 取 tasklet 最新检查点日志。
func (dao *IngestionTaskletLogDAO) LatestLogByTaskletID(taskletID string) (*entity.IngestionTaskletLog, error) {
	var tasklet *entity.IngestionTaskletLog
	err := DB.Where("tasklet_id = ?", taskletID).Order("create_time DESC").First(&tasklet).Error
	return tasklet, err
}

// DeleteByTaskletID 按 tasklet ID 硬删除全部日志。
func (dao *IngestionTaskletLogDAO) DeleteByTaskletID(taskID string) (int64, error) {
	result := DB.Unscoped().Where("task_id = ?", taskID).Delete(&entity.IngestionTaskletLog{})
	return result.RowsAffected, result.Error
}
