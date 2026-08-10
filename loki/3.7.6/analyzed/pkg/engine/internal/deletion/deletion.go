// package deletion 提供查询引擎读取 compactor 删除请求的辅助逻辑。
package deletion

import (
	"context"
	"time"

	"github.com/grafana/dskit/tenant"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
)

// Getter 抽象 compactor 侧按租户与时间范围拉取 DeleteRequest 的能力。
// Getter defines methods to get deletion requests.
type Getter interface {
	GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *deletion.TimeRange) ([]deletionproto.DeleteRequest, error)
}

// Request represents a deletion request.
// Request 是引擎内部简化的删除区间：LogQL selector 与纳秒起止时间。
type Request struct {
	Selector string
	Start    int64
	End      int64
}

// DeletesForUser 从 context 取 tenantID，forQuerytimeFiltering=false 以获取全部重叠删除。
// DeletesForUser returns the delete reqs for a user (taken from request context) overlapping the provided time range.
func DeletesForUser(ctx context.Context, startT, endT time.Time, g Getter) ([]*Request, error) {
	userID, err := tenant.TenantID(ctx)
	if err != nil {
		return nil, err
	}

	// forQuerytimeFiltering is set to false as we want to get all deletes in the time range, not just pending ones.
	d, err := g.GetAllDeleteRequestsForUser(ctx, userID, false, &deletion.TimeRange{
		Start: model.TimeFromUnixNano(startT.UnixNano()),
		End:   model.TimeFromUnixNano(endT.UnixNano()),
	})
	if err != nil {
		return nil, err
	}

	deletes := make([]*Request, 0, len(d))
	for _, del := range d {
		deletes = append(deletes, &Request{
			Selector: del.Query,
			Start:    del.StartTime.UnixNano(),
			End:      del.EndTime.UnixNano(),
		})
	}

	return deletes, nil
}
// 查询执行层据此过滤命中删除 selector 的日志行或跳过已删时间窗。
