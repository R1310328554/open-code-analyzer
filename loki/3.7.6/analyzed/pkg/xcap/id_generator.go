package xcap

// xcap 包 identifier 为 8 字节 Capture/Region 唯一 ID：与 OTel SpanID 同格式，便于在 trace 与 xcap 间关联。

import (
	"encoding/binary"
	"encoding/hex"
	"math/rand/v2"
)

// identifier 固定 [8]byte，String 输出小写 hex，IsZero 判断是否未分配。
// identifier is an 8-byte unique identifier for captures and regions.
// It is compatible with otel SpanID, sharing the same [8]byte format.
type identifier [8]byte

// ID is an exported alias for identifier.
type ID = identifier

var zeroID identifier

// IsValid reports whether the ID is valid.
func (id identifier) IsValid() bool {
	return id != zeroID
}

// IsZero reports whether the ID is zero (all zeros).
func (id identifier) IsZero() bool {
	return id == zeroID
}

// String returns the hex string representation of the ID.
func (id identifier) String() string {
	return hex.EncodeToString(id[:])
}

// newID 使用 math/rand/v2 生成非零 ID，全零时循环重抽避免与无效 ID 混淆。
// newID returns a new random ID. The ID is guaranteed to be non-zero.
func newID() identifier {
	var id identifier
	for {
		binary.NativeEndian.PutUint64(id[:], rand.Uint64())
		if id.IsValid() {
			break
		}
	}
	return id
}

// NewID 为跨包导出的 ID 工厂，Capture 与 Region 创建时各自调用。
// NewID returns a new random ID. The ID is guaranteed to be non-zero.
// This is the exported version of newID for use in other packages.
func NewID() ID {
	return newID()
}
// IsValid 等价于非零检查，proto 序列化时 parentID 为零表示根 Region。
