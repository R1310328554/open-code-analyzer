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
// file.go — 租户文件树数据访问层：管理文件夹层级、根目录初始化、知识库镜像目录及并发去重逻辑。

//

package dao

import (
	"fmt"
	"log"
	"ragflow/internal/entity"
	"ragflow/internal/utility"
	"strings"
)

// FileDAO 文件表的数据访问对象。
type FileDAO struct{}

// NewFileDAO 创建文件 DAO 实例。
func NewFileDAO() *FileDAO {
	return &FileDAO{}
}

// GetByID 按 ID 查询文件或文件夹记录。
func (dao *FileDAO) GetByID(id string) (*entity.File, error) {
	var file entity.File
	err := DB.Where("id = ?", id).First(&file).Error
	if err != nil {
		return nil, err
	}
	return &file, nil
}

// GetByPfID 按父文件夹分页列出子项，支持关键词搜索与排序。
func (dao *FileDAO) GetByPfID(tenantID, pfID string, page, pageSize int, orderby string, desc bool, keywords string) ([]*entity.File, int64, error) {
	var files []*entity.File
	var total int64

	query := DB.Model(&entity.File{}).
		Where("tenant_id = ? AND parent_id = ? AND id != ?", tenantID, pfID, pfID)

	// 应用文件名关键词模糊过滤
	if keywords != "" {
		query = query.Where("LOWER(name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	// 统计符合条件的总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 应用排序字段与升降序
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderby + " " + orderDirection)

	// 应用分页偏移与条数限制
	if page > 0 && pageSize > 0 {
		offset := (page - 1) * pageSize
		if err := query.Offset(offset).Limit(pageSize).Find(&files).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Find(&files).Error; err != nil {
			return nil, 0, err
		}
	}

	return files, total, nil
}

// GetRootFolder 获取或懒创建租户根目录（parent_id 指向自身）。
func (dao *FileDAO) GetRootFolder(tenantID string) (*entity.File, error) {
	var file entity.File
	err := DB.Where("tenant_id = ? AND parent_id = id", tenantID).First(&file).Error
	if err == nil {
		return &file, nil
	}

	// 根目录不存在时自动创建
	fileID := utility.GenerateToken()
	file = entity.File{
		ID:        fileID,
		ParentID:  fileID,
		TenantID:  tenantID,
		CreatedBy: tenantID,
		Name:      "/",
		Type:      "folder",
		Size:      0,
	}
	file.SourceType = ""

	if err = DB.Create(&file).Error; err != nil {
		return nil, err
	}
	return &file, nil
}

// GetParentFolder 查询指定文件的直接父文件夹。
func (dao *FileDAO) GetParentFolder(fileID string) (*entity.File, error) {
	var file entity.File
	err := DB.Where("id = ?", fileID).First(&file).Error
	if err != nil {
		return nil, err
	}

	var parentFile entity.File
	err = DB.Where("id = ?", file.ParentID).First(&parentFile).Error
	if err != nil {
		return nil, err
	}
	return &parentFile, nil
}

// ListByParentID 列出父目录下全部直接子项。
func (dao *FileDAO) ListByParentID(parentID string) ([]*entity.File, error) {
	var files []*entity.File
	err := DB.Where("parent_id = ? AND id != ?", parentID, parentID).Find(&files).Error
	return files, err
}

// GetFolderSize 递归累加文件夹及子树内所有文件大小。
func (dao *FileDAO) GetFolderSize(folderID string) (int64, error) {
	var size int64

	var dfs func(parentID string) error
	dfs = func(parentID string) error {
		var files []*entity.File
		if err := DB.Select("id", "size", "type").
			Where("parent_id = ? AND id != ?", parentID, parentID).
			Find(&files).Error; err != nil {
			return err
		}

		for _, f := range files {
			size += f.Size
			if f.Type == "folder" {
				if err := dfs(f.ID); err != nil {
					return err
				}
			}
		}
		return nil
	}

	if err := dfs(folderID); err != nil {
		return 0, err
	}
	return size, nil
}

// HasChildFolder 判断文件夹是否含有子文件夹。
func (dao *FileDAO) HasChildFolder(folderID string) (bool, error) {
	var count int64
	err := DB.Model(&entity.File{}).
		Where("parent_id = ? AND id != ? AND type = ?", folderID, folderID, "folder").
		Count(&count).Error
	return count > 0, err
}

// GetAllParentFolders 从当前节点向上收集至根的路径链。
func (dao *FileDAO) GetAllParentFolders(startID string) ([]*entity.File, error) {
	var parentFolders []*entity.File
	currentID := startID

	for currentID != "" {
		var file entity.File
		err := DB.Where("id = ?", currentID).First(&file).Error
		if err != nil {
			return nil, err
		}

		parentFolders = append(parentFolders, &file)

		// 到达根节点（parent_id 等于自身 id）时停止向上遍历
		if file.ParentID == file.ID {
			break
		}
		currentID = file.ParentID
	}

	return parentFolders, nil
}

// Create 插入新文件记录。
func (dao *FileDAO) Create(file *entity.File) error {
	return DB.Create(file).Error
}

// UpdateByID 按 ID 部分更新文件字段。
func (dao *FileDAO) UpdateByID(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.File{}).Where("id = ?", id).Updates(updates).Error
}

