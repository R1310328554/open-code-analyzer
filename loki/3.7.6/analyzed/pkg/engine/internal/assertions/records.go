package assertions

// Arrow RecordBatch 断言：检测重复列名与 LogQL 结果标签短名冲突。

import (
	"fmt"

	"github.com/apache/arrow-go/v18/arrow"

	"github.com/grafana/loki/v3/pkg/engine/internal/semconv"
)

// CheckColumnDuplicates 遍历 schema 全名，发现重复列名即 panic。
// CheckColumnDuplicates checks for duplicate full column names in the record.
func CheckColumnDuplicates(record arrow.RecordBatch) {
	if !Enabled {
		return
	}

	if record == nil {
		return
	}

	seen := make(map[string]struct{})
	for _, f := range record.Schema().Fields() {
		if _, ok := seen[f.Name]; ok {
			panic(fmt.Sprintf("duplicate column name: %s", f.Name))
		}
		seen[f.Name] = struct{}{}
	}
}

// CheckLabelValuesDuplicates 按 semconv 短名分组，同行多非空值则 panic。
// CheckLabelValuesDuplicates checks duplicate short column names in the record and that only one value is present.
// Short column names are used as labels in the LogQL result. Duplicate short names will collapse into a single label,
// therefore only one value is allowed. It is valid to have multiple columns with the same short name, but different
// full names. This happens after Compat.
func CheckLabelValuesDuplicates(record arrow.RecordBatch) {
	if !Enabled {
		return
	}

	if record == nil {
		return
	}

	cols := make(map[string][]int)
	for i, f := range record.Schema().Fields() {
		ident, err := semconv.ParseFQN(f.Name)
		if err != nil {
			continue
		}
		cols[ident.ShortName()] = append(cols[ident.ShortName()], i)
	}

	for s, idxs := range cols {
		if len(idxs) > 1 {
			for i := range record.NumRows() {
				values := 0
				for _, j := range idxs {
					if !record.Column(j).IsNull(int(i)) && record.Column(j).IsValid(int(i)) && record.Column(j).ValueStr(int(i)) != "" {
						values++
					}
				}
				if values > 1 {
					panic(fmt.Sprintf("duplicate label values: %s=%s", s, record.Column(idxs[0]).ValueStr(int(i))))
				}
			}
		}
	}
}
// Compat 后允许多列共享短名但全名不同，此时仅校验同行有效值不超过一个。
