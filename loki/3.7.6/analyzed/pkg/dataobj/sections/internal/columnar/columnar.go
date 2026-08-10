// columnar 包为基于 dataset 的列式区段提供通用的打开、列描述与排序元数据访问。
// Package columnar provides a base implementation for sections which store
// columnar data using
// [github.com/grafana/loki/v3/pkg/dataobj/internal/dataset].
package columnar

import (
	"context"
	"fmt"

	"github.com/grafana/loki/v3/pkg/dataobj/internal/dataset"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/metadata/datasetmd"
)

// Section 表示已打开的列式区段，持有解码器、列列表与排序信息。
// Section represents an opened dataset-based section.
type Section struct {
	tenant  string
	decoder *Decoder

	columns  []*Column
	sortInfo *datasetmd.SortInfo
}

// Open 从 Decoder 读取区段元数据并构建 Column 描述列表。
// Open opens a [Section] from an underlying [Decoder].
//
// Open returns an error if the section metadata couldn't be read.
func Open(ctx context.Context, tenant string, dec *Decoder) (*Section, error) {
	sec := &Section{
		tenant:  tenant,
		decoder: dec,
	}
	if err := sec.init(ctx); err != nil {
		return nil, fmt.Errorf("intializing section: %w", err)
	}
	return sec, nil
}

func (s *Section) init(ctx context.Context) error {
	sectionMetadata, err := s.decoder.SectionMetadata(ctx)
	if err != nil {
		return fmt.Errorf("decoding metadata: %w", err)
	}

	for _, col := range sectionMetadata.GetColumns() {
		s.columns = append(s.columns, &Column{
			Section: s,
			Type: dataset.ColumnType{
				Physical: col.Type.Physical,
				Logical:  sectionMetadata.Dictionary[col.Type.LogicalRef],
			},
			Tag: sectionMetadata.Dictionary[col.TagRef],

			desc: col,
		})
	}

	s.sortInfo = sectionMetadata.SortInfo
	return nil
}

// Decoder returns the decoder used to decode the section.
func (s *Section) Decoder() *Decoder { return s.decoder }

// Tenant returns the tenant that owns the section.
func (s *Section) Tenant() string { return s.tenant }

// Columns returns the set of Columns in the section. The slice of returned
// sections must not be mutated.
func (s *Section) Columns() []*Column { return s.columns }

// PrimarySortOrder 返回主排序列的类型与方向，缺失时返回错误。
// PrimarySortOrder returns the primary sort order information of the section
// as a tuple of [ColumnType] and [SortDirection].
func (s *Section) PrimarySortOrder() (dataset.ColumnType, datasetmd.SortDirection, error) {
	if s.sortInfo == nil || len(s.sortInfo.ColumnSorts) == 0 {
		return dataset.ColumnType{}, datasetmd.SORT_DIRECTION_UNSPECIFIED, fmt.Errorf("missing sort order information")
	}

	si := s.sortInfo.ColumnSorts[0] // primary sort order
	idx := int(si.ColumnIndex)
	if idx < 0 || idx >= len(s.columns) {
		return dataset.ColumnType{}, datasetmd.SORT_DIRECTION_UNSPECIFIED, fmt.Errorf("invalid column reference in sort info")
	}

	return s.columns[idx].Type, si.Direction, nil
}

// Column 封装单列的类型、标签与 protobuf 列描述，供 Reader 解码使用。
// A Column represents one of the columns in the section. Valid columns can only
// be retrieved by calling [Section.Columns].
//
// Data in columns can be read by using a [Reader].
type Column struct {
	Section *Section           // Section that contains this Column.
	Type    dataset.ColumnType // Type of the column.
	Tag     string             // Optional tag of the column.

	desc *datasetmd.ColumnDesc // Column description used for further decoding and reading.
}

// Metadata returns the protobuf metadata of the column.
func (c *Column) Metadata() *datasetmd.ColumnDesc { return c.desc }
// 各具体区段类型（logs、indexpointers 等）均复用此 columnar 基础设施。
