package indexpointers

// indexpointers section 打开与列类型定义：指向 index data object 的路径与时间窗口。

import (
	"context"
	"fmt"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/internal/columnar"
)

var sectionType = dataobj.SectionType{
	Namespace: "github.com/grafana/loki",
	Kind:      "indexpointers",
	Version:   columnar.FormatVersion,
}

// CheckSection 通过 SectionType 命名空间与 kind 判断是否为 indexpointers 段。
// CheckSection returns true if section is a indexpointers section.
func CheckSection(section *dataobj.Section) bool { return sectionType.Equals(section.Type) }

// Section 包装 columnar.Section 并解析为 path/min/max 三列 Column 列表。
// Section represents an opened indexpointers section.
type Section struct {
	inner   *columnar.Section
	columns []*Column
}

// Open 校验类型与版本，解码 columnar 元数据并初始化列描述。
// Open opens a Section from an underlying [dataobj.Section]. Open returns an
// error if the section metadata could not be read or if the provided ctx is
// canceled.
func Open(ctx context.Context, section *dataobj.Section) (*Section, error) {
	if !CheckSection(section) {
		return nil, fmt.Errorf("section type mismatch: got=%s want=%s", section.Type, sectionType)
	} else if section.Type.Version != columnar.FormatVersion {
		return nil, fmt.Errorf("unsupported section version: got=%d want=%d", section.Type.Version, columnar.FormatVersion)
	}

	dec, err := columnar.NewDecoder(section.Reader, section.Type.Version)
	if err != nil {
		return nil, fmt.Errorf("creating decoder: %w", err)
	}

	columnarSection, err := columnar.Open(ctx, section.Tenant, dec)
	if err != nil {
		return nil, fmt.Errorf("opening columnar section: %w", err)
	}

	sec := &Section{inner: columnarSection}
	if err := sec.init(); err != nil {
		return nil, fmt.Errorf("intializing section: %w", err)
	}
	return sec, nil
}

func (s *Section) init() error {
	for _, col := range s.inner.Columns() {
		colType, err := ParseColumnType(col.Type.Logical)
		if err != nil {
			// Skip over unrecognized columns; probably come from a newer
			// version of the code.
			continue
		}

		s.columns = append(s.columns, &Column{
			Section: s,
			Name:    col.Tag,
			Type:    colType,

			inner: col,
		})
	}

	return nil
}

// Columns returns the set of Columns in the section. The slice of returned
// sections must not be mutated.
//
// Unrecognized columns (e.g., when running older code against newer indexpointers
// sections) are skipped.
func (s *Section) Columns() []*Column { return s.columns }

// ColumnType represents the kind of information stored in a [Column].
// ColumnType 枚举 path、min_timestamp、max_timestamp 三类逻辑列。
type ColumnType int

const (
	ColumnTypeInvalid      ColumnType = iota // ColumnTypeInvalid is an invalid column.
	ColumnTypePath                           // ColumnTypePath is a column containing the path to the index object.
	ColumnTypeMinTimestamp                   // ColumnTypeMinTimestamp is a column containing the minimum timestamp of the index object.
	ColumnTypeMaxTimestamp                   // ColumnTypeMaxTimestamp is a column containing the maximum timestamp of the index object.
)

var columnTypeNames = map[ColumnType]string{
	ColumnTypeInvalid:      "invalid",
	ColumnTypePath:         "path",
	ColumnTypeMinTimestamp: "min_timestamp",
	ColumnTypeMaxTimestamp: "max_timestamp",
}

// ParseColumnType parses a [ColumnType] from a string. The expected string
// format is same same as the return value of [ColumnType.String].
func ParseColumnType(text string) (ColumnType, error) {
	switch text {
	case "invalid":
		return ColumnTypeInvalid, nil
	case "path":
		return ColumnTypePath, nil
	case "min_timestamp":
		return ColumnTypeMinTimestamp, nil
	case "max_timestamp":
		return ColumnTypeMaxTimestamp, nil
	}

	return ColumnTypeInvalid, fmt.Errorf("invalid column type %q", text)
}

// String returns the human-readable name of ct.
func (ct ColumnType) String() string {
	text, ok := columnTypeNames[ct]
	if !ok {
		return fmt.Sprintf("ColumnType(%d)", ct)
	}
	return text
}

// Column 关联 Section 与底层 columnar.Column，供 Reader 与 Predicate 引用。
// A Column represents one of the columns in the indexpointers section. Valid columns
// can only be retrieved by calling [Section.Columns].
//
// Data in columns can be read by using a [Reader].
type Column struct {
	Section *Section
	Name    string
	Type    ColumnType

	inner *columnar.Column
}
// 未识别的新版列在 init 时被跳过以保持向前兼容。
