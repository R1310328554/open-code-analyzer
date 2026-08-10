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

// Linux 平台文件预分配：syscall.Fallocate 预留块；ENOTSUP/EINTR 时回退 Seek+Truncate。

package fileutil

import (
	"errors"
	"os"
	"syscall"
)

func preallocExtend(f *os.File, sizeInBytes int64) error {
	// use mode = 0 to change size
	err := syscall.Fallocate(int(f.Fd()), 0, 0, sizeInBytes)
	if err != nil {
		var errno syscall.Errno
		// not supported; fallback
		// fallocate EINTRs frequently in some environments; fallback
		if errors.As(err, &errno) && (errno == syscall.ENOTSUP || errno == syscall.EINTR) {
			return preallocExtendTrunc(f, sizeInBytes)
		}
	}
	return err
}

// preallocFixed 以 FALLOC_FL_KEEP_SIZE 预分配但不改变 stat 报告的文件大小。
func preallocFixed(f *os.File, sizeInBytes int64) error {
	// use mode = 1 to keep size; see FALLOC_FL_KEEP_SIZE
	err := syscall.Fallocate(int(f.Fd()), 1, 0, sizeInBytes)
	if err != nil {
		var errno syscall.Errno
		// treat not supported as nil error
		if errors.As(err, &errno) && errno == syscall.ENOTSUP {
			return nil
		}
	}
	return err
}
