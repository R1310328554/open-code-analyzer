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

// 非 Windows 平台 head chunk 文件预分配大小：小量预分配避免单测 mmap flake。

//go:build !windows

package chunks

// HeadChunkFilePreallocationSize 新建 head chunk 文件时的预分配字节数（非 Windows）。
// HeadChunkFilePreallocationSize is the size to which the m-map file should be preallocated when a new file is cut.
// Windows needs pre-allocations while the other OS does not. But we observed that a 0 pre-allocation causes unit tests to flake.
// This small allocation for non-Windows OSes removes the flake.
var HeadChunkFilePreallocationSize int64 = MinWriteBufferSize * 2
