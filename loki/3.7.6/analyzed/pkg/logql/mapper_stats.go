package logql

// mapper_stats 记录范围映射器将查询拆分为多少个子查询，供队列与调试统计使用。

type MapperStats struct {
	splitQueries int
}

func NewMapperStats() *MapperStats {
	return &MapperStats{}
}

// AddSplitQueries 将本次拆分产生的子查询数累加到 splitQueries。
// AddSplitQueries add num split queries to the counter
func (s *MapperStats) AddSplitQueries(num int) {
	s.splitQueries += num
}

// GetSplitQueries 返回当前累计的拆分查询数量。
// GetSplitQueries returns the number of split queries
func (s *MapperStats) GetSplitQueries() int {
	return s.splitQueries
}

// resetSplitQueries 在映射结果为 noop 时清零计数，避免误报拆分开销。
// resetSplitQueries resets the number of split queries
func (s *MapperStats) resetSplitQueries() {
	s.splitQueries = 0
}
// 拆分计数与 MapperMetrics 中的 downstream 指标配合，反映查询并行度。
