package dataset

// value_encoding 定义值编解码接口与全局注册表，
// 各 encoding 实现文件在 init 中调用 registerValueEncoding 注册。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/metadata/datasetmd"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/streamio"
	"github.com/grafana/loki/v3/pkg/memory"
)

// valueEncoder 按物理类型与编码类型将 Value 序列写入 streamio.Writer。
// A valueEncoder encodes sequences of [Value], writing them to an underlying
// [streamio.Writer]. Implementations of encoding types must call
// registerValueEncoding to register themselves.
type valueEncoder interface {
	// PhysicalType returns the type of values supported by the valueEncoder.
	PhysicalType() datasetmd.PhysicalType

	// EncodingType returns the encoding type used by the valueEncoder.
	EncodingType() datasetmd.EncodingType

	// Encode encodes an individual [Value]. Encode returns an error if encoding
	// fails or if value is an unsupported type.
	Encode(value Value) error

	// Flush encodes any buffered data and immediately writes it to the
	// underlying [streamio.Writer]. Flush returns an error if encoding fails.
	Flush() error

	// Reset discards any state and resets the valueEncoder to write to w. This
	// permits reusing a valueEncoder rather than allocating a new one.
	Reset(w streamio.Writer)
}

// valueDecoder 从字节切片解码为 columnar.Array，流结束返回 io.EOF。
// A valueDecoder reads values from an underlying byte slice. Implementations of
// encoding types must call registerValueEncoding to register themselves.
type valueDecoder interface {
	// PhysicalType returns the type of values supported by the decoder.
	PhysicalType() datasetmd.PhysicalType

	// EncodingType returns the encoding type used by the decoder.
	EncodingType() datasetmd.EncodingType

	// Decode decodes an array up to count values using the provided allocator.
	// At the end of the stream, Decode returns nil, [io.EOF].
	Decode(alloc *memory.Allocator, count int) (columnar.Array, error)

	// Reset discards any state and resets the decoder to read from data.
	// This permits reusing a decoder rather than allocating a new one.
	Reset(data []byte)
}

// registry stores known value encoders and decoders. We use a global variable
// to track implementations to allow encoding implementations to be
// self-contained in a single file.
var registry = map[registryKey]registryEntry{}

type (
	registryKey struct {
		Physical datasetmd.PhysicalType
		Encoding datasetmd.EncodingType
	}

	registryEntry struct {
		NewEncoder func(streamio.Writer) valueEncoder
		NewDecoder func([]byte) valueDecoder
	}
)

// registerValueEncoding registers an encoder and decoder for a specified
// physicalType and encodingType tuple. If another encoding has been registered
// for the same tuple, registerValueEncoding panics.
//
// registerValueEncoding should be called in an init method of files
// implementing encodings.
// registerValueEncoding 注册物理/编码类型元组对应的编解码工厂。
func registerValueEncoding(
	physicalType datasetmd.PhysicalType,
	encodingType datasetmd.EncodingType,
	entry registryEntry,
) {
	key := registryKey{
		Physical: physicalType,
		Encoding: encodingType,
	}
	if _, exist := registry[key]; exist {
		panic(fmt.Sprintf("dataset: registerValueEncoding already called for %s/%s", physicalType, encodingType))
	}

	registry[key] = entry
}

// newValueEncoder creates a new valueEncoder for the specified physicalType and
// encodingType. If no encoding is registered for the specified combination of
// physicalType and encodingType, newValueEncoder returns nil and false.
// newValueEncoder 按类型查找注册表并构造编码器实例。
func newValueEncoder(physicalType datasetmd.PhysicalType, encodingType datasetmd.EncodingType, w streamio.Writer) (valueEncoder, bool) {
	key := registryKey{
		Physical: physicalType,
		Encoding: encodingType,
	}
	entry, exist := registry[key]
	if !exist {
		return nil, false
	}
	return entry.NewEncoder(w), true
}

// newValueDecoder creates a new decoder for the specified physicalType and
// encodingType. If no encoding is registered for the specified combination of
// physicalType and encodingType, newValueDecoder returns nil and false.
// newValueDecoder 按类型查找注册表并构造解码器实例。
func newValueDecoder(physicalType datasetmd.PhysicalType, encodingType datasetmd.EncodingType, data []byte) (valueDecoder, bool) {
	key := registryKey{
		Physical: physicalType,
		Encoding: encodingType,
	}
	entry, exist := registry[key]
	if !exist {
		return nil, false
	}

	switch {
	case entry.NewDecoder != nil:
		return entry.NewDecoder(data), true
	default:
		return nil, false
	}
}
// 重复注册同一类型组合会 panic，保证编码实现全局唯一。
