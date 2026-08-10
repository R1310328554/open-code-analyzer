package syntax

// linefilter 为 LineFilter 结构提供二进制序列化，供查询计划缓存与分片间传输行过滤器 AST 片段。

import (
	"github.com/grafana/loki/v3/pkg/logql/log"
	"github.com/grafana/loki/v3/pkg/util/encoding"
)

// 编码格式：Ty/Match/Op 均为 uvarint 长度前缀字符串，见上方 ASCII 布局说明。
// Binary encoding of the LineFilter
// integer is varint encoded
// strings are variable-length encoded
//
// +---------+--------------+-------------+
// | Ty      | Match        | Op          |
// +---------+--------------+-------------+
// | value   | len  | value | len | value |
// +---------+--------------+-------------+

// Equal 比较三个字段是否完全一致，用于 AST 去重与缓存键比较。
func (lf LineFilter) Equal(o LineFilter) bool {
	return lf.Ty == o.Ty &&
		lf.Match == o.Match &&
		lf.Op == o.Op
}

func (lf LineFilter) Size() int {
	return lenUint64(uint64(lf.Ty)) +
		lenUint64(uint64(len(lf.Match))) +
		len(lf.Match) +
		lenUint64(uint64(len(lf.Op))) +
		len(lf.Op)
}

// MarshalTo 将 LineFilter 写入 b，使用 encoding.EncWith 追加 uvarint 字段。
func (lf LineFilter) MarshalTo(b []byte) (int, error) {
	buf := encoding.EncWith(b[:0])
	buf.PutUvarint(int(lf.Ty))
	buf.PutUvarintStr(lf.Match)
	buf.PutUvarintStr(lf.Op)
	return len(b), nil
}

func (lf *LineFilter) Unmarshal(b []byte) error {
	buf := encoding.DecWith(b)
	lf.Ty = log.LineMatchType(buf.Uvarint())
	lf.Match = buf.UvarintStr()
	lf.Op = buf.UvarintStr()
	return nil
}

// lenUint64 计算 uvarint 编码长度，与 Size 中预计算逻辑一致。
// utility copied from implementation of binary.PutUvarint()
func lenUint64(x uint64) int {
	i := 0
	for x >= 0x80 {
		x >>= 7
		i++
	}
	return i + 1
}
// Match/Op 为原始字符串，Ty 对应 log.LineMatchType 枚举整型值。
