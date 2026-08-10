// llama runner 子进程输出捕获、错误检测与 OOM 判定。
package llm

import (
	"bytes"
	"io"
	"strings"
	"sync/atomic"
)

// StatusWriter 包装 io.Writer，从 runner stdout/stderr 捕获错误行。
// StatusWriter is a writer that captures error messages from the llama runner process
type StatusWriter struct {
	out io.Writer
	// 子进程可能将 stdout/stderr 接到同一 StatusWriter；os/exec 会串行 Write。
	// Subprocess wrappers may wire both stdout and stderr to the same
	// StatusWriter, and os/exec serializes Write calls in that case.
	lastErrMsg atomic.Value
}

const maxCapturedErrorBytes = 8 * 1024 // 捕获错误消息的最大字节数

// NewStatusWriter 创建 StatusWriter，可选透传到底层 writer。
func NewStatusWriter(out io.Writer) *StatusWriter {
	return &StatusWriter{
		out: out,
	}
}

// LastError 返回最近捕获的错误消息。
func (w *StatusWriter) LastError() string {
	if w == nil {
		return ""
	}
	if v := w.lastErrMsg.Load(); v != nil {
		return v.(string)
	}
	return ""
}

// SetLastError 直接设置最近错误消息。
func (w *StatusWriter) SetLastError(msg string) {
	if w == nil {
		return
	}
	w.lastErrMsg.Store(msg)
}

// AppendError 追加错误行并截断至 maxCapturedErrorBytes。
func (w *StatusWriter) AppendError(msg string) {
	if w == nil || msg == "" {
		return
	}

	if current := w.LastError(); current != "" {
		msg = current + "\n" + msg
	}

	if len(msg) > maxCapturedErrorBytes {
		msg = msg[len(msg)-maxCapturedErrorBytes:]
		if i := strings.IndexByte(msg, '\n'); i >= 0 {
			msg = msg[i+1:]
		}
	}

	w.SetLastError(msg)
}

// TODO：可用正则匹配 libcublasLt.so 等缺失库错误。
// TODO - regex matching to detect errors like
// libcublasLt.so.11: cannot open shared object file: No such file or directory
// TODO - if we later see error lines split across multiple Write calls in real
// logs, add a small rolling buffer here to capture those fragments.

// errorPrefixes 触发错误捕获的行前缀列表。
var errorPrefixes = []string{
	"mlx:",
	"MLX:",
	"panic:",
	"fatal error:",
	"error:",
	"Error:",
	"CUDA error",
	"ROCm error",
	"cudaMalloc failed",
	"\"ERR\"",
	"error loading model",
	"GGML_ASSERT",
	"Deepseek2 does not support K-shift",
	"signal arrived during cgo execution",
	"llama_init_from_model:",
}

// outOfMemorySubstrings 判定 OOM 的子串列表。
var outOfMemorySubstrings = []string{
	"out of memory",
	"out of device memory",
	"cudaMalloc failed",
	"hipMalloc failed",
	"failed to allocate",
	"allocation failed",
	"not enough memory",
	"insufficient memory",
	"vk_error_out_of_device_memory",
	"erroroutofmemory",
}

// recoverableOutOfMemorySubstrings 可恢复 OOM（如关闭 pipeline 并行重试）。
var recoverableOutOfMemorySubstrings = []string{
	"retrying without pipeline parallelism",
}

// IsOutOfMemory 根据 error 文本判断是否 OOM。
func IsOutOfMemory(err error) bool {
	if err == nil {
		return false
	}
	return IsOutOfMemoryMessage(err.Error())
}

// isRecoverableOutOfMemory 判断 OOM 是否可恢复重试。
func isRecoverableOutOfMemory(err error) bool {
	if err == nil {
		return false
	}
	return isRecoverableOutOfMemoryMessage(err.Error())
}

// IsOutOfMemoryMessage 对消息文本做 OOM 子串匹配。
func IsOutOfMemoryMessage(msg string) bool {
	msg = strings.ToLower(msg)
	for _, needle := range outOfMemorySubstrings {
		if strings.Contains(msg, strings.ToLower(needle)) {
			return true
		}
	}
	return false
}

// isRecoverableOutOfMemoryMessage 检查最后一行是否为可恢复 OOM。
func isRecoverableOutOfMemoryMessage(msg string) bool {
	lastLine := lastNonEmptyLine(msg)
	if !IsOutOfMemoryMessage(lastLine) {
		return false
	}

	lastLine = strings.ToLower(lastLine)
	for _, needle := range recoverableOutOfMemorySubstrings {
		if strings.Contains(lastLine, strings.ToLower(needle)) {
			return true
		}
	}
	return false
}

// lastNonEmptyLine 返回消息中最后一条非空行。
func lastNonEmptyLine(msg string) string {
	lines := strings.Split(strings.TrimSpace(msg), "\n")
	for i := len(lines) - 1; i >= 0; i-- {
		if line := strings.TrimSpace(lines[i]); line != "" {
			return line
		}
	}
	return ""
}

// Write 按行扫描错误前缀并透传到底层 writer。
func (w *StatusWriter) Write(b []byte) (int, error) {
	for _, raw := range bytes.Split(b, []byte{'\n'}) {
		line := strings.TrimRight(string(raw), " \t\r")
		if line == "" {
			continue
		}

		if errMsg := statusErrorLine(line); errMsg != "" {
			w.AppendError(errMsg)
		}
	}

	if w.out == nil {
		return len(b), nil
	}

	return w.out.Write(b)
}

// statusErrorLine 从单行日志提取应记录的错误片段。
func statusErrorLine(line string) string {
	errStart := -1
	errPrefix := ""
	for _, prefix := range errorPrefixes {
		if i := strings.Index(line, prefix); i >= 0 && (errStart < 0 || i < errStart) {
			errStart = i
			errPrefix = prefix
		}
	}

	if errStart >= 0 {
		return errPrefix + strings.TrimRight(line[errStart+len(errPrefix):], " \t\r")
	}

	if IsOutOfMemoryMessage(line) {
		return line
	}

	return ""
}
