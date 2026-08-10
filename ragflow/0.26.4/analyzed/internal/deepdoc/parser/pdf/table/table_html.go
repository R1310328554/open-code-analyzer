// table_html.go — 表格行到 HTML 的序列化：支持 colspan/rowspan、caption、表头行标记，以及 DOCX/XLSX 等格式的简易 [][]string 转换。

package table

import (
	"fmt"
	"html"
	"strings"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// RowsToHTML 将 TSR 单元格行转为 HTML 表格，支持 caption、th/td、colspan/rowspan 与 covered 跳过。
func RowsToHTML(rows [][]pdf.TSRCell, caption string, headerRows map[int]bool, spanInfo map[[2]int][2]int, covered map[[2]int]bool) string {
	var b strings.Builder
	b.WriteString("<table>")
	if caption != "" {
		b.WriteString("<caption>")
		b.WriteString(html.EscapeString(caption))
		b.WriteString("</caption>")
	}
	for ri, row := range rows {
		b.WriteString("<tr>")
		for ci, cell := range row {
			if covered[[2]int{ri, ci}] { continue }
			tag := "td"
			if headerRows[ri] { tag = "th" }
			b.WriteString("<")
			b.WriteString(tag)
			sp := ""
			if s, ok := spanInfo[[2]int{ri, ci}]; ok {
				if s[0] > 1 {
					sp = fmt.Sprintf("colspan=%d", s[0])
				}
				if s[1] > 1 {
					if sp != "" { sp += " " }
					sp += fmt.Sprintf("rowspan=%d", s[1])
				}
			}
			if sp != "" {
				b.WriteString(" ")
				b.WriteString(sp)
			}
			b.WriteString(" >")
			b.WriteString(html.EscapeString(cell.Text))
			b.WriteString("</")
			b.WriteString(tag)
			b.WriteString(">")
		}
		b.WriteString("</tr>")
	}
	b.WriteString("</table>")
	return b.String()
}

// SimpleRowsToHTML 将 [][]string 转为 HTML 表格，首行作为表头；供 DOCX/XLSX/PPTX/HTML 解析器使用。
func SimpleRowsToHTML(rows [][]string) string {
	if len(rows) == 0 {
		return "<table></table>"
	}
	nCols := 0
	for _, row := range rows {
		if len(row) > nCols { nCols = len(row) }
	}
	var b strings.Builder
	b.WriteString("<table>")
	for ri, row := range rows {
		b.WriteString("<tr>")
		tag := "td"
		if ri == 0 { tag = "th" }
		for ci := 0; ci < nCols; ci++ {
			text := ""
			if ci < len(row) { text = row[ci] }
			b.WriteString("<")
			b.WriteString(tag)
			b.WriteString(" >")
			b.WriteString(html.EscapeString(text))
			b.WriteString("</")
			b.WriteString(tag)
			b.WriteString(">")
		}
		b.WriteString("</tr>")
	}
	b.WriteString("</table>")
	return b.String()
}

// RowsToStrings 将 TSR 单元格网格转为纯文字二维数组。
func RowsToStrings(rows [][]pdf.TSRCell) [][]string {
	out := make([][]string, len(rows))
	for ri, row := range rows {
		out[ri] = make([]string, len(row))
		for ci, c := range row {
			out[ri][ci] = c.Text
		}
	}
	return out
}

// HasText 判断行网格中是否存在非空单元格文字。
func HasText(rows [][]pdf.TSRCell) bool {
	for _, row := range rows {
		for _, c := range row {
			if strings.TrimSpace(c.Text) != "" { return true }
		}
	}
	return false
}

// HasAnyText 判断单元格切片中是否存在非空文字。
func HasAnyText(cells []pdf.TSRCell) bool {
	for _, c := range cells {
		if strings.TrimSpace(c.Text) != "" { return true }
	}
	return false
}
