package mempool

// mempool 包 bucket 子类型描述内存池 slab 配置：Size 为缓冲个数，Capacity 为单块字节容量，支持人类可读字符串解析。

import (
	"errors"
	"fmt"
	"strconv"
	"strings"

	"github.com/c2h5oh/datasize"
)

type Bucket struct {
	Size     int    // Number of buffers
	Capacity uint64 // Size of a buffer
}

func (b Bucket) Parse(s string) (any, error) {
	parts := strings.Split(s, "x")
	if len(parts) != 2 {
		return nil, errors.New("bucket must be in format {count}x{bytes}")
	}

	size, err := strconv.Atoi(parts[0])
	if err != nil {
		return nil, err
	}

	capacity, err := datasize.ParseString(parts[1])
	if err != nil {
		panic(err.Error())
	}

	return Bucket{
		Size:     size,
		Capacity: uint64(capacity),
	}, nil
}

func (b Bucket) String() string {
	return fmt.Sprintf("%dx%s", b.Size, datasize.ByteSize(b.Capacity).String())
}

// Buckets 为多个 slab 的切片，String 输出逗号分隔便于日志与 flag 展示。
type Buckets []Bucket

func (b Buckets) String() string {
	s := make([]string, 0, len(b))
	for i := range b {
		s = append(s, b[i].String())
	}
	return strings.Join(s, ",")
}
// Parse 容量解析失败时会 panic，调用方应在配置加载阶段提前校验格式。
