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

// 测试临时目录：MkdirTemp 封装、Close 重试删除、DirHash 对目录内容做 SHA256 指纹。

package testutil

import (
	"crypto/sha256"
	"io"
	"os"
	"path/filepath"
	"strconv"
	"testing"

	"github.com/stretchr/testify/require"
)

const (
	// The base directory used for test emissions, which instructs the operating
	// system to use the default temporary directory as the base or TMPDIR
	// environment variable.
	defaultDirectory = ""

	// NilCloser is a no-op Closer.
	NilCloser = nilCloser(true)

	// The number of times that a TemporaryDirectory will retry its removal.
	temporaryDirectoryRemoveRetries = 2
)

type (
	// Closer 封装目录清理的 Close 方法。
// Closer is the interface that wraps the Close method.
	Closer interface {
		// Close reaps the underlying directory and its children. The directory
		// could be deleted by its users already.
		Close()
	}

	nilCloser bool

	// TemporaryDirectory 表示可关闭的临时路径，供测试读写后清理。
// TemporaryDirectory models a closeable path for transient POSIX disk
	// activities.
	TemporaryDirectory interface {
		Closer

		// Path returns the underlying path for access.
		Path() string
	}

	// temporaryDirectory is kept as a private type due to private fields and
	// their interactions.
	temporaryDirectory struct {
		path   string
		tester testing.TB
	}

	callbackCloser struct {
		fn func()
	}
)

func (nilCloser) Close() {
}

func (c callbackCloser) Close() {
	c.fn()
}

// NewCallbackCloser 返回关闭时执行 fn 的 Closer。
// NewCallbackCloser returns a Closer that calls the provided function upon
// closing.
func NewCallbackCloser(fn func()) Closer {
	return &callbackCloser{
		fn: fn,
	}
}

func (t temporaryDirectory) Close() {
	retries := temporaryDirectoryRemoveRetries
	err := os.RemoveAll(t.path)
	for err != nil && retries > 0 {
		switch {
		case os.IsNotExist(err):
			err = nil
		default:
			retries--
			err = os.RemoveAll(t.path)
		}
	}
	require.NoError(t.tester, err)
}

func (t temporaryDirectory) Path() string {
	return t.path
}

// NewTemporaryDirectory 在系统 temp 下创建目录并绑定 testing.TB 做断言清理。
// NewTemporaryDirectory creates a new temporary directory for transient POSIX
// activities.
func NewTemporaryDirectory(name string, t testing.TB) (handler TemporaryDirectory) {
	var (
		directory string
		err       error
	)

	directory, err = os.MkdirTemp(defaultDirectory, name)
	require.NoError(t, err)

	handler = temporaryDirectory{
		path:   directory,
		tester: t,
	}

	return handler
}

// DirHash 遍历目录文件，将内容、大小、名称与修改时间写入 SHA256。
// DirHash returns a hash of all files attributes and their content within a directory.
func DirHash(t *testing.T, path string) []byte {
	hash := sha256.New()
	err := filepath.Walk(path, func(path string, info os.FileInfo, err error) error {
		require.NoError(t, err)

		if info.IsDir() {
			return nil
		}
		f, err := os.Open(path)
		require.NoError(t, err)
		defer f.Close()

		_, err = io.Copy(hash, f)
		require.NoError(t, err)

		_, err = io.WriteString(hash, strconv.Itoa(int(info.Size())))
		require.NoError(t, err)

		_, err = io.WriteString(hash, info.Name())
		require.NoError(t, err)

		modTime, err := info.ModTime().GobEncode()
		require.NoError(t, err)

		_, err = hash.Write(modTime)
		require.NoError(t, err)
		return nil
	})
	require.NoError(t, err)

	return hash.Sum(nil)
}
