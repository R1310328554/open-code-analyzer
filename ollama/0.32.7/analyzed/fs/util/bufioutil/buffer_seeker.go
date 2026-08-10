// 带 Seek 的缓冲读取：ReadSeeker 与 bufio.Reader 协同定位。
package bufioutil

import (
	"bufio"
	"io"
)

// BufferedSeeker 在 ReadSeeker 上叠加 bufio 缓冲并正确 Seek。
type BufferedSeeker struct {
	rs io.ReadSeeker
	br *bufio.Reader
}

// NewBufferedSeeker 创建指定缓冲大小的 BufferedSeeker。
func NewBufferedSeeker(rs io.ReadSeeker, size int) *BufferedSeeker {
	return &BufferedSeeker{
		rs: rs,
		br: bufio.NewReaderSize(rs, size),
	}
}

// Read 从内部 bufio.Reader 读取。
func (b *BufferedSeeker) Read(p []byte) (int, error) {
	return b.br.Read(p)
}

// Seek 调整底层 ReadSeeker 位置并重置 bufio 缓冲。
func (b *BufferedSeeker) Seek(offset int64, whence int) (int64, error) {
	if whence == io.SeekCurrent {
		offset -= int64(b.br.Buffered())
	}
	n, err := b.rs.Seek(offset, whence)
	if err != nil {
		return 0, err
	}
	b.br.Reset(b.rs)
	return n, nil
}
