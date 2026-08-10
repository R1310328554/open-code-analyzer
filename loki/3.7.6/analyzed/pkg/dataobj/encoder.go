package dataobj

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"slices"

	"github.com/grafana/loki/v3/pkg/dataobj/internal/metadata/filemd"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/streamio"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/protocodec"
	"github.com/grafana/loki/v3/pkg/scratch"
)

var (
	magic = []byte("DOBJ")

	// legacyMagic is the magic bytes used in the original dataobj format where
	// file metadata was kept at the bottom of the file.
	legacyMagic = []byte("THOR")
)

const (
	fileFormatVersion = 0x1
)

// encoder 将多个 section 编码为层级化 data object，元数据置于头部、各 section 数据与元数据区连续排列。
// encoder encodes a data object. Data objects are hierarchical, split into
// distinct sections that contain their own hierarchy.
type encoder struct {
	totalBytes int // Total bytes buffered in store so far.

	store    scratch.Store
	sections []sectionInfo // Sections buffered in store.

	typesReady       bool
	dictionary       []string
	dictionaryLookup map[string]uint32
	rawTypes         []*filemd.SectionType
	typeRefLookup    map[SectionType]uint32
}

// sectionInfo 记录单个 section 的类型、数据/元数据区域句柄与租户信息。
type sectionInfo struct {
	Type SectionType

	Data     sectionRegion
	Metadata sectionRegion

	Tenant string // Owning tenant of the section, if any.

	// ExtensionData holds additional encoded info about the section, written to
	// the file-level metadata.
	ExtensionData []byte
}

// newEncoder 基于 scratch.Store 创建编码器，section 先缓冲后 Flush 输出。
// newEncoder creates a new Encoder which writes a data object to the provided
// writer.
func newEncoder(store scratch.Store) *encoder {
	return &encoder{
		store: store,
	}
}

// AppendSection 追加 section 数据与元数据到内部 store，并累计字节计数。
// AppendSection appends a section to the data object. AppendSection panics if
// typ is not SectionTypeLogs or SectionTypeStreams.
func (enc *encoder) AppendSection(typ SectionType, opts *WriteSectionOptions, data, metadata []byte) {
	var (
		dataHandle     = enc.store.Put(data)
		metadataHandle = enc.store.Put(metadata)
	)

	si := sectionInfo{
		Type: typ,

		Data:     sectionRegion{Handle: dataHandle, Size: len(data)},
		Metadata: sectionRegion{Handle: metadataHandle, Size: len(metadata)},
	}
	if opts != nil {
		si.Tenant = opts.Tenant
		si.ExtensionData = slices.Clone(opts.ExtensionData) // Avoid retaining references to caller memory.
	}

	enc.sections = append(enc.sections, si)
	enc.totalBytes += len(data) + len(metadata)
}

// getTypeRef 为 SectionType 分配或查找类型引用，维护字典与 rawTypes 表。
// getTypeRef returns the type reference for the given type or creates a new
// one.
func (enc *encoder) getTypeRef(typ SectionType) uint32 {
	if !enc.typesReady {
		enc.initTypeRefs()
	}

	ref, ok := enc.typeRefLookup[typ]
	if !ok {
		// Create a new type reference.
		enc.typeRefLookup[typ] = uint32(len(enc.rawTypes))
		enc.rawTypes = append(enc.rawTypes, &filemd.SectionType{
			NameRef: &filemd.SectionType_NameRef{
				NamespaceRef: enc.getDictionaryKey(typ.Namespace),
				KindRef:      enc.getDictionaryKey(typ.Kind),
			},
			Version: typ.Version,
		})
		return enc.typeRefLookup[typ]
	}
	return ref
}

func (enc *encoder) initTypeRefs() {
	enc.initDictionary()

	enc.rawTypes = []*filemd.SectionType{
		{NameRef: nil}, // Invalid type.
	}

	var invalidType SectionType // Zero value for SectionType is reserved for invalid types.
	enc.typeRefLookup = map[SectionType]uint32{invalidType: 0}

	enc.typesReady = true
}

func (enc *encoder) initDictionary() {
	if len(enc.dictionary) > 0 && len(enc.dictionaryLookup) > 0 {
		return // Already initialized.
	}

	// Reserve the zero index in the dictionary for an invalid entry. This is
	// only required for the type refs, but it's still easier to debug.
	enc.dictionary = []string{""}
	enc.dictionaryLookup = map[string]uint32{"": 0}
}

// getDictionaryKey 为字符串分配字典索引，零索引保留为无效项。
// getDictionaryKey returns the dictionary key for the given text or creates a
// new entry.
func (enc *encoder) getDictionaryKey(text string) uint32 {
	enc.initDictionary()

	key, ok := enc.dictionaryLookup[text]
	if ok {
		return key
	}

	key = uint32(len(enc.dictionary))
	enc.dictionary = append(enc.dictionary, text)
	enc.dictionaryLookup[text] = key
	return key
}

