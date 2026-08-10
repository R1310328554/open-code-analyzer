// Copyright The Prometheus Authors
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

// forcedirectio 构建标签：测试环境强制所有写入走 Direct IO 路径。

// This allows seamless testing of the Direct I/O writer across all tsdb tests.

//go:build linux && forcedirectio

package fileutil

import "os"

func NewDirectIOWriter(f *os.File, size int) (BufWriter, error) {
	return newDirectIOWriter(f, size)
}

// NewBufioWriterWithSize 在测试构建中同样路由到 Direct IO writer。
func NewBufioWriterWithSize(f *os.File, size int) (BufWriter, error) {
	return NewDirectIOWriter(f, size)
}

func UncachedIOSupported() bool {
	return true
}
