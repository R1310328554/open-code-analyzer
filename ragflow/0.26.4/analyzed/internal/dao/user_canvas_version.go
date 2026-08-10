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
// user_canvas_version.go — 用户画布版本历史 DAO：管理发布/草稿版本、SaveOrReplaceLatest 合并逻辑及未发布版本数量上限清理。

//

package dao

import (
	"errors"
	"reflect"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	"ragflow/internal/entity"
)

// ErrUserCanvasVersionNotFound 按 id 或 canvas id 查版本无结果时返回；上层映射为 404。
var ErrUserCanvasVersionNotFound = errors.New("user_canvas_version: not found")

// UserCanvasVersionDAO 画布版本表的数据访问对象。
type UserCanvasVersionDAO struct{}

// SaveOrReplaceLatestVersionOptions 控制 SaveOrReplaceLatest 的保存参数。
type SaveOrReplaceLatestVersionOptions struct {
	NewID           string
	UserCanvasID    string
	Title           *string
	Description     *string
	DSL             entity.JSONMap
	Release         bool
	KeepUnpublished int
	SameDSL         func(entity.JSONMap) bool
}

// NewUserCanvasVersionDAO 返回无状态 DAO 实例，可共享或按需创建。
func NewUserCanvasVersionDAO() *UserCanvasVersionDAO {
	return &UserCanvasVersionDAO{}
}

// Create 插入新版本行；ID/UserCanvasID/Title/Description/DSL 由调用方赋值，时间戳由 BaseModel 钩子写入。
func (dao *UserCanvasVersionDAO) Create(v *entity.UserCanvasVersion) error {
	return DB.Create(v).Error
}

// GetByID 按主键查单条版本；不存在返回 ErrUserCanvasVersionNotFound。
func (dao *UserCanvasVersionDAO) GetByID(id string) (*entity.UserCanvasVersion, error) {
	var v entity.UserCanvasVersion
	err := DB.Where("id = ?", id).First(&v).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, ErrUserCanvasVersionNotFound
		}
		return nil, err
	}
	return &v, nil
}

// ListByCanvasID 返回画布全部版本，按 create_time 降序（最新在前）。
func (dao *UserCanvasVersionDAO) ListByCanvasID(canvasID string) ([]*entity.UserCanvasVersion, error) {
	var vs []*entity.UserCanvasVersion
	err := DB.Where("user_canvas_id = ?", canvasID).
		Order("create_time DESC").
		Find(&vs).Error
	return vs, err
}

// GetLatest 返回画布最新一条版本；从未发布过则 ErrUserCanvasVersionNotFound。
func (dao *UserCanvasVersionDAO) GetLatest(canvasID string) (*entity.UserCanvasVersion, error) {
	var v entity.UserCanvasVersion
	err := DB.Where("user_canvas_id = ?", canvasID).
		Order("create_time DESC").
		First(&v).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, ErrUserCanvasVersionNotFound
		}
		return nil, err
	}
	return &v, nil
}

// Delete 按 id 删除单条版本；行不存在时为 no-op。
func (dao *UserCanvasVersionDAO) Delete(id string) error {
	return DB.Where("id = ?", id).Delete(&entity.UserCanvasVersion{}).Error
}

// DeleteTx Delete 的事务变体；DeleteVersion 中与父画布统计更新同事务。
func (dao *UserCanvasVersionDAO) DeleteTx(tx *gorm.DB, id string) error {
	return tx.Where("id = ?", id).Delete(&entity.UserCanvasVersion{}).Error
}

// DeleteByCanvasID 删除画布下全部版本；删画布时级联（§2.9），返回实际删除行数。
func (dao *UserCanvasVersionDAO) DeleteByCanvasID(canvasID string) (int64, error) {
	res := DB.Where("user_canvas_id = ?", canvasID).Delete(&entity.UserCanvasVersion{})
	return res.RowsAffected, res.Error
}

// DeleteByCanvasIDTx DeleteByCanvasID 的事务变体；DeleteAgent 与删画布同事务。
func (dao *UserCanvasVersionDAO) DeleteByCanvasIDTx(tx *gorm.DB, canvasID string) (int64, error) {
	res := tx.Where("user_canvas_id = ?", canvasID).Delete(&entity.UserCanvasVersion{})
	return res.RowsAffected, res.Error
}

