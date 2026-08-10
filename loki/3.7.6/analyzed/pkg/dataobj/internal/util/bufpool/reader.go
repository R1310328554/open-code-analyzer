package bufpool

// reader 子模块复用 bufio.Reader，减少高频 IO 路径上的分配开销。

import (
	"bufio"
	"io"
	"sync"
)

var bufioPool = sync.Pool{
	New: func() any {
		return bufio.NewReader(nil)
	},
}

// GetReader 从池取出 Reader 并重置为读取 r，调用方用完后须 PutReader。
// GetReader returns a pooled [bufio.Reader]. The returned reader is reset to
// read from r.
func GetReader(r io.Reader) *bufio.Reader {
	br := bufioPool.Get().(*bufio.Reader)
	br.Reset(r)
	return br
}

// PutReader 先 Reset(nil) 释放底层引用再归还，归还后不可继续使用。
// PutReader puts the reader back into the pool. It is not safe to use the
// reader after calling PutReader.
func PutReader(br *bufio.Reader) {
	br.Reset(nil) // Release reference to underlying reader.
	bufioPool.Put(br)
}
// sync.Pool 托管 bufio.Reader 实例，适合短生命周期批量读取。
