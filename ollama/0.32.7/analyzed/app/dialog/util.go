//go:build windows

package dialog

// firstOf 返回参数列表中第一个非空字符串。
func firstOf(args ...string) string {func firstOf(args ...string) string {
	for _, arg := range args {
		if arg != "" {
			return arg
		}
	}
	return ""
}
