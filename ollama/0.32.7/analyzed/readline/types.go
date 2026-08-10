// readline 控制字符、方向键码与 ANSI 光标/颜色转义序列常量。
package readline

import "strconv"

// 控制字符与编辑键的 ASCII/转义码。
const (
	CharNull      = 0
	CharLineStart = 1
	CharBackward  = 2
	CharInterrupt = 3
	CharDelete    = 4
	CharLineEnd   = 5
	CharForward   = 6
	CharBell      = 7
	CharCtrlH     = 8
	CharTab       = 9
	CharCtrlJ     = 10
	CharKill      = 11
	CharCtrlL     = 12
	CharEnter     = 13
	CharNext      = 14
	CharCtrlO     = 15 // Ctrl+O：展开工具输出
	CharPrev      = 16
	CharBckSearch = 18
	CharFwdSearch = 19
	CharTranspose = 20
	CharCtrlU     = 21
	CharCtrlW     = 23
	CharCtrlY     = 25
	CharCtrlZ     = 26
	CharEsc       = 27
	CharSpace     = 32
	CharEscapeEx  = 91
	CharBackspace = 127
)

// 方向键与 Home/End 的 ANSI 末字节。
const (
	KeyDel    = 51
	KeyUp     = 65
	KeyDown   = 66
	KeyRight  = 67
	KeyLeft   = 68
	MetaEnd   = 70
	MetaStart = 72
)

// 终端控制转义序列（光标、清屏、颜色、 bracketed paste）。
const (
	Esc = "\x1b"

	CursorSave    = Esc + "[s"
	CursorRestore = Esc + "[u"

	CursorEOL  = Esc + "[E"
	CursorBOL  = Esc + "[1G"
	CursorHide = Esc + "[?25l"
	CursorShow = Esc + "[?25h"

	ClearToEOL  = Esc + "[K"
	ClearLine   = Esc + "[2K"
	ClearScreen = Esc + "[2J"
	CursorReset = Esc + "[0;0f"

	ColorGrey    = Esc + "[38;5;245m"
	ColorDefault = Esc + "[0m"

	ColorBold = Esc + "[1m"

	StartBracketedPaste = Esc + "[?2004h"
	EndBracketedPaste   = Esc + "[?2004l"
)

// CursorUpN 生成光标上移 n 行的 ANSI 序列。
func CursorUpN(n int) string {
	return Esc + "[" + strconv.Itoa(n) + "A"
}

// CursorDownN 生成光标下移 n 行的 ANSI 序列。
func CursorDownN(n int) string {
	return Esc + "[" + strconv.Itoa(n) + "B"
}

// CursorRightN 生成光标右移 n 列的 ANSI 序列。
func CursorRightN(n int) string {
	return Esc + "[" + strconv.Itoa(n) + "C"
}

// CursorLeftN 生成光标左移 n 列的 ANSI 序列。
func CursorLeftN(n int) string {
	return Esc + "[" + strconv.Itoa(n) + "D"
}

// 单步光标移动快捷常量。
var (
	CursorUp    = CursorUpN(1)
	CursorDown  = CursorDownN(1)
	CursorRight = CursorRightN(1)
	CursorLeft  = CursorLeftN(1)
)

// Bracketed paste 模式起止标记。
const (
	CharBracketedPaste      = 50
	CharBracketedPasteStart = "00~"
	CharBracketedPasteEnd   = "01~"
)
