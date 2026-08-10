package deletion

// perTenantDeleteRequestsClient 包装 DeleteRequestsClient：
// 按租户 deletion 限额决定是否返回删除请求，未启用时返回空列表。

import (
	"context"
	"time"

	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
	"github.com/grafana/loki/v3/pkg/validation"
)

const deletionNotAvailableMsg = "deletion is not available for this tenant"

// Limits 抽象租户删除模式、保留期与流级保留策略配置。
type Limits interface {
	DeletionMode(userID string) string
	RetentionPeriod(userID string) time.Duration
	StreamRetention(userID string) []validation.StreamRetention
}

type perTenantDeleteRequestsClient struct {
	client DeleteRequestsClient
	limits Limits
}

// NewPerTenantDeleteRequestsClient 构造带限额检查的删除请求客户端装饰器。
func NewPerTenantDeleteRequestsClient(c DeleteRequestsClient, l Limits) DeleteRequestsClient {
	return &perTenantDeleteRequestsClient{
		client: c,
		limits: l,
	}
}

func (c *perTenantDeleteRequestsClient) GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *TimeRange) ([]deletionproto.DeleteRequest, error) {
	hasDelete, err := validDeletionLimit(c.limits, userID)
	if err != nil {
		return nil, err
	}

	if hasDelete {
		return c.client.GetAllDeleteRequestsForUser(ctx, userID, forQuerytimeFiltering, timeRange)
	}
	return nil, nil
}

func (c *perTenantDeleteRequestsClient) Stop() {
	c.client.Stop()
}
