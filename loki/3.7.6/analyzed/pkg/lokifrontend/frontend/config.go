package frontend

// frontend 子包提供 CombinedFrontendConfig 与 InitFrontend：按 scheduler 地址、ring 或 downstream URL 选择 V1/V2/代理三种查询前端实现。

import (
	"flag"
	"net/http"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/netutil"
	"github.com/grafana/dskit/ring"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/lokifrontend/frontend/transport"
	v1 "github.com/grafana/loki/v3/pkg/lokifrontend/frontend/v1"
	v2 "github.com/grafana/loki/v3/pkg/lokifrontend/frontend/v2"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

// CombinedFrontendConfig 合并 Handler 与 V1/V2 配置以保持旧版 YAML 结构兼容。
// This struct combines several configuration options together to preserve backwards compatibility.
type CombinedFrontendConfig struct {
	Handler    transport.HandlerConfig `yaml:",inline"`
	FrontendV1 v1.Config               `yaml:",inline"`
	FrontendV2 v2.Config               `yaml:",inline"`

	DownstreamURL string `yaml:"downstream_url"`
}

func (cfg *CombinedFrontendConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.Handler.RegisterFlags(f)
	cfg.FrontendV1.RegisterFlags(f)
	cfg.FrontendV2.RegisterFlags(f)

	f.StringVar(&cfg.DownstreamURL, "frontend.downstream-url", "", "URL of downstream Prometheus.")
}

// InitFrontend 三分支：downstream 代理、V2+scheduler ring、或经典 V1 frontend。
// InitFrontend initializes frontend (either V1 -- without scheduler, or V2 -- with scheduler) or no frontend at
// all if downstream Prometheus URL is used instead.
//
// Returned RoundTripper can be wrapped in more round-tripper middlewares, and then eventually registered
// into HTTP server using the Handler from this package. Returned RoundTripper is always non-nil
// (if there are no errors), and it uses the returned frontend (if any).
// InitFrontend 返回 query-range Handler 及可选 V1/V2 实例，供 modules initQueryFrontend 挂载路由。
func InitFrontend(cfg CombinedFrontendConfig, ring ring.ReadRing, limits v1.Limits, grpcListenPort int, log log.Logger, reg prometheus.Registerer, metricsNamespace string, codec transport.Codec) (queryrangebase.Handler, *v1.Frontend, *v2.Frontend, error) {
	switch {
	case cfg.DownstreamURL != "":
		// If the user has specified a downstream Prometheus, then we should use that.
		rt, err := NewDownstreamRoundTripper(cfg.DownstreamURL, http.DefaultTransport, codec)
		return rt, nil, nil, err
	case cfg.FrontendV2.SchedulerAddress != "" || ring != nil:
		// If query-scheduler address is configured, use Frontend.
		if cfg.FrontendV2.Addr == "" {
			addr, err := netutil.GetFirstAddressOf(cfg.FrontendV2.InfNames, log, cfg.FrontendV2.EnableIPv6)
			if err != nil {
				return nil, nil, nil, errors.Wrap(err, "failed to get frontend address")
			}

			cfg.FrontendV2.Addr = addr
		}

		if cfg.FrontendV2.Port == 0 {
			cfg.FrontendV2.Port = grpcListenPort
		}

		fr, err := v2.NewFrontend(cfg.FrontendV2, ring, log, reg, codec, metricsNamespace)
		return fr, nil, fr, err

	default:
		// No scheduler = use original frontend.
		fr, err := v1.New(cfg.FrontendV1, limits, log, reg, metricsNamespace)
		if err != nil {
			return nil, nil, nil, err
		}
		return transport.AdaptGrpcRoundTripperToHandler(fr, codec), fr, nil, nil
	}
}
// V2 未配置 Addr 时从 InfNames 解析首地址；Port 为 0 则回退 grpcListenPort。
