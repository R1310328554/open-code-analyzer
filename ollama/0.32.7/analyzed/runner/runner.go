// 推理 runner 入口：按引擎标志分派到 MLX 等后端。
package runner

import (
	"fmt"

	"github.com/ollama/ollama/x/mlxrunner"
)

// Execute 解析 runner 子命令；当前仅支持 --mlx-engine。
func Execute(args []string) error {
	if args[0] == "runner" {
		args = args[1:]
	}

	if len(args) > 0 {
		switch args[0] {
		case "--mlx-engine":
			return mlxrunner.Execute(args[1:])
		}
	}
	return fmt.Errorf("unknown runner engine, expected --mlx-engine")
}
