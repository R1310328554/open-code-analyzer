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

// Linux Direct IO 工厂：按平台选择 bufio 或 O_DIRECT writer，并报告 uncached IO 能力。

//go:build linux && !forcedirectio

package fileutil

import (
	"bufio"
	"os"
)

func NewBufioWriterWithSize(f *os.File, size int) (BufWriter, error) {
	return &writer{bufio.NewWriterSize(f, size)}, nil
}

// NewDirectIOWriter 构造满足对齐约束的 directIOWriter。
func NewDirectIOWriter(f *os.File, size int) (BufWriter, error) {
	return newDirectIOWriter(f, size)
}

func UncachedIOSupported() bool {
	return true
}
