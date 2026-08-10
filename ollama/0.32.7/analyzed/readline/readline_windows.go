// Windows 平台：Ctrl+Z 暂不支持。
package readline

// handleCharCtrlZ Windows 上不支持 SIGSTOP，直接返回。
func handleCharCtrlZ(fd uintptr, state any) (string, error) {
	// not supported
	return "", nil
}
