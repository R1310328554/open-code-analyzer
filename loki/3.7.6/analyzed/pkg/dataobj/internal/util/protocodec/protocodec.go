// protocodec 以 uvarint 长度前缀流式编解码 protobuf，便于顺序读写消息流。
// Package protocodec provides utilities for encoding and decoding protobuf
// messages into files.
//
// Protobuf messages are prepended with their size as a uvarint so they can be
// streamed.
package protocodec

import (
	"encoding/binary"
	"fmt"
	"io"
	"sync"

	"github.com/gogo/protobuf/proto"

	"github.com/grafana/loki/v3/pkg/dataobj/internal/streamio"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/bufpool"
)

// Size 返回 Encode 所需总字节数，含 uvarint 前缀与序列化正文。
// Size returns the number of bytes that would be required to call [Encode] for
// pb.
func Size(pb proto.Message) int {
	protoSize := proto.Size(pb)

	scratch := [binary.MaxVarintLen64]byte{}
	uvarintSize := binary.PutUvarint(scratch[:], uint64(protoSize))

	return uvarintSize + protoSize
}

// Encode 先写消息长度 uvarint，再写入 proto 序列化字节。
// Encode encodes a protobuf message into w. Encoded messages can be decoded
// with [Decode].
func Encode(w streamio.Writer, pb proto.Message) error {
	buf := protoBufferPool.Get().(*proto.Buffer)
	buf.Reset()
	defer protoBufferPool.Put(buf)

	if err := buf.Marshal(pb); err != nil {
		return err
	}

	// Every protobuf message is always prepended with its size as a uvarint.
	messageSize := len(buf.Bytes())
	if err := streamio.WriteUvarint(w, uint64(messageSize)); err != nil {
		return err
	}

	sz, err := w.Write(buf.Bytes())
	if err != nil {
		return err
	} else if sz != messageSize {
		return io.ErrShortWrite
	}

	return nil
}

// Decode 读 uvarint 长度后从 bufpool 取缓冲，再 Unmarshal 到 pb。
// Decode decodes a message encoded with protocodec from r and stores it in pb.
func Decode(r streamio.Reader, pb proto.Message) error {
	size, err := binary.ReadUvarint(r)
	if err != nil {
		return fmt.Errorf("read proto message size: %w", err)
	}

	// We know exactly how big of a buffer we need here, so we can get a bucketed
	// buffer from bufpool.
	buf := bufpool.Get(int(size))
	defer bufpool.Put(buf)

	n, err := io.Copy(buf, io.LimitReader(r, int64(size)))
	if err != nil {
		return fmt.Errorf("read proto message: %w", err)
	} else if uint64(n) != size {
		return fmt.Errorf("read proto message: got=%d want=%d", n, size)
	}

	if err := proto.Unmarshal(buf.Bytes(), pb); err != nil {
		return fmt.Errorf("unmarshal proto message: %w", err)
	}
	return nil
}

var protoBufferPool = sync.Pool{
	New: func() any {
		return new(proto.Buffer)
	},
}
// proto.Buffer 池化复用，减少高频序列化路径上的临时分配。
