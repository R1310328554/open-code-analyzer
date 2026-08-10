package util //nolint:revive

// util 包 SizeReader 包装 io.Reader 并累计已读字节数，供压缩/解压路径统计实际消耗流量而不二次扫描。

import (
	"io"
)

type sizeReader struct {
	size int64
	r    io.Reader
}

type SizeReader interface {
	io.Reader
	Size() int64
}

// NewSizeReader 返回 SizeReader，Size() 可随时查询累计读取字节。
// NewSizeReader returns an io.Reader that will have the number of bytes
// read from r available.
func NewSizeReader(r io.Reader) SizeReader {
	return &sizeReader{r: r}
}

func (v *sizeReader) Read(p []byte) (int, error) {
	n, err := v.r.Read(p)
	v.size += int64(n)
	return n, err
}

func (v *sizeReader) Size() int64 {
	return v.size
}
// Read 错误时仍更新已成功读取的字节计数，与标准 io.Reader 语义一致。
