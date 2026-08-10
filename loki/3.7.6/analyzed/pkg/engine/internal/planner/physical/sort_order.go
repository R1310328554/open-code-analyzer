package physical

// SortOrder 表示扫描与 TopK 的排序方向：未排序、升序 ASC 或降序 DESC。

type SortOrder uint8

const (
	UNSORTED SortOrder = iota
	ASC
	DESC
)

// String 输出 UNSORTED/ASC/DESC，processSort 将 logical.Sort 映射为 TopK 的 Ascending 字段。
// String returns the string representation of the [SortOrder].
func (o SortOrder) String() string {
	switch o {
	case UNSORTED:
		return "UNSORTED"
	case ASC:
		return "ASC"
	case DESC:
		return "DESC"
	default:
		return "UNDEFINED"
	}
}
// DESC 为日志查询默认方向，按时间戳从新到旧读取以配合 limit 下推。
