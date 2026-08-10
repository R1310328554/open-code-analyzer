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

// Darwin 平台 Fdatasync：当前实现回退为 Sync（注释提及 F_FULLFSYNC 持久化语义）。

//go:build darwin

package fileutil

import (
	"os"
)

// Fdatasync 在 macOS 上应确保持久化到物理介质；此处暂用 Sync 包装。
// Fdatasync on darwin platform invokes fcntl(F_FULLFSYNC) for actual persistence
// on physical drive media.
func Fdatasync(f *os.File) error {
	return f.Sync()
}
