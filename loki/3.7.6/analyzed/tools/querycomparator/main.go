package main

// querycomparator 主入口通过 kingpin 注册 compare、metastore、execute 子命令：用于回归测试 chunk 与 dataobj 查询路径或比对双集群结果。

import (
	"os"

	"github.com/alecthomas/kingpin/v2"
)

func main() {
	app := kingpin.New("querycomparator", "A command-line tool to compare query results between two hosts.")
	addCompareCommand(app)
	addMetastoreCommand(app)
	addExecuteCommand(app)
	kingpin.MustParse(app.Parse(os.Args[1:]))
}
// 工具面向开发者与 CI，不部署为 Loki 组件；依赖环境凭证访问远端对象存储。
