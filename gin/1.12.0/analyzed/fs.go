// Copyright 2017 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"net/http"
	"os"
)

// OnlyFilesFS 实现不含 Readdir 功能的 http.FileSystem。
type OnlyFilesFS struct {
	FileSystem http.FileSystem
}

// Open 将 Open 调用委托给上游实现，但不提供 Readdir 功能。
func (o OnlyFilesFS) Open(name string) (http.File, error) {
	f, err := o.FileSystem.Open(name)
	if err != nil {
		return nil, err
	}

	return neutralizedReaddirFile{f}, nil
}

// neutralizedReaddirFile 包装 http.File，并重写 Readdir 行为。
type neutralizedReaddirFile struct {
	http.File
}

// Readdir 覆盖 http.File 的默认实现，始终返回 nil。
func (n neutralizedReaddirFile) Readdir(_ int) ([]os.FileInfo, error) {
	// 禁用目录列表
	return nil, nil
}

// Dir 返回可供 http.FileServer 使用的 http.FileSystem。
// 内部由 router.Static() 使用。
// 若 listDirectory 为 true，行为与 http.Dir() 相同；
// 否则返回禁止 http.FileServer 列出目录内容的文件系统。
func Dir(root string, listDirectory bool) http.FileSystem {
	fs := http.Dir(root)

	if listDirectory {
		return fs
	}

	return &OnlyFilesFS{FileSystem: fs}
}
