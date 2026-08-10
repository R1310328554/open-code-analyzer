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

// stats.go — GET 对话统计：按日期范围与可选 canvas_id 汇总 API 会话指标。

//

package handler

import (
	"errors"
	"time"

	"ragflow/internal/common"
	"ragflow/internal/service"

	"github.com/gin-gonic/gin"
)

// GetStats returns API conversation statistics for the current user's tenant.
func (h *SystemHandler) GetStats(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	now := time.Now()
	fromDate := c.DefaultQuery("from_date", now.AddDate(0, 0, -7).Format("2006-01-02 00:00:00"))
	toDate := c.DefaultQuery("to_date", now.Format("2006-01-02 15:04:05"))
	if len(toDate) == 10 {
		toDate += " 23:59:59"
	}

	var source *string
	if _, ok := c.GetQuery("canvas_id"); ok {
		agentSource := "agent"
		source = &agentSource
	}

	stats, err := h.systemService.GetStats(user.ID, fromDate, toDate, source)
	if err != nil {
		code := common.CodeExceptionError
		if errors.Is(err, service.ErrTenantNotFound) {
			code = common.CodeDataError
		}
		common.ErrorWithCode(c, int(code), err.Error())
		return
	}

	common.SuccessWithData(c, stats, "success")
}

// from_date 默认近 7 天；to_date 仅日期时补 23:59:59；canvas_id 存在时 source 过滤为 agent。
