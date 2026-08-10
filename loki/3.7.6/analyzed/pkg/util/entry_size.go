package util //nolint:revive

// util 包 entry_size 计算 push.Entry 及结构化元数据的字节占用，用于 ingest 限流、配额统计与 chunk 大小估算。

import (
	"slices"

	"github.com/grafana/loki/pkg/push"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

func EntriesTotalSize(entries []push.Entry) int {
	size := 0
	for _, entry := range entries {
		size += EntryTotalSize(&entry)
	}
	return size
}

// EntryTotalSize 为单条日志行长度加上 StructuredMetadataSize 结果。
func EntryTotalSize(entry *push.Entry) int {
	return len(entry.Line) + StructuredMetadataSize(entry.StructuredMetadata)
}

var ExcludedStructuredMetadataLabels = []string{constants.LevelLabel}

func StructuredMetadataSize(metas push.LabelsAdapter) int {
	size := 0
	for _, meta := range metas {
		if slices.Contains(ExcludedStructuredMetadataLabels, meta.Name) {
			continue
		}
		size += len(meta.Name) + len(meta.Value)
	}
	return size
}
// 大小统计不含时间戳与 stream 标签，仅反映 payload 本体便于与 line 限制对齐。
