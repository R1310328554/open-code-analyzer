// compare.go — Go 与 Python PDF 管道输出对比：逐 PDF 统计 boxes/sections/文本相似度，生成 CSV/Excel 报告及 DLA/TSR/表格中间结果对比。

package tool

import (
	"encoding/csv"
	"encoding/json"
	"fmt"
	"math"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/xuri/excelize/v2"
	"golang.org/x/text/unicode/norm"
)

// Diff 存储单个 PDF 的 Go 与 Python 输出对比指标。
type Diff struct {
	File             string
	PagesOk          bool
	BoxesInitDiffPct float64
	BoxesTMDiffPct   float64
	BoxesVMDiffPct   float64
	SectionsDiffPct  float64
	TextLenDiffPct   float64
	CharsDiffPct     float64
	TablesDiff       int
	CharSim          float64
	LcsSim           float64
	RawCharSim       float64 // CharSim without NFKC normalization
	RawLcsSim        float64 // LcsSim without space stripping
}

// CompareWithPython 将 Go 批处理结果与 Python 参考输出逐项对比并打印汇总。
func CompareWithPython(log TLogger, goResults []BatchResult, pyResults []PyResult, goTextDir, pyTextDir string) {
	pyMap := make(map[string]PyResult, len(pyResults))
	for _, pr := range pyResults {
		pyMap[pr.File] = pr
	}
	goMap := make(map[string]BatchResult, len(goResults))
	for _, r := range goResults {
		goMap[r.File] = r
	}

	var diffs []Diff
	matched, mismatched := 0, 0

	for _, r := range goResults {
		py, ok := pyMap[r.File]
		if !ok {
			continue
		}
		d := Diff{File: r.File, TablesDiff: r.TSTables - py.Tables}
		if py.Pages > 0 {
			d.PagesOk = r.Pages == py.Pages
			if r.Pages == py.Pages {
				matched++
			} else {
				mismatched++
			}
		}
		if py.BoxesInitial > 0 {
			d.BoxesInitDiffPct = math.Abs(float64(r.BoxesInitial-py.BoxesInitial)) / float64(py.BoxesInitial) * 100
		}
		if py.BoxesTextMerge > 0 {
			d.BoxesTMDiffPct = math.Abs(float64(r.BoxesTextMerg-py.BoxesTextMerge)) / float64(py.BoxesTextMerge) * 100
		}
		if py.BoxesVertMerge > 0 {
			d.BoxesVMDiffPct = math.Abs(float64(r.BoxesVertMerg-py.BoxesVertMerge)) / float64(py.BoxesVertMerge) * 100
		}
		if py.Sections > 0 {
			d.SectionsDiffPct = math.Abs(float64(r.Sections-py.Sections)) / float64(py.Sections) * 100
		}
		if py.TextLen > 0 {
			d.TextLenDiffPct = math.Abs(float64(r.TextLen-py.TextLen)) / float64(py.TextLen) * 100
		}
		if py.Chars > 0 {
			d.CharsDiffPct = math.Abs(float64(r.Chars-py.Chars)) / float64(py.Chars) * 100
		}

		goTextPath := filepath.Join(goTextDir, r.File+".txt")
		pyTextPath := filepath.Join(pyTextDir, r.File+".txt")
		if goTxt, err := os.ReadFile(goTextPath); err == nil {
			if pyTxt, err := os.ReadFile(pyTextPath); err == nil {
				goStr, pyStr := string(goTxt), string(pyTxt)
				// NFKC 归一化：全角→半角（如「，（」→「,(」）
				goStr = norm.NFKC.String(goStr)
				pyStr = norm.NFKC.String(pyStr)
				d.CharSim = CharSimilarity(goStr, pyStr)
				// 段落级 LCS：按位置窗口对齐段落，逐段 LCS 再双向 F1。
				d.LcsSim = SectionAlignedScore(goStr, pyStr)
				// 原始指标：不做 NFKC/去空格。
				d.RawCharSim = RawCharSimilarity(string(goTxt), string(pyTxt))
				d.RawLcsSim = SectionAlignedScore(string(goTxt), string(pyTxt))
			}
		}
		diffs = append(diffs, d)
		log.Logf("  [%d/%d] %s CharDiff=D%.1f%% LcsDiff=D%.1f%% RawCharDiff=D%.1f%% RawLcsDiff=D%.1f%%",
			len(diffs), len(goResults), r.File, 100-d.CharSim, 100-d.LcsSim, 100-d.RawCharSim, 100-d.RawLcsSim)
	}

	sort.Slice(diffs, func(i, j int) bool { return diffs[i].SectionsDiffPct < diffs[j].SectionsDiffPct })

	log.Logf("\n=== Go vs Python (%d PDFs) ===", len(diffs))
	log.Logf("Pages match: %d/%d", matched, matched+mismatched)
	log.Logf("%-40s %-18s %-18s %s %s %s %s %s %s %s %s %s %s",
		"file", "Go:init->tm->vm->sec", "Py:init->tm->vm->sec",
		"Init%", "TM%", "VM%", "Sec%", "Txt%", "TabD", "CharDiff%", "LcsDiff%", "RawCharDiff%", "RawLcsDiff%")
	log.Logf("%s", strings.Repeat("-", 168))

	for _, d := range diffs {
		py := pyMap[d.File]
		gr := goMap[d.File]
		goStages := fmt.Sprintf("%3d->%3d->%3d->%3d", gr.BoxesInitial, gr.BoxesTextMerg, gr.BoxesVertMerg, gr.Sections)
		pyStages := fmt.Sprintf("%3d->%3d->%3d->%3d", py.BoxesInitial, py.BoxesTextMerge, py.BoxesVertMerge, py.Sections)
		log.Logf("%-40s %-18s %-18s %4.0f%% %4.0f%% %4.0f%% %4.0f%% %4.0f%% %+4d %.0f%% %.0f%% %.0f%% %.0f%%",
			d.File, goStages, pyStages,
			d.BoxesInitDiffPct, d.BoxesTMDiffPct, d.BoxesVMDiffPct,
			d.SectionsDiffPct, d.TextLenDiffPct, d.TablesDiff,
			100-d.CharSim, 100-d.LcsSim,
			100-d.RawCharSim, 100-d.RawLcsSim)
	}

	n := len(diffs)
	if n == 0 {
		return
	}

	type stats struct {
		median, mean, max, min float64
		over5, over10          int
	}
	computeStats := func(get func(Diff) float64) stats {
		sort.Slice(diffs, func(i, j int) bool { return get(diffs[i]) < get(diffs[j]) })
		s := stats{min: 1e9}
		if n%2 == 0 {
			s.median = (get(diffs[n/2-1]) + get(diffs[n/2])) / 2
		} else {
			s.median = get(diffs[n/2])
		}
		var sum float64
		for _, d := range diffs {
			v := get(d)
			sum += v
			if v > s.max {
				s.max = v
			}
			if v < s.min {
				s.min = v
			}
			if v > 5 {
				s.over5++
			}
			if v > 10 {
				s.over10++
			}
		}
		s.mean = sum / float64(n)
		return s
	}

	label := func(name string, s stats) string {
		return fmt.Sprintf("%s Med=%.1f%% Mean=%.1f%% Min=%.0f%% Max=%.0f%% >5%%:%d >10%%:%d",
			name, s.median, s.mean, s.min, s.max, s.over5, s.over10)
	}

	log.Logf("\nSummary (n=%d):", n)
	log.Logf("  %s", label("BoxesInit ", computeStats(func(d Diff) float64 { return d.BoxesInitDiffPct })))
	log.Logf("  %s", label("TextMerge", computeStats(func(d Diff) float64 { return d.BoxesTMDiffPct })))
	log.Logf("  %s", label("VertMerge", computeStats(func(d Diff) float64 { return d.BoxesVMDiffPct })))
	log.Logf("  %s", label("Sections ", computeStats(func(d Diff) float64 { return d.SectionsDiffPct })))
	log.Logf("  %s", label("TextLen  ", computeStats(func(d Diff) float64 { return d.TextLenDiffPct })))
	log.Logf("  %s", label("CharDiff  ", computeStats(func(d Diff) float64 { return 100 - d.CharSim })))
	log.Logf("  %s", label("LcsDiff   ", computeStats(func(d Diff) float64 { return 100 - d.LcsSim })))
	log.Logf("  %s", label("RawCharDiff", computeStats(func(d Diff) float64 { return 100 - d.RawCharSim })))
	log.Logf("  %s", label("RawLcsDiff ", computeStats(func(d Diff) float64 { return 100 - d.RawLcsSim })))

	// 自动生成带时间戳的 xlsx 报告。
	mode := filepath.Base(filepath.Dir(goTextDir)) // 通常为 "ocr"
	ts := time.Now().Format("20060102_1504")
	xlsxDir := filepath.Join("testdata", "output")
	os.MkdirAll(xlsxDir, 0755)
	xlsxPath := filepath.Join(xlsxDir, fmt.Sprintf("compare_%s_%s.xlsx", mode, ts))
	if err := WriteExcel(xlsxPath, diffs); err != nil {
		log.Logf("Excel write error: %v", err)
	} else {
		log.Logf("Excel report: %s", xlsxPath)
	}

	// 若设置 BATCH_CSV 环境变量则另写 CSV（向后兼容）。
	if csvPath := os.Getenv("BATCH_CSV"); csvPath != "" {
		if err := WriteCSV(csvPath, diffs); err != nil {
			log.Logf("CSV write error: %v", err)
		} else {
			log.Logf("CSV written to %s", csvPath)
		}
	}
}

