// GGUF 缓冲读取：在 bufio.Reader 上追踪字节偏移。
package gguf

import (
	"bufio"
	"io"
)

// bufferedReader 包装 bufio.Reader 并维护逻辑文件偏移。
type bufferedReader struct {
	offset int64
	*bufio.Reader
}

// newBufferedReader 创建指定缓冲大小的偏移追踪读取器。
func newBufferedReader(rs io.ReadSeeker, size int) *bufferedReader {
	return &bufferedReader{
		Reader: bufio.NewReaderSize(rs, size),
	}
}

// Read 读取数据并累加 offset。
func (rs *bufferedReader) Read(p []byte) (n int, err error) {
	n, err = rs.Reader.Read(p)
	rs.offset += int64(n)
	return n, err
}
