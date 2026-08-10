// safetensors 张量提取：流式读取单张量/打包 blob，避免整文件进内存。
package safetensors

import (
	"bytes"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"sort"
)

// tensorInfo 保存 safetensors 头中的张量元数据。
// tensorInfo holds tensor metadata from safetensors headers.
type tensorInfo struct {
	Dtype       string  `json:"dtype"`
	Shape       []int32 `json:"shape"`
	DataOffsets [2]int  `json:"data_offsets"`
}

// TensorExtractor 从 safetensors 文件提取单张量；每路 io.Reader 可流式写入 blob。
// TensorExtractor extracts individual tensors from a safetensors file.
// It provides io.Reader interfaces for each tensor's raw data, enabling
// streaming writes to blobs without loading entire tensors into memory.
type TensorExtractor struct {
	file       *os.File
	dataOffset int64 // 张量数据区起始偏移
	// Start of tensor data region
	header     map[string]tensorInfo
}

// TensorData 含张量元数据及原始字节的 SectionReader。
// TensorData holds tensor metadata and a reader for its raw bytes.
type TensorData struct {
	Name   string
	Dtype  string
	Shape  []int32
	Size   int64
	reader *io.SectionReader
}

// WithName 浅拷贝并替换逻辑张量名，共享同一底层 reader。
// WithName returns a shallow copy of TensorData with a different logical tensor
// name but the same underlying raw data reader.
func (td *TensorData) WithName(name string) *TensorData {
	if td == nil {
		return nil
	}
	shape := make([]int32, len(td.Shape))
	copy(shape, td.Shape)
	return &TensorData{
		Name:   name,
		Dtype:  td.Dtype,
		Shape:  shape,
		Size:   td.Size,
		reader: td.reader,
	}
}

// Reader 返回张量原始字节的 io.Reader。
// Reader returns an io.Reader for the tensor's raw bytes.
func (td *TensorData) Reader() io.Reader {
	return td.reader
}

// safetensorsHeader 构建仅含单张量的最小 safetensors JSON 头。
// safetensorsHeader builds the JSON header for a minimal safetensors blob
// containing a single tensor keyed by its name.
func (td *TensorData) safetensorsHeader() []byte {
	header := map[string]any{
		td.Name: tensorInfo{
			Dtype:       td.Dtype,
			Shape:       td.Shape,
			DataOffsets: [2]int{0, int(td.Size)},
		},
	}
	headerJSON, _ := json.Marshal(header)

	// 头 JSON 填充至 8 字节对齐。
	// Pad header to 8-byte alignment
	padding := (8 - len(headerJSON)%8) % 8
	headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...)
	return headerJSON
}

// SafetensorsReader 输出最小 safetensors 包装流，供 mlx_load_safetensors 零拷贝加载。
// SafetensorsReader returns a reader that outputs the tensor wrapped in
// minimal safetensors format. This allows using mlx_load_safetensors on
// individual tensor blobs for native zero-copy loading.
// The tensor is keyed by its name in the safetensors header.
func (td *TensorData) SafetensorsReader() io.Reader {
	headerJSON := td.safetensorsHeader()

	// 8 字节小端长度前缀 + JSON 头。
	// Build header with size prefix
	headerBuf := new(bytes.Buffer)
	binary.Write(headerBuf, binary.LittleEndian, uint64(len(headerJSON)))
	headerBuf.Write(headerJSON)

	// MultiReader：头 + 张量数据。
	// Return multi-reader: header + tensor data
	td.reader.Seek(0, io.SeekStart)
	return io.MultiReader(headerBuf, td.reader)
}

// SafetensorsSize 返回包装后 safetensors 总字节数。
// SafetensorsSize returns the total size of the safetensors-wrapped tensor.
func (td *TensorData) SafetensorsSize() int64 {
	headerJSON := td.safetensorsHeader()
	return 8 + int64(len(headerJSON)) + td.Size
}

// NewTensorDataFromBytes 从已有原始字节构造 TensorData。
// NewTensorDataFromBytes creates a TensorData from raw tensor bytes.
// This is useful for constructing packed blobs from already-extracted data.
func NewTensorDataFromBytes(name, dtype string, shape []int32, rawData []byte) *TensorData {
	return &TensorData{
		Name:   name,
		Dtype:  dtype,
		Shape:  shape,
		Size:   int64(len(rawData)),
		reader: io.NewSectionReader(bytes.NewReader(rawData), 0, int64(len(rawData))),
	}
}

// NewTensorDataFromReaderAt 用任意 ReaderAt 支撑大张量，避免全量进内存。
// NewTensorDataFromReaderAt creates a TensorData backed by an arbitrary
// io.ReaderAt. This is useful for constructing large synthetic tensors from
// temporary files without loading the full payload into memory.
func NewTensorDataFromReaderAt(name, dtype string, shape []int32, readerAt io.ReaderAt, size int64) *TensorData {
	return &TensorData{
		Name:   name,
		Dtype:  dtype,
		Shape:  shape,
		Size:   size,
		reader: io.NewSectionReader(readerAt, 0, size),
	}
}

// ExtractRawFromSafetensors 剥离 safetensors 头，读出纯张量字节。
// ExtractRawFromSafetensors reads a safetensors-wrapped reader and extracts
// the raw tensor data bytes (stripping the header).
func ExtractRawFromSafetensors(r io.Reader) ([]byte, error) {
	// 读 8 字节小端头长度。
	// Read header size (8 bytes, little endian)
	var headerSize uint64
	if err := binary.Read(r, binary.LittleEndian, &headerSize); err != nil {
		return nil, fmt.Errorf("failed to read header size: %w", err)
	}

	// 跳过 JSON 头。
	// Skip header
	if _, err := io.CopyN(io.Discard, r, int64(headerSize)); err != nil {
		return nil, fmt.Errorf("failed to skip header: %w", err)
	}

	// 读剩余原始张量数据。
	// Read remaining bytes (the raw tensor data)
	return io.ReadAll(r)
}

