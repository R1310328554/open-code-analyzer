package v1

// schema 定义 Bloom 块二进制格式的版本号、魔数与压缩编码：当前仅支持 V3，Encode/Decode 读写 magic+version+codec 三元组头。

import (
	"fmt"
	"io"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/compression"
	"github.com/grafana/loki/v3/pkg/util/encoding"
)

type Version byte

func (v Version) String() string {
	return fmt.Sprintf("v%d", v)
}

const (
	magicNumber = uint32(0xCA7CAFE5)

	// Add new versions below
	V1 Version = iota
	// V2 supports single series blooms encoded over multiple pages
	// to accommodate larger single series
	V2
	// V2 indicated schema for indexed structured metadata
	V3

	CurrentSchemaVersion = V3
)

var (
	SupportedVersions = []Version{V3}

	ErrUnsupportedSchemaVersion = errors.New("unsupported schema version")
)

// Schema 绑定 version 与 compression.Codec，Compatible 要求完全一致才兼容。
type Schema struct {
	version  Version
	encoding compression.Codec
}

func NewSchema(version Version, encoding compression.Codec) Schema {
	return Schema{
		version:  version,
		encoding: encoding,
	}
}

func (s Schema) String() string {
	return fmt.Sprintf("%s,encoding=%s", s.version, s.encoding)
}

func (s Schema) Compatible(other Schema) bool {
	return s == other
}

func (s Schema) Version() Version {
	return s.version
}

// Len 返回 schema 头字节长度：4 字节魔数 + 1 版本 + 1 编码。
// byte length
func (s Schema) Len() int {
	// magic number + version + encoding
	return 4 + 1 + 1
}

func (s *Schema) DecompressorPool() compression.ReaderPool {
	return compression.GetReaderPool(s.encoding)
}

func (s *Schema) CompressorPool() compression.WriterPool {
	return compression.GetWriterPool(s.encoding)
}

func (s *Schema) Encode(enc *encoding.Encbuf) {
	enc.Reset()
	enc.PutBE32(magicNumber)
	enc.PutByte(byte(s.version))
	enc.PutByte(byte(s.encoding))
}

// DecodeFrom 从 ReadSeeker 读取固定长度头并校验魔数、版本 V3 及合法 codec。
func (s *Schema) DecodeFrom(r io.ReadSeeker) error {
	// TODO(owen-d): improve allocations
	schemaBytes := make([]byte, s.Len())
	_, err := io.ReadFull(r, schemaBytes)
	if err != nil {
		return errors.Wrap(err, "reading schema")
	}

	dec := encoding.DecWith(schemaBytes)
	return s.Decode(&dec)
}

func (s *Schema) Decode(dec *encoding.Decbuf) error {
	number := dec.Be32()
	if number != magicNumber {
		return errors.Errorf("invalid magic number. expected %x, got  %x", magicNumber, number)
	}
	s.version = Version(dec.Byte())
	if s.version != V3 {
		return errors.Errorf("invalid version. expected %d, got %d", 3, s.version)
	}

	s.encoding = compression.Codec(dec.Byte())
	if _, err := compression.ParseCodec(s.encoding.String()); err != nil {
		return errors.Wrap(err, "parsing encoding")
	}

	return dec.Err()
}
// V2 支持单 series 跨多页 bloom；V3 增加 indexed structured metadata 字段模式。
