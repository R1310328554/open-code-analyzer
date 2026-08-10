// Copyright The Prometheus Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// fileutil 文件预分配：按平台调用 fallocate/F_PREALLOCATE 或 Seek+Truncate 扩展文件，减少 WAL/block 写入时的磁盘碎片与延迟。

package fileutil

import (
	"io"
	"os"
)

// Preallocate 为文件预留 sizeInBytes 空间；extendFile 为 true 时同时扩展逻辑文件大小。
// Preallocate tries to allocate the space for given
// file. This operation is only supported on linux by a
// few filesystems (btrfs, ext4, etc.).
// If the operation is unsupported, no error will be returned.
// Otherwise, the error encountered will be returned.
func Preallocate(f *os.File, sizeInBytes int64, extendFile bool) error {
	if sizeInBytes == 0 {
		// fallocate will return EINVAL if length is 0; skip
		return nil
	}
	if extendFile {
		return preallocExtend(f, sizeInBytes)
	}
	return preallocFixed(f, sizeInBytes)
}

// preallocExtendTrunc 通过 Seek 到目标偏移并 Truncate 实现通用预分配回退路径。
func preallocExtendTrunc(f *os.File, sizeInBytes int64) error {
	curOff, err := f.Seek(0, io.SeekCurrent)
	if err != nil {
		return err
	}
	size, err := f.Seek(sizeInBytes, io.SeekEnd)
	if err != nil {
		return err
	}
	if _, err = f.Seek(curOff, io.SeekStart); err != nil {
		return err
	}
	if sizeInBytes > size {
		return nil
	}
	return f.Truncate(sizeInBytes)
}
