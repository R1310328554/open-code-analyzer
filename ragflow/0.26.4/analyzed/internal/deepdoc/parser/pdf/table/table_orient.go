// table_orient.go — 表格方向检测：对裁剪图尝试 0/90/180/270° 旋转，用 OCR 检测区域数与面积评分选最佳朝向。

package table

import (
	"context"
	"fmt"
	"image"
	"log/slog"
	"math"

	pdf "ragflow/internal/deepdoc/parser/pdf/type"
	"ragflow/internal/deepdoc/parser/pdf/util"
)

// EvaluateTableOrientation 测试四向旋转，按 OCR 检测框数量与面积比评分选最佳角度。非 0° 须得分>1.4×0° 且 0°<6.0 才采纳。对齐 Python _evaluate_table_orientation。
func EvaluateTableOrientation(ctx context.Context, tableImg image.Image, doc pdf.DocAnalyzer) (bestAngle int, bestImg image.Image, scores map[int]float64) {
	rotations := []struct {
		angle int
		name  string
	}{
		{0, "original"},
		{90, "rotate_90"},
		{180, "rotate_180"},
		{270, "rotate_270"},
	}

	scores = make(map[int]float64, 4)
	bestScore := float64(-1)
	bestAngle = 0
	bestImg = tableImg

	for _, rot := range rotations {
		rotated := tableImg
		if rot.angle != 0 {
			rotated = util.RotateImageCW(tableImg, rot.angle)
			if rotated == nil {
				slog.Warn("table rotate failed", "angle", rot.angle)
				continue
			}
		}

		detectBoxes, err := doc.OCRDetect(ctx, rotated)
		if err != nil || len(detectBoxes) == 0 {
			scores[rot.angle] = 0
			continue
		}

		// 评分：检测区域数为主，面积占比为 tiebreaker。
		imageArea := float64(rotated.Bounds().Dx() * rotated.Bounds().Dy())
		totalRegions := 0
		var totalArea float64
		for _, box := range detectBoxes {
			x0 := math.Min(box.X0, math.Min(box.X1, math.Min(box.X2, box.X3)))
			y0 := math.Min(box.Y0, math.Min(box.Y1, math.Min(box.Y2, box.Y3)))
			x1 := math.Max(box.X0, math.Max(box.X1, math.Max(box.X2, box.X3)))
			y1 := math.Max(box.Y0, math.Max(box.Y1, math.Max(box.Y2, box.Y3)))
			if x0 >= x1 || y0 >= y1 {
				continue
			}
			totalRegions++
			totalArea += (x1 - x0) * (y1 - y0)
		}
		if totalRegions == 0 {
			scores[rot.angle] = 0
			continue
		}
		areaRatio := totalArea / imageArea
		combined := float64(totalRegions) * (1 + 0.06*areaRatio)
		scores[rot.angle] = combined

		slog.Debug("table orientation",
			"angle", rot.angle,
			"regions", totalRegions,
			"area_ratio", fmt.Sprintf("%.4f", areaRatio),
			"combined", fmt.Sprintf("%.2f", combined))

		if combined > bestScore {
			bestScore = combined
			bestAngle = rot.angle
			bestImg = rotated
		}
	}

	// 绝对阈值：仅当非 0° 得分≥1.4×0° 且 0° 区域数<6 时才采用旋转。
	score0 := scores[0]
	if bestAngle != 0 && score0 > 0 {
		if !(bestScore > score0*1.4 && score0 < 6.0) {
			bestAngle = 0
			bestImg = tableImg
			bestScore = score0
		}
	}

	slog.Debug("best table orientation",
		"angle", bestAngle,
		"score", fmt.Sprintf("%.4f", bestScore))

	return bestAngle, bestImg, scores
}
