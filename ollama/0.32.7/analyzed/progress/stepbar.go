// 按步骤计数的简易进度条。
package progress

import (
	"fmt"
	"strings"
)

// StepBar 显示基于步骤的进度（current/total）。
// StepBar displays step-based progress.
type StepBar struct {
	message string
	current int
	total   int
}

// NewStepBar 创建总步数为 total 的步骤条。
func NewStepBar(message string, total int) *StepBar {
	return &StepBar{message: message, total: total}
}

// Set 更新当前步骤索引。
func (s *StepBar) Set(current int) {
	s.current = current
}

func (s *StepBar) String() string {
	percent := float64(s.current) / float64(s.total) * 100
	barWidth := s.total
	empty := barWidth - s.current

	// "Generating   0% ▕         ▏ 0/9"
	return fmt.Sprintf("%s %3.0f%% ▕%s%s▏ %d/%d",
		s.message, percent,
		strings.Repeat("█", s.current), strings.Repeat(" ", empty),
		s.current, s.total)
}