// CreateTx Create 的事务变体。
func (dao *UserCanvasVersionDAO) CreateTx(tx *gorm.DB, v *entity.UserCanvasVersion) error {
	return tx.Create(v).Error
}

// SaveOrReplaceLatest 插入新版本或原地更新最新匹配草稿；若最新已发布且当前为草稿保存，则新建草稿以保留已发布快照。
func (dao *UserCanvasVersionDAO) SaveOrReplaceLatest(opts SaveOrReplaceLatestVersionOptions) (*entity.UserCanvasVersion, error) {
	if opts.KeepUnpublished <= 0 {
		opts.KeepUnpublished = 20
	}
	var saved *entity.UserCanvasVersion
	if err := DB.Transaction(func(tx *gorm.DB) error {
		var parent struct {
			ID string
		}
		if err := tx.Clauses(clause.Locking{Strength: "UPDATE"}).
			Table((&entity.UserCanvas{}).TableName()).
			Select("id").
			Where("id = ?", opts.UserCanvasID).
			Take(&parent).Error; err != nil {
			return err
		}

		var latest entity.UserCanvasVersion
		err := tx.Where("user_canvas_id = ?", opts.UserCanvasID).
			Order("create_time DESC, id DESC").
			First(&latest).Error
		if err != nil {
			if !errors.Is(err, gorm.ErrRecordNotFound) {
				return err
			}
		} else if opts.sameDSL(latest.DSL) {
			if !latest.Release || opts.Release {
				updates := map[string]interface{}{
					"dsl":     opts.DSL,
					"release": opts.Release,
				}
				if opts.Title != nil {
					updates["title"] = opts.Title
				}
				if opts.Description != nil {
					updates["description"] = opts.Description
				}
				if err := tx.Model(&entity.UserCanvasVersion{}).
					Where("id = ?", latest.ID).
					Updates(updates).Error; err != nil {
					return err
				}
				latest.DSL = opts.DSL
				latest.Release = opts.Release
				if opts.Title != nil {
					latest.Title = opts.Title
				}
				if opts.Description != nil {
					latest.Description = opts.Description
				}
				saved = &latest
				return dao.deleteAllUnpublishedExcessTx(tx, opts.UserCanvasID, opts.KeepUnpublished)
			}
		}
		row := &entity.UserCanvasVersion{
			ID:           opts.NewID,
			UserCanvasID: opts.UserCanvasID,
			Title:        opts.Title,
			Description:  opts.Description,
			Release:      opts.Release,
			DSL:          opts.DSL,
		}
		if err := tx.Create(row).Error; err != nil {
			return err
		}
		saved = row
		return dao.deleteAllUnpublishedExcessTx(tx, opts.UserCanvasID, opts.KeepUnpublished)
	}); err != nil {
		return nil, err
	}
	return saved, nil
}

// sameDSL 判断 DSL 是否与 opts 中内容相同；可注入 SameDSL 自定义比较。
func (opts SaveOrReplaceLatestVersionOptions) sameDSL(dsl entity.JSONMap) bool {
	if opts.SameDSL != nil {
		return opts.SameDSL(dsl)
	}
	return reflect.DeepEqual(dsl, opts.DSL)
}

// DeleteAllUnpublishedExcess 保留最新 keep 条未发布版本，删除更旧的未发布行；已发布版本不受影响。
func (dao *UserCanvasVersionDAO) DeleteAllUnpublishedExcess(canvasID string, keep int) error {
	return dao.deleteAllUnpublishedExcessTx(DB, canvasID, keep)
}

// deleteAllUnpublishedExcessTx 事务内执行未发布版本超额清理。
func (dao *UserCanvasVersionDAO) deleteAllUnpublishedExcessTx(tx *gorm.DB, canvasID string, keep int) error {
	if keep < 0 {
		keep = 0
	}
	var ids []string
	if err := tx.Model(&entity.UserCanvasVersion{}).
		Where(map[string]interface{}{"user_canvas_id": canvasID, "release": false}).
		Order("create_time DESC").
		Pluck("id", &ids).Error; err != nil {
		return err
	}
	if len(ids) <= keep {
		return nil
	}
	ids = ids[keep:]
	return tx.Where("id IN ?", ids).Delete(&entity.UserCanvasVersion{}).Error
}
