// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 非 builtinassets 构建下的 UI 静态资源 http.FileSystem：按工作目录选择 web/ui 路径并过滤部分 bootstrap/map 文件。

//go:build !builtinassets

package ui

import (
	"net/http"
	"os"
	"path"
	"path/filepath"
	"strings"

	"github.com/shurcooL/httpfs/filter"
	"github.com/shurcooL/httpfs/union"
)

// Assets 在 init 时根据 cwd 定位 static 目录并通过 union 挂载到 /static。
// Assets contains the project's assets.
var Assets = func() http.FileSystem {
	wd, err := os.Getwd()
	if err != nil {
		panic(err)
	}
	var assetsPrefix string
	switch filepath.Base(wd) {
// 从仓库根目录运行 prometheus 二进制时，静态资源前缀为 ./web/ui。
	case "prometheus":
		// When running Prometheus (without built-in assets) from the repo root.
		assetsPrefix = "./web/ui"
	case "web":
		// When running web tests.
		assetsPrefix = "./ui"
	case "ui":
		// When generating statically compiled-in assets.
		assetsPrefix = "./"
	}

// filter.Keep 排除 source map 与 bootstrap 主题 CSS，减小开发态暴露面。
	static := filter.Keep(
		http.Dir(path.Join(assetsPrefix, "static")),
		func(path string, fi os.FileInfo) bool {
			return fi.IsDir() ||
				(!strings.HasSuffix(path, "map.js") &&
					!strings.HasSuffix(path, "/bootstrap.js") &&
					!strings.HasSuffix(path, "/bootstrap-theme.css") &&
					!strings.HasSuffix(path, "/bootstrap.css"))
		},
	)

	return union.New(map[string]http.FileSystem{
		"/static": static,
	})
}()
