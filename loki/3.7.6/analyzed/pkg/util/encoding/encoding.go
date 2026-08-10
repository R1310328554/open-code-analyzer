package encoding

// encoding 包扩展 Prometheus TSDB Encbuf/Decbuf，支持多字节读写、跳过预留空间及 Castagnoli CRC 尾部校验。

import (
	"encoding/binary"
	"hash/crc32"

	"github.com/prometheus/prometheus/tsdb/encoding"
)

func EncWith(b []byte) (res Encbuf) {
	res.B = b
	return res
}

func EncWrap(inner encoding.Encbuf) Encbuf { return Encbuf{Encbuf: inner} }

// Encbuf 嵌入 TSDB Encbuf 并增加 PutString/Skip 等 Loki 块格式所需方法。
// Encbuf extends encoding.Encbuf with support for multi byte encoding
type Encbuf struct {
	encoding.Encbuf
}

func (e *Encbuf) PutString(s string) { e.B = append(e.B, s...) }

func (e *Encbuf) Skip(i int) {
	e.B = e.B[:len(e.B)+i]
}

func DecWith(b []byte) (res Decbuf) {
	res.B = b
	return res
}

func DecWrap(inner encoding.Decbuf) Decbuf { return Decbuf{Decbuf: inner} }

// Decbuf 提供 Bytes 切片读取与 CheckCrc，校验失败时设置 Decbuf.E 错误态。
// Decbuf extends encoding.Decbuf with support for multi byte decoding
type Decbuf struct {
	encoding.Decbuf
}

func (d *Decbuf) Bytes(n int) []byte {
	if d.E != nil {
		return nil
	}
	if len(d.B) < n {
		d.E = encoding.ErrInvalidSize
		return nil
	}
	x := d.B[:n]
	d.B = d.B[n:]
	return x
}

// CheckCrc 读取尾部 4 字节大端 CRC，与缓冲区内容计算的 Castagnoli 值比对。
func (d *Decbuf) CheckCrc(castagnoliTable *crc32.Table) error {
	if d.E != nil {
		return d.E
	}
	if len(d.B) < 4 {
		d.E = encoding.ErrInvalidSize
		return d.E
	}

	offset := len(d.B) - 4
	expCRC := binary.BigEndian.Uint32(d.B[offset:])
	d.B = d.B[:offset]

	if d.Crc32(castagnoliTable) != expCRC {
		d.E = encoding.ErrInvalidChecksum
		return d.E
	}
	return nil
}
// DecWith/EncWith 工厂函数避免重复初始化零值结构体，简化块编解码调用链。
