// Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// chat_channel.go — 聊天渠道（ChatChannel）CRUD：对接第三方 IM/通知渠道配置并关联 chat_id。


package handler

import (
	"strings"

	"github.com/gin-gonic/gin"

	"ragflow/internal/common"
	"ragflow/internal/entity"
	"ragflow/internal/service"
)

// ChatChannelService 渠道服务接口
type ChatChannelService interface {
	CreateChatChannel(tenantID, name, channelType string, config entity.JSONMap, chatID *string) (*entity.ChatChannel, error)
	List(tenantID string) ([]*entity.ChatChannelListResponse, error)
	GetChatChannel(userID, channelID string) (*entity.ChatChannel, common.ErrorCode, error)
	UpdateChatChannel(userID, channelID string, req map[string]interface{}) (*entity.ChatChannel, common.ErrorCode, error)
	DeleteChatChannel(userID, channelID string) (bool, common.ErrorCode, error)
}

// ChatChannelHandler 聊天渠道 HTTP 处理器
type ChatChannelHandler struct {
	chatChannelService ChatChannelService
}

// NewChatChannelHandler 注入渠道服务
func NewChatChannelHandler(chatChannelService ChatChannelService) *ChatChannelHandler {
	return &ChatChannelHandler{chatChannelService: chatChannelService}
}

// NewChatChannel 启动代码使用的无参构造，内部 NewChatChannelService shape used by boot code.
func NewChatChannel() *ChatChannelHandler {
	return NewChatChannelHandler(service.NewChatChannelService())
}

// CreateChatChannelRequest 创建渠道请求体
type CreateChatChannelRequest struct {
	Name    string         `json:"name" binding:"required"`
	Channel string         `json:"channel" binding:"required"`
	Config  entity.JSONMap `json:"config" binding:"required"`
	ChatID  *string        `json:"chat_id"`
}

// CreateChatChannel POST /chat-channels 创建渠道
func (h *ChatChannelHandler) CreateChatChannel(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	var req CreateChatChannelRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, "Invalid request: "+err.Error())
		return
	}

	row, err := h.chatChannelService.CreateChatChannel(
		user.ID,
		req.Name,
		req.Channel,
		req.Config,
		req.ChatID,
	)
	if err != nil {
		common.ResponseWithCodeData(c, common.CodeServerError, nil, err.Error())
		return
	}
	common.SuccessWithData(c, row, "success")
}

// ListChatChannel GET /chat-channels 列出当前用户渠道
func (h *ChatChannelHandler) ListChatChannel(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	rows, err := h.chatChannelService.List(user.ID)
	if err != nil {
		common.ResponseWithCodeData(c, common.CodeServerError, nil, err.Error())
		return
	}
	common.SuccessWithData(c, rows, "success")
}

// GetChatChannel 获取单个渠道详情
func (h *ChatChannelHandler) GetChatChannel(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	userID := strings.TrimSpace(user.ID)
	if userID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "user_id is required")
		return
	}

	channelID := strings.TrimSpace(c.Param("channel_id"))
	if channelID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "channel_id is required")
		return
	}

	channel, code, err := h.chatChannelService.GetChatChannel(userID, channelID)
	if code != common.CodeSuccess || err != nil {
		writeChatChannelError(c, code, chatChannelErrMsg(code, err))
		return
	}

	common.SuccessWithData(c, channel, "success")
}

// UpdateChatChannel PATCH 更新渠道配置
func (h *ChatChannelHandler) UpdateChatChannel(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	userID := strings.TrimSpace(user.ID)
	if userID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "user_id is required")
		return
	}

	channelID := strings.TrimSpace(c.Param("channel_id"))
	if channelID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "channel_id is required")
		return
	}

	var request map[string]interface{}
	if err := c.ShouldBindJSON(&request); err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, err.Error())
		return
	}

	result, code, err := h.chatChannelService.UpdateChatChannel(userID, channelID, unwrapChatChannelPayload(request))
	if code != common.CodeSuccess || err != nil {
		writeChatChannelError(c, code, chatChannelErrMsg(code, err))
		return
	}

	common.SuccessWithData(c, result, "success")
}

// DeleteChatChannel DELETE 删除渠道
func (h *ChatChannelHandler) DeleteChatChannel(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	userID := strings.TrimSpace(user.ID)
	if userID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "user_id is required")
		return
	}

	channelID := strings.TrimSpace(c.Param("channel_id"))
	if channelID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "channel_id is required")
		return
	}

	result, code, err := h.chatChannelService.DeleteChatChannel(userID, channelID)
	if code != common.CodeSuccess || err != nil {
		writeChatChannelError(c, code, chatChannelErrMsg(code, err))
		return
	}

	common.SuccessWithData(c, result, "success")
}

// unwrapChatChannelPayload 解包 {data:{...}} 嵌套
func unwrapChatChannelPayload(payload map[string]interface{}) map[string]interface{} {
	if data, ok := payload["data"].(map[string]interface{}); ok {
		return data
	}
	return payload
}

// writeChatChannelError 统一渠道错误响应形状
func writeChatChannelError(c *gin.Context, code common.ErrorCode, message string) {
	if code == common.CodeAuthenticationError && message == "No authorization." {
		common.ResponseWithCodeData(c, code, false, message)
		return
	}
	common.ResponseWithCodeData(c, code, nil, message)
}

// chatChannelErrMsg 优先返回 err.Error，否则 code.Message
func chatChannelErrMsg(code common.ErrorCode, err error) string {
	if err != nil {
		return err.Error()
	}
	return code.Message()
}

// 鉴权失败且文案为 No authorization. 时 data 返回 false；Update 支持 payload.data  unwrap。
