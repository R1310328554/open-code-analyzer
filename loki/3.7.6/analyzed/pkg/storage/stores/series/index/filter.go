package index

// filter 为索引 QueryPages 回调包装结果过滤层，供缓存与 Bigtable 等整行读取后端在客户端按 RangeValue 前缀/起点与 ValueEqual 条件裁剪条目。

import "bytes"

// QueryFilter 包装 QueryPagesCallback，在回调侧应用 Query 中的 range/value 过滤条件。
// QueryFilter wraps a callback to ensure the results are filtered correctly;
// useful for the cache and Bigtable backend, which only ever fetches the whole
// row.
func QueryFilter(callback QueryPagesCallback) QueryPagesCallback {
	return func(query Query, batch ReadBatchResult) bool {
		return callback(query, &filteringBatch{
			query:           query,
			ReadBatchResult: batch,
		})
	}
}

// filteringBatch 装饰 ReadBatchResult，使 Iterator 返回经 Query 条件过滤后的条目。
type filteringBatch struct {
	query Query
	ReadBatchResult
}

func (f filteringBatch) Iterator() ReadBatchIterator {
	return &filteringBatchIter{
		query:             f.query,
		ReadBatchIterator: f.ReadBatchResult.Iterator(),
	}
}

type filteringBatchIter struct {
	query Query
	ReadBatchIterator
}

// Next 跳过不满足 RangeValuePrefix、RangeValueStart 或 ValueEqual 的索引行。
func (f *filteringBatchIter) Next() bool {
	for f.ReadBatchIterator.Next() {
		rangeValue, value := f.RangeValue(), f.Value()

		if len(f.query.RangeValuePrefix) != 0 && !bytes.HasPrefix(rangeValue, f.query.RangeValuePrefix) {
			continue
		}
		if len(f.query.RangeValueStart) != 0 && bytes.Compare(f.query.RangeValueStart, rangeValue) > 0 {
			continue
		}
		if len(f.query.ValueEqual) != 0 && !bytes.Equal(value, f.query.ValueEqual) {
			continue
		}

		return true
	}

	return false
}
// 整行拉取后端依赖此包装避免把无关 range 键返回给上层 schema 解析逻辑。
