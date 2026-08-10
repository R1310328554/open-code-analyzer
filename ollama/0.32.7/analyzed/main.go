// Ollama CLI 入口：启动 Cobra 命令行。
package main

import (
	"context"

	"github.com/spf13/cobra"

	"github.com/ollama/ollama/cmd"
)

// main 解析并执行 Ollama CLI 根命令。
func main() {
	cobra.CheckErr(cmd.NewCLI().ExecuteContext(context.Background()))
}