// WriteCSV 用 encoding/csv 写入对比结果，正确转义含逗号/引号的文件名。
func WriteCSV(path string, diffs []Diff) error {
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()

	w := csv.NewWriter(f)
	defer w.Flush()

	if err := w.Write([]string{"file", "init%", "tm%", "vm%", "sec%", "txt%", "tabsD", "chrdiff%", "lcsdiff%", "rawChr%", "rawLcs%"}); err != nil {
		return err
	}
	for _, d := range diffs {
		row := []string{
			d.File,
			strconv.FormatFloat(d.BoxesInitDiffPct, 'f', 1, 64),
			strconv.FormatFloat(d.BoxesTMDiffPct, 'f', 1, 64),
			strconv.FormatFloat(d.BoxesVMDiffPct, 'f', 1, 64),
			strconv.FormatFloat(d.SectionsDiffPct, 'f', 1, 64),
			strconv.FormatFloat(d.TextLenDiffPct, 'f', 1, 64),
			strconv.Itoa(d.TablesDiff),
			strconv.FormatFloat(100-d.CharSim, 'f', 1, 64),
			strconv.FormatFloat(100-d.LcsSim, 'f', 1, 64),
			strconv.FormatFloat(100-d.RawCharSim, 'f', 1, 64),
			strconv.FormatFloat(100-d.RawLcsSim, 'f', 1, 64),
		}
		if err := w.Write(row); err != nil {
			return err
		}
	}
	w.Flush()
	return w.Error()
}

