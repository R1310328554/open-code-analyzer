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

package service

// chat_channel.go 管理第三方聊天渠道与助手的绑定关系。

import (
	"errors"
	"fmt"
	"ragflow/internal/utility"

	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"
)

// ChatChannelService 聊天渠道服务，负责 CRUD 与租户/成员权限校验。
type ChatChannelService struct {
	chatChannelDAO *dao.ChatChannelDAO
	chatDAO        *dao.ChatDAO
	userTenantDAO  *dao.UserTenantDAO
}

// NewChatChannelService 构造 ChatChannelService。
func NewChatChannelService() *ChatChannelService {
	return &ChatChannelService{
		chatChannelDAO: dao.NewChatChannel(),
		chatDAO:        dao.NewChatDAO(),
		userTenantDAO:  dao.NewUserTenantDAO(),
	}
}

// Insert 插入渠道行，自动补 UUID 与默认 status=1。
func (s *ChatChannelService) Insert(channel *entity.ChatChannel) error {
	if channel == nil {
		return errors.New("channel is nil")
	}
	if channel.ID == "" {
		channel.ID = utility.GenerateUUID()
	}
	if channel.Status == 0 {
		channel.Status = 1
	}
	return s.chatChannelDAO.Create(channel)
}

// GetByID 按主键读取渠道（不做租户过滤）。
func (s *ChatChannelService) GetByID(id string) (*entity.ChatChannel, error) {
	if id == "" {
		return nil, errors.New("id is empty")
	}
	return s.chatChannelDAO.GetByIDOnly(id)
}

// List 列出租户下全部聊天渠道。
func (s *ChatChannelService) List(tenantID string) ([]*entity.ChatChannelListResponse, error) {
	return s.chatChannelDAO.ListByTenantID(tenantID)
}

// CreateChatChannel 创建渠道；可选绑定 chat_id 并校验助手归属。
func (s *ChatChannelService) CreateChatChannel(tenantID, name, channelType string, config entity.JSONMap, chatID *string) (*entity.ChatChannel, error) {
	if chatID != nil && *chatID != "" {
		dialog, err := s.chatDAO.GetByID(*chatID)
		if err != nil {
			if dao.IsNotFoundErr(err) {
				return nil, errors.New("Can't find this chat assistant!")
			}
			return nil, err
		}
		if dialog.TenantID != tenantID {
			return nil, errors.New("No authorization.")
		}
	}
	row := &entity.ChatChannel{
		ID:       utility.GenerateUUID(),
		TenantID: tenantID,
		Name:     name,
		Channel:  channelType,
		Config:   config,
		ChatID:   chatID,
		Status:   1,
	}

	if err := s.Insert(row); err != nil {
		return nil, err
	}

	created, err := s.GetByID(row.ID)
	if err != nil {
		return nil, fmt.Errorf("failed to load created chat channel: %w", err)
	}
	return created, nil
}

// accessible 判断用户是否为渠道租户所有者或关联租户成员。
func (s *ChatChannelService) accessible(userID, channelID string) (*entity.ChatChannel, bool, error) {
	channel, err := s.chatChannelDAO.GetByIDOnly(channelID)
	if err != nil {
		if dao.IsNotFoundErr(err) {
			return nil, false, nil
		}
		return nil, false, err
	}

	if channel.TenantID == userID {
		return channel, true, nil
	}

	tenantIDs, err := s.userTenantDAO.GetTenantIDsByUserID(userID)
	if err != nil {
		return nil, false, err
	}
	for _, tenantID := range tenantIDs {
		if tenantID == channel.TenantID {
			return channel, true, nil
		}
	}

	return channel, false, nil
}

// GetChatChannel 获取单条渠道详情（含鉴权）。
func (s *ChatChannelService) GetChatChannel(userID, channelID string) (*entity.ChatChannel, common.ErrorCode, error) {
	_, ok, err := s.accessible(userID, channelID)
	if err != nil {
		return nil, common.CodeServerError, err
	}
	if !ok {
		return nil, common.CodeAuthenticationError, errors.New("No authorization.")
	}

	channel, err := s.chatChannelDAO.GetByIDOnly(channelID)
	if err != nil {
		if dao.IsNotFoundErr(err) {
			return nil, common.CodeDataError, errors.New("Can't find this chat channel!")
		}
		return nil, common.CodeServerError, err
	}
	return channel, common.CodeSuccess, nil
}

// UpdateChatChannel 更新 name/config/chat_id 等可写字段。
func (s *ChatChannelService) UpdateChatChannel(userID, channelID string, req map[string]interface{}) (*entity.ChatChannel, common.ErrorCode, error) {
	channel, ok, err := s.accessible(userID, channelID)
	if err != nil {
		return nil, common.CodeServerError, err
	}
	if !ok {
		return nil, common.CodeAuthenticationError, errors.New("No authorization.")
	}
	if channel == nil {
		return nil, common.CodeDataError, errors.New("Can't find this chat channel!")
	}

	updates := map[string]interface{}{}

	if value, exists := req["name"]; exists {
		name, ok := value.(string)
		if !ok {
			return nil, common.CodeDataError, errors.New("name must be string")
		}
		updates["name"] = name
	}

	if value, exists := req["config"]; exists {
		if value == nil {
			updates["config"] = nil
		} else {
			config, ok := value.(map[string]interface{})
			if !ok {
				return nil, common.CodeDataError, errors.New("config must be object")
			}
			updates["config"] = entity.JSONMap(config)
		}
	}

	if value, exists := req["chat_id"]; exists {
		if value == nil {
			updates["chat_id"] = nil
		} else {
			chatID, ok := value.(string)
			if !ok {
				return nil, common.CodeDataError, errors.New("chat_id must be string or null")
			}
			if chatID != "" {
				dialog, err := s.chatDAO.GetByID(chatID)
				if err != nil {
					if dao.IsNotFoundErr(err) {
						return nil, common.CodeDataError, errors.New("Can't find this chat assistant!")
					}
					return nil, common.CodeServerError, err
				}
				if dialog.TenantID != channel.TenantID {
					return nil, common.CodeAuthenticationError, errors.New("No authorization.")
				}
			}
			updates["chat_id"] = chatID
		}
	}

	if len(updates) > 0 {
		if err := s.chatChannelDAO.UpdateByID(channelID, channel.TenantID, updates); err != nil {
			return nil, common.CodeDataError, err
		}
	}

	updated, err := s.chatChannelDAO.GetByIDOnly(channelID)
	if err != nil {
		if dao.IsNotFoundErr(err) {
			return nil, common.CodeDataError, errors.New("Can't find this chat channel!")
		}
		return nil, common.CodeServerError, err
	}
	return updated, common.CodeSuccess, nil
}

// DeleteChatChannel 删除渠道（需 accessible）。
func (s *ChatChannelService) DeleteChatChannel(userID, channelID string) (bool, common.ErrorCode, error) {
	channel, ok, err := s.accessible(userID, channelID)
	if err != nil {
		return false, common.CodeServerError, err
	}
	if !ok {
		return false, common.CodeAuthenticationError, errors.New("No authorization.")
	}
	if channel == nil {
		return false, common.CodeAuthenticationError, errors.New("No authorization.")
	}

	if err := s.chatChannelDAO.DeleteByID(channelID, channel.TenantID); err != nil {
		return false, common.CodeDataError, err
	}
	return true, common.CodeSuccess, nil
}
// chat_channel.go — 聊天渠道 CRUD：绑定助手、租户权限与配置更新。
