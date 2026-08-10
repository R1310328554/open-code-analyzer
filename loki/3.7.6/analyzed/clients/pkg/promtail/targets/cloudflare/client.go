package cloudflare

// Cloudflare Logpull API 客户端封装：按 zone 与字段列表拉取 HTTP 日志迭代器。
// wrappedClient 便于单测注入 mock，getClient 为可替换工厂函数。

import (
	"context"
	"time"

	"github.com/grafana/cloudflare-go"
)

// LogpullReceived 接口抽象，start/end 定义拉取时间窗口。
// Client is a wrapper around the Cloudflare API that allow for testing and being zone/fields aware.
type Client interface {
	LogpullReceived(ctx context.Context, start, end time.Time) (cloudflare.LogpullReceivedIterator, error)
}

// 持有 cloudflare.API、zoneID 与请求字段列表的实际实现。
type wrappedClient struct {
	client *cloudflare.API
	zoneID string
	fields []string
}

func (w *wrappedClient) LogpullReceived(ctx context.Context, start, end time.Time) (cloudflare.LogpullReceivedIterator, error) {
	return w.client.LogpullReceived(ctx, w.zoneID, start, end, cloudflare.LogpullReceivedOption{
		Fields: w.fields,
	})
}

// 默认工厂：APIToken 认证后返回绑定 zone/fields 的 wrappedClient。
var getClient = func(apiKey, zoneID string, fields []string) (Client, error) {
	c, err := cloudflare.NewWithAPIToken(apiKey)
	if err != nil {
		return nil, err
	}
	return &wrappedClient{
		client: c,
		zoneID: zoneID,
		fields: fields,
	}, nil
}
