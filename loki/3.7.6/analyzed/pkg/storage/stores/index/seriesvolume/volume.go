package seriesvolume

// seriesvolume 包聚合各 store 返回的 log volume 统计，支持按 series 或 labels 维度合并排序。

import (
	"fmt"
	"sort"
	"sync"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// 常量定义空匹配 MatchAny、默认 limit、聚合维度 series/labels 及超限错误模板。
const (
	MatchAny     = "{}"
	DefaultLimit = 100
	Series       = "series"
	Labels       = "labels"

	DefaultAggregateBy = Series

	ErrVolumeMaxSeriesHit = "the query hit the max number of series limit (limit: %d series)"
)

// TODO(masslessparticle): Lock striping to reduce contention on this map
// Accumulator 线程安全地累加 label/series 名到字节量的映射，并强制执行 series 上限。
type Accumulator struct {
	lock            sync.RWMutex
	volumes         map[string]uint64
	limit           int32
	volumeMaxSeries int
}

func NewAccumulator(limit int32, maxSize int) *Accumulator {
	return &Accumulator{
		volumes:         make(map[string]uint64),
		limit:           limit,
		volumeMaxSeries: maxSize,
	}
}

// AddVolume 累加指定 name 的字节 volume，超过 volumeMaxSeries 时返回错误。
func (acc *Accumulator) AddVolume(name string, size uint64) error {
	acc.lock.Lock()
	defer acc.lock.Unlock()

	acc.volumes[name] += size
	if len(acc.volumes) > acc.volumeMaxSeries {
		return fmt.Errorf(ErrVolumeMaxSeriesHit, acc.volumeMaxSeries)
	}

	return nil
}

func (acc *Accumulator) Volumes() *logproto.VolumeResponse {
	acc.lock.RLock()
	defer acc.lock.RUnlock()

	return MapToVolumeResponse(acc.volumes, int(acc.limit))
}

// Merge 将多个 VolumeResponse 按 name 求和后映射为按 volume 降序的受限列表。
func Merge(responses []*logproto.VolumeResponse, limit int32) *logproto.VolumeResponse {
	mergedVolumes := make(map[string]uint64)
	for _, res := range responses {
		if res == nil {
			// Some stores return nil responses
			continue
		}

		for _, v := range res.Volumes {
			mergedVolumes[v.Name] += v.GetVolume()
		}
	}

	return MapToVolumeResponse(mergedVolumes, int(limit))
}

func MapToVolumeResponse(mergedVolumes map[string]uint64, limit int) *logproto.VolumeResponse {
	volumes := make([]logproto.Volume, 0, len(mergedVolumes))
	for name, size := range mergedVolumes {
		volumes = append(volumes, logproto.Volume{
			Name:   name,
			Volume: size,
		})
	}

	sort.Slice(volumes, func(i, j int) bool {
		if volumes[i].Volume == volumes[j].Volume {
			return volumes[i].Name < volumes[j].Name
		}

		return volumes[i].Volume > volumes[j].Volume
	})

	if limit < len(volumes) {
		volumes = volumes[:limit]
	}

	return &logproto.VolumeResponse{
		Volumes: volumes,
		Limit:   int32(limit),
	}
}

func ValidateAggregateBy(aggregateBy string) bool {
	switch aggregateBy {
	case Labels:
		return true
	case Series:
		return true
	default:
		return false
	}
}

func AggregateBySeries(aggregateBy string) bool {
	return aggregateBy == Series
}
// MapToVolumeResponse 在 volume 相同时按 name 字典序打破平局，再截断到 limit 条。
