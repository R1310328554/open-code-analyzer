package instrument

// instrument 包提供轻量请求计时包装，基于 dskit Collector 上报延迟而不创建 OTel span。

import (
	"context"
	"time"

	"github.com/grafana/dskit/instrument"
)

// TimeRequest 在 f 执行前后调用 col.Before/After，toStatusCode 缺省用 instrument.ErrorCode。
// TimeRequest reports how much time was spent on the given function  `f`.
//
// It is a thinner version of weaveworks/common/instrument.CollectedRequest that doesn't emit spans.
func TimeRequest(ctx context.Context, method string, col instrument.Collector, toStatusCode func(error) string, f func(context.Context) error) error {
	if toStatusCode == nil {
		toStatusCode = instrument.ErrorCode
	}

	start := time.Now()
	col.Before(ctx, method, start)
	err := f(ctx)
	col.After(ctx, method, toStatusCode(err), start)

	return err
}
// 相比 weaveworks CollectedRequest，本实现更薄，仅采集耗时指标不发射分布式追踪 span。
