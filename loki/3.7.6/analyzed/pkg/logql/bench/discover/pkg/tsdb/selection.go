package tsdb

// selection 按 service_name 分组轮询选取 canonical selector，避免单一服务垄断基准测试流样本。

import (
	"sort"

	"github.com/grafana/loki/v3/pkg/loghttp"
)

// selectWithDiversity 在流数超过上限时跨服务轮询取样，组内排序保证确定性。
// selectWithDiversity selects up to maxStreams canonical selectors from the
// deduplicated seen map using a service-diversity strategy.
//
// Strategy:
//  1. Group selectors by service_name (selectors without a service_name are
//     grouped under the empty string and participate in the same round-robin).
//  2. Round-robin across groups in sorted order, taking one selector from
//     each group per round, until maxStreams slots are filled.
//  3. Within each group, selectors are sorted for determinism.
//
// This ensures that no single service can monopolise the output set when many
// streams are available. Services with fewer streams are exhausted naturally;
// remaining slots fill from services that still have candidates.
//
// If len(seen) <= maxStreams, all selectors are returned (no truncation needed).
func selectWithDiversity(seen map[string]loghttp.LabelSet, maxStreams int) []string {
	if len(seen) <= maxStreams {
// 流数未超限时直接返回全部 selector 的字典序列表。
		// No capping needed: return all selectors in sorted order.
		result := make([]string, 0, len(seen))
		for canonical := range seen {
			result = append(result, canonical)
		}
		sort.Strings(result)
		return result
	}

// 无 service_name 标签的流归入空字符串组，仍参与轮询。
	// Group by service_name (empty string key for selectors without one).
	groups := make(map[string][]string)
	for canonical, ls := range seen {
		svc := ls["service_name"]
		groups[svc] = append(groups[svc], canonical)
	}

	// Sort each group for determinism, and collect sorted group keys.
	keys := make([]string, 0, len(groups))
	for svc, sels := range groups {
		sort.Strings(sels)
		keys = append(keys, svc)
	}
	sort.Strings(keys)

// 每轮从各组取一条，小服务先耗尽，剩余名额由大服务填充。
	// Interleave: round-robin one selector from each group per round.
	result := make([]string, 0, maxStreams)
	for round := 0; len(result) < maxStreams; round++ {
		exhausted := 0
		for _, svc := range keys {
			if round >= len(groups[svc]) {
				exhausted++
				continue
			}
			result = append(result, groups[svc][round])
			if len(result) >= maxStreams {
				break
			}
		}
		if exhausted == len(keys) {
			break // All groups exhausted.
		}
	}

	sort.Strings(result)
	return result
}
// 最终 result 再次排序，便于与 golden 文件及跨运行 diff 对齐。
