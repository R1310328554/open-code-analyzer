// ui 包通过 vfs 暴露 Promtail 管理界面静态资源；go:generate 触发 assets 再生成。
// Package ui provides the assets via a virtual filesystem.
package ui

import (
	// The blank import is to make Go modules happy.
	_ "github.com/prometheus/alertmanager/pkg/modtimevfs"
	_ "github.com/shurcooL/vfsgen"
)

//go:generate go run -tags=dev assets_generate.go -build_flags="$GOFLAGS"
