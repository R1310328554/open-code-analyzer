package arrowconv

// columnar 子模块：将 Loki columnar.RecordBatch 零拷贝或浅拷贝转为 Apache Arrow RecordBatch，供 Arrow 生态工具消费。

import (
	"fmt"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
	arrowmemory "github.com/apache/arrow-go/v18/arrow/memory"

	"github.com/grafana/loki/v3/pkg/columnar"
)

// ToRecordBatch 按 schema 逐列复制 validity 与值缓冲区，构建 Arrow 数组。
// ToRecordBatch converts a columnar RecordBatch into an Arrow RecordBatch using
// the provided schema for the output types.
func ToRecordBatch(src *columnar.RecordBatch, schema *arrow.Schema) (arrow.RecordBatch, error) {
	nrows := src.NumRows()
	var arrs []arrow.Array

	for colIdx := range src.NumCols() {
		field := schema.Field(int(colIdx))
		var arr arrow.Array

		srcCol := src.Column(colIdx)

		// Clone the source validity bitmap. We use Clone rather than copying
		// Bytes directly so that Clone can normalize offsets.
		srcValidity := src.Column(colIdx).Validity()
		clonedValidity := srcValidity.Clone(nil)
		dstValidity, dstOffset := clonedValidity.Bytes()
		if dstOffset != 0 {
			panic("cloned bitmap should be aligned")
		}

		switch field.Type.ID() {
		// 整型列复制 values 缓冲区并保留 null 位图，构造 Int64Array。
case arrow.INT64:
			srcInt64 := srcCol.(*columnar.Number[int64])
			srcBytes := arrow.GetBytes(srcInt64.Values())
			dstBytes := make([]byte, len(srcBytes))
			copy(dstBytes, srcBytes)

			data := array.NewData(
				field.Type,
				int(nrows),
				[]*arrowmemory.Buffer{
					arrowmemory.NewBufferBytes(dstValidity),
					arrowmemory.NewBufferBytes(dstBytes),
				},
				nil,
				srcInt64.Nulls(),
				0,
			)
			arr = array.NewInt64Data(data)

		case arrow.UINT64:
			srcUint64 := srcCol.(*columnar.Number[uint64])
			srcBytes := arrow.GetBytes(srcUint64.Values())
			dstBytes := make([]byte, len(srcBytes))
			copy(dstBytes, srcBytes)

			data := array.NewData(
				field.Type,
				int(nrows),
				[]*arrowmemory.Buffer{
					arrowmemory.NewBufferBytes(dstValidity),
					arrowmemory.NewBufferBytes(dstBytes),
				},
				nil,
				srcUint64.Nulls(),
				0,
			)
			arr = array.NewUint64Data(data)

		// BINARY/STRING 列额外复制 offsets，以支持变长字节与 UTF-8 字符串。
case arrow.BINARY:
			srcBinary := srcCol.(*columnar.UTF8)
			srcBytes := srcBinary.Data()
			dstBytes := make([]byte, len(srcBytes))
			copy(dstBytes, srcBytes)

			srcOffsetsBytes := arrow.GetBytes(srcBinary.Offsets())
			dstOffsetsBytes := make([]byte, len(srcOffsetsBytes))
			copy(dstOffsetsBytes, srcOffsetsBytes)

			data := array.NewData(
				field.Type,
				int(nrows),
				[]*arrowmemory.Buffer{
					arrowmemory.NewBufferBytes(dstValidity),
					arrowmemory.NewBufferBytes(dstOffsetsBytes),
					arrowmemory.NewBufferBytes(dstBytes),
				},
				nil,
				srcBinary.Nulls(),
				0,
			)
			arr = array.NewBinaryData(data)

		case arrow.STRING:
			srcUTF8 := srcCol.(*columnar.UTF8)
			srcBytes := srcUTF8.Data()
			dstBytes := make([]byte, len(srcBytes))
			copy(dstBytes, srcBytes)

			srcOffsetsBytes := arrow.GetBytes(srcUTF8.Offsets())
			dstOffsetsBytes := make([]byte, len(srcOffsetsBytes))
			copy(dstOffsetsBytes, srcOffsetsBytes)

			data := array.NewData(
				field.Type,
				int(nrows),
				[]*arrowmemory.Buffer{
					arrowmemory.NewBufferBytes(dstValidity),
					arrowmemory.NewBufferBytes(dstOffsetsBytes),
					arrowmemory.NewBufferBytes(dstBytes),
				},
				nil,
				srcUTF8.Nulls(),
				0,
			)
			arr = array.NewStringData(data)

		case arrow.TIMESTAMP:
			srcInt64 := srcCol.(*columnar.Number[int64])
			srcBytes := arrow.GetBytes(srcInt64.Values())
			dstBytes := make([]byte, len(srcBytes))
			copy(dstBytes, srcBytes)

			data := array.NewData(
				field.Type,
				int(nrows),
				[]*arrowmemory.Buffer{
					arrowmemory.NewBufferBytes(dstValidity),
					arrowmemory.NewBufferBytes(dstBytes),
				},
				nil,
				srcInt64.Nulls(),
				0,
			)
			arr = array.NewTimestampData(data)

		default:
			return nil, fmt.Errorf("unsupported column type: %v", field.Type)
		}

		arrs = append(arrs, arr)
	}

	return array.NewRecordBatch(schema, arrs, nrows), nil
}
// 不支持的 Arrow 列类型返回错误，避免静默丢弃数据。