// DeleteByTenantID 按租户硬删除全部文件记录。
func (dao *FileDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.File{})
	return result.RowsAffected, result.Error
}

// DeleteByIDs 按 ID 列表批量硬删除。
func (dao *FileDAO) DeleteByIDs(ids []string) (int64, error) {
	if len(ids) == 0 {
		return 0, nil
	}
	result := DB.Unscoped().Where("id IN ?", ids).Delete(&entity.File{})
	return result.RowsAffected, result.Error
}

// GetAllIDsByTenantID 返回租户下全部文件 ID。
func (dao *FileDAO) GetAllIDsByTenantID(tenantID string) ([]string, error) {
	var ids []string
	err := DB.Model(&entity.File{}).Where("tenant_id = ?", tenantID).Pluck("id", &ids).Error
	return ids, err
}

// GetByIDs 按 ID 列表批量查询文件。
func (dao *FileDAO) GetByIDs(ids []string) ([]*entity.File, error) {
	var files []*entity.File
	if len(ids) == 0 {
		return files, nil
	}
	err := DB.Where("id IN ?", ids).Find(&files).Error
	return files, err
}

// ListAllFilesByParentID 列出父目录下全部直接子文件。
func (dao *FileDAO) ListAllFilesByParentID(parentID string) ([]*entity.File, error) {
	var files []*entity.File
	err := DB.Where("parent_id = ? AND id != ?", parentID, parentID).Find(&files).Error
	return files, err
}

// ListNonFolderByParentID 列出父目录下非文件夹类型的直接子项。
func (dao *FileDAO) ListNonFolderByParentID(parentID string) ([]*entity.File, error) {
	var files []*entity.File
	err := DB.Where("parent_id = ? AND id != ? AND type != ?", parentID, parentID, "folder").Find(&files).Error
	return files, err
}

// ListFolderByParentID 列出父目录下的直接子文件夹。
func (dao *FileDAO) ListFolderByParentID(parentID string) ([]*entity.File, error) {
	var files []*entity.File
	err := DB.Where("parent_id = ? AND type = ?", parentID, "folder").Find(&files).Error
	return files, err
}

// GetByParentIDAndName 按父目录 ID 与文件名精确查询。
func (dao *FileDAO) GetByParentIDAndName(parentID, name string) (*entity.File, error) {
	var file entity.File
	err := DB.Where("parent_id = ? AND name = ?", parentID, name).First(&file).Error
	if err != nil {
		return nil, err
	}
	return &file, nil
}

// GetIDListByID 按路径名数组递归解析并收集文件 ID 链。
func (dao *FileDAO) GetIDListByID(id string, names []string, count int, res []string) ([]string, error) {
	if count < len(names) {
		file, err := dao.GetByParentIDAndName(id, names[count])
		if err != nil {
			return res, nil
		}
		res = append(res, file.ID)
		return dao.GetIDListByID(file.ID, names, count+1, res)
	}
	return res, nil
}

