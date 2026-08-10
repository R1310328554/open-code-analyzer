// 无 CGO 的 Linux 原生探测桩：返回需 cgo 的错误。
//go:build linux && !cgo

package discover

import (
	"context"
	"errors"
)

// runPlatformNativeProbe 在无 CGO 构建中不可用。
func runPlatformNativeProbe(context.Context, []string) ([]nativeProbeDevice, error) {
	return nil, errors.New("native GPU discovery requires cgo on Linux")
}
