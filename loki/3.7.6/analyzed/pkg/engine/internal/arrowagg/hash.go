package arrowagg

// Arrow schema/field/metadata 哈希：为 Records 去重与缓存提供稳定指纹。

import (
	"hash/maphash"

	"github.com/apache/arrow-go/v18/arrow"
)

// hashSchema 将字节序、各字段及 schema 级 metadata 写入 maphash。
// hashSchema hashes the schema into h.
func hashSchema(h *maphash.Hash, schema *arrow.Schema) {
	_, _ = h.WriteString(schema.Endianness().String())

	// [arrow.Schema.Fields] creates a copy of the fields slice, so we want to
	// iterate over indices instead to avoid unnecessary allocations.
	for i := range schema.NumFields() {
		hashField(h, schema.Field(i))
	}

	hashMetadata(h, schema.Metadata())
}

// hashField 哈希字段名、类型指纹、可空标志与字段 metadata。
// hashField hashes a field into h.
func hashField(h *maphash.Hash, field arrow.Field) {
	_, _ = h.WriteString(field.Name)
	_, _ = h.WriteString(field.Type.Fingerprint())

	if field.Nullable {
		_ = h.WriteByte(1)
	} else {
		_ = h.WriteByte(0)
	}

	hashMetadata(h, field.Metadata)
}

// hashMetadata 按 key 顺序将键值对序列化进哈希状态。
// hashMetadata hashes Arrow metadata into h.
func hashMetadata(h *maphash.Hash, md arrow.Metadata) {
	for _, key := range md.Keys() {
		value, _ := md.GetValue(key)

		_, _ = h.WriteString(key)
		_, _ = h.WriteString(value)
	}
}
// 字段迭代使用索引访问以避免 schema.Fields() 分配额外切片。
