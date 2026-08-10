package ruler

// ruler 包入口：NewRuler 组装 Loki 多租户规则管理器、Evaluator 与 rulestore，并兼容 deprecated remote_write client 单客户端配置。

import (
	"github.com/go-kit/log"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/prometheus/config"
	"go.opentelemetry.io/otel"

	ruler "github.com/grafana/loki/v3/pkg/ruler/base"
	"github.com/grafana/loki/v3/pkg/ruler/rulestore"
)

var tracer = otel.Tracer("pkg/ruler")

// NewRuler 将 client 迁移至 clients 映射后调用 base.NewDefaultMultiTenantManager。
func NewRuler(cfg Config, evaluator Evaluator, reg prometheus.Registerer, logger log.Logger, ruleStore rulestore.RuleStore, limits RulesLimits, metricsNamespace string) (*ruler.Ruler, error) {
	// For backward compatibility, client and clients are defined in the remote_write config.
	// When both are present, an error is thrown.
	if len(cfg.RemoteWrite.Clients) > 0 && cfg.RemoteWrite.Client != nil {
		return nil, errors.New("ruler remote write config: both 'client' and 'clients' options are defined; 'client' is deprecated, please only use 'clients'")
	}

	if len(cfg.RemoteWrite.Clients) == 0 && cfg.RemoteWrite.Client != nil {
		if cfg.RemoteWrite.Clients == nil {
			cfg.RemoteWrite.Clients = make(map[string]config.RemoteWriteConfig)
		}

		cfg.RemoteWrite.Clients["default"] = *cfg.RemoteWrite.Client
	}

	mgr, err := ruler.NewDefaultMultiTenantManager(
		cfg.Config,
		MultiTenantRuleManager(cfg, evaluator, limits, logger, reg),
		reg,
		logger,
		limits,
		metricsNamespace,
	)
	if err != nil {
		return nil, err
	}
	return ruler.NewRuler(
		cfg.Config,
		MultiTenantManagerAdapter(mgr),
		reg,
		logger,
		ruleStore,
		limits,
		metricsNamespace,
	)
}
// tracer 为 pkg/ruler OpenTelemetry 追踪器，供 remote 求值等路径使用。