// CreateFolder 在指定父目录下创建文件夹记录。
func (dao *FileDAO) CreateFolder(parentID, tenantID, name, fileType string) (*entity.File, error) {
	file := &entity.File{
		ID:         utility.GenerateToken(),
		ParentID:   parentID,
		TenantID:   tenantID,
		CreatedBy:  tenantID,
		Name:       name,
		Type:       fileType,
		Size:       0,
		SourceType: "",
	}
	if err := DB.Create(file).Error; err != nil {
		return nil, err
	}
	return file, nil
}

// Insert 插入新文件记录（Create 别名）。
func (dao *FileDAO) Insert(file *entity.File) error {
	return DB.Create(file).Error
}

// IsParentFolderExist 检查父文件夹 ID 是否存在。
func (dao *FileDAO) IsParentFolderExist(parentID string) bool {
	var count int64
	err := DB.Model(&entity.File{}).Where("id = ?", parentID).Count(&count).Error
	if err != nil || count == 0 {
		return false
	}
	return true
}

// Query 按名称、父目录、租户等可选条件查询文件。
func (dao *FileDAO) Query(name string, parentID string, tenantID string) []*entity.File {
	var files []*entity.File
	query := DB.Model(&entity.File{})
	if name != "" {
		query = query.Where("name = ?", name)
	}
	if parentID != "" {
		query = query.Where("parent_id = ?", parentID)
	}
	if tenantID != "" {
		query = query.Where("tenant_id = ?", tenantID)
	}
	query.Find(&files)
	return files
}

// Delete 按 ID 硬删除单个文件。
func (dao *FileDAO) Delete(id string) error {
	return DB.Unscoped().Where("id = ?", id).Delete(&entity.File{}).Error
}

// GetDatasetIDByFileID 通过 file2document 关联查询文件所属知识库 ID。
func (dao *FileDAO) GetDatasetIDByFileID(fileID string) ([]string, error) {
	var datasetIDs []string
	rows, err := DB.Model(&entity.File{}).
		Select("knowledgebase.id").
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
		var kbID string
		if err := rows.Scan(&kbID); err != nil {
			continue
		}
		datasetIDs = append(datasetIDs, kbID)
	}

	return datasetIDs, nil
}

// reparentAndDeleteFolder 先将重复文件夹的子项迁移到保留节点，再硬删除重复行，避免竞态产生的孤儿记录。
func reparentAndDeleteFolder(dupID, keepID string) error {
	// 将重复文件夹下的子项 parent_id 改指向保留文件夹
	if err := DB.Model(&entity.File{}).
		Where("parent_id = ?", dupID).
		Update("parent_id", keepID).Error; err != nil {
		return fmt.Errorf("failed to reparent children from %s to %s: %w", dupID, keepID, err)
	}

	// 硬删除重复的文件夹行
	if err := DB.Unscoped().Where("id = ?", dupID).Delete(&entity.File{}).Error; err != nil {
		return fmt.Errorf("failed to delete duplicate folder %s: %w", dupID, err)
	}

	return nil
}

// DatasetFolderName 知识库镜像根文件夹的固定名称。
const DatasetFolderName = ".knowledgebase"

