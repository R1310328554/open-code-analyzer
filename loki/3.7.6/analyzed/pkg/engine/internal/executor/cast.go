package executor

// cast 一元算子：将字符串列转换为 float64（数值/时长/字节），失败时附带错误列。

import (
	"fmt"
	"strconv"
	"time"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
	"github.com/apache/arrow-go/v18/arrow/memory"
	"github.com/dustin/go-humanize"

	"github.com/grafana/loki/v3/pkg/engine/internal/semconv"
	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// castFn 返回 UnaryFunction，按 UnaryOp 选择 convertFloat/Duration/Bytes。
func castFn(operation types.UnaryOp) UnaryFunction {
	return UnaryFunc(func(input arrow.Array) (arrow.Array, error) {
		sourceCol, ok := input.(*array.String)
		if !ok {
			return nil, fmt.Errorf("expected column to be of type string, got %T", input)
		}

		// Get conversion function and process values
		conversionFn := getConversionFunction(operation)
		castCol, errTracker := castValues(sourceCol, conversionFn)

		// Build error columns if needed
		errorCol, errorDetailsCol := errTracker.buildArrays()

		// Build output schema and record
		fields := buildValueAndErrorFields(errTracker.hasErrors)
		return buildValueAndErrorStruct(castCol, errorCol, errorDetailsCol, fields)
	})
}

// conversionFn 解析单行字符串；出错时 castValues 写 0.0 并记录 errorTracker。
type conversionFn func(value string) (float64, error)

func getConversionFunction(operation types.UnaryOp) conversionFn {
	switch operation {
	case types.UnaryOpCastBytes:
		return convertBytes
	case types.UnaryOpCastDuration:
		return convertDuration
	default:
		return convertFloat
	}
}

func castValues(
	sourceCol *array.String,
	conversionFn conversionFn,
) (arrow.Array, *errorTracker) {
	castBuilder := array.NewFloat64Builder(memory.DefaultAllocator)

	tracker := newErrorTracker()

	for i := 0; i < sourceCol.Len(); i++ {
		if sourceCol.IsNull(i) {
			castBuilder.AppendNull()
			tracker.recordSuccess()
		} else {
			valueStr := sourceCol.Value(i)
			if val, err := conversionFn(valueStr); err == nil {
				castBuilder.Append(val)
				tracker.recordSuccess()
			} else {
				// Use 0.0 as default for errors, for backwards compatibility with old engine
				castBuilder.Append(0.0)
				tracker.recordError(i, err)
			}
		}
	}

	return castBuilder.NewArray(), tracker
}

// buildValueAndErrorFields 始终含 value 列，有错误时再追加 error 与 error_details。
func buildValueAndErrorFields(
	hasErrors bool,
) []arrow.Field {
	fields := make([]arrow.Field, 0, 3)

	// Add value field. Not nullable in practice since we use 0.0 when conversion fails, but as of
	// writing all coumns are marked as nullable, even Timestamp and Message, so staying consistent
	fields = append(fields, semconv.FieldFromIdent(semconv.ColumnIdentValue, true))

	// Add error fields if needed
	if hasErrors {
		fields = append(fields,
			semconv.FieldFromIdent(semconv.ColumnIdentError, true),
			semconv.FieldFromIdent(semconv.ColumnIdentErrorDetails, true),
		)
	}

	return fields
}

func buildValueAndErrorStruct(
	valVol, errorCol, errorDetailsCol arrow.Array,
	fields []arrow.Field,
) (*array.Struct, error) {
	hasErrors := errorCol != nil

	totalCols := 1
	if hasErrors {
		totalCols += 2
	}

	columns := make([]arrow.Array, totalCols)

	// Add new columns - these are newly created so don't need extra retain
	columns[0] = valVol
	if hasErrors {
		columns[1] = errorCol
		columns[2] = errorDetailsCol
	}

	// NewStructArrayWithFields will retain all columns
	return array.NewStructArrayWithFields(columns, fields)
}

func convertFloat(v string) (float64, error) {
	return strconv.ParseFloat(v, 64)
}

func convertDuration(v string) (float64, error) {
	d, err := time.ParseDuration(v)
	if err != nil {
		return 0, err
	}
	return d.Seconds(), nil
}

func convertBytes(v string) (float64, error) {
	b, err := humanize.ParseBytes(v)
	if err != nil {
		return 0, err
	}
	return float64(b), nil
}

// errorTracker 延迟分配 error/error_details 列，仅在首行转换失败时创建 builder。
type errorTracker struct {
	hasErrors      bool
	errorBuilder   *array.StringBuilder
	detailsBuilder *array.StringBuilder
}

func newErrorTracker() *errorTracker {
	return &errorTracker{}
}

func (et *errorTracker) recordError(rowIndex int, err error) {
	if !et.hasErrors {
		et.errorBuilder = array.NewStringBuilder(memory.DefaultAllocator)
		et.detailsBuilder = array.NewStringBuilder(memory.DefaultAllocator)
		// Backfill empty strings for previous rows
		for range rowIndex {
			et.errorBuilder.Append("")
			et.detailsBuilder.Append("")
		}
		et.hasErrors = true
	}
	et.errorBuilder.Append(types.SampleExtractionErrorType)
	et.detailsBuilder.Append(err.Error())
}

func (et *errorTracker) recordSuccess() {
	if et.hasErrors {
		et.errorBuilder.Append("")
		et.detailsBuilder.Append("")
	}
}

func (et *errorTracker) buildArrays() (arrow.Array, arrow.Array) {
	if !et.hasErrors {
		return nil, nil
	}
	return et.errorBuilder.NewArray(), et.detailsBuilder.NewArray()
}
// 转换失败默认 0.0 与旧引擎行为一致，错误类型为 SampleExtractionErrorType。
