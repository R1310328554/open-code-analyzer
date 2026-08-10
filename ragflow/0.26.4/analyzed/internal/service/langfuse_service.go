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

// langfuse_service.go 实现租户 Langfuse API Key 的校验、存储与查询。

import (
	"context"
	"errors"
	"fmt"

	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"

	"gorm.io/gorm"
)

// langfuseVerifier 抽象 Langfuse 凭据校验，便于单测时注入 mock 而无需真实网络请求。
// logic can be unit-tested without performing real network calls.
type langfuseVerifier interface {
	// AuthCheck mirrors the Python langfuse SDK auth_check().
	AuthCheck(ctx context.Context, host, publicKey, secretKey string) (bool, error)
	// GetProject mirrors api.projects.get().dict()["data"][0] (id, name).
	GetProject(ctx context.Context, host, publicKey, secretKey string) (string, string, error)
}

// defaultLangfuseVerifier 使用真实 LangfuseClient 执行在线校验。
type defaultLangfuseVerifier struct{}

func (defaultLangfuseVerifier) AuthCheck(ctx context.Context, host, publicKey, secretKey string) (bool, error) {
	client := NewLangfuseClient(host, publicKey, secretKey)
	defer client.Shutdown(context.Background())
	return client.AuthCheck(ctx)
}

func (defaultLangfuseVerifier) GetProject(ctx context.Context, host, publicKey, secretKey string) (string, string, error) {
	client := NewLangfuseClient(host, publicKey, secretKey)
	defer client.Shutdown(context.Background())
	return client.GetProject(ctx)
}

// LangfuseService 实现 /langfuse/api-key 业务逻辑，对齐 Python TenantLangfuseService。
// the Python TenantLangfuseService + langfuse_api handlers.
type LangfuseService struct {
	langfuseDAO *dao.LangfuseDAO
	verifier    langfuseVerifier
}

// NewLangfuseService 创建带默认在线校验器的 LangfuseService。
func NewLangfuseService() *LangfuseService {
	return &LangfuseService{
		langfuseDAO: dao.NewLangfuse(),
		verifier:    defaultLangfuseVerifier{},
	}
}

// SetAPIKey 校验 Langfuse 公钥/私钥/Host 后按租户 upsert 凭据。
// for a tenant.
func (s *LangfuseService) SetAPIKey(tenantID, secretKey, publicKey, host string) (*entity.TenantLangfuse, common.ErrorCode, error) {
	if secretKey == "" || publicKey == "" || host == "" {
		return nil, common.CodeDataError, errors.New("Missing required fields")
	}

	ok, err := s.verifier.AuthCheck(context.Background(), host, publicKey, secretKey)
	if err != nil {
		return nil, common.CodeServerError, err
	}
	if !ok {
		return nil, common.CodeDataError, errors.New("Invalid Langfuse keys")
	}

	row := &entity.TenantLangfuse{
		TenantID:  tenantID,
		SecretKey: secretKey,
		PublicKey: publicKey,
		Host:      host,
	}

	if err := s.langfuseDAO.SaveByTenantID(row); err != nil {
		return nil, common.CodeServerError, err
	}

	return row, common.CodeSuccess, nil
}

// GetAPIKey 返回已存凭据并调用 Langfuse API 补充 project id/name。
// id/name.
func (s *LangfuseService) GetAPIKey(tenantID string) (*entity.LangfuseInfoResponse, common.ErrorCode, string, error) {
	row, err := s.langfuseDAO.GetByTenantID(tenantID)
	if err != nil {
		return nil, common.CodeServerError, "", err
	}
	if row == nil {
		return nil, common.CodeSuccess, "Have not record any Langfuse keys.", nil
	}

	projectID, projectName, err := s.verifier.GetProject(context.Background(), row.Host, row.PublicKey, row.SecretKey)
	if err != nil {
		if errors.Is(err, ErrLangfuseUnauthorized) {
			return nil, common.CodeDataError, "Invalid Langfuse keys loaded", err
		}
		if IsLangfuseAPIError(err) {
			return nil, common.CodeSuccess, fmt.Sprintf("Error from Langfuse: %s", err.Error()), nil
		}
		return nil, common.CodeServerError, "", err
	}

	info := &entity.LangfuseInfoResponse{
		TenantID:    row.TenantID,
		Host:        row.Host,
		SecretKey:   row.SecretKey,
		PublicKey:   row.PublicKey,
		ProjectID:   projectID,
		ProjectName: projectName,
	}
	return info, common.CodeSuccess, "success", nil
}

// DeleteAPIKey 删除租户已保存的 Langfuse 凭据。
func (s *LangfuseService) DeleteAPIKey(tenantID string) (bool, common.ErrorCode, string, error) {
	if err := s.langfuseDAO.DeleteExistingByTenantID(tenantID); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return false, common.CodeSuccess, "Have not record any Langfuse keys.", nil
		}
		return false, common.CodeServerError, "", err
	}
	return true, common.CodeSuccess, "", nil
}
// langfuse_service.go — 租户 Langfuse 凭据 CRUD，校验密钥并回填项目信息。
