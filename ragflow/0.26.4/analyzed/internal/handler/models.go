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

// models.go — 租户已添加模型列表与单模型详情 HTTP 端点（GET /models、GET /models/:name）。

//

package handler

import (
	"ragflow/internal/common"
	"ragflow/internal/service"
	"strconv"

	"github.com/gin-gonic/gin"
)

// ModelHandler 模型目录 HTTP 处理器（ListAllModels / ShowModel）
type ModelHandler struct {
	modelProviderService *service.ModelProviderService
}

// NewModelHandler 注入 ModelProviderService
func NewModelHandler(modelProviderService *service.ModelProviderService) *ModelHandler {
	return &ModelHandler{
		modelProviderService: modelProviderService,
	}
}

// ListAllModels 分页列出全部租户模型
func (h *ModelHandler) ListAllModels(c *gin.Context) {

	page := 0
	if v := c.Query("page"); v != "" {
		if p, err := strconv.Atoi(v); err == nil && p > 0 {
			page = p
		}
	}

	pageSize := 0
	if v := c.Query("page_size"); v != "" {
		if ps, err := strconv.Atoi(v); err == nil && ps > 0 {
			pageSize = ps
		}
	}

	// 调用服务层列出租户模型
	models, err := h.modelProviderService.ListAllModels(page, pageSize)
	if err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, err.Error())
		return
	}

	common.SuccessWithData(c, models, "success")
	return
}

// ShowModel 路径参数 model_name 为 Base64 编码
func (h *ModelHandler) ShowModel(c *gin.Context) {
	encodedModelName := c.Param("model_name")
	if encodedModelName == "" {
		common.ErrorWithCode(c, 400, "Encoded model name is empty")
		return
	}

	decodedModelName, err := common.DecodeFromBase64(encodedModelName)
	if err != nil {
		common.ErrorWithCode(c, 400, err.Error())
		return
	}
	if decodedModelName == "" {
		common.ErrorWithCode(c, 400, "Decoded model name is empty")
		return
	}

	// 按解码后的模型名查询详情
	model, err := h.modelProviderService.ShowModel(decodedModelName)
	if err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, err.Error())
		return
	}

	common.SuccessWithData(c, model, "success")
}

// page/page_size 为 0 表示不分页；模型名需经 common.DecodeFromBase64 解码。
