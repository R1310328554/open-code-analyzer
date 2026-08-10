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
// user_canvas.go — 用户画布（Agent Canvas）数据访问层：提供画布 CRUD、租户/团队可见性控制、标签筛选与排序白名单，对齐 Python UserCanvasService 行为并缓解 IDOR 风险。

//

package dao

import (
	"errors"
	"regexp"
	"strings"

	"gorm.io/gorm"

	"ragflow/internal/entity"
)

// ErrUserCanvasNotFound 在 GetByIDForUser 中返回，表示画布不存在或调用方无读权限。故意不区分「不存在」与「无权限」，防止通过响应枚举他人画布 ID（IDOR 缓解，见 plan §4.8）。

// userCanvasOrderableColumns 为 ORDER BY 可排序列白名单，防止用户传入的 orderby 参数直接拼入 SQL。
var userCanvasOrderableColumns = map[string]struct{}{
	"id":              {},
	"user_id":         {},
	"title":           {},
	"permission":      {},
	"canvas_type":     {},
	"canvas_category": {},
	"create_time":     {},
	"create_date":     {},
	"update_time":     {},
	"update_date":     {},
}

// userCanvasOrderClause 根据白名单生成未限定表前缀的排序子句，非法字段回退 create_time。
func userCanvasOrderClause(orderby string, desc bool) string {
	if _, ok := userCanvasOrderableColumns[orderby]; !ok {
		orderby = "create_time"
	}
	if desc {
		return orderby + " DESC"
	}
	return orderby + " ASC"
}

// userCanvasQualifiedOrderClause 生成带 user_canvas. 前缀的排序子句，用于联表查询。
func userCanvasQualifiedOrderClause(orderby string, desc bool) string {
	if _, ok := userCanvasOrderableColumns[orderby]; !ok {
		orderby = "create_time"
	}
	order := "user_canvas." + orderby
	if desc {
		return order + " DESC"
	}
	return order + " ASC"
}

