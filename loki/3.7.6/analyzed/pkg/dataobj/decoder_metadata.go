package dataobj

// 本文件提供各类 Decoder 共享的元数据解码例程：
// 解析文件头/尾、魔数校验与 protobuf 文件级 Metadata。

import (
	"encoding/binary"
	"errors"
	"fmt"
	"io"

	"github.com/grafana/loki/v3/pkg/dataobj/internal/metadata/filemd"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/streamio"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/protocodec"
)

// decode* 系列函数供多种 Decoder 实现复用，避免重复解析逻辑。
// decode* methods for metadata shared by Decoder implementations.

var errLegacyMagic = errors.New("file uses legacy magic value")

// decodeHeader 读取 DOBJ 魔数与元数据长度，legacy 魔数返回 errLegacyMagic。
// decodeHeader decodes the header of the file to retrieve the metadata size
// and the magic value.
//
// If the header has the legacy magic value, it returns [errLegacyMagic].
func decodeHeader(r streamio.Reader) (metadataSize uint32, err error) {
	var gotMagic [4]byte
	if _, err := io.ReadFull(r, gotMagic[:]); err != nil {
		return 0, fmt.Errorf("read magic: %w", err)
	} else if string(gotMagic[:]) == string(legacyMagic) {
		return 0, errLegacyMagic
	} else if string(gotMagic[:]) != string(magic) {
		return 0, fmt.Errorf("unexpected magic: got=%q want=%q", gotMagic, magic)
	}

	if err := binary.Read(r, binary.LittleEndian, &metadataSize); err != nil {
		return 0, fmt.Errorf("read metadata size: %w", err)
	}
	return
}

// decodeTailer 仅用于 legacy 格式，从文件末尾读取元数据长度与 THOR 魔数。
// decodeTailer decodes the tailer of the file to retrieve the metadata size
// and the magic value. Only works for files with the legacy magic value.
func decodeTailer(r streamio.Reader) (metadataSize uint32, err error) {
	if err := binary.Read(r, binary.LittleEndian, &metadataSize); err != nil {
		return 0, fmt.Errorf("read metadata size: %w", err)
	}

	var gotMagic [4]byte
	if _, err := io.ReadFull(r, gotMagic[:]); err != nil {
		return 0, fmt.Errorf("read magic: %w", err)
	} else if string(gotMagic[:]) != string(legacyMagic) {
		return 0, fmt.Errorf("unexpected magic: got=%q want=%q", gotMagic, legacyMagic)
	}

	return
}

// decodeFileMetadata 校验格式版本后通过 protocodec 解码 filemd.Metadata。
// decodeFileMetadata decodes file metadata from r.
func decodeFileMetadata(r streamio.Reader) (*filemd.Metadata, error) {
	gotVersion, err := streamio.ReadUvarint(r)
	if err != nil {
		return nil, fmt.Errorf("read file format version: %w", err)
	} else if gotVersion != fileFormatVersion {
		return nil, fmt.Errorf("unexpected file format version: got=%d want=%d", gotVersion, fileFormatVersion)
	}

	var md filemd.Metadata
	if err := protocodec.Decode(r, &md); err != nil {
		return nil, fmt.Errorf("file metadata: %w", err)
	}
	return &md, nil
}
// 元数据解码是打开 data object 的第一步，错误会阻断后续 section 访问。
