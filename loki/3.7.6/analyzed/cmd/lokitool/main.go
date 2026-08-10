package main

// lokitool CLI 入口：注册 rule 与 audit 管理子命令及 version 查询，
// 封装 pkg/tool/commands 中对 Loki 规则与审计日志的操作。

import (
	"fmt"
	"os"

	"github.com/alecthomas/kingpin/v2"

	"github.com/prometheus/common/version"

	"github.com/grafana/loki/v3/pkg/tool/commands"
)

var (
	ruleCommand  commands.RuleCommand
	auditCommand commands.AuditCommand
)

// 创建 kingpin 应用，挂载 RuleCommand、AuditCommand 后解析命令行。
func main() {
	app := kingpin.New("lokitool", "A command-line tool to manage Loki.")
	ruleCommand.Register(app)
	auditCommand.Register(app)

	app.Command("version", "Get the version of the lokitool CLI").Action(func(_ *kingpin.ParseContext) error {
		fmt.Println(version.Print("loki"))
		return nil
	})

	kingpin.MustParse(app.Parse(os.Args[1:]))
}