// escapeSQLLike 转义 LIKE 模式中的 \、%、_，供标签正则/LIKE 安全使用。
func escapeSQLLike(s string) string {
	replacer := strings.NewReplacer(`\`, `\\`, `%`, `\%`, `_`, `\_`)
	return replacer.Replace(s)
}

// splitUserCanvasTags 按逗号拆分标签字符串并去除空白。
func splitUserCanvasTags(raw string) []string {
	parts := strings.Split(raw, ",")
	tags := make([]string, 0, len(parts))
	for _, tag := range parts {
		tag = strings.TrimSpace(tag)
		if tag != "" {
			tags = append(tags, tag)
		}
	}
	return tags
}

// applyUserCanvasTagFilter 用 REGEXP 对 user_canvas.tags 做多标签 OR 过滤。
func applyUserCanvasTagFilter(query *gorm.DB, tags []string) *gorm.DB {
	if len(tags) == 0 {
		return query
	}
	tagQuery := DB.Session(&gorm.Session{NewDB: true})
	hasTag := false
	for _, tag := range tags {
		tag = strings.TrimSpace(tag)
		if tag == "" {
			continue
		}
		pattern := "(^|,)[[:space:]]*" + regexp.QuoteMeta(tag) + "[[:space:]]*(,|$)"
		cond := DB.Where("user_canvas.tags REGEXP ?", pattern)
		if !hasTag {
			tagQuery = tagQuery.Where(cond)
			hasTag = true
		} else {
			tagQuery = tagQuery.Or(cond)
		}
	}
	if !hasTag {
		return query
	}
	return query.Where(tagQuery)
}

// ErrUserCanvasNotFound 画布未找到或无访问权限的统一错误。
var ErrUserCanvasNotFound = errors.New("user_canvas: not found or access denied")

// UserCanvasDAO 用户画布表的数据访问对象。
type UserCanvasDAO struct{}

// NewUserCanvasDAO 创建 UserCanvasDAO 实例。
func NewUserCanvasDAO() *UserCanvasDAO {
	return &UserCanvasDAO{}
}

// Create 插入新画布记录。
func (dao *UserCanvasDAO) Create(userCanvas *entity.UserCanvas) error {
	return DB.Create(userCanvas).Error
}

// GetByID 按主键查询画布；未找到时返回 ErrUserCanvasNotFound。
func (dao *UserCanvasDAO) GetByID(id string) (*entity.UserCanvas, error) {
	var canvas entity.UserCanvas
	err := DB.Where("id = ?", id).First(&canvas).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, ErrUserCanvasNotFound
		}
		return nil, err
	}
	return &canvas, nil
}

// GetByIDForUser 按画布 ID 查询并 enforce 可见性：
//   - permission=me 或 user_id=请求用户 的画布始终返回；
//   - permission=team 时仅当画布 owner 属于请求用户所在租户时返回（谓词同 ListByTenantIDs）。
// 其余情况统一返回 ErrUserCanvasNotFound，避免 HTTP 层泄露「存在但无权」与「不存在」。
func (dao *UserCanvasDAO) GetByIDForUser(canvasID, userID string, tenantIDs []string) (*entity.UserCanvas, error) {
	if canvasID == "" {
		return nil, ErrUserCanvasNotFound
	}
	if userID == "" {
		return nil, ErrUserCanvasNotFound
	}

	// owner=userID is allowed regardless of permission, matching the
	// ListByTenantIDs predicate used by GET /api/v1/agents.
	ownerOrTeam := DB.Where("user_id = ?", userID)
	if len(tenantIDs) > 0 {
		ownerOrTeam = ownerOrTeam.Or(
			"user_id IN ? AND permission = ?", tenantIDs, "team",
		)
	}

	var canvas entity.UserCanvas
	err := DB.Where("id = ?", canvasID).Where(ownerOrTeam).First(&canvas).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, ErrUserCanvasNotFound
		}
		return nil, err
	}
	return &canvas, nil
}

// Update 全量保存画布实体。
func (dao *UserCanvasDAO) Update(userCanvas *entity.UserCanvas) error {
	return DB.Save(userCanvas).Error
}

// Accessible 判断 userID 是否可访问 canvasID，规则同 GetByIDForUser。用于下游鉴权（如 PR #16169 沙箱产物下载）。任何错误返回 false，等价 404，不泄露画布是否存在。
// 租户范围：permission=team 的画布仅当 userID 属于 owner 的租户之一时可访问；tenantIDs 为空则禁用 team 分支（安全默认）。无租户列表时应先 GetTenantIDsByUserID。
func (dao *UserCanvasDAO) Accessible(canvasID, userID string, tenantIDs []string) bool {
	if canvasID == "" || userID == "" {
		return false
	}
	// Owner can always access their own canvas regardless of permission.
	// Team-permission canvases are reachable only when the caller is a
	// member of one of the owner's tenants — mirrors the predicate in
	// GetByIDForUser / ListByTenantIDs.
	ownerOrTeam := DB.Where("user_id = ?", userID)
	if len(tenantIDs) > 0 {
		ownerOrTeam = ownerOrTeam.Or(
			"user_id IN ? AND permission = ?", tenantIDs, "team",
		)
	}
	var canvas entity.UserCanvas
	err := DB.Select("id").
		Where("id = ?", canvasID).
		Where(ownerOrTeam).
		First(&canvas).Error
	if err != nil {
		return false
	}
	return canvas.ID == canvasID
}

// Delete 按 ID 硬删除画布。
func (dao *UserCanvasDAO) Delete(id string) error {
	// gorm v2 treats the first non-int inline arg as a column name, not a
	// primary-key value — passing `id` verbatim produced WHERE ID = ?
	// and made MySQL complain about an unknown "AGENT_ID" column. The
	// explicit Where+Delete form is the same pattern used by
	// API4ConversationDAO.Delete (see api_token.go:142-144).
	return DB.Where("id = ?", id).Delete(&entity.UserCanvas{}).Error
}

// UpdateTx Update 的事务变体，供 publish-agent、delete-agent 等多步写原子提交。
func (dao *UserCanvasDAO) UpdateTx(tx *gorm.DB, userCanvas *entity.UserCanvas) error {
	return tx.Save(userCanvas).Error
}

// DeleteTx Delete 的事务变体；调用方须已加载并校验访问权限。
func (dao *UserCanvasDAO) DeleteTx(tx *gorm.DB, id string) error {
	// See Delete() above for the rationale on Where("id = ?", id).
	return tx.Where("id = ?", id).Delete(&entity.UserCanvas{}).Error
}

// GetByUserAndTitle 按 user_id+title（可选 canvas_category）查画布；不存在返回 (nil,nil)。CreateAgent 用于「标题已存在」校验，对齐 Python UserCanvasService.query。
func (dao *UserCanvasDAO) GetByUserAndTitle(userID, title, canvasCategory string) (*entity.UserCanvas, error) {
	q := DB.Where("user_id = ? AND title = ?", userID, title)
	if canvasCategory != "" {
		q = q.Where("canvas_category = ?", canvasCategory)
	}
	var row entity.UserCanvas
	if err := q.First(&row).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &row, nil
}

// GetList 分页列出某租户下画布，支持 id/title/category/type 过滤；对齐 Python get_list。
func (dao *UserCanvasDAO) GetList(tenantID string, pageNumber, itemsPerPage int, orderby string, desc bool, id, title string, canvasCategory, canvasType string) ([]*entity.UserCanvas, error) {

	query := DB.Model(&entity.UserCanvas{}).
		Where("user_id = ?", tenantID)

	if id != "" {
		query = query.Where("id = ?", id)
	}
	if title != "" {
		query = query.Where("title = ?", title)
	}
	if canvasCategory != "" {
		query = query.Where("canvas_category = ?", canvasCategory)
	}

	if canvasType != "" {
		query = query.Where("canvas_type = ?", canvasType)
	}

	// 排序：经 userCanvasOrderClause 白名单校验，禁止原始 orderby 拼 SQL
	// Route orderby through userCanvasOrderClause above so user-supplied
	// query params can never reach Order() verbatim. The helper validates
	// against userCanvasOrderableColumns (a closed allowlist) and falls
	// back to "create_time" on any miss, so the string spliced into the
	// SQL fragment is always one of a fixed set of column names.
	query = query.Order(userCanvasOrderClause(orderby, desc))

	// 分页：pageNumber/itemsPerPage 均大于 0 时应用 Offset/Limit
	if pageNumber > 0 && itemsPerPage > 0 {
		offset := (pageNumber - 1) * itemsPerPage
		query = query.Offset(offset).Limit(itemsPerPage)
	}

	var canvases []*entity.UserCanvas
	err := query.Find(&canvases).Error
	return canvases, err
}

// GetAllCanvasesByTenantIDs 返回租户 ID 列表下可见画布摘要（team 权限或本人）；对齐 Python get_all_agents_by_tenant_ids。
func (dao *UserCanvasDAO) GetAllCanvasesByTenantIDs(tenantIDs []string, userID string) ([]*CanvasBasicInfo, error) {

	query := DB.Model(&entity.UserCanvas{}).
		Select("id, avatar, title, permission, canvas_type, canvas_category").
		Where("user_id IN (?) AND permission = ?", tenantIDs, "team").
		Or("user_id = ?", userID).
		Order("create_time ASC")

	var results []*CanvasBasicInfo
	err := query.Scan(&results).Error
	return results, err
}

// UserCanvasListItem ListByTenantIDs 联表 user 后的列表行结构。
type UserCanvasListItem struct {
	ID             string  `gorm:"column:id"`
	Avatar         *string `gorm:"column:avatar"`
	Title          *string `gorm:"column:title"`
	Description    *string `gorm:"column:description"`
	Permission     string  `gorm:"column:permission"`
	UserID         string  `gorm:"column:user_id"`
	TenantID       string  `gorm:"column:tenant_id"`
	Nickname       *string `gorm:"column:nickname"`
	TenantAvatar   *string `gorm:"column:tenant_avatar"`
	CanvasType     *string `gorm:"column:canvas_type"`
	CanvasCategory string  `gorm:"column:canvas_category"`
	Tags           string  `gorm:"column:tags"`
	CreateTime     *int64  `gorm:"column:create_time"`
	UpdateTime     *int64  `gorm:"column:update_time"`
}

// ListByTenantIDs 按 ownerIDs 列出可访问 agent 画布，支持关键词/标签/分页/排序；对齐 Python get_by_tenant_ids。
func (dao *UserCanvasDAO) ListByTenantIDs(ownerIDs []string, userID string, page, pageSize int, orderby string, desc bool, keywords, canvasCategory, canvasType string, tags []string) ([]*UserCanvasListItem, int64, error) {
	if len(ownerIDs) == 0 {
		return nil, 0, nil
	}

	// 谓词：ownerIDs 中 team 权限画布 + userID 拥有的全部画布。
	base := DB.Model(&entity.UserCanvas{}).
		Select(`user_canvas.id,
			user_canvas.avatar,
			user_canvas.title,
			user_canvas.description,
			user_canvas.permission,
			user_canvas.user_id,
			user_canvas.user_id AS tenant_id,
			user.nickname,
			user.avatar AS tenant_avatar,
			user_canvas.canvas_type,
			user_canvas.canvas_category,
			user_canvas.tags,
			user_canvas.create_time,
			user_canvas.update_time`).
		Joins("LEFT JOIN user ON user_canvas.user_id = user.id").
		Where(
			DB.Where("user_canvas.user_id IN ? AND user_canvas.permission = ?", ownerIDs, "team").
				Or("user_canvas.user_id = ?", userID),
			"user_canvas.user_id IN ?",
			ownerIDs,
		).Where(
		DB.Where("user_canvas.permission = ?", "team").
			Or("user_canvas.user_id = ?", userID))

	if canvasCategory != "" {
		base = base.Where("user_canvas.canvas_category = ?", canvasCategory)
	}

	if canvasType != "" {
		base = base.Where("canvas_type = ?", canvasType)
	}

	if keywords != "" {
		like := "%" + keywords + "%"
		base = base.Where("user_canvas.title LIKE ?", like)
	}
	base = applyUserCanvasTagFilter(base, tags)

	var total int64
	if err := base.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	order := userCanvasQualifiedOrderClause(orderby, desc)
	// codeql[go/sql-injection] False positive: `order` was just derived
	// from userCanvasQualifiedOrderClause above, which validates `orderby`
	// against userCanvasOrderableColumns (a closed allowlist) and
	// defaults to "create_time" on miss. The string spliced into
	// Order() is always one of a fixed set of qualified column names.
	query := base.Order(order)

	if page > 0 && pageSize > 0 {
		query = query.Offset((page - 1) * pageSize).Limit(pageSize)
	}

	var canvases []*UserCanvasListItem
	if err := query.Scan(&canvases).Error; err != nil {
		return nil, 0, err
	}
	return canvases, total, nil
}

// ListTags 统计 userID 可见画布上各标签出现次数。
func (dao *UserCanvasDAO) ListTags(ownerIDs []string, userID string, canvasCategory string) (map[string]int, error) {
	if len(ownerIDs) == 0 {
		return map[string]int{}, nil
	}

	query := DB.Model(&entity.UserCanvas{}).
		Select("user_canvas.tags").
		Where(
			DB.Where("user_canvas.user_id IN ? AND user_canvas.permission = ?", ownerIDs, "team").
				Or("user_canvas.user_id = ?", userID),
		)

	if canvasCategory != "" {
		query = query.Where("user_canvas.canvas_category = ?", canvasCategory)
	} else {
		query = query.Where("user_canvas.canvas_category = ?", "agent_canvas")
	}

	var rows []struct {
		Tags string `gorm:"column:tags"`
	}
	if err := query.Scan(&rows).Error; err != nil {
		return nil, err
	}

	counts := make(map[string]int)
	for _, row := range rows {
		for _, tag := range splitUserCanvasTags(row.Tags) {
			counts[tag]++
		}
	}
	return counts, nil
}

// GetByCanvasID 按 canvas ID 查询（GetByID 别名）。
func (dao *UserCanvasDAO) GetByCanvasID(canvasID string) (*entity.UserCanvas, error) {
	return dao.GetByID(canvasID)
}

// CanvasBasicInfo 列表接口返回的画布摘要字段。
type CanvasBasicInfo struct {
	ID             string  `gorm:"column:id" json:"id"`
	Avatar         *string `gorm:"column:avatar" json:"avatar,omitempty"`
	Title          *string `gorm:"column:title" json:"title,omitempty"`
	Permission     string  `gorm:"column:permission" json:"permission"`
	CanvasType     *string `gorm:"column:canvas_type" json:"canvas_type,omitempty"`
	CanvasCategory string  `gorm:"column:canvas_category" json:"canvas_category"`
}

// DeleteByUserID 按用户 ID 硬删除全部画布。
func (dao *UserCanvasDAO) DeleteByUserID(userID string) (int64, error) {
	result := DB.Unscoped().Where("user_id = ?", userID).Delete(&entity.UserCanvas{})
	return result.RowsAffected, result.Error
}

// GetAllCanvasIDsByUserID 返回用户拥有的全部画布 ID。
func (dao *UserCanvasDAO) GetAllCanvasIDsByUserID(userID string) ([]string, error) {
	var canvasIDs []string
	err := DB.Model(&entity.UserCanvas{}).
		Where("user_id = ?", userID).
		Pluck("id", &canvasIDs).Error
	return canvasIDs, err
}

// UpdateDSL 按画布 ID 更新 dsl JSON 字段。
func (dao *UserCanvasDAO) UpdateDSL(canvasID string, dsl entity.JSONMap) (int64, error) {
	result := DB.Model(&entity.UserCanvas{}).Where("id = ?", canvasID).Update("dsl", dsl)
	return result.RowsAffected, result.Error
}

// UpdateFields 按 ID 部分更新指定列。
func (dao *UserCanvasDAO) UpdateFields(canvasID string, fields map[string]interface{}) (int64, error) {
	result := DB.Model(&entity.UserCanvas{}).Where("id = ?", canvasID).Updates(fields)
	return result.RowsAffected, result.Error
}

// UpdateTags 按画布 ID 更新逗号分隔的 tags 字段。
func (dao *UserCanvasDAO) UpdateTags(canvasID, tags string) (int64, error) {
	result := DB.Model(&entity.UserCanvas{}).Where("id = ?", canvasID).Update("tags", tags)
	return result.RowsAffected, result.Error
}
