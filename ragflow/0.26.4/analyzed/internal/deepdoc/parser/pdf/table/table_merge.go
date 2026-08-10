
// table_merge.go — 跨页表格合并：将相邻页 X 重叠且 Y 邻近的 TableItem 合并为一张表，尊重 NoMerge 标记。

package table

import (
	"sort"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// MergeTablesAcrossPages 合并连续页 X 重叠且 Y 邻近的 TableItem，对齐 Python _extract_table_figure 表格合并。
func MergeTablesAcrossPages(tables []pdf.TableItem, medianHeights map[int]float64) []pdf.TableItem {
	if len(tables) <= 1 {
		return tables
	}
	// 按页码与 top 排序以保证确定性邻接关系。
	type indexed struct {
		idx int
		pg  int
		top float64
	}
	var items []indexed
	for i, tbl := range tables {
		if len(tbl.Positions) == 0 {
			continue
		}
		p := tbl.Positions[0]
		pg := 0
		if len(p.PageNumbers) > 0 {
			pg = p.PageNumbers[0]
		}
		items = append(items, indexed{i, pg, p.Top})
	}
	sort.Slice(items, func(i, j int) bool {
		if items[i].pg != items[j].pg {
			return items[i].pg < items[j].pg
		}
		return items[i].top < items[j].top
	})

	merged := make([]bool, len(tables))
	var result []pdf.TableItem

	for _, it := range items {
		if merged[it.idx] {
			continue
		}
		anchor := tables[it.idx]
		merged[it.idx] = true

		// Python nomerge_lout_no：其后紧跟 caption/title/reference 的表格禁止跨页合并。
		if anchor.NoMerge {
			result = append(result, anchor)
			continue
		}

		anchorPg := it.pg
		anchorBtm := anchor.Positions[0].Bottom

		// 查找连续页的续表候选。
		for _, jt := range items {
			if merged[jt.idx] || jt.pg <= anchorPg {
				continue
			}
			// Python nomerge_lout_no：跳过已标记 NoMerge 的候选。
			if tables[jt.idx].NoMerge {
				continue
			}
			if jt.pg-anchorPg > 1 {
				break // 页码必须连续
			}
			if len(tables[jt.idx].Positions) == 0 {
				continue
			}
			bp := tables[jt.idx].Positions[0]
			bpg := 0
			if len(bp.PageNumbers) > 0 {
				bpg = bp.PageNumbers[0]
			}
			if bpg != anchorPg+1 {
				continue
			}
			// 检查 X 方向重叠。
			ap := anchor.Positions[0]
			if ap.Right < bp.Left || bp.Right < ap.Left {
				continue
			}
			// 检查 Y 邻近：续表 top 应靠近前表 bottom，Python：y_dis <= mh * 23。
			mh := 10.0
			if medianHeights != nil {
				if h, ok := medianHeights[anchorPg]; ok && h > 0 {
					mh = h
				}
			}
			yDis := (bp.Top + bp.Bottom - anchorBtm - ap.Bottom) / 2
			if yDis > mh*23 {
				continue
			}
			// 合并：拼接 cells 与 positions，合并 caption。
			anchor.Cells = append(anchor.Cells, tables[jt.idx].Cells...)
			anchor.Positions = append(anchor.Positions, tables[jt.idx].Positions...)
			if tables[jt.idx].Caption != "" {
				if anchor.Caption != "" {
					anchor.Caption += " "
				}
				anchor.Caption += tables[jt.idx].Caption
			}
			merged[jt.idx] = true
			anchorPg = bpg
			anchorBtm = bp.Bottom
			ap = anchor.Positions[len(anchor.Positions)-1]
		}
		result = append(result, anchor)
	}
	// 追加 Positions 为空的未处理表，避免静默丢失。
	for i := range tables {
		if !merged[i] {
			result = append(result, tables[i])
		}
	}
	return result
}

