package util //nolint:revive

// ReadSeeker 将任意 io.Reader 适配为 io.ReadSeeker，供对象存储 PutObject 等需要可重定位读的场景。

import (
	"bytes"
	"fmt"
	"io"
)

// ReadSeeker 若 r 已实现 ReadSeeker 则直接返回，否则 ReadAll 后包装为 bytes.NewReader。
func ReadSeeker(r io.Reader) (io.ReadSeeker, error) {
	if rs, ok := r.(io.ReadSeeker); ok {
		return rs, nil
	}
	data, err := io.ReadAll(r)
	if err != nil {
		return nil, fmt.Errorf("error in ReadSeeker ReadAll(): %w", err)
	}
	return bytes.NewReader(data), nil
}
// 全量读入内存适用于中小型 payload；大对象应优先使用原生 ReadSeeker 避免额外拷贝。
