package gcplog

// GCP Cloud Logging target 工厂：按 SubscriptionType 选择 Pull 或 Push 实现，
// 统一 Target 接口供 targetmanager 管理。

import (
	"fmt"

	"github.com/go-kit/log"
	"github.com/prometheus/prometheus/model/relabel"
	"google.golang.org/api/option"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// pullTarget 与 pushTarget 共用的 Target 接口，含 Stop 生命周期。
// Target is a common interface implemented by both GCPLog targets.
type Target interface {
	target.Target
	Stop() error
}

// subscription_type 为空或 pull 走 Pub/Sub 拉取，push 启动独立 HTTP 接收端。
// NewGCPLogTarget creates a GCPLog target either with the push or pull implementation, depending on the configured
// subscription type.
func NewGCPLogTarget(
	metrics *Metrics,
	logger log.Logger,
	handler api.EntryHandler,
	relabel []*relabel.Config,
	jobName string,
	config *scrapeconfig.GcplogTargetConfig,
	clientOptions ...option.ClientOption,
) (Target, error) {
	switch config.SubscriptionType {
	case "pull", "":
		return newPullTarget(metrics, logger, handler, relabel, jobName, config, clientOptions...)
// Push 模式无需 Pub/Sub client option，由 GCP 主动 POST 日志。
	case "push":
		return newPushTarget(metrics, logger, handler, jobName, config, relabel)
	default:
		return nil, fmt.Errorf("invalid subscription type: %s. valid options are 'push' and 'pull'", config.SubscriptionType)
	}
}
