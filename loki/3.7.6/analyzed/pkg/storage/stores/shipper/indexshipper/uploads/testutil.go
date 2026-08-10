package uploads

// uploads testutil 提供 mockIndex 与 buildTestIndexes：在临时目录生成若干假索引文件供 upload 单元测试使用。

import (
	"fmt"
	"io"
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
)

type mockIndex struct {
	*os.File
}

func newMockIndex(t *testing.T, path string) *mockIndex {
	fl, err := os.Create(path)
	require.NoError(t, err)
	return &mockIndex{fl}
}

func (m *mockIndex) Name() string {
	return filepath.Base(m.File.Name())
}

func (m *mockIndex) Path() string {
	return m.File.Name()
}

func (m *mockIndex) Reader() (io.ReadSeeker, error) {
	return m.File, nil
}

// buildTestIndexes 批量创建 index-N 文件并写入可识别内容便于断言。
func buildTestIndexes(t *testing.T, path string, numIndexes int) map[string]*mockIndex {
	testIndexes := make(map[string]*mockIndex)
	for i := 0; i < numIndexes; i++ {
		fileName := fmt.Sprintf("index-%d", i)
		indexPath := filepath.Join(path, fileName)

		index := newMockIndex(t, indexPath)
		_, err := index.WriteString(fileName)
		require.NoError(t, err)

		testIndexes[indexPath] = index
	}

	return testIndexes
}
// newMockIndex 用 require 断言创建失败，测试用例可专注 upload 逻辑本身。
