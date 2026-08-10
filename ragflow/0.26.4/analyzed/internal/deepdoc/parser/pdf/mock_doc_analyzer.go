// mock_doc_analyzer.go — 单元测试用 DocAnalyzer 桩：可注入 DLA/TSR/OCR 返回值与各方法错误路径。

package pdf

import (
	"context"
	"fmt"
	"image"
	pdf "ragflow/internal/deepdoc/parser/pdf/type"
)

// MockDocAnalyzer 返回预定义推理结果；各 Err 字段非 nil 时模拟对应失败路径。
type MockDocAnalyzer struct {
	DLARegions []pdf.DLARegion
	TSRCells   []pdf.TSRCell
	OCRBoxes   []pdf.OCRBox
	OCRTexts   []pdf.OCRText
	// OCRBatchTexts returns per-image texts for OCRRecognizeBatch.
	// If nil, OCRTexts is returned for every image.
	OCRBatchTexts [][]pdf.OCRText
	// OCRBatchErr 为第 i 张图注入批量 OCR 错误。
	OCRBatchErr func(i int) error
	// 各方法独立错误注入字段。
	DLAErr          error
	TSRErr          error
	OCRDetectErr    error
	OCRRecognizeErr error

	Healthy bool
}

// DLA 返回预设 DLARegions 或 DLAErr。
func (m *MockDocAnalyzer) DLA(_ context.Context, _ image.Image) ([]pdf.DLARegion, error) {
	if m.DLAErr != nil {
		return nil, m.DLAErr
	}
	return m.DLARegions, nil
}
// TSR 返回预设 TSRCells 或 TSRErr。
func (m *MockDocAnalyzer) TSR(_ context.Context, _ image.Image) ([]pdf.TSRCell, error) {
	if m.TSRErr != nil {
		return nil, m.TSRErr
	}
	return m.TSRCells, nil
}
// OCRDetect 返回预设 OCRBoxes 或 OCRDetectErr。
func (m *MockDocAnalyzer) OCRDetect(_ context.Context, _ image.Image) ([]pdf.OCRBox, error) {
	if m.OCRDetectErr != nil {
		return nil, m.OCRDetectErr
	}
	return m.OCRBoxes, nil
}
// OCRRecognize 返回预设 OCRTexts 或 OCRRecognizeErr。
func (m *MockDocAnalyzer) OCRRecognize(_ context.Context, _ image.Image) ([]pdf.OCRText, error) {
	if m.OCRRecognizeErr != nil {
		return nil, m.OCRRecognizeErr
	}
	return m.OCRTexts, nil
}
// OCRRecognizeBatch 按索引返回 OCRBatchTexts/OCRTexts，nil 图与 OCRBatchErr 注入错误。
func (m *MockDocAnalyzer) OCRRecognizeBatch(_ context.Context, cropped []image.Image) ([][]pdf.OCRText, []error) {
	results := make([][]pdf.OCRText, len(cropped))
	errs := make([]error, len(cropped))
	for i, img := range cropped {
		if img == nil {
			errs[i] = fmt.Errorf("image[%d] is nil", i)
			continue
		}
		if m.OCRBatchErr != nil {
			errs[i] = m.OCRBatchErr(i)
		}
		if m.OCRBatchTexts != nil && i < len(m.OCRBatchTexts) {
			results[i] = m.OCRBatchTexts[i]
		} else {
			results[i] = m.OCRTexts
		}
	}
	return results, errs
}
// Health 返回 Healthy 字段。
func (m *MockDocAnalyzer) Health() bool { return m.Healthy }