// WriteExcel 写入带条件着色的 xlsx 对比报告。
func WriteExcel(path string, diffs []Diff) error {
	f := excelize.NewFile()
	defer f.Close()
	sheet := "Comparison"
	f.SetSheetName("Sheet1", sheet)

	// 定义表头/绿黄红条件格式样式。
	headerStyle, _ := f.NewStyle(&excelize.Style{
		Font:      &excelize.Font{Bold: true},
		Fill:      excelize.Fill{Type: "pattern", Pattern: 1, Color: []string{"D9E1F2"}},
		Alignment: &excelize.Alignment{Horizontal: "center"},
	})
	greenStyle, _ := f.NewStyle(&excelize.Style{
		Fill:   excelize.Fill{Type: "pattern", Pattern: 1, Color: []string{"C6EFCE"}},
		NumFmt: 2,
	})
	yellowStyle, _ := f.NewStyle(&excelize.Style{
		Fill:   excelize.Fill{Type: "pattern", Pattern: 1, Color: []string{"FFEB9C"}},
		NumFmt: 2,
	})
	redStyle, _ := f.NewStyle(&excelize.Style{
		Fill:   excelize.Fill{Type: "pattern", Pattern: 1, Color: []string{"FFC7CE"}},
		NumFmt: 2,
	})

	// 写入表头行。
	headers := []string{"File", "Init%", "TM%", "VM%", "Sec%", "Txt%", "TabsD", "ChrDiff%", "LcsDiff%"}
	for i, h := range headers {
		cell, _ := excelize.CoordinatesToCellName(i+1, 1)
		f.SetCellValue(sheet, cell, h)
		f.SetCellStyle(sheet, cell, cell, headerStyle)
	}

	// 写入数据行。
	for row, d := range diffs {
		r := row + 2 // 1-indexed, skip header
		vals := []float64{
			0, // placeholder for file
			d.BoxesInitDiffPct, d.BoxesTMDiffPct, d.BoxesVMDiffPct,
			d.SectionsDiffPct, d.TextLenDiffPct, float64(d.TablesDiff),
			100 - d.CharSim, 100 - d.LcsSim,
		}

		// A 列：文件名。
		f.SetCellValue(sheet, cellName(1, r), d.File)

		// B-I 列：数值指标。
		for col := 2; col <= 9; col++ {
			cell := cellName(col, r)
			v := vals[col-1]
			f.SetCellValue(sheet, cell, v)
			// 着色：绿<5%、黄 5-20%、红≥20%（TabsD 为计数不着色）。
			if col == 7 { // TabsD 为计数非百分比，不着色
				continue
			}
			abs := math.Abs(v)
			switch {
			case abs < 5:
				f.SetCellStyle(sheet, cell, cell, greenStyle)
			case abs < 20:
				f.SetCellStyle(sheet, cell, cell, yellowStyle)
			default:
				f.SetCellStyle(sheet, cell, cell, redStyle)
			}
		}
	}

	// 设置列宽。
	f.SetColWidth(sheet, "A", "A", 45)
	f.SetColWidth(sheet, "B", "I", 12)

	// 冻结首行。
	f.SetPanes(sheet, &excelize.Panes{
		Freeze:      true,
		Split:       false,
		XSplit:      0,
		YSplit:      1,
		TopLeftCell: "A2",
		ActivePane:  "bottomLeft",
	})

	return f.SaveAs(path)
}

