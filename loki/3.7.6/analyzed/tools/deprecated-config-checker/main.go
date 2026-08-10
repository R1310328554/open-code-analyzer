package main

// deprecated-config-checker 命令行入口：校验 Loki config/runtime-config，分色打印废弃与已删项，任一命中则 exit 1 并指向升级文档。

import (
	"flag"
	"fmt"
	"os"

	"github.com/fatih/color"

	"github.com/grafana/loki/v3/tools/deprecated-config-checker/checker"
)

const upgradeGuideURL = "https://grafana.com/docs/loki/latest/setup/upgrade/"

func RegisterFlags(f *flag.FlagSet) {
	f.BoolVar(&color.NoColor, "no-color", false, "Disable color output")
}

// main 解析 flag、构造 Checker，依次检查 static 与 runtime 四类 deprecate/delete 列表。
func main() {
	var cfg checker.Config

	fs := flag.NewFlagSet(os.Args[0], flag.ExitOnError)
	RegisterFlags(fs)
	cfg.RegisterFlags(fs)
	if err := fs.Parse(os.Args[1:]); err != nil {
		panic(err)
	}

	if err := cfg.Validate(); err != nil {
		panic(err)
	}

	c, err := checker.NewChecker(cfg)
	if err != nil {
		panic(err)
	}

	deprecates := c.CheckConfigDeprecated()
	if len(deprecates) > 0 {
		fmt.Print(color.YellowString("-- Deprecated Configs --\n\n"))
		for _, d := range deprecates {
			fmt.Printf("%s %s\n\n", color.YellowString("[*]"), d)
		}
	}

	deletes := c.CheckConfigDeleted()
	if len(deletes) > 0 {
		fmt.Print(color.RedString("-- Deleted Configs --\n\n"))
		for _, d := range deletes {
			fmt.Printf("%s %s\n\n", color.RedString("[-]"), d)
		}
	}

	deprecatesRuntime := c.CheckRuntimeConfigDeprecated()
	if len(deprecatesRuntime) > 0 {
		fmt.Print(color.YellowString("-- Deprecated Runtime Configs --\n\n"))
		for _, d := range deprecatesRuntime {
			fmt.Printf("%s %s\n\n", color.YellowString("[*]"), d)
		}
	}

	deletesRuntime := c.CheckRuntimeConfigDeleted()
	if len(deletesRuntime) > 0 {
		fmt.Print(color.RedString("-- Deleted Runtime Configs --\n\n"))
		for _, d := range deletesRuntime {
			fmt.Printf("%s %s\n\n", color.RedString("[-]"), d)
		}
	}

	if len(deprecates) > 0 || len(deletes) > 0 {
		fmt.Printf("Please refer to the upgrade guide for more details: %s\n", upgradeGuideURL)
		os.Exit(1)
	}

	fmt.Printf("No deprecated or deleted configs found.\n")
}
// upgradeGuideURL 指向 Grafana Loki setup/upgrade 页面供运维迁移参考。
