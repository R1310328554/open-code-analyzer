//go:build windows || darwin

// package logrotate 提供日志文件轮转工具；后续可能并入 app 包。
// package logrotate provides utilities for rotating logs
// TODO (jmorgan): this most likely doesn't need it's own
// package and can be moved to app where log files are created
package logrotate

import (
	"log/slog"
	"os"
	"strconv"
	"strings"
)

// MaxLogFiles 为保留的历史日志文件数量上限。
const MaxLogFiles = 5

// Rotate 将现有日志依次重命名为 filename-1、filename-2…，超出 MaxLogFiles 的删除。
func Rotate(filename string) {
	if _, err := os.Stat(filename); os.IsNotExist(err) {
		return
	}

	index := strings.LastIndex(filename, ".")
	pre := filename[:index]
	post := "." + filename[index+1:]
	for i := MaxLogFiles; i > 0; i-- {
		older := pre + "-" + strconv.Itoa(i) + post
		newer := pre + "-" + strconv.Itoa(i-1) + post
		if i == 1 {
			newer = pre + post
		}
		if _, err := os.Stat(newer); err == nil {
			if _, err := os.Stat(older); err == nil {
				err := os.Remove(older)
				if err != nil {
					slog.Warn("Failed to remove older log", "older", older, "error", err)
					continue
				}
			}
			err := os.Rename(newer, older)
			if err != nil {
				slog.Warn("Failed to rotate log", "older", older, "newer", newer, "error", err)
			}
		}
	}
}
