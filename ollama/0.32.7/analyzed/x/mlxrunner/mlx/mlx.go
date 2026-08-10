// MLX Go 包核心：CGO 绑定、错误捕获、Eval 与 GPU 可用性。
package mlx

//go:generate go run generator/main.go -output=. ./include/mlx/c/*.h
// 从 C 头生成 generated.h 等 CGO 绑定。

// #cgo CXXFLAGS: -std=c++17
// #cgo CPPFLAGS: -I${SRCDIR}/include
// #cgo LDFLAGS: -lstdc++
// #cgo darwin LDFLAGS: -framework Foundation -framework Metal -framework Accelerate
// #include "generated.h"
// #include <string.h>
//
// static __thread char _mlx_last_error_msg[1024] = {0};
// static __thread int  _mlx_last_error_flag = 0;
//
// static void _mlx_capture_error_handler(const char* msg, void* data) {
//     (void)data;
//     strncpy(_mlx_last_error_msg, msg, sizeof(_mlx_last_error_msg) - 1);
//     _mlx_last_error_msg[sizeof(_mlx_last_error_msg) - 1] = '\0';
//     _mlx_last_error_flag = 1;
// }
//
// static void mlx_install_capture_handler(void) {
//     if (mlx_set_error_handler_) {
//         mlx_set_error_handler_(_mlx_capture_error_handler, NULL, NULL);
//     }
// }
//
// static void mlx_clear_last_error(void) {
//     _mlx_last_error_flag = 0;
//     _mlx_last_error_msg[0] = '\0';
// }
//
// static const char* mlx_get_last_error(void) {
//     return _mlx_last_error_flag ? _mlx_last_error_msg : "";
// }
import "C"

import (
	"fmt"
	"runtime"
)

func init() {
	// 替换默认 exit(-1) 错误处理，捕获 C 侧错误信息供 Go 返回。
	// Replace the default exit(-1) error handler with one that captures
	// the error message so we can surface it in Go.
	C.mlx_install_capture_handler()
}

// Version 返回 MLX 核心库版本字符串。
// Version returns the MLX core library version string.
func Version() string {
	str := C.mlx_string_new()
	defer C.mlx_string_free(str)
	C.mlx_version(&str)
	return C.GoString(C.mlx_string_data(str))
}

// mlxCall 锁定 OS 线程以读取 thread-local MLX 错误状态。
// mlxCall locks the goroutine to its OS thread so the thread-local error state
// is read from the same thread that executed fn.
func mlxCall(fallback string, fn func() C.int) error {
	runtime.LockOSThread()
	defer runtime.UnlockOSThread()

	C.mlx_clear_last_error()
	if fn() != 0 {
		msg := C.GoString(C.mlx_get_last_error())
		if msg == "" {
			msg = fallback
		}
		return fmt.Errorf("mlx: %s", msg)
	}
	return nil
}

// mlxCheck 在 MLX 调用失败时 panic；图构建/求值通常不可恢复。
// mlxCheck panics with the captured MLX error. Most array operations cannot
// recover from a failed graph construction or evaluation.
func mlxCheck(fallback string, fn func() C.int) {
	if err := mlxCall(fallback, fn); err != nil {
		panic(err.Error())
	}
}

func doEval(outputs []*Array, async bool) {
	if len(outputs) == 0 {
		return
	}

	vector := C.mlx_vector_array_new()
	defer C.mlx_vector_array_free(vector)

	for _, output := range outputs {
		if output != nil && output.Valid() {
			C.mlx_vector_array_append_value(vector, output.ctx)
		}
	}

	mlxCheck("eval failed", func() C.int {
		if async {
			return C.mlx_async_eval(vector)
		}
		return C.mlx_eval(vector)
	})
}

// AsyncEval 异步求值输出张量。
func AsyncEval(outputs ...*Array) {
	doEval(outputs, true)
}

// Eval 同步求值输出张量。
func Eval(outputs ...*Array) {
	doEval(outputs, false)
}

// MetalIsAvailable 判断 Metal GPU 是否可用。
// MetalIsAvailable returns true if a Metal GPU is available.
func MetalIsAvailable() bool {
	var available C._Bool
	C.mlx_metal_is_available(&available)
	return bool(available)
}

// CUDAIsAvailable 判断 CUDA GPU 是否可用。
// CUDAIsAvailable returns true if a CUDA GPU is available.
func CUDAIsAvailable() bool {
	var available C._Bool
	C.mlx_cuda_is_available(&available)
	return bool(available)
}
