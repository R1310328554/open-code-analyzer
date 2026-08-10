package downloads

// testutil 为 downloads 包单元测试提供 mock Index 与本地索引文件布置辅助。

import (
	"io"
	"os"
	"path/filepath"
	"strconv"
	"testing"

	"github.com/stretchr/testify/require"
)

type mockIndex struct {
	*os.File
}

func openMockIndexFile(t *testing.T, path string) *mockIndex {
	fl, err := os.Open(path)
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

// setupIndexesAtPath 在指定目录写入 [start,end) 编号文件，返回完整路径列表。
func setupIndexesAtPath(t *testing.T, userID, path string, start, end int) []string {
	require.NoError(t, os.MkdirAll(path, 0750))
	var testIndexes []string
	for ; start < end; start++ {
		fileName := buildIndexFilename(userID, start)
		indexPath := filepath.Join(path, fileName)

		require.NoError(t, os.WriteFile(indexPath, []byte(fileName), 0640)) // #nosec G306 -- this is fencing off the "other" permissions -- nosemgrep: incorrect-default-permissions
		testIndexes = append(testIndexes, indexPath)
	}

	return testIndexes
}

// buildIndexFilename 公共索引用纯数字名，租户索引为 userID-indexNum 格式。
func buildIndexFilename(userID string, indexNum int) string {
	if userID == "" {
		return strconv.Itoa(indexNum)
	}

	return userID + "-" + strconv.Itoa(indexNum)
}
// openMockIndexFile 打开磁盘文件供测试模拟 shipper OpenIndexFileFunc 回调。
