// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"bufio"
	"bytes"
	"cmp"
	"errors"
	"fmt"
	"io"
	"log"
	"net/http"
	"net/http/httputil"
	"os"
	"runtime"
	"strings"
	"syscall"
	"time"

	"github.com/gin-gonic/gin/internal/bytesconv"
)

const (
	dunno     = "???"
	stackSkip = 3
)

// RecoveryFunc 定义可传递给 CustomRecovery 的函数。
type RecoveryFunc func(c *Context, err any)

// 恢复返回一个从任何恐慌中恢复的中间件，并写入 500（如果有）。
func Recovery() HandlerFunc {
	return RecoveryWithWriter(DefaultErrorWriter)
}

// CustomRecovery 返回一个从任何恐慌中恢复的中间件，并调用提供的句柄函数来处理它。
func CustomRecovery(handle RecoveryFunc) HandlerFunc {
	return RecoveryWithWriter(DefaultErrorWriter, handle)
}

// RecoveryWithWriter 为给定 writer 返回一个中间件，该中间件从任何恐慌中恢复并写入 500（如果有）。
func RecoveryWithWriter(out io.Writer, recovery ...RecoveryFunc) HandlerFunc {
	if len(recovery) > 0 {
		return CustomRecoveryWithWriter(out, recovery[0])
	}
	return CustomRecoveryWithWriter(out, defaultHandleRecovery)
}

// CustomRecoveryWithWriter 为给定的 writer 返回一个中间件，该中间件从任何恐慌中恢复并调用提供的句柄函数来处理它。
func CustomRecoveryWithWriter(out io.Writer, handle RecoveryFunc) HandlerFunc {
	var logger *log.Logger
	if out != nil {
		logger = log.New(out, "\n\n\x1b[31m", log.LstdFlags)
	}
	return func(c *Context) {
		defer func() {
			if rec := recover(); rec != nil {
				// 检查是否有损坏的连接，因为它并不是真正的连接
				//  导致恐慌堆栈跟踪的情况。
				var isBrokenPipe bool
				err, ok := rec.(error)
				if ok {
					isBrokenPipe = errors.Is(err, syscall.EPIPE) ||
						errors.Is(err, syscall.ECONNRESET) ||
						errors.Is(err, http.ErrAbortHandler)
				}
				if logger != nil {
					if isBrokenPipe {
						logger.Printf("%s\n%s%s", rec, secureRequestDump(c.Request), reset)
					} else if IsDebugging() {
						logger.Printf("[Recovery] %s panic recovered:\n%s\n%s\n%s%s",
							timeFormat(time.Now()), secureRequestDump(c.Request), rec, stack(stackSkip), reset)
					} else {
						logger.Printf("[Recovery] %s panic recovered:\n%s\n%s%s",
							timeFormat(time.Now()), rec, stack(stackSkip), reset)
					}
				}
				if isBrokenPipe {
					// 如果连接已断开，我们无法向其写入状态。
					c.Error(err) //nolint: errcheck
					c.Abort()
				} else {
					handle(c, rec)
				}
			}
		}()
		c.Next()
	}
}

// secureRequestDump 返回经过清理的 HTTP 请求转储，其中包含授权标头，
//  如果存在，则替换为屏蔽值（“授权：*”）以避免泄露敏感凭据。
//
//  目前，仅清理授权标头。所有其他标头和请求数据保持不变。
func secureRequestDump(r *http.Request) string {
	httpRequest, _ := httputil.DumpRequest(r, false)
	lines := strings.Split(bytesconv.BytesToString(httpRequest), "\r\n")
	for i, line := range lines {
		if strings.HasPrefix(line, "Authorization:") {
			lines[i] = "Authorization: *"
		}
	}
	return strings.Join(lines, "\r\n")
}

func defaultHandleRecovery(c *Context, _ any) {
	c.AbortWithStatus(http.StatusInternalServerError)
}

// stack 返回一个格式良好的堆栈帧，跳过跳过帧。
func stack(skip int) []byte {
	buf := new(bytes.Buffer) // the returned data
	// 当我们循环时，我们打开文件并读取它们。这些变量记录了当前
	//  加载的文件。
	var (
		nLine    string
		lastFile string
		err      error
	)
	for i := skip; ; i++ { // Skip the expected number of frames
		pc, file, line, ok := runtime.Caller(i)
		if !ok {
			break
		}
		// 至少打印这么多。如果我们找不到来源，它就不会显示。
		fmt.Fprintf(buf, "%s:%d (0x%x)\n", file, line, pc)
		if file != lastFile {
			nLine, err = readNthLine(file, line-1)
			if err != nil {
				continue
			}
			lastFile = file
		}
		fmt.Fprintf(buf, "\t%s: %s\n", function(pc), cmp.Or(nLine, dunno))
	}
	return buf.Bytes()
}

// readNthLine 读取文件中的第 n 行。
//  如果找到则返回该行的修剪内容，
//  如果该行不存在，则为空字符串。
//  如果打开文件时出错，则会返回错误。
func readNthLine(file string, n int) (string, error) {
	if n < 0 {
		return "", nil
	}

	f, err := os.Open(file)
	if err != nil {
		return "", err
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)
	for i := 0; i < n; i++ {
		if !scanner.Scan() {
			return "", nil
		}
	}

	if scanner.Scan() {
		return strings.TrimSpace(scanner.Text()), nil
	}

	return "", nil
}

// 如果可能，函数返回包含 PC 的函数的名称。
func function(pc uintptr) string {
	fn := runtime.FuncForPC(pc)
	if fn == nil {
		return dunno
	}
	name := fn.Name()
	// 名称包含包路径，这是多余的，因为文件名已包含在内；且带有中间点。
	// 例如我们看到 runtime/debug.*T·ptrmethod，希望得到 *T.ptrmethod。
	// 包路径可能含点（如 code.google.com/...），因此先去掉路径前缀。
	if lastSlash := strings.LastIndexByte(name, '/'); lastSlash >= 0 {
		name = name[lastSlash+1:]
	}
	if period := strings.IndexByte(name, '.'); period >= 0 {
		name = name[period+1:]
	}
	name = strings.ReplaceAll(name, "·", ".")
	return name
}

// timeFormat 返回记录器的自定义时间字符串。
func timeFormat(t time.Time) string {
	return t.Format("2006/01/02 - 15:04:05")
}
