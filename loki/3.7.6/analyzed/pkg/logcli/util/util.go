package util //nolint:revive

// util 包提供 logcli 共用的标签过滤辅助函数。

import "github.com/grafana/loki/v3/pkg/loghttp"

func MatchLabels(on bool, l loghttp.LabelSet, names []string) loghttp.LabelSet {
	ret := loghttp.LabelSet{}

	nameSet := map[string]struct{}{}
	for _, n := range names {
		nameSet[n] = struct{}{}
	}

// 遍历源 LabelSet，键是否在 nameSet 中决定是否复制到结果集。
	for k, v := range l {
		if _, ok := nameSet[k]; on == ok {
			ret[k] = v
		}
	}

	return ret
}
// print 与 query/tail 包均复用此函数以保持标签过滤行为一致。
