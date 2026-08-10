// SPDX-License-Identifier: AGPL-3.0-only

package atomicfs

import (
	"io"
	"os"
	"path/filepath"

	"github.com/grafana/dskit/multierror"
)

// Create 在同目录创建 .tmp 临时文件，Close 时 fsync 后 rename 到目标路径。
// Create creates a new file at a temporary path that will be renamed to the
// supplied path on close from a temporary file in the same directory, ensuring
// all data and the containing directory have been fsynced to disk.
func Create(path string) (*File, error) {
	// We rename from a temporary file in the same directory to because rename
	// can only operate on two files that are on the same filesystem. Creating
	// a temporary file in the same directory is an easy way to guarantee that.
	final := filepath.Clean(path)
	tmp := tempPath(final)

	file, err := os.Create(tmp)
	if err != nil {
		return nil, err
	}

	return &File{
		File:      file,
		finalPath: final,
	}, nil
}

// tempPath 生成 final.tmp 路径，逻辑须与清理临时文件的单元测试保持一致。
// tempPath returns a path for the temporary version of a file. This function exists
// to ensure the logic here stays in sync with unit tests that check for this file being
// cleaned up.
func tempPath(final string) string {
	return final + ".tmp"
}

// File 包装 os.File，Close 失败时删除临时文件，成功则 fsync 父目录确保目录项落盘。
// File is a wrapper around an os.File instance that uses a temporary file for writes
// that is renamed to its final path when Close is called. The Close method will also
// ensure that all data from the file has been fsynced as well as the containing
// directory. If the temporary file cannot be renamed or fsynced on Close, it is
// removed.
type File struct {
	*os.File
	finalPath string
}

func (a *File) Close() error {
	cleanup := true
	defer func() {
		if cleanup {
			_ = os.Remove(a.Name())
		}
	}()

	merr := multierror.New()
	merr.Add(a.Sync())
	merr.Add(a.File.Close())
	if err := merr.Err(); err != nil {
		return err
	}

	if err := os.Rename(a.Name(), a.finalPath); err != nil {
		return err
	}

	cleanup = false
	// After writing the file and calling fsync on it, fsync the containing directory
	// to ensure the directory entry is persisted to disk.
	//
	// From https://man7.org/linux/man-pages/man2/fsync.2.html
	// > Calling fsync() does not necessarily ensure that the entry in the
	// > directory containing the file has also reached disk.  For that an
	// > explicit fsync() on a file descriptor for the directory is also
	// > needed.
	dir, err := os.Open(filepath.Dir(a.finalPath))
	if err != nil {
		return err
	}

	merr.Add(dir.Sync())
	merr.Add(dir.Close())
	return merr.Err()
}

// CreateFile 通过 Create 写入 reader 内容并 Close，保证数据与目录项均已持久化。
// CreateFile safely writes the contents of data to filePath, ensuring that all data
// has been fsynced as well as the containing directory of the file.
func CreateFile(filePath string, data io.Reader) error {
	f, err := Create(filePath)
	if err != nil {
		return err
	}

	_, err = io.Copy(f, data)
	merr := multierror.New(err)
	merr.Add(f.Close())
	return merr.Err()
}
// rename 仅能在同一文件系统内原子完成，故临时文件必须与最终路径同目录。
