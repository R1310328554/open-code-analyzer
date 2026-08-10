package compression

// compression 包定义 chunk 可用的压缩编解码器枚举：
// 常量顺序不可变更，因数值会序列化写入存储。

import (
	"fmt"
	"slices"
	"strings"
)

// Codec 用 byte 枚举标识 chunk 压缩算法，支持 gzip、lz4、snappy、flate、zstd 等。
// Codec identifies an available compression codec.
type Codec byte

// The different available codecs
// Make sure to preserve the order, as the numeric values are serialized!
//
//nolint:revive
const (
	None Codec = iota
	GZIP
	Dumb // not supported
	LZ4_64k
	Snappy
	LZ4_256k
	LZ4_1M
	LZ4_4M
	Flate
	Zstd
)

var supportedCodecs = []Codec{
	None,
	GZIP,
	LZ4_64k,
	Snappy,
	LZ4_256k,
	LZ4_1M,
	LZ4_4M,
	Flate,
	Zstd,
}

func (e Codec) String() string {
	switch e {
	case GZIP:
		return "gzip"
	case None:
		return "none"
	case LZ4_64k:
		return "lz4-64k"
	case LZ4_256k:
		return "lz4-256k"
	case LZ4_1M:
		return "lz4-1M"
	case LZ4_4M:
		return "lz4"
	case Snappy:
		return "snappy"
	case Flate:
		return "flate"
	case Zstd:
		return "zstd"
	default:
		return "unknown"
	}
}

// IsSupported 判断该 Codec 是否在 supportedCodecs 列表中。
// IsSupported reports whether the codec is one this package can read and write.
func (e Codec) IsSupported() bool {
	return slices.Contains(supportedCodecs, e)
}

// ParseCodec 按名称（大小写不敏感）解析 Codec，失败时返回支持列表。
// ParseCodec parses a chunk encoding (compression codec) by its name.
func ParseCodec(enc string) (Codec, error) {
	for _, e := range supportedCodecs {
		if strings.EqualFold(e.String(), enc) {
			return e, nil
		}
	}
	return 0, fmt.Errorf("invalid encoding: %s, supported: %s", enc, SupportedCodecs())
}

// SupportedCodecs 返回逗号分隔的可用编解码器名称字符串。
// SupportedCodecs returns the list of supported Encoding.
func SupportedCodecs() string {
	var sb strings.Builder
	for i := range supportedCodecs {
		sb.WriteString(supportedCodecs[i].String())
		if i != len(supportedCodecs)-1 {
			sb.WriteString(", ")
		}
	}
	return sb.String()
}
