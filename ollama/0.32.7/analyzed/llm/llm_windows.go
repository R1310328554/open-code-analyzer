// Windows llama-server 子进程启动标志与 SysProcAttr 配置。
package llm

import (
	"syscall"
)

// Windows 进程创建标志常量。
const (
	CREATE_DEFAULT_ERROR_MODE   = 0x04000000 // 启用默认 DLL 缺失错误对话框
	ABOVE_NORMAL_PRIORITY_CLASS = 0x00008000 // 高于普通优先级，避免后台服务饿死 CPU
	CREATE_NO_WINDOW            = 0x08000000 // 不创建控制台窗口
)

// LlamaServerSysProcAttr Windows 上 llama-server 子进程的 SysProcAttr。
var LlamaServerSysProcAttr = &syscall.SysProcAttr{
	// 启用默认错误处理：PATH 缺 DLL 时弹出 GUI 对话框而非静默退出。
	// Wire up the default error handling logic If for some reason a DLL is
	// missing in the path this will pop up a GUI Dialog explaining the fault so
	// the user can either fix their PATH, or report a bug. Without this
	// setting, the process exits immediately with a generic exit status but no
	// way to (easily) figure out what the actual missing DLL was.
	//
	// 高于普通优先级，避免作为后台服务时被前台程序抢占 CPU。
	// Setting Above Normal priority class ensures when running as a "background service"
	// with "programs" given best priority, we aren't starved of cpu cycles
	CreationFlags: CREATE_DEFAULT_ERROR_MODE | ABOVE_NORMAL_PRIORITY_CLASS | CREATE_NO_WINDOW,
}