func cellName(col, row int) string {
	s, _ := excelize.CoordinatesToCellName(col, row)
	return s
}

// CompareTablesWithPython 含逐单元格文本对比。
func CompareTablesWithPython(log TLogger, goTablesDir, pyTablesDir string) {
	goEntries, err := os.ReadDir(goTablesDir)
	if err != nil {
		log.Logf("Tables compare: no Go tables dir %s", goTablesDir)
		return
	}

	type goTable struct {
		Rows [][]string `json:"rows"`
	}
	type pyCell struct {
		X0     float64 `json:"x0"`
		X1     float64 `json:"x1"`
		Top    float64 `json:"top"`
		Bottom float64 `json:"bottom"`
		Text   string  `json:"text"`
		Page   int     `json:"page"`
	}
	type pyResult struct {
		Cells []pyCell   `json:"cells"`
		Page  int        `json:"page"`
		Rows  [][]string `json:"rows"`
	}
	type pyFile struct {
		Tables  int        `json:"tables"`
		Results []pyResult `json:"results"`
	}

	matched, tableDiffs, cellDiffs, textMismatches := 0, 0, 0, 0
	totalCellsCompared, totalCellsMatched := 0, 0

	log.Logf("\n=== Table Comparison (Go vs Python) ===")
	log.Logf("%-40s %6s %6s %6s %6s %8s %s",
		"file", "GoTbl", "PyTbl", "GoCel", "PyCel", "TxtMatch", "Result")
	log.Logf("%s", strings.Repeat("-", 100))

	for _, e := range goEntries {
		if e.IsDir() || !strings.HasSuffix(e.Name(), ".json") {
			continue
		}

		goPath := filepath.Join(goTablesDir, e.Name())
		pyPath := filepath.Join(pyTablesDir, e.Name())
		if !FileExists(pyPath) {
			continue
		}

		// 读取 Go 侧表格 JSON。
		goData, _ := os.ReadFile(goPath)
		var goTables []goTable
		if err := json.Unmarshal(goData, &goTables); err != nil {
			log.Logf("  %s: Go JSON parse error: %v", e.Name(), err)
			continue
		}

		// 读取 Python 侧表格 JSON。
		pyData, _ := os.ReadFile(pyPath)
		var pyF pyFile
		if err := json.Unmarshal(pyData, &pyF); err != nil {
			log.Logf("  %s: Py JSON parse error: %v", e.Name(), err)
			continue
		}

		matched++

		// 统计单元格总数。
		goTotalCells := 0
		for _, t := range goTables {
			for _, row := range t.Rows {
				goTotalCells += len(row)
			}
		}
		pyTotalCells := 0
		for _, r := range pyF.Results {
			if len(r.Cells) > 0 {
				pyTotalCells += len(r.Cells)
			} else {
				for _, row := range r.Rows {
					pyTotalCells += len(row)
				}
			}
		}

		// 逐表逐行逐单元格文本比对。
		cellsCompared, cellsMatched := 0, 0
		nTables := min(len(goTables), len(pyF.Results))
		for ti := 0; ti < nTables; ti++ {
			goRows := goTables[ti].Rows
			pyRows := pyF.Results[ti].Rows
			nRows := min(len(goRows), len(pyRows))
			for ri := 0; ri < nRows; ri++ {
				nCols := min(len(goRows[ri]), len(pyRows[ri]))
				for ci := 0; ci < nCols; ci++ {
					cellsCompared++
					if strings.TrimSpace(goRows[ri][ci]) == strings.TrimSpace(pyRows[ri][ci]) {
						cellsMatched++
					}
				}
			}
		}

		totalCellsCompared += cellsCompared
		totalCellsMatched += cellsMatched

		// 汇总匹配状态 emoji。
		status := "✅"
		txtMatch := ""
		if len(goTables) != len(pyF.Results) {
			tableDiffs++
			status = "❌ tables"
		}
		if goTotalCells != pyTotalCells {
			cellDiffs++
			if status == "✅" {
				status = "⚠️ cells"
			}
		}
		if cellsCompared > 0 {
			pct := float64(cellsMatched) / float64(cellsCompared) * 100
			txtMatch = fmt.Sprintf("%.0f%%", pct)
			if pct < 100 && status == "✅" {
				status = "⚠️ text"
				textMismatches++
			}
			if pct < 100 && status != "✅" {
				textMismatches++
			}
		} else {
			txtMatch = "-"
		}

		name := strings.TrimSuffix(e.Name(), ".json")
		log.Logf("%-40s %6d %6d %6d %6d %8s %s",
			name, len(goTables), len(pyF.Results), goTotalCells, pyTotalCells, txtMatch, status)
	}

	if matched == 0 {
		log.Logf("No matching table files found")
		return
	}

	txtPct := 0.0
	if totalCellsCompared > 0 {
		txtPct = float64(totalCellsMatched) / float64(totalCellsCompared) * 100
	}
	log.Logf("\nTable Summary: %d PDFs, %d table diffs, %d cell diffs, %d text mismatches",
		matched, tableDiffs, cellDiffs, textMismatches)
	log.Logf("Cell text match: %d/%d (%.1f%%)", totalCellsMatched, totalCellsCompared, txtPct)
}

