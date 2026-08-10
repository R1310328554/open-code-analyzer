// MLX 专用 OS 线程：单线程初始化 GPU、串行执行量化内核并在 panic 时恢复。
package create

import (
	"fmt"
	"runtime"
	"sync"
	"sync/atomic"

	"github.com/ollama/ollama/x/mlxrunner/mlx"
)

var (
	mlxThreadOnce    sync.Once
	mlxThreadStarted atomic.Bool
	mlxWork          chan func()
	mlxInitErr       error
)

// runOnMLXThread 在 pinned MLX 线程上执行 f；首次调用时初始化 MLX/GPU。
// runOnMLXThread runs f on the MLX thread and returns its error. The thread is
// started (and MLX initialized) on first use. A panic in f is recovered and
// returned as an error so a kernel failure cannot kill the pinned thread.
//
// TODO(pdevine): This method should be revisited when the `ollama create` is
// instead run on the ollama server process instead of the client.
// runOnMLXThread 通过 channel 投递工作并等待完成；f 内 panic 转为 error。
func runOnMLXThread(f func() error) error {
	mlxThreadOnce.Do(func() {
		mlxWork = make(chan func())
		ready := make(chan error)
		go func() {
			runtime.LockOSThread() // 进程生命周期内锁定 OS 线程，永不解锁
			err := mlx.CheckInit()
			if err == nil && mlx.GPUIsAvailable() {
				mlx.SetDefaultDeviceGPU()
			}
			ready <- err
			if err != nil {
				return
			}
			for work := range mlxWork {
				work()
			}
		}()
		mlxInitErr = <-ready
		mlxThreadStarted.Store(mlxInitErr == nil)
	})
	if mlxInitErr != nil {
		return fmt.Errorf("MLX init failed: %w", mlxInitErr)
	}

	done := make(chan error, 1)
	mlxWork <- func() {
		defer func() {
			if r := recover(); r != nil {
				done <- fmt.Errorf("mlx: %v", r)
			}
		}()
		done <- f()
	}
	return <-done
}

// sweepMLX 释放 MLX 缓冲缓存；若从未启动 MLX 则为空操作。
// sweepMLX releases the MLX buffer cache. It is a no-op if no MLX work has run.
// sweepMLX 在导入流水线结束时调用 ClearCache 与 Sweep。
func sweepMLX() {
	if !mlxThreadStarted.Load() {
		return
	}
	_ = runOnMLXThread(func() error {
		mlx.ClearCache()
		mlx.Sweep()
		return nil
	})
}