// BuildPackedSafetensorsReader 流式输出含多 tensor 的有效 safetensors 文件。
// BuildPackedSafetensorsReader builds a streaming io.Reader that outputs a valid
// safetensors file containing multiple tensors. Used for packing expert tensors
// into a single blob without loading all data into memory.
// Each TensorData must have been obtained from GetTensor.
func BuildPackedSafetensorsReader(tensors []*TensorData) io.Reader {
	return BuildPackedSafetensorsReaderWithMetadata(tensors, nil)
}

// BuildPackedSafetensorsReaderWithMetadata 同上，可附加 __metadata__。
// BuildPackedSafetensorsReaderWithMetadata builds a streaming io.Reader that
// outputs a valid safetensors file containing multiple tensors and optional
// metadata.
func BuildPackedSafetensorsReaderWithMetadata(tensors []*TensorData, metadata map[string]string) io.Reader {
	// 按顺序累加 data_offsets 构建头。
	// Build the header with sequential data offsets
	header := make(map[string]any, len(tensors)+1)
	var offset int
	for _, td := range tensors {
		header[td.Name] = tensorInfo{
			Dtype:       td.Dtype,
			Shape:       td.Shape,
			DataOffsets: [2]int{offset, offset + int(td.Size)},
		}
		offset += int(td.Size)
	}
	if len(metadata) > 0 {
		header["__metadata__"] = metadata
	}

	headerJSON, _ := json.Marshal(header)

	// Pad header to 8-byte alignment
	padding := (8 - len(headerJSON)%8) % 8
	headerJSON = append(headerJSON, bytes.Repeat([]byte(" "), padding)...)

	// Build header with size prefix
	headerBuf := new(bytes.Buffer)
	binary.Write(headerBuf, binary.LittleEndian, uint64(len(headerJSON)))
	headerBuf.Write(headerJSON)

	// Build multi-reader: header + all tensor data readers
	readers := make([]io.Reader, 0, 1+len(tensors))
	readers = append(readers, headerBuf)
	for _, td := range tensors {
		td.reader.Seek(0, io.SeekStart)
		readers = append(readers, td.reader)
	}

	return io.MultiReader(readers...)
}

// OpenForExtraction 打开文件供提取；调用方须 Close。
// OpenForExtraction opens a safetensors file for tensor extraction.
// The caller must call Close() when done.
func OpenForExtraction(path string) (*TensorExtractor, error) {
	f, err := os.Open(path)
	if err != nil {
		return nil, fmt.Errorf("failed to open file: %w", err)
	}

	var headerSize uint64
	if err := binary.Read(f, binary.LittleEndian, &headerSize); err != nil {
		f.Close()
		return nil, fmt.Errorf("failed to read header size: %w", err)
	}

	headerBytes := make([]byte, headerSize)
	if _, err := f.Read(headerBytes); err != nil {
		f.Close()
		return nil, fmt.Errorf("failed to read header: %w", err)
	}

	var header map[string]tensorInfo
	if err := json.Unmarshal(headerBytes, &header); err != nil {
		f.Close()
		return nil, fmt.Errorf("failed to parse header: %w", err)
	}

	delete(header, "__metadata__")

	return &TensorExtractor{
		file:       f,
		dataOffset: 8 + int64(headerSize), // 8 bytes for header size + header content
		header:     header,
	}, nil
}

// GetTensor 按名返回元数据与 SectionReader。
// GetTensor returns tensor metadata and a reader for extracting a single tensor.
func (te *TensorExtractor) GetTensor(name string) (*TensorData, error) {
	info, ok := te.header[name]
	if !ok {
		return nil, fmt.Errorf("tensor %q not found", name)
	}

	start := te.dataOffset + int64(info.DataOffsets[0])
	size := int64(info.DataOffsets[1] - info.DataOffsets[0])

	return &TensorData{
		Name:   name,
		Dtype:  info.Dtype,
		Shape:  info.Shape,
		Size:   size,
		reader: io.NewSectionReader(te.file, start, size),
	}, nil
}

// ListTensors 返回排序后的全部张量名。
// ListTensors returns all tensor names in sorted order.
func (te *TensorExtractor) ListTensors() []string {
	names := make([]string, 0, len(te.header))
	for name := range te.header {
		names = append(names, name)
	}
	sort.Strings(names)
	return names
}

// TensorCount 返回文件中张量个数。
// TensorCount returns the number of tensors in the file.
func (te *TensorExtractor) TensorCount() int {
	return len(te.header)
}

// Close 关闭底层文件。
// Close closes the underlying file.
func (te *TensorExtractor) Close() error {
	return te.file.Close()
}

// ExtractAll 返回全部 TensorData；调用方仍须 Close TensorExtractor。
// ExtractAll returns TensorData for all tensors in the file.
// Each TensorData has a reader that reads from the original file.
// The caller must call Close() on the TensorExtractor when done.
func (te *TensorExtractor) ExtractAll() ([]*TensorData, error) {
	names := te.ListTensors()
	tensors := make([]*TensorData, 0, len(names))

	for _, name := range names {
		td, err := te.GetTensor(name)
		if err != nil {
			return nil, err
		}
		tensors = append(tensors, td)
	}

	return tensors, nil
}
