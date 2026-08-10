//go:build dev
// +build dev

package ui

// dev 构建标签下的 ui.Assets：直接从 ./static 与 ./templates 目录提供文件系统。
// filter 排除 bootstrap/map 等冗余文件，便于本地开发无需重新 vfsgen。

import (
	"net/http"
	"os"
	"strings"

	"github.com/shurcooL/httpfs/filter"
	"github.com/shurcooL/httpfs/union"
)

var static http.FileSystem = filter.Keep(
	http.Dir("./static"),
	func(path string, fi os.FileInfo) bool {
		return fi.IsDir() ||
			(!strings.HasSuffix(path, "map.js") &&
				!strings.HasSuffix(path, "/bootstrap.js") &&
				!strings.HasSuffix(path, "/bootstrap-theme.css") &&
				!strings.HasSuffix(path, "/bootstrap.css"))
	},
)

var templates http.FileSystem = filter.Keep(
	http.Dir("./templates"),
	func(path string, fi os.FileInfo) bool {
		return fi.IsDir() || strings.HasSuffix(path, ".html")
	},
)

// dev 模式下 union 合并 templates 与 static 两棵目录树为单一 http.FileSystem。
// Assets contains the project's assets loaded from local file system when build with `-tags dev`
var Assets http.FileSystem = union.New(map[string]http.FileSystem{
	"/templates": templates,
	"/static":    static,
})
