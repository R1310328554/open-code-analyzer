package main

import (
	"fmt"
	"os"

	"github.com/ollama/ollama/runner"
)

// main 将命令行参数转交给 runner 子系统执行。
func main() {
	if err := runner.Execute(os.Args[1:]); err != nil {
		fmt.Fprintf(os.Stderr, "error: %s\n", err)
		os.Exit(1)
	}
}
