package main

// dataobj-inspect 入口：基于 kingpin 注册 dump、stats、list-streams、
// print-streams 四个子命令，供运维调试 Loki data object 二进制文件。

import (
	"fmt"
	"os"

	"github.com/alecthomas/kingpin/v2"
)

func exitWithErr(err error) {
	fmt.Fprint(os.Stderr, err.Error())
	os.Exit(1)
}

func main() {
	app := kingpin.New("dataobj-inspect", "A command-line tool to inspect data objects.")
	// 依次注册 dump、stats、list-streams、print-streams 四类诊断子命令。
addDumpCommand(app)
	addStatsCommand(app)
	addListStreamsCommand(app)
	addPrintStreamsCommand(app)
	kingpin.MustParse(app.Parse(os.Args[1:]))
}
