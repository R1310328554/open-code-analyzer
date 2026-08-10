// arrowtest 提供测试用 Arrow Record 构造与往返转换：从 map 行推断 schema 并比对结果。
// Package arrowtest provides utilities for testing Arrow records.
package arrowtest

import (
	"cmp"
	"fmt"
	"slices"
	"time"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
	"github.com/apache/arrow-go/v18/arrow/memory"
	"github.com/apache/arrow-go/v18/arrow/scalar"
)

// Rows 与 Row 类型别名分别表示多行测试数据与列名到 Go 值的单行映射。
type (
	// Rows is a slice of [Row].
	Rows []Row

	// Row represents a single record row as a map of column name to value.
	Row map[string]any
)

// Schema 从首行推断字段类型并校验后续行列一致，列名按字母序排序。
// Schema inferrs a [arrow.Schema] from each row in Rows. Columns in rows
// are sorted alphabetically.
//
// Schema panics if any of the following conditions are true:
//
// - A value cannot be converted into an Arrow data type.
// - Two rows have different sets of columns.
// - Two columns do not have the same Go type across rows.
func (rows Rows) Schema() *arrow.Schema {
	if len(rows) == 0 {
		// Empty schema.
		return arrow.NewSchema(nil, nil)
	}

	var (
		fields        = make([]arrow.Field, 0, len(rows[0]))
		columnToField = make(map[string]int)
	)

	// Get all the fields from the first row.
	for key, value := range rows[0] {
		// If value is nil, we will replace it with the first non-nil value we see
		// in the following loop.
		field := arrow.Field{
			Name:     key,
			Type:     determineDatatype(value),
			Nullable: true,
		}

		columnToField[key] = len(fields)
		fields = append(fields, field)
	}

	// Check the rest of the rows for consistency.
	for _, row := range rows[1:] {
		for key, value := range row {
			index, ok := columnToField[key]
			if !ok {
				panic(fmt.Sprintf("arrowtest.Schema: column %q not found in first row", key))
			}
			field := &fields[index]

			gotType := determineDatatype(value)

			if !arrow.TypeEqual(field.Type, gotType) {
				// The types don't match. We need to check for nulls here:
				//
				// If the expected type is null, we should replace it with a concrete
				// type. If gotType is null, we can ignore it (null scalars can be
				// casted appropriately).

				if arrow.TypeEqual(field.Type, arrow.Null) {
					field.Type = gotType
				} else if !arrow.TypeEqual(gotType, arrow.Null) {
					panic(fmt.Sprintf("arrowtest.Schema: column %q has different types across rows: got=%s, want=%s", key, gotType, field.Type))
				}
			}
		}
	}

	slices.SortFunc(fields, func(a, b arrow.Field) int {
		return cmp.Compare(a.Name, b.Name)
	})

	return arrow.NewSchema(fields, nil)
}

func determineDatatype(value any) arrow.DataType {
	switch value := value.(type) {
	case time.Time:
		return &arrow.TimestampType{Unit: arrow.Nanosecond, TimeZone: value.Location().String()}
	default:
		return scalar.MakeScalar(value).DataType()
	}
}

// Record 按 schema 逐列 Append 行值，nil 写入 null，time.Time 转为 timestamp scalar。
// Record converts rows into an [arrow.RecordBatch] with the provided schema. A
// schema can be inferred from rows by using [Rows.Schema].
//
// Record panics if schema references a column not found in one of the rows.
func (rows Rows) Record(alloc memory.Allocator, schema *arrow.Schema) arrow.RecordBatch {
	builder := array.NewRecordBuilder(alloc, schema)

	for i := range schema.NumFields() {
		field := schema.Field(i)
		fieldBuilder := builder.Field(i)

		for _, row := range rows {
			value, ok := row[field.Name]
			if !ok {
				panic(fmt.Sprintf("arrowtest.Record: column %q not found in row %d", field.Name, i))
			}

			if value == nil {
				fieldBuilder.AppendNull()
				continue
			}

			var s scalar.Scalar

			switch v := value.(type) {
			case time.Time:
				s = scalar.NewTimestampScalar(arrow.Timestamp(v.UnixNano()), determineDatatype(v))
			case *time.Time:
				s = scalar.NewTimestampScalar(arrow.Timestamp(v.UnixNano()), determineDatatype(v))
			default:
				s = scalar.MakeScalar(v)
			}

			if err := scalar.Append(fieldBuilder, s); err != nil {
				panic(fmt.Sprintf("arrowtest.Record: failed to append value %v for column %q: %v", value, field.Name, err))
			}
		}
	}

	return builder.NewRecordBatch()
}

// RecordRows 将 RecordBatch 每行还原为 Row map，timestamp 列特殊转为 time.Time。
// RecordRows converts an [arrow.RecordBatch] into [Rows] for comparison in tests.
// RecordRows requires all columns in the record to have a unique name.
//
// All values are converted to their direct Go equivalents.
//
// Callers building expected [Rows] must use the same functions.
func RecordRows(rec arrow.RecordBatch) (Rows, error) {
	rows := make(Rows, rec.NumRows())

	for i := range int(rec.NumRows()) {
		row := make(Row, rec.NumCols())

		for j := range int(rec.NumCols()) {
			row[rec.Schema().Field(j).Name] = getArrayValue(rec.Column(j), i)
		}

		rows[i] = row
	}

	return rows, nil
}

// getArrayValue converts a value from an [arrow.Array] at the given index back
// into a Go value. Timestamps have a special case so they are converted into a
// [time.Time].
func getArrayValue(arr arrow.Array, index int) any {
	switch arr := arr.(type) {
	case *array.Timestamp:
		toTimestamp, err := arr.DataType().(*arrow.TimestampType).GetToTimeFunc()
		if err != nil {
			panic(err)
		}
		return toTimestamp(arr.Value(index))

	default:
		return arr.GetOneForMarshal(index)
	}
}

// TableRows 先 mergeTable 合并分块再调用 RecordRows，要求列名全局唯一。
// TableRows concatenates all chunks of the [arrow.Table] into a single
// [arrow.RecordBactch], and then returns it as [Rows]. TableRows requires all
// columns in the table to have a unique name.
//
// See [RecordRows] for specifies on how values are converted into Go values
// for a [Row].
func TableRows(alloc memory.Allocator, table arrow.Table) (Rows, error) {
	rec, err := mergeTable(alloc, table)
	if err != nil {
		return nil, err
	}

	return RecordRows(rec)
}

// mergeTable merges all chunks in an [arrow.Table] into a single
// [arrow.RecordBatch].
func mergeTable(alloc memory.Allocator, table arrow.Table) (arrow.RecordBatch, error) {
	recordColumns := make([]arrow.Array, table.NumCols())

	for i := range int(table.NumCols()) {
		column, err := array.Concatenate(table.Column(i).Data().Chunks(), alloc)
		if err != nil {
			return nil, err
		}
		recordColumns[i] = column
	}

	return array.NewRecordBatch(table.Schema(), recordColumns, table.NumRows()), nil
}
// determineDatatype 对 time.Time 使用纳秒精度 TimestampType，其余走 scalar 推断。
