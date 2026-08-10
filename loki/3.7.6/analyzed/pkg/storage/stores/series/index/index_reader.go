package index

// index_reader 提供离线/维护场景下的索引表扫描：Reader 按表读取 Entry 并分发给 EntryProcessor。

import (
	"context"
)

// EntryProcessor 按租户过滤并逐条处理 IndexEntry，Flush 在表扫描结束时调用。
// EntryProcessor receives index entries from a table.
type EntryProcessor interface {
	ProcessIndexEntry(indexEntry Entry) error

	// Will this user be accepted by the processor?
	AcceptUser(user string) bool

	// Called at the end of reading of index entries.
	Flush() error
}

// Reader 枚举索引表名并对单表执行 ReadIndexEntries 流式解析。
// Reader parses index entries and passes them to the IndexEntryProcessor.
type Reader interface {
	IndexTableNames(ctx context.Context) ([]string, error)

	// Reads a single table from index, and passes individual index entries to the processors.
	//
	// All entries with the same TableName, HashValue and RangeValue are passed to the same processor,
	// and all such entries (with different Values) are passed before index entries with different
	// values of HashValue and RangeValue are passed to the same processor.
	//
	// This allows IndexEntryProcessor to find when values for given Hash and Range finish:
	// as soon as new Hash and Range differ from last IndexEntry.
	//
	// Index entries passed to the same processor arrive sorted by HashValue and RangeValue.
	ReadIndexEntries(ctx context.Context, table string, processors []EntryProcessor) error
}
// 同一 HashValue+RangeValue 的多 Value 会连续送达同一 processor，便于检测值组结束边界。
