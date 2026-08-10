// Linux llama-server 子进程 SysProcAttr 默认值。
package llm

import (
	"syscall"
)

// LlamaServerSysProcAttr Linux 上 llama-server 子进程的默认 SysProcAttr。
var LlamaServerSysProcAttr = &syscall.SysProcAttr{}
