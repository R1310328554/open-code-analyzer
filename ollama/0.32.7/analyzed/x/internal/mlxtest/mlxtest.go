// Package mlxtest 为通过 cgo 调用 MLX 的测试提供共享脚手架。
// Package mlxtest provides shared scaffolding for tests that exercise MLX// Package mlxtest provides shared scaffolding for tests that exercise MLX
// through the cgo wrapper in x/mlxrunner/mlx.
package mlxtest

import (
	"runtime"
	"testing"

	"github.com/ollama/ollama/x/mlxrunner/mlx"
)

// SkipIfUnavailable 在 MLX 动态库不可加载时跳过测试。
// SkipIfUnavailable skips the test when the MLX dynamic library cannot be
// loaded (e.g. no MLX backend built for this platform).
func SkipIfUnavailable(t *testing.T) {
	t.Helper()
	if err := mlx.CheckInit(); err != nil {
		t.Skipf("MLX not available: %v", err)
	}
}

// Setup 准备原生 MLX 测试：不可用时跳过，并将 goroutine 固定到 OS 线程。
// MLX 默认流缓存为线程局部，race detector 迁移 goroutine 会 panic。
// Setup prepares a test that calls into MLX natively: it skips when MLX is
// unavailable and pins the test goroutine to its OS thread for the duration
// of the test.
//
// The thread pin is load-bearing, not defensive: MLX's default stream cache
// is thread-local, and anything that migrates the goroutine mid-test (the
// race detector's scheduler in particular) otherwise panics with
// "There is no Stream(gpu, 0) in current thread".
//
// Setup deliberately does not switch devices or sweep caches: switching the
// default device re-creates the process-wide default stream, and sweeping the
// allocator cache between tests changes allocator reuse — both perturbed
// tests that share lazy arrays with subtests running on other threads.
func Setup(t *testing.T) {
	t.Helper()

	SkipIfUnavailable(t)

	runtime.LockOSThread()
	t.Cleanup(runtime.UnlockOSThread)
}