// InitDatasetDocs 为租户初始化 .knowledgebase 镜像目录树，对齐 Python init_dataset_docs，并去重竞态产生的重复文件夹。
func (dao *FileDAO) InitDatasetDocs(rootID, tenantID string, file2DocumentDAO *File2DocumentDAO) error {
	var existing []*entity.File
	err := DB.Where("name = ? AND parent_id = ? AND tenant_id = ?", DatasetFolderName, rootID, tenantID).
		Order("create_time ASC").
		Find(&existing).Error
	if err != nil {
		return err
	}

	if len(existing) > 0 {
		if len(existing) > 1 {
			log.Printf("[WARN] Found %d duplicate '%s' folders under root %s, keeping only the first",
				len(existing), DatasetFolderName, rootID)
			keepID := existing[0].ID
			for _, dup := range existing[1:] {
				if err := reparentAndDeleteFolder(dup.ID, keepID); err != nil {
					log.Printf("[ERROR] Failed to deduplicate folder %s: %v", dup.ID, err)
				}
			}
		}
		return nil
	}

	datasetFolder, err := dao.newAFileFromDataset(tenantID, DatasetFolderName, rootID)
	if err != nil {
		return err
	}

	var datasets []entity.Knowledgebase
	err = DB.Select("id", "name").
		Where("tenant_id = ?", tenantID).
		Find(&datasets).Error
	if err != nil {
		return err
	}

	for _, ds := range datasets {
		datasetFolderForDataset, err := dao.newAFileFromDataset(tenantID, ds.Name, datasetFolder.ID)
		if err != nil {
			continue
		}

		var documents []entity.Document
		err = DB.Where("kb_id = ?", ds.ID).Find(&documents).Error
		if err != nil {
			continue
		}

		for _, doc := range documents {
			if err := dao.addFileFromKB(&doc, datasetFolderForDataset.ID, tenantID, file2DocumentDAO); err != nil {
				return err
			}
		}
	}

	return nil
}

// newAFileFromDataset 创建或返回知识库对应的文件夹节点，并处理并发重复。
func (dao *FileDAO) newAFileFromDataset(tenantID, name, parentID string) (*entity.File, error) {
	var existingFiles []*entity.File
	err := DB.Where("tenant_id = ? AND parent_id = ? AND name = ?", tenantID, parentID, name).Order("create_time ASC").Find(&existingFiles).Error
	if err != nil {
		return nil, err
	}

	if len(existingFiles) > 0 {
		if len(existingFiles) > 1 {
			log.Printf("[WARN] Found %d duplicate entries named '%s' under parent %s, keeping only the first",
				len(existingFiles), name, parentID)
			keepID := existingFiles[0].ID
			for _, dup := range existingFiles[1:] {
				if err := reparentAndDeleteFolder(dup.ID, keepID); err != nil {
					log.Printf("[ERROR] Failed to deduplicate file entry %s: %v", dup.ID, err)
				}
			}
		}
		return existingFiles[0], nil
	}

	fileID := utility.GenerateToken()
	file := &entity.File{
		ID:         fileID,
		ParentID:   parentID,
		TenantID:   tenantID,
		CreatedBy:  tenantID,
		Name:       name,
		Type:       "folder",
		Size:       0,
		SourceType: "knowledgebase",
	}

	if err = DB.Create(file).Error; err != nil {
		return nil, err
	}
	return file, nil
}

// addFileFromKB 为知识库文档创建镜像文件及 file2document 映射。
func (dao *FileDAO) addFileFromKB(doc *entity.Document, datasetFolderID, tenantID string, file2DocumentDAO *File2DocumentDAO) error {
	var f2dCount int64
	err := DB.Model(&entity.File2Document{}).
		Where("document_id = ?", doc.ID).
		Count(&f2dCount).Error
	if err != nil {
		return err
	}

	if f2dCount > 0 {
		return nil
	}

	docName := ""
	if doc.Name != nil {
		docName = *doc.Name
	}

	docLocation := ""
	if doc.Location != nil {
		docLocation = *doc.Location
	}

	fileID := utility.GenerateToken()
	file := &entity.File{
		ID:         fileID,
		ParentID:   datasetFolderID,
		TenantID:   tenantID,
		CreatedBy:  tenantID,
		Name:       docName,
		Type:       doc.Type,
		Size:       doc.Size,
		Location:   &docLocation,
		SourceType: "knowledgebase",
	}

	if err = DB.Create(file).Error; err != nil {
		return err
	}

	f2dID := utility.GenerateToken()
	f2d := &entity.File2Document{
		ID:         f2dID,
		FileID:     &fileID,
		DocumentID: &doc.ID,
	}

	if err = DB.Create(f2d).Error; err != nil {
		return err
	}

	return nil
}
