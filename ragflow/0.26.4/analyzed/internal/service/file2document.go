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

package service

// file2document.go 将文件管理树中的文件关联到知识库并异步转换为文档。

import (
	"errors"
	"path/filepath"
	"strings"

	"go.uber.org/zap"

	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"
	"ragflow/internal/utility"
)

// File2DocumentService 哨兵错误，Handler 映射为 Python 兼容响应码。
// Python-compatible response codes/messages. Returning sentinels (instead of
// wrapped DAO/runtime errors) prevents internal DB details from leaking through
// the API response path.
var (
	// ErrLinkFileNotFound 文件不存在（对齐 Python）。
	ErrLinkFileNotFound = errors.New("File not found!")
	// ErrLinkDatasetNotFound 知识库不存在。
	ErrLinkDatasetNotFound = errors.New("Can't find this dataset!")
	// ErrLinkNoAuthorization 无访问权限。
	ErrLinkNoAuthorization = errors.New("No authorization.")
	// ErrLinkInternal 可安全暴露的通用内部错误。
	ErrLinkInternal = errors.New("Internal server error.")
)

// File2DocumentService 处理文件与知识库的关联转换。
type File2DocumentService struct {
	fileDAO          *dao.FileDAO
	file2DocumentDAO *dao.File2DocumentDAO
	kbDAO            *dao.KnowledgebaseDAO
	userTenantDAO    *dao.UserTenantDAO
	documentSvc      *DocumentService
}

// NewFile2DocumentService 构造服务实例。
func NewFile2DocumentService() *File2DocumentService {
	return &File2DocumentService{
		fileDAO:          dao.NewFileDAO(),
		file2DocumentDAO: dao.NewFile2DocumentDAO(),
		kbDAO:            dao.NewKnowledgebaseDAO(),
		userTenantDAO:    dao.NewUserTenantDAO(),
		documentSvc:      NewDocumentService(),
	}
}

// LinkToDatasetsRequest POST /files/link-to-datasets 请求体。
type LinkToDatasetsRequest struct {
	FileIDs []string `json:"file_ids"`
	KbIDs   []string `json:"kb_ids"`
}

// LinkToDatasets 校验输入、展开文件夹、鉴权后在后台 goroutine 执行 convertFiles。
// schedules convertFiles in a goroutine — mirroring Python convert().
// Returns immediately (fire-and-forget for the heavy DB work).
//
// On validation failure it returns a sentinel error (see ErrLink* above) so the
// handler can map it to a Python-compatible response without leaking internals.
func (s *File2DocumentService) LinkToDatasets(userID string, req *LinkToDatasetsRequest) error {
	// ── 1. Validate files exist ───────────────────────────────────────────────
	files, err := s.fileDAO.GetByIDs(req.FileIDs)
	if err != nil {
		common.Warn("LinkToDatasets: GetByIDs failed", zap.Error(err))
		return ErrLinkInternal
	}
	filesSet := make(map[string]*entity.File, len(files))
	for _, f := range files {
		filesSet[f.ID] = f
	}
	for _, id := range req.FileIDs {
		if filesSet[id] == nil {
			return ErrLinkFileNotFound
		}
	}

	// ── 2. Validate KBs exist ────────────────────────────────────────────────
	kbMap := make(map[string]*entity.Knowledgebase, len(req.KbIDs))
	for _, kbID := range req.KbIDs {
		kb, err := s.kbDAO.GetByID(kbID)
		if err != nil || kb == nil {
			return ErrLinkDatasetNotFound
		}
		kbMap[kbID] = kb
	}

	// ── 3. Expand folders to leaf files, then deduplicate ─────────────────────
	// Mixed folder + direct file inputs (or overlapping folders) can yield the
	// same leaf file more than once; dedupe so each file is converted exactly
	// once.
	expanded := make([]string, 0, len(req.FileIDs))
	for _, id := range req.FileIDs {
		file := filesSet[id]
		if file.Type == FileTypeFolder {
			inner, err := s.getAllInnermostFileIDs(id)
			if err != nil {
				common.Warn("LinkToDatasets: folder expansion failed", zap.String("fileID", id), zap.Error(err))
				return ErrLinkInternal
			}
			expanded = append(expanded, inner...)
		} else {
			expanded = append(expanded, id)
		}
	}
	allFileIDs := dedupeStrings(expanded)

	// ── 4. Validate expanded file permissions ─────────────────────────────────
	for _, id := range allFileIDs {
		file, err := s.fileDAO.GetByID(id)
		if err != nil || file == nil {
			return ErrLinkFileNotFound
		}
		if !s.checkFileTeamPermission(file, userID) {
			return ErrLinkNoAuthorization
		}
	}

	// ── 5. Validate KB permissions ────────────────────────────────────────────
	for _, kb := range kbMap {
		if !s.checkKBTeamPermission(kb, userID) {
			return ErrLinkNoAuthorization
		}
	}

	// ── 6. Run conversion in background (fire-and-forget) ────────────────────
	kbIDs := req.KbIDs
	go func() {
		if err := s.convertFiles(allFileIDs, kbIDs, userID); err != nil {
			common.Warn("file2document.convertFiles failed",
				zap.Strings("file_ids", allFileIDs),
				zap.Strings("kb_ids", kbIDs),
				zap.Error(err))
		}
	}()

	return nil
}

