// readline 包错误类型：中断与编辑提示。
package readline

import (
	"errors"
)

// 预定义 readline 控制流错误。
var (
	ErrInterrupt  = errors.New("Interrupt") // Ctrl+C 中断输入
	ErrEditPrompt = errors.New("EditPrompt") // Ctrl+B 请求外部编辑
)

// InterruptError 携带中断时的部分输入行。
type InterruptError struct {
	Line []rune
}

func (*InterruptError) Error() string {
	return "Interrupted"
}
