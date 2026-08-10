// 数值格式化：大整数的人类可读缩写（K/M/B）。
package format

import (
	"fmt"
	"math"
	"strconv"
)

// 数量级常量：千、百万、十亿。
const (
	Thousand = 1000
	Million  = Thousand * 1000
	Billion  = Million * 1000
)

// HumanNumber 将大整数格式化为 K/M/B 缩写，小数位数随量级调整。
func HumanNumber(b uint64) string {
	switch {
	case b >= Billion:
		number := float64(b) / Billion
		if number == math.Floor(number) {
			return fmt.Sprintf("%.0fB", number) // 整数时不显示小数
			// no decimals if whole number
		}
		return fmt.Sprintf("%.1fB", number) // 非整数时保留一位小数
		// one decimal if not a whole number
	case b >= Million:
		number := float64(b) / Million
		if number == math.Floor(number) {
			return fmt.Sprintf("%.0fM", number) // no decimals if whole number
		}
		return fmt.Sprintf("%.2fM", number) // two decimals if not a whole number
	case b >= Thousand:
		return fmt.Sprintf("%.0fK", float64(b)/Thousand)
	default:
		return strconv.FormatUint(b, 10)
	}
}
