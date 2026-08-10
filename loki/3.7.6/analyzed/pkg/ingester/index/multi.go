package index

// multi 按存储周期切换倒排索引实现：普通 modulo 分片与 TSDB bit-prefix 索引并存，查询时按时间选 backend。

import (
	"fmt"
	"time"

	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/types"
)

// periodIndex 类型封装该模块的状态与行为。
type periodIndex struct {
	time.Time
	idx int // address of the index to use
}

// Multi 按配置周期持有多个倒排索引 backend，Add/Delete 同步写入全部 backend。
type Multi struct {
	periods []periodIndex
	indices []Interface
}

// NewMultiInvertedIndex 创建组件实例并完成必要初始化。
func NewMultiInvertedIndex(periods []config.PeriodConfig, indexShards uint32) (*Multi, error) {
	var (
		err error

		ii          Interface // always stored in 0th index
		bitPrefixed Interface // always stored in 1st index

		periodIndices []periodIndex
	)

	for _, pd := range periods {
		switch pd.IndexType {
		case types.TSDBType:
			if bitPrefixed == nil {
				bitPrefixed, err = NewBitPrefixWithShards(indexShards)
				if err != nil {
					return nil, fmt.Errorf("creating tsdb inverted index for period starting %v: %w", pd.From, err)
				}
			}
			periodIndices = append(periodIndices, periodIndex{
				Time: pd.From.Time.Time(),
				idx:  1, // tsdb inverted index is always stored in position one
			})
		default:
			if ii == nil {
				ii = NewWithShards(indexShards)
			}
			periodIndices = append(periodIndices, periodIndex{
				Time: pd.From.Time.Time(),
				idx:  0, // regular inverted index is always stored in position zero
			})
		}
	}

	return &Multi{
		periods: periodIndices,
		indices: []Interface{ii, bitPrefixed},
	}, nil
}

// 将 fingerprint 注册到倒排索引。
func (m *Multi) Add(labels []logproto.LabelAdapter, fp model.Fingerprint) (result labels.Labels) {
	for _, i := range m.indices {
		if i != nil {
			result = i.Add(labels, fp)
		}
	}
	return
}

// 从倒排索引移除 fingerprint。
func (m *Multi) Delete(labels labels.Labels, fp model.Fingerprint) {
	for _, i := range m.indices {
		if i != nil {
			i.Delete(labels, fp)
		}
	}

}

// 按 matcher 查找 fingerprint 集合。
func (m *Multi) Lookup(t time.Time, matchers []*labels.Matcher, shard *logql.Shard) ([]model.Fingerprint, error) {
	return m.indexFor(t).Lookup(matchers, shard)
}

// 返回索引中所有标签名。
func (m *Multi) LabelNames(t time.Time, shard *logql.Shard) ([]string, error) {
	return m.indexFor(t).LabelNames(shard)
}

// 返回指定标签名的全部取值。
func (m *Multi) LabelValues(t time.Time, name string, shard *logql.Shard) ([]string, error) {
	return m.indexFor(t).LabelValues(name, shard)
}

// indexFor 按查询时间点选择对应周期的索引实现，越界返回 noopInvertedIndex。
// Query planning is responsible for ensuring no query spans more than one inverted index.
// Therefore we don't need to account for both `from` and `through`.
func (m *Multi) indexFor(t time.Time) Interface {
	for i := range m.periods {
		if !m.periods[i].After(t) && (i+1 == len(m.periods) || t.Before(m.periods[i+1].Time)) {
			return m.indices[m.periods[i].idx]
		}
	}
	return noopInvertedIndex{}
}

// noopInvertedIndex 类型封装该模块的状态与行为。
type noopInvertedIndex struct{}

func (noopInvertedIndex) Add(_ []logproto.LabelAdapter, _ model.Fingerprint) labels.Labels {
	return labels.EmptyLabels()
}

// 从倒排索引移除 fingerprint。
func (noopInvertedIndex) Delete(_ labels.Labels, _ model.Fingerprint) {}

func (noopInvertedIndex) Lookup(_ []*labels.Matcher, _ *logql.Shard) ([]model.Fingerprint, error) {
	return nil, nil
}

// 返回索引中所有标签名。
func (noopInvertedIndex) LabelNames(_ *logql.Shard) ([]string, error) {
	return nil, nil
}

// 返回指定标签名的全部取值。
func (noopInvertedIndex) LabelValues(_ string, _ *logql.Shard) ([]string, error) {
	return nil, nil
}
// 查询规划需保证单次查询不跨多个倒排索引周期边界。
