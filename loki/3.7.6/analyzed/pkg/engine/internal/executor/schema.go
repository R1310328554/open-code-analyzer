package executor

// schema 提供 Arrow RecordBatch 的模式替换与兼容性校验，仅改元数据不改底层列数据。

import (
	"fmt"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
)

// changeSchema 复用原列数组，将 schema 替换为 newSchema，字段类型与可空性须兼容。
// changeSchema creates a record with the same data as input, but with the
// schema set to newSchema.
//
// changeSchema can be used for changing field names, field metadata, or
// overall schema metadata. Field datatypes must be consistent between the
// previous and new schemas.
//
// changeSchema returns an error if the new schema is not compatible with the
// old schema.
func changeSchema(input arrow.RecordBatch, newSchema *arrow.Schema) (arrow.RecordBatch, error) {
	if err := validateSchemaCompatibility(input.Schema(), newSchema); err != nil {
		return nil, fmt.Errorf("incompatible schema: %w", err)
	}

	var (
		numCols = input.NumCols()
		numRows = input.NumRows()
	)

	cols := make([]arrow.Array, numCols)
	for i := range numCols {
		cols[i] = input.Column(int(i))
	}

	return array.NewRecordBatch(newSchema, cols, numRows), nil
}

// validateSchemaCompatibility 逐字段比较类型、可空性与字节序，不兼容则返回详细错误。
// validateSchemaCompatibility checks if two schemas are compatible:
//
// - Both schemas have the same endianness.
// - Both schemas have the same number of fields.
// - The data type of each field matches between the two schemas.
// - The nullability of each field matches between the two schemas.
//
// validateSchemaCompatibility returns nil if the schemas are compatible.
func validateSchemaCompatibility(a, b *arrow.Schema) error {
	if a.NumFields() != b.NumFields() {
		return fmt.Errorf("schemas have different number of fields: %d vs %d", a.NumFields(), b.NumFields())
	}

	if a.Endianness() != b.Endianness() {
		return fmt.Errorf("schemas have different endianness: %s vs %s", a.Endianness(), b.Endianness())
	}

	for i := range a.NumFields() {
		aField, bField := a.Field(i), b.Field(i)

		if !arrow.TypeEqual(aField.Type, bField.Type) {
			return fmt.Errorf("field %d has different types: %s (%s) vs %s (%s)", i, aField.Type, aField.Name, bField.Type, bField.Name)
		} else if aField.Nullable != bField.Nullable {
			return fmt.Errorf("field %d has different nullability: %t (%s) vs %t (%s)", i, aField.Nullable, aField.Name, bField.Nullable, bField.Name)
		}
	}

	return nil
}
// 常用于重命名列、调整 metadata 或统一 semconv 字段命名，不改变 Arrow 数组内容。
