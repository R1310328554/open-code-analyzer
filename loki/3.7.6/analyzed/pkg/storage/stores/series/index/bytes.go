package index

// Bytes 为 protobuf customtype，避免 proto 编解码时对底层 byte slice 多余拷贝。

import (
	"bytes"
)

// Bytes 是 []byte 的类型别名，实现 gogo proto 自定义 Marshal/Unmarshal 语义。
// Bytes exists to stop proto copying the byte array
type Bytes []byte

// Marshal 直接返回底层 slice 视图，零拷贝序列化索引缓存条目。
// Marshal just returns bs
func (bs *Bytes) Marshal() ([]byte, error) {
	return []byte(*bs), nil
}

// MarshalTo copies Bytes to data
func (bs *Bytes) MarshalTo(data []byte) (n int, err error) {
	return copy(data, *bs), nil
}

// Unmarshal 将 Bytes 指向传入 buffer，反序列化时不复制 payload。
// Unmarshal updates Bytes to be data, without a copy
func (bs *Bytes) Unmarshal(data []byte) error {
	*bs = data
	return nil
}

// Size returns the length of Bytes
func (bs *Bytes) Size() int {
	return len(*bs)
}

// Equal returns true if other equals Bytes
func (bs *Bytes) Equal(other Bytes) bool {
	return bytes.Equal(*bs, other)
}

// Compare Bytes to other
func (bs *Bytes) Compare(other Bytes) int {
	return bytes.Compare(*bs, other)
}
// Equal/Compare 委托 bytes 包比较列键与值，供索引缓存条目去重与排序。