// convertFiles 对齐 Python _convert_files：清理旧文档后在各 KB 创建新文档与映射。
// documents (routing through DocumentService so KB counters are updated), drop
// the file2document mappings, then create a new document in each target KB and
// a fresh mapping.
func (s *File2DocumentService) convertFiles(fileIDs, kbIDs []string, userID string) error {
	for _, fileID := range fileIDs {
		// Remove existing documents linked to this file. Routing through
		// DocumentService.RemoveDocumentKeepFile ensures KB doc_num/chunk_num/
		// token_num counters are decremented (mirrors Python remove_document)
		// while preserving the file record itself for re-linking.
		mappings, err := s.file2DocumentDAO.GetByFileID(fileID)
		if err != nil {
			common.Warn("convertFiles: GetByFileID failed", zap.String("fileID", fileID), zap.Error(err))
		}
		for _, m := range mappings {
			if m.DocumentID == nil {
				continue
			}
			if err := s.documentSvc.RemoveDocumentKeepFile(*m.DocumentID); err != nil {
				common.Warn("convertFiles: RemoveDocumentKeepFile failed",
					zap.String("docID", *m.DocumentID), zap.Error(err))
			}
		}
		// Drop the file2document mappings for this file (mirrors Python
		// File2DocumentService.delete_by_file_id, done once per file).
		if err := s.file2DocumentDAO.DeleteByFileID(fileID); err != nil {
			common.Warn("convertFiles: DeleteByFileID failed", zap.String("fileID", fileID), zap.Error(err))
		}

		// Reload the source file.
		file, err := s.fileDAO.GetByID(fileID)
		if err != nil || file == nil {
			continue
		}

		// Create a document + mapping in each target KB.
		for _, kbID := range kbIDs {
			kb, err := s.kbDAO.GetByID(kbID)
			if err != nil || kb == nil {
				continue
			}

			parserID := selectUploadParser(utility.FileType(file.Type), file.Name, kb.ParserID)
			suffix := strings.TrimPrefix(filepath.Ext(file.Name), ".")
			doc := &entity.Document{
				ID:           utility.GenerateUUID(),
				KbID:         kb.ID,
				ParserID:     parserID,
				ParserConfig: kb.ParserConfig,
				CreatedBy:    userID,
				Type:         file.Type,
				Name:         &file.Name,
				Suffix:       suffix,
				Size:         file.Size,
			}
			if file.Location != nil {
				doc.Location = file.Location
			}
			if kb.PipelineID != nil {
				doc.PipelineID = kb.PipelineID
			}

			// InsertDocument creates the row and increments KB doc_num in one
			// transaction, so a failed insert never leaves a stale counter.
			if err := s.documentSvc.InsertDocument(doc); err != nil {
				common.Warn("convertFiles: InsertDocument failed",
					zap.String("kbID", kbID), zap.String("fileID", fileID), zap.Error(err))
				continue
			}

			mapping := &entity.File2Document{
				ID:         utility.GenerateUUID(),
				FileID:     &fileID,
				DocumentID: &doc.ID,
			}
			if err := s.file2DocumentDAO.Create(mapping); err != nil {
				common.Warn("convertFiles: Create file2document mapping failed",
					zap.String("fileID", fileID), zap.String("docID", doc.ID), zap.Error(err))
			}
		}
	}
	return nil
}

// getAllInnermostFileIDs 递归收集文件夹下全部叶子文件 ID。
// Mirrors Python FileService.get_all_innermost_file_ids.
func (s *File2DocumentService) getAllInnermostFileIDs(folderID string) ([]string, error) {
	children, err := s.fileDAO.ListByParentID(folderID)
	if err != nil {
		return nil, err
	}
	var ids []string
	for _, child := range children {
		if child.Type == FileTypeFolder {
			sub, err := s.getAllInnermostFileIDs(child.ID)
			if err != nil {
				return nil, err
			}
			ids = append(ids, sub...)
		} else {
			ids = append(ids, child.ID)
		}
	}
	return ids, nil
}

// checkFileTeamPermission 校验文件团队权限（租户或 KB 团队）。
// true when file.TenantID == userID or user is in the file tenant's team.
func (s *File2DocumentService) checkFileTeamPermission(file *entity.File, userID string) bool {
	if file.TenantID == userID {
		return true
	}

	datasetIDs, err := s.fileDAO.GetDatasetIDByFileID(file.ID)
	if err != nil || len(datasetIDs) == 0 {
		return false
	}

	for _, datasetID := range datasetIDs {
		kb, err := s.kbDAO.GetByID(datasetID)
		if err != nil || kb == nil {
			continue
		}
		if s.checkKBTeamPermission(kb, userID) {
			return true
		}
	}
	return false
}

// checkKBTeamPermission 校验知识库团队权限。
// true when kb.TenantID == userID or user is in the KB tenant's team.
func (s *File2DocumentService) checkKBTeamPermission(kb *entity.Knowledgebase, userID string) bool {
	return hasKBTeamPermission(kb, userID, dao.NewTenantDAO())
}

// dedupeStrings 去重字符串切片并保持首次出现顺序。
// first-seen order.
func dedupeStrings(in []string) []string {
	seen := make(map[string]struct{}, len(in))
	out := make([]string, 0, len(in))
	for _, v := range in {
		if _, ok := seen[v]; ok {
			continue
		}
		seen[v] = struct{}{}
		out = append(out, v)
	}
	return out
}
// file2document.go — 文件关联知识库：校验权限、展开文件夹并异步转换为文档。
