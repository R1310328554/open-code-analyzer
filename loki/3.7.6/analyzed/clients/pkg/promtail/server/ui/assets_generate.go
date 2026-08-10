//go:build ignore
// +build ignore

package main

// go:generate 构建工具：将 ui.Assets 经 modtimevfs 固定 mtime 后由 vfsgen 生成 assets_vfsdata.go。
// 仅 ignore 标签编译，产物带 !dev build tag 供生产嵌入静态资源。

import (
	"log"
	"time"

	"github.com/prometheus/alertmanager/pkg/modtimevfs"
	"github.com/shurcooL/vfsgen"

	"github.com/grafana/loki/v3/clients/pkg/promtail/server/ui"
)

func main() {
	fs := modtimevfs.New(ui.Assets, time.Unix(1, 0))
	err := vfsgen.Generate(fs, vfsgen.Options{
		PackageName:  "ui",
		BuildTags:    "!dev",
		VariableName: "Assets",
	})
	if err != nil {
		log.Fatalln(err)
	}
}
