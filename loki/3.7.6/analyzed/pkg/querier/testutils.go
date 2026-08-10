package querier

// querier 包 testutils 提供 DefaultLimitsConfig，用 flagext.DefaultValues 填充 validation.Limits 默认值供测试使用。

import (
	"github.com/grafana/dskit/flagext"

	"github.com/grafana/loki/v3/pkg/validation"
)

func DefaultLimitsConfig() validation.Limits {
	limits := validation.Limits{}
	flagext.DefaultValues(&limits)
	return limits
}
// 与 production limits 默认一致，便于 querier 集成测试复用同一配置基线。
