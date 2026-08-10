package deletion

// deletion 包为 querier 提供查询时删除请求过滤：从 compactor 获取租户 delete 并与时间窗求交。

import (
	"context"
	"time"

	"github.com/grafana/dskit/tenant"

	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
	"github.com/grafana/loki/v3/pkg/logproto"
)

// DeleteGetter 抽象 compactor 侧按用户列举 DeleteRequest 的接口。
type DeleteGetter interface {
	GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *deletion.TimeRange) ([]deletionproto.DeleteRequest, error)
}

// DeletesForUserQuery 从 context 取租户 ID，过滤与 [startT,endT] 重叠的删除规则为 logproto.Delete。
// DeletesForUserQuery returns the deletes for a user (taken from request context) within a given time range.
func DeletesForUserQuery(ctx context.Context, startT, endT time.Time, g DeleteGetter) ([]*logproto.Delete, error) {
	userID, err := tenant.TenantID(ctx)
	if err != nil {
		return nil, err
	}

	d, err := g.GetAllDeleteRequestsForUser(ctx, userID, true, nil)
	if err != nil {
		return nil, err
	}

	start := startT.UnixNano()
	end := endT.UnixNano()

	var deletes []*logproto.Delete
	for _, del := range d {
		if del.StartTime.UnixNano() <= end && del.EndTime.UnixNano() >= start {
			deletes = append(deletes, &logproto.Delete{
				Selector: del.Query,
				Start:    del.StartTime.UnixNano(),
				End:      del.EndTime.UnixNano(),
			})
		}
	}

	return deletes, nil
}
// forQuerytimeFiltering=true 时 compactor 返回适合查询路径的已解析删除条目。
