// 非 Linux/Windows 平台的原生 GPU 探测未实现桩。
//go:build !linux && !windows

package discover

import (
	"context"
	"errors"
)

// runPlatformNativeProbe 在当前平台未实现原生 GPU 发现。
func runPlatformNativeProbe(context.Context, []string) ([]nativeProbeDevice, error) {
	return nil, errors.New("native GPU discovery is not implemented on this platform")
}