// ── DLA 中间结果对比 ──

type jsonDlaPage struct {
	Page    int             `json:"page"`
	Regions []jsonDlaRegion `json:"regions"`
}
type jsonDlaRegion struct {
	Label string  `json:"label"` // Go 使用 "label" 字段
	Type  string  `json:"type"`  // Python 使用 "type" 字段
	X0    float64 `json:"x0"`
	Y0    float64 `json:"y0"`
	X1    float64 `json:"x1"`
	Y1    float64 `json:"y1"`
}

// CompareDLAWithPython 对比逐页 DLA layout 区域，两目录均为 {pdf}.json。
func CompareDLAWithPython(log TLogger, goDLADir, pyDLADir string) {
	goEntries, _ := os.ReadDir(goDLADir)
	pyEntries, _ := os.ReadDir(pyDLADir)
	pySet := map[string]bool{}
	for _, e := range pyEntries {
		pySet[e.Name()] = true
	}

	matched := 0
	log.Logf("\n=== DLA Comparison (Go vs Python) ===")
	log.Logf("%-40s %6s %6s %6s %6s %6s",
		"file", "GoPg", "PyPg", "GoReg", "PyReg", "TblReg")
	log.Logf("%s", strings.Repeat("-", 80))

	for _, e := range goEntries {
		if !strings.HasSuffix(e.Name(), ".json") || !pySet[e.Name()] {
			continue
		}
		goData, _ := os.ReadFile(filepath.Join(goDLADir, e.Name()))
		pyData, _ := os.ReadFile(filepath.Join(pyDLADir, e.Name()))

		var goPages []jsonDlaPage
		json.Unmarshal(goData, &goPages)
		var pyPages []jsonDlaPage
		json.Unmarshal(pyData, &pyPages)

		matched++
		goRegions, pyRegions := 0, 0
		goTables, pyTables := 0, 0
		for _, p := range goPages {
			goRegions += len(p.Regions)
			for _, r := range p.Regions {
				if dlaRegionIsTable(r) {
					goTables++
				}
			}
		}
		for _, p := range pyPages {
			pyRegions += len(p.Regions)
			for _, r := range p.Regions {
				if dlaRegionIsTable(r) {
					pyTables++
				}
			}
		}

		name := strings.TrimSuffix(e.Name(), ".json")
		log.Logf("%-40s %6d %6d %6d %6d %6d",
			name, len(goPages), len(pyPages), goRegions, pyRegions, goTables-pyTables)
	}
	if matched == 0 {
		log.Logf("No matching DLA files found (go=%s py=%s)", goDLADir, pyDLADir)
	}
}

