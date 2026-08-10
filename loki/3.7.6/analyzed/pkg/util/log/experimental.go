package log

// log 包 experimental 子模块在启用实验特性时输出 warn 日志，并递增 loki_experimental_features_in_use_total 指标供运维观测。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

// experimentalFeaturesInUse 计数器按特性名维度统计当前启用的实验路径次数。
var experimentalFeaturesInUse = promauto.NewCounter(
	prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "experimental_features_in_use_total",
		Help:      "The number of experimental features in use.",
	},
)

// WarnExperimentalUse 记录 feature 名称并 Inc 指标，应在特性入口统一调用。
// WarnExperimentalUse logs a warning and increments the experimental features metric.
func WarnExperimentalUse(feature string, logger log.Logger) {
	level.Warn(logger).Log("msg", "experimental feature in use", "feature", feature)
	experimentalFeaturesInUse.Inc()
}
// 实验特性指标 namespace 使用 constants.Loki，与核心组件指标前缀一致。
