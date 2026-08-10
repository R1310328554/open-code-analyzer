package util //nolint:revive

// util 包 LogAdapter 将 go-kit logger 适配为 tail 包期望的标准库风格接口，Fatal/Panic 系列方法在记录后仍执行 os.Exit 或 panic。

import (
	"fmt"
	"os"
	"strings"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
)

type LogAdapter struct {
	log.Logger
}

func NewLogAdapter(l log.Logger) LogAdapter {
	return LogAdapter{
		Logger: l,
	}
}

// Fatal 以 error 级别记录后以退出码 1 终止进程，对应标准库 log.Fatal。
// Fatal implements tail.logger
func (l LogAdapter) Fatal(v ...interface{}) {
	level.Error(l).Log("msg", fmt.Sprint(v...))
	os.Exit(1)
}

// Fatalf implements tail.logger
func (l LogAdapter) Fatalf(format string, v ...interface{}) {
	level.Error(l).Log("msg", fmt.Sprintf(strings.TrimSuffix(format, "\n"), v...))
	os.Exit(1)
}

// Fatalln implements tail.logger
func (l LogAdapter) Fatalln(v ...interface{}) {
	level.Error(l).Log("msg", fmt.Sprint(v...))
	os.Exit(1)
}

// Panic implements tail.logger
func (l LogAdapter) Panic(v ...interface{}) {
	s := fmt.Sprint(v...)
	level.Error(l).Log("msg", s)
	panic(s)
}

// Panicf implements tail.logger
func (l LogAdapter) Panicf(format string, v ...interface{}) {
	s := fmt.Sprintf(strings.TrimSuffix(format, "\n"), v...)
	level.Error(l).Log("msg", s)
	panic(s)
}

// Panicln implements tail.logger
func (l LogAdapter) Panicln(v ...interface{}) {
	s := fmt.Sprint(v...)
	level.Error(l).Log("msg", s)
	panic(s)
}

// Print 将消息以 info 级别写入结构化日志，供 tail 客户端非致命输出。
// Print implements tail.logger
func (l LogAdapter) Print(v ...interface{}) {
	level.Info(l).Log("msg", fmt.Sprint(v...))
}

// Printf implements tail.logger
func (l LogAdapter) Printf(format string, v ...interface{}) {
	level.Info(l).Log("msg", fmt.Sprintf(strings.TrimSuffix(format, "\n"), v...))
}

// Println implements tail.logger
func (l LogAdapter) Println(v ...interface{}) {
	level.Info(l).Log("msg", fmt.Sprint(v...))
}
// Printf/Println 会去掉格式串末尾换行，避免 logfmt 字段出现多余空行。
