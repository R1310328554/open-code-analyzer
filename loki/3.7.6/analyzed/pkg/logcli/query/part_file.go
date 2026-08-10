package query

// PartFile 封装并行/分片下载时的临时文件生命周期：先写 .tmp，Finalize 时重命名为最终 .part 文件。

import (
	"errors"
	"fmt"
	"os"
	"sync"
)

// PartFile 表示尚未完成下载的中间文件，配合 Finalize 原子替换。
// PartFile partially complete file.
// Expected usage:
//  1. Create the temp file: CreateTemp
//  2. Write the data to the file
//  3. When you're done, call Finalize and the temp file will be closed and renamed
type PartFile struct {
	finalName string
	fd        *os.File
	lock      sync.Mutex
}

// NewPartFile initializes a new partial file object, setting the filename
// which will be used when the file is closed with the Finalize function.
//
// This will not create the file, call CreateTemp to create the file.
func NewPartFile(filename string) *PartFile {
	return &PartFile{
		finalName: filename,
	}
}

// Exists 检查 finalName 对应的目标文件是否已存在（非 .tmp）。
// Exists checks if the completed file exists.
func (f *PartFile) Exists() (bool, error) {
	if _, err := os.Stat(f.finalName); err == nil {
		// No error means file exits, and we can stat it.
		return true, nil
	} else if errors.Is(err, os.ErrNotExist) {
		// File does not exist.
		return false, nil
	} else { // nolint:revive
		// Unclear if file exists or not, we cannot stat it.
		return false, fmt.Errorf("failed to check if part file exists: %s: %s", f.finalName, err)
	}
}

// CreateTempFile 在 finalName 旁创建 .tmp 文件并打开写入句柄。
// CreateTempFile creates the temp file to store the data before Finalize is called.
func (f *PartFile) CreateTempFile() error {
	f.lock.Lock()
	defer f.lock.Unlock()

	tmpName := f.finalName + ".tmp"

	fd, err := os.Create(tmpName)
	if err != nil {
		return fmt.Errorf("failed to create part file: %s: %s", tmpName, err)
	}

	f.fd = fd
	return nil
}

// Write to the temporary file.
func (f *PartFile) Write(b []byte) (int, error) {
	return f.fd.Write(b)
}

// Close closes the temporary file.
// Double close is handled gracefully without error so that Close can be deferred for errors,
// and is also called when Finalize is called.
func (f *PartFile) Close() error {
	f.lock.Lock()
	defer f.lock.Unlock()

// Close 通过 fd==nil 防止重复关闭，允许 defer 与 Finalize 各调用一次。
	// Prevent double close
	if f.fd == nil {
		return nil
	}

	filename := f.fd.Name()

	if err := f.fd.Sync(); err != nil {
		return fmt.Errorf("failed to fsync part file: %s: %s", filename, err)
	}

	if err := f.fd.Close(); err != nil {
		return fmt.Errorf("filed to close part file: %s: %s", filename, err)
	}

	f.fd = nil
	return nil
}

// Finalize 先 Sync/Close 临时文件，再 os.Rename 为最终分片文件名。
// Finalize closes the temporary file, and renames it to the final file name.
func (f *PartFile) Finalize() error {
	tmpFileName := f.fd.Name()

	if err := f.Close(); err != nil {
		return fmt.Errorf("failed to close part file: %s: %s", tmpFileName, err)
	}

	f.lock.Lock()
	defer f.lock.Unlock()

	if err := os.Rename(tmpFileName, f.finalName); err != nil {
		return fmt.Errorf("failed to rename part file: %s: %s", tmpFileName, err)
	}

	return nil
}
// mutex 保护 fd 的创建与关闭，避免并发 Write 与 Close 竞态。
