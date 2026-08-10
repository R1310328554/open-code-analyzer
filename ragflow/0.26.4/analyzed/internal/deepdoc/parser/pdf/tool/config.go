// config.go — PDF 批处理/对比工具配置：从环境变量加载 BATCH_* 参数、输出路径与 OCR 跳过等开关。

package tool

import (
	"fmt"
	"os"
	"path/filepath"
	"strconv"
	"time"
)

type Config struct {
	Count         int // 批处理 PDF 数量上限（BATCH_COUNT）
	Single        string // 单文件模式：仅处理指定 PDF 名（BATCH_SINGLE）
	SkipOCR       bool // 跑 DLA+TSR 但跳过图像 OCR
	CompareOnly   bool // 仅对比不跑 Go 管道（BATCH_COMPARE_ONLY）
	CompareFilter string // 对比文件名过滤子串（BATCH_COMPARE_FILTER）
	CSVOutput     string // CSV 报告输出路径
	GoTextDir     string // Go 管道输出 txt 目录
	PyTextDir     string // Python 参考输出 txt 目录
	TablesDir     string // Go 表格 JSON 输出目录
	GoSuffix      string // Go 输出变体后缀（如 ocr）
}

// LoadConfig 从环境变量加载批处理/对比工具配置。
func LoadConfig() Config {
	goVariant := "ocr"
	pyVariant := "ocr"
	td := filepath.Join("testdata")
	return Config{
		Count:         envInt("BATCH_COUNT", 0),
		Single:        os.Getenv("BATCH_SINGLE"),
		SkipOCR:       os.Getenv("BATCH_SKIP_OCR") == "1",
		CompareOnly:   os.Getenv("BATCH_COMPARE_ONLY") == "1",
		CompareFilter: os.Getenv("BATCH_COMPARE_FILTER"),
		CSVOutput:     envStr("BATCH_COMPARE_CSV", filepath.Join(td, "output", fmt.Sprintf("compare_%s.csv", time.Now().Format("20060102_150405")))),
		GoTextDir:     filepath.Join(td, "output", "go", goVariant, "text"),
		PyTextDir:     filepath.Join(td, "output", "py", pyVariant, "text"),
		TablesDir:     filepath.Join(td, "output", "go", goVariant, "tables"),
		GoSuffix:      goVariant,
	}
}

// envInt 读取整型环境变量，失败时返回默认值。
func envInt(key string, def int) int {
	v := os.Getenv(key)
	if v == "" {
		return def
	}
	n, err := strconv.Atoi(v)
	if err != nil {
		return def
	}
	return n
}

// envStr 读取字符串环境变量，空则返回默认值。
func envStr(key, def string) string {
	v := os.Getenv(key)
	if v == "" {
		return def
	}
	return v
}

// FileExists 判断路径是否存在。
func FileExists(path string) bool {
	_, err := os.Stat(path)
	return err == nil
}
