package pointers

// Arrow RecordBatch 与 SectionPointer 互转：按列类型填充目标切片行。

import (
	"time"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
)

type PopulateColumnFilter func(arrow.Field, ColumnType) bool

func PopulateSectionKey(_ arrow.Field, columnType ColumnType) bool {
	return columnType == ColumnTypePath || columnType == ColumnTypeSection
}

// sectionPointerColumns 列出流索引指针所需的全部列类型集合。
var sectionPointerColumns = map[ColumnType]struct{}{
	ColumnTypePath:             {},
	ColumnTypeSection:          {},
	ColumnTypePointerKind:      {},
	ColumnTypeStreamID:         {},
	ColumnTypeStreamIDRef:      {},
	ColumnTypeMinTimestamp:     {},
	ColumnTypeMaxTimestamp:     {},
	ColumnTypeRowCount:         {},
	ColumnTypeUncompressedSize: {},
}

func PopulateSection(_ arrow.Field, columnType ColumnType) bool {
	_, ok := sectionPointerColumns[columnType]
	return ok
}

// InternalLabelsColumn 从批次中提取 __streamLabelNames__ 内部标签列。
func InternalLabelsColumn(rec arrow.RecordBatch) *array.String {
	schema := rec.Schema()
	for fIdx := range schema.Fields() {
		field := schema.Field(fIdx)
		if field.Name == InternalLabelsFieldName {
			return rec.Column(fIdx).(*array.String)
		}
	}
	return nil
}

// FromRecordBatch 按 populate 过滤器将 Arrow 列值写入 dest 中对应 SectionPointer 字段。
func FromRecordBatch(
	rec arrow.RecordBatch,
	dest []SectionPointer,
	populate func(arrow.Field, ColumnType) bool,
) (int, error) {
	schema := rec.Schema()
	numRows := int(rec.NumRows())
	if len(dest) < numRows {
		numRows = len(dest)
	}

	for fIdx := range schema.Fields() {
		field := schema.Field(fIdx)
		col := rec.Column(fIdx)
		ct := ColumnTypeFromField(field)

		if !populate(field, ct) {
			continue
		}

		switch ct {
		case ColumnTypePath:
			values := col.(*array.String)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].Path = values.Value(rIdx)
			}
		case ColumnTypeSection:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].Section = values.Value(rIdx)
			}
		case ColumnTypePointerKind:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].PointerKind = PointerKind(values.Value(rIdx))
			}
		case ColumnTypeStreamID:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].StreamID = values.Value(rIdx)
			}
		case ColumnTypeStreamIDRef:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].StreamIDRef = values.Value(rIdx)
			}
		case ColumnTypeMinTimestamp:
			values := col.(*array.Timestamp)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].StartTs = time.Unix(0, int64(values.Value(rIdx)))
			}
		case ColumnTypeMaxTimestamp:
			values := col.(*array.Timestamp)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].EndTs = time.Unix(0, int64(values.Value(rIdx)))
			}
		case ColumnTypeRowCount:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].LineCount = values.Value(rIdx)
			}
		case ColumnTypeUncompressedSize:
			values := col.(*array.Int64)
			for rIdx := range numRows {
				if col.IsNull(rIdx) {
					continue
				}
				dest[rIdx].UncompressedSize = values.Value(rIdx)
			}
		default:
			continue
		}
	}

	return numRows, nil
}
// 时间戳列以纳秒 Unix 时间写入 StartTs/EndTs。
