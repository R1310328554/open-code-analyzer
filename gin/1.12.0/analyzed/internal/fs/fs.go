package fs

import (
	"io/fs"
	"net/http"
)

// 文件系统实现了一个[fs.FS]。
type FileSystem struct {
	http.FileSystem
}

// Open 将 `Open` 传递给上游实现并返回一个 [fs.File]。
func (o FileSystem) Open(name string) (fs.File, error) {
	f, err := o.FileSystem.Open(name)
	if err != nil {
		return nil, err
	}

	return fs.File(f), nil
}
