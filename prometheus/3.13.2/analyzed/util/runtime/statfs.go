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

// OpenBSD/NetBSD/Solaris 平台 statfs 桩：FsType/FsSize 暂不支持，返回 unknown/0。

//go:build openbsd || netbsd || solaris

package runtime

// FsType 在此类 Unix 变体上未实现 magic 映射，恒返回 unknown。
// FsType returns the file system type or "unknown" if unsupported.
func FsType(path string) string {
	return "unknown"
}

// FsSize 未调用 statfs，无法获取容量时返回 0。
// FsSize returns the file system size or 0 if unsupported.
func FsSize(path string) uint64 {
	return 0
}
