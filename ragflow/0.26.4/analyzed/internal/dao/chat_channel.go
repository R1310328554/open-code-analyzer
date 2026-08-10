// chat_channel.go — 聊天渠道 DAO：管理 chat_channel 表，关联 dialog 实现多渠道接入配置。

package dao

import "ragflow/internal/entity"

// ChatChannelDAO 聊天渠道（Webhook/IM 等）数据访问对象。
type ChatChannelDAO struct{}

// NewChatChannel 构造 ChatChannelDAO 实例。
func NewChatChannel() *ChatChannelDAO {
	return &ChatChannelDAO{}
}

// Create 插入新聊天渠道记录。
func (dao *ChatChannelDAO) Create(channel *entity.ChatChannel) error {
	return DB.Create(channel).Error
}

// GetByIDOnly 仅按 ID 查询渠道（不校验租户）。
func (dao *ChatChannelDAO) GetByIDOnly(id string) (*entity.ChatChannel, error) {
	var channel entity.ChatChannel
	err := DB.Where("id = ?", id).First(&channel).Error
	if err != nil {
		return nil, err
	}
	return &channel, err
}

// GetByID 按 ID 与 tenant_id 联合查询，用于租户隔离。
func (dao *ChatChannelDAO) GetByID(id string, tenantID string) (*entity.ChatChannel, error) {
	var channel entity.ChatChannel
	err := DB.Where("id = ? AND tenant_id = ?", id, tenantID).First(&channel).Error
	if err != nil {
		return nil, err
	}
	return &channel, err
}

// UpdateByID 按 ID 与租户更新单条渠道记录。
func (dao *ChatChannelDAO) UpdateByID(id string, tenantID string, updates map[string]any) error {
	return DB.Model(&entity.ChatChannel{}).Where("id = ? AND tenant_id = ?", id, tenantID).Updates(updates).Error
}

// DeleteByID 按 ID 与租户删除单条渠道。
func (dao *ChatChannelDAO) DeleteByID(id string, tenantID string) error {
	return DB.Where("id = ? AND tenant_id = ?", id, tenantID).Delete(&entity.ChatChannel{}).Error
}

// ListByTenantID 列出租户全部渠道，JOIN dialog 取对话名称。
func (dao *ChatChannelDAO) ListByTenantID(tenantID string) ([]*entity.ChatChannelListResponse, error) {
	results := make([]*entity.ChatChannelListResponse, 0)

	err := DB.Table("chat_channel").
		Select("chat_channel.id, chat_channel.name, chat_channel.channel, chat_channel.chat_id, chat_channel.status, dialog.name as dialog_name").
		Joins("LEFT JOIN dialog ON dialog.id = chat_channel.chat_id").
		Where("chat_channel.tenant_id = ?", tenantID).
		Order("chat_channel.create_time DESC").
		Scan(&results).Error

	return results, err
}