// ── TSR 原始中间结果对比 ──

type tsrRawCell struct {
	TableIndex int     `json:"table_index"`
	Page       int     `json:"page"`
	Label      string  `json:"label"`
	X0         float64 `json:"x0"`
	Y0         float64 `json:"y0"`
	X1         float64 `json:"x1"`
	Y1         float64 `json:"y1"`
	Text       string  `json:"text"`
}

// CompareTSRRawWithPython 对比各表原始 TSR 单元格与 label 差异。
func CompareTSRRawWithPython(log TLogger, goTSRDir, pyTSRDir string) {
	goEntries, _ := os.ReadDir(goTSRDir)
	pyEntries, _ := os.ReadDir(pyTSRDir)
	pySet := map[string]bool{}
	for _, e := range pyEntries {
		pySet[e.Name()] = true
	}

	matched := 0
	totalDiffs := 0
	log.Logf("\n=== TSR Raw Comparison (Go vs Python) ===")
	log.Logf("%-40s %6s %6s %8s %8s %6s",
		"file", "GoTbl", "PyTbl", "GoCell", "PyCell", "LabelD")
	log.Logf("%s", strings.Repeat("-", 85))

	for _, e := range goEntries {
		if !strings.HasSuffix(e.Name(), ".json") || !pySet[e.Name()] {
			continue
		}
		goData, _ := os.ReadFile(filepath.Join(goTSRDir, e.Name()))
		pyData, _ := os.ReadFile(filepath.Join(pyTSRDir, e.Name()))

		var goCells []tsrRawCell
		json.Unmarshal(goData, &goCells)
		var pyCells []tsrRawCell
		json.Unmarshal(pyData, &pyCells)

		// 按 table_index 分组。
		goByTable := map[int][]tsrRawCell{}
		pyByTable := map[int][]tsrRawCell{}
		for _, c := range goCells {
			goByTable[c.TableIndex] = append(goByTable[c.TableIndex], c)
		}
		for _, c := range pyCells {
			pyByTable[c.TableIndex] = append(pyByTable[c.TableIndex], c)
		}

		matched++
		labelDiffs := 0
		goTotal, pyTotal := len(goCells), len(pyCells)
		for ti := range goByTable {
			goTab := goByTable[ti]
			pyTab := pyByTable[ti]
			n := min(len(goTab), len(pyTab))
			for i := 0; i < n; i++ {
				if goTab[i].Label != pyTab[i].Label {
					labelDiffs++
				}
			}
			labelDiffs += abs(len(goTab) - len(pyTab))
		}
		if labelDiffs > 0 {
			totalDiffs++
		}

		name := strings.TrimSuffix(e.Name(), ".json")
		log.Logf("%-40s %6d %6d %8d %8d %6d",
			name, len(goByTable), len(pyByTable), goTotal, pyTotal, labelDiffs)
	}
	if matched == 0 {
		log.Logf("No matching TSR raw files found (go=%s py=%s)", goTSRDir, pyTSRDir)
	} else {
		log.Logf("TSR Raw Summary: %d PDFs, %d with label diffs", matched, totalDiffs)
	}
}

func dlaRegionIsTable(r jsonDlaRegion) bool {
	label := r.Label
	if label == "" {
		label = r.Type
	}
	return label == "table"
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}
