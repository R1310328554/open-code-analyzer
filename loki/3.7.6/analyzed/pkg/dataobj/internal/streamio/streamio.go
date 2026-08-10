// streamio 包定义 dataobj 编解码共用的流式二进制读写接口。
// Package streamio defines interfaces shared by other packages for streaming
// binary data.
package streamio

import "io"

// Reader is an interface that combines an [io.Reader] and an [io.ByteReader].
// Reader 组合 io.Reader 与 io.ByteReader，支持按字节流式读取。
type Reader interface {
	io.Reader
	io.ByteReader
}

// Writer is an interface that combines an [io.Writer] and an [io.ByteWriter].
type Writer interface {
	io.Writer
	io.ByteWriter
}

// Discard is a [Writer] for which all calls succeed without doing anything.
// Discard 为丢弃所有写入的成功 Writer，用于仅计数或测大小的编码路径。
var Discard Writer = discard{}

type discard struct{}

func (discard) Write(p []byte) (int, error) { return len(p), nil }
func (discard) WriteByte(_ byte) error      { return nil }
// discard 实现空操作 Write/WriteByte，调用始终成功。