// Metadata 两遍扫描 section 列表，先布局元数据区再布局数据区偏移。
func (enc *encoder) Metadata() (*filemd.Metadata, error) {
	enc.initDictionary()

	// relativeOffset is the offset where regions are written relative to the
	// "body" of the file (e.g., after the header).
	//
	// Decoders use the known header size to reinterpret the offset into an
	// absolute offset.
	relativeOffset := 0

	// The metadata regions and data regions of all sections are encoded
	// contiguously. To represent this in the SectionInfo headers, we update
	// them in two passes, once for the data and once for the metadata.
	sections := make([]*filemd.SectionInfo, len(enc.sections))

	// Determine metadata region location.
	for i, info := range enc.sections {
		sections[i] = &filemd.SectionInfo{
			TypeRef: enc.getTypeRef(info.Type),
			Layout: &filemd.SectionLayout{
				Metadata: &filemd.Region{
					Offset: uint64(relativeOffset),
					Length: uint64(info.Metadata.Size),
				},
			},

			ExtensionData: info.ExtensionData,
			TenantRef:     enc.getDictionaryKey(info.Tenant),
		}

		relativeOffset += info.Metadata.Size
	}

	// Determine data region locations.
	for i, info := range enc.sections {
		// sections[i] is initialized in the previous loop, so we can directly
		// update the layout to include the data region.
		sections[i].Layout.Data = &filemd.Region{
			Offset: uint64(relativeOffset),
			Length: uint64(info.Data.Size),
		}

		relativeOffset += info.Data.Size
	}

	md := &filemd.Metadata{
		Sections:   sections,
		Dictionary: enc.dictionary,
		Types:      enc.rawTypes,
	}
	return md, nil
}

// encodeSize 估算完整编码后的对象字节数，含头、体、尾与 protobuf 元数据。
// encodeSize reports the final encoded size of the object.
func (enc *encoder) encodeSize(computedMetadata *filemd.Metadata) int64 {
	var size int64

	size += int64(len(legacyMagic)) // header

	// body
	for _, sec := range enc.sections {
		size += int64(sec.Data.Size)
		size += int64(sec.Metadata.Size)
	}

	// metadata
	size += int64(streamio.UvarintSize(fileFormatVersion))
	size += int64(protocodec.Size(computedMetadata))

	// tailer
	size += int64(4) // file metadata size (32 bits)
	size += int64(len(legacyMagic))

	return size
}

// Bytes returns the total number of bytes appended to the data object.
func (enc *encoder) Bytes() int { return enc.totalBytes }

// Flush 生成可流式读取的 snapshot，调用方须在完成后调用 Reset。
// Flush converts all accumulated data into a [snapshot], allowing for streaming
// encoded bytes. [snapshot.Close] should be called when the snapshot is no
// longer needed to release sections.
//
// Callers must manually call [encoder.Reset] after calling Flush to prepare for
// the next encoding session. This is done to allow callers to ensure that the
// returned snapshot is complete before the encoder is reset.
func (enc *encoder) Flush() (*snapshot, error) {
	if len(enc.sections) == 0 {
		return nil, fmt.Errorf("empty Encoder")
	}

	// The overall structure is:
	//
	// header:
	//  [magic]
	//  [file metadata + version size (32 bits)]
	//  [file metadata version]
	//  [file metadata]
	// body:
	//  [metadata regions]
	//  [data regions]
	// tailer:
	//  [magic]

	var headerBuffer bytes.Buffer

	metadata, err := enc.Metadata()
	if err != nil {
		return nil, fmt.Errorf("building metadata: %w", err)
	} else if _, err := headerBuffer.Write(magic); err != nil {
		return nil, fmt.Errorf("writing magic header: %w", err)
	}

	metadataSize := streamio.UvarintSize(fileFormatVersion) + protocodec.Size(metadata)

	if err := binary.Write(&headerBuffer, binary.LittleEndian, uint32(metadataSize)); err != nil {
		return nil, fmt.Errorf("writing metadata size: %w", err)
	} else if err := streamio.WriteUvarint(&headerBuffer, fileFormatVersion); err != nil {
		return nil, err
	} else if err := protocodec.Encode(&headerBuffer, metadata); err != nil {
		return nil, err
	}

	// Convert our sections into regions for the snapshot to use. The order of
	// regions *must* match the order of offset+length written in
	// [encoder.Metadata]: all the metadata regions, followed by all the data
	// regions.
	//
	// We encode metadata regions first to allow a single prefetch of the start
	// of the file to also prefetch metadata regions.
	regions := make([]sectionRegion, 0, len(enc.sections)*2)
	for _, sec := range enc.sections {
		regions = append(regions, sec.Metadata)
	}
	for _, sec := range enc.sections {
		regions = append(regions, sec.Data)
	}

	snapshot, err := newSnapshot(
		enc.store,
		headerBuffer.Bytes(), // header
		regions,
		magic, // tailer
	)
	if err != nil {
		return nil, fmt.Errorf("creating snapshot: %w", err)
	}
	return snapshot, nil
}

// Reset 清空 section 缓冲、字典与类型表，准备下一轮编码。
// Reset resets the Encoder to a fresh state.
func (enc *encoder) Reset() {
	enc.totalBytes = 0

	enc.sections = nil

	enc.typesReady = false
	enc.dictionary = nil
	enc.dictionaryLookup = nil
	enc.rawTypes = nil
	enc.typeRefLookup = nil
}

// countingWriter 包装 Writer 并统计已写字节数，用于尺寸观测。
type countingWriter struct {
	w     streamio.Writer
	count int64
}

func (cw *countingWriter) Write(p []byte) (n int, err error) {
	n, err = cw.w.Write(p)
	cw.count += int64(n)
	return n, err
}

func (cw *countingWriter) WriteByte(c byte) error {
	if err := cw.w.WriteByte(c); err != nil {
		return err
	}
	cw.count++
	return nil
}
// 编码器采用先 metadata 后 data 的连续布局，便于单次预读覆盖全部 section 元数据。
