package types

// stream.go — 流式协议：ChannelStream、DuplexStream 与 Filter/Map 装饰。


import (
	"context"
	"io"
	"sync"
	"time"
)

// StreamChunk 流式输出单块数据。
type StreamChunk struct {
	Mode      StreamMode
	Data      interface{}
	Step      int
	Node      string
	Metadata  map[string]interface{}
	Timestamp int64
	Index     int
}

// StreamProtocol 图执行流式输出接口（Emit/Iterator/Close）。
type StreamProtocol interface {
	// Emit 向流发送 chunk。
	Emit(ctx context.Context, chunk *StreamChunk) error

	// Iterator 返回可阻塞读取的 StreamIterator。
	Iterator(ctx context.Context) StreamIterator

	// Close 关闭流并释放资源。
	Close() error

	// IsClosed 流是否已关闭。
	IsClosed() bool

	// Mode 返回 StreamMode。
	Mode() StreamMode
}

// StreamIterator 流块迭代器（Next/HasMore/Close）。
type StreamIterator interface {
	// Next 阻塞读取下一块，ctx 取消或 EOF 返回错误。
	Next(ctx context.Context) (*StreamChunk, error)

	// HasMore 是否还有未读 chunk。
	HasMore() bool

	// Close closes the iterator.
	Close() error
}

// ChannelStream 基于 chan *StreamChunk 的 StreamProtocol 实现。
type ChannelStream struct {
	mode   StreamMode
	ch     chan *StreamChunk
	closed bool
	closer chan struct{}
	mu     sync.RWMutex
}

// NewChannelStream 创建带缓冲的通道流。
func NewChannelStream(mode StreamMode, bufferSize int) *ChannelStream {
	return &ChannelStream{
		mode:   mode,
		ch:     make(chan *StreamChunk, bufferSize),
		closer: make(chan struct{}),
	}
}

// Emit emits a chunk to the stream.
func (s *ChannelStream) Emit(ctx context.Context, chunk *StreamChunk) error {
	s.mu.RLock()
	if s.closed {
		s.mu.RUnlock()
		return &StreamError{Message: "stream is closed"}
	}
	s.mu.RUnlock()

	chunk.Mode = s.mode
	chunk.Timestamp = time.Now().UnixNano()

	select {
	case s.ch <- chunk:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	}
}

// Iterator returns an iterator over the stream chunks.
func (s *ChannelStream) Iterator(ctx context.Context) StreamIterator {
	return &channelIterator{
		stream: s,
		ch:     s.ch,
	}
}

// Close closes the stream.
func (s *ChannelStream) Close() error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if s.closed {
		return nil
	}

	s.closed = true
	close(s.ch)
	return nil
}

// IsClosed returns true if the stream is closed.
func (s *ChannelStream) IsClosed() bool {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.closed
}

// Mode returns the stream mode.
func (s *ChannelStream) Mode() StreamMode {
	return s.mode
}

// channelIterator implements StreamIterator.
type channelIterator struct {
	stream *ChannelStream
	ch     <-chan *StreamChunk
	closed bool
}

// Next returns the next chunk.
func (it *channelIterator) Next(ctx context.Context) (*StreamChunk, error) {
	if it.closed {
		return nil, io.EOF
	}

	select {
	case chunk, ok := <-it.ch:
		if !ok {
			it.closed = true
			return nil, io.EOF
		}
		return chunk, nil
	case <-ctx.Done():
		return nil, ctx.Err()
	}
}

// HasMore returns true if there are more chunks.
func (it *channelIterator) HasMore() bool {
	if it.closed {
		return false
	}
	select {
	case <-it.stream.closer:
		return false
	default:
		return len(it.ch) > 0
	}
}

// Close closes the iterator.
func (it *channelIterator) Close() error {
	it.closed = true
	return nil
}

// DuplexStream 多路复用多个 StreamProtocol 到单一输出流。
type DuplexStream struct {
	streams []StreamProtocol
	output  *ChannelStream
	wg      sync.WaitGroup
	ctx     context.Context
	cancel  context.CancelFunc
}

// NewDuplexStream 为每个源流启动 multiplex goroutine。
func NewDuplexStream(ctx context.Context, streams ...StreamProtocol) *DuplexStream {
	childCtx, cancel := context.WithCancel(ctx)
	ds := &DuplexStream{
		streams: streams,
		output:  NewChannelStream(StreamModeValues, 100),
		ctx:     childCtx,
		cancel:  cancel,
	}

	// Start multiplexing goroutines
	for i, stream := range streams {
		ds.wg.Add(1)
		go ds.multiplex(i, stream)
	}

	return ds
}

// multiplex 转发 chunk 并在 Metadata 标记 source 索引。
func (ds *DuplexStream) multiplex(index int, stream StreamProtocol) {
	defer ds.wg.Done()

	iter := stream.Iterator(ds.ctx)
	for {
		chunk, err := iter.Next(ds.ctx)
		if err != nil {
			break
		}

		// Add source index to metadata
		if chunk.Metadata == nil {
			chunk.Metadata = make(map[string]interface{})
		}
		chunk.Metadata["source"] = index

		if err := ds.output.Emit(ds.ctx, chunk); err != nil {
			break
		}
	}
}

// Emit emits a chunk to the output stream.
func (ds *DuplexStream) Emit(ctx context.Context, chunk *StreamChunk) error {
	return ds.output.Emit(ctx, chunk)
}

// Iterator returns an iterator over the multiplexed stream.
func (ds *DuplexStream) Iterator(ctx context.Context) StreamIterator {
	return ds.output.Iterator(ctx)
}

// Close closes all streams.
func (ds *DuplexStream) Close() error {
	ds.cancel()
	ds.wg.Wait()

	for _, stream := range ds.streams {
		stream.Close()
	}
	return ds.output.Close()
}

// IsClosed returns true if any stream is closed.
func (ds *DuplexStream) IsClosed() bool {
	return ds.output.IsClosed()
}

// Mode returns the output stream mode.
func (ds *DuplexStream) Mode() StreamMode {
	return ds.output.Mode()
}

// Output returns the output stream.
func (ds *DuplexStream) Output() StreamProtocol {
	return ds.output
}

// FilterStream 谓词过滤装饰流。
type FilterStream struct {
	source StreamProtocol
	filter func(*StreamChunk) bool
}

// NewFilterStream 创建过滤流。
func NewFilterStream(source StreamProtocol, filter func(*StreamChunk) bool) *FilterStream {
	return &FilterStream{
		source: source,
		filter: filter,
	}
}

// Emit emits a chunk if it passes the filter.
func (fs *FilterStream) Emit(ctx context.Context, chunk *StreamChunk) error {
	if fs.filter == nil || fs.filter(chunk) {
		return fs.source.Emit(ctx, chunk)
	}
	return nil
}

// Iterator returns a filtered iterator.
func (fs *FilterStream) Iterator(ctx context.Context) StreamIterator {
	return &filterIterator{
		source: fs.source.Iterator(ctx),
		filter: fs.filter,
	}
}

// Close closes the source stream.
func (fs *FilterStream) Close() error {
	return fs.source.Close()
}

// IsClosed returns true if the source stream is closed.
func (fs *FilterStream) IsClosed() bool {
	return fs.source.IsClosed()
}

// Mode returns the source stream mode.
func (fs *FilterStream) Mode() StreamMode {
	return fs.source.Mode()
}

// filterIterator implements filtered iteration.
type filterIterator struct {
	source StreamIterator
	filter func(*StreamChunk) bool
	peeked *StreamChunk
	err    error
}

// Next returns the next filtered chunk.
func (fi *filterIterator) Next(ctx context.Context) (*StreamChunk, error) {
	for {
		if fi.peeked != nil {
			chunk := fi.peeked
			fi.peeked = nil
			return chunk, nil
		}

		if fi.err != nil {
			return nil, fi.err
		}

		chunk, err := fi.source.Next(ctx)
		if err != nil {
			fi.err = err
			return nil, err
		}

		if fi.filter == nil || fi.filter(chunk) {
			return chunk, nil
		}
	}
}

// HasMore returns true if there are more filtered chunks.
func (fi *filterIterator) HasMore() bool {
	if fi.peeked != nil || fi.err != nil {
		return fi.peeked != nil && fi.err == nil
	}
	return fi.source.HasMore()
}

// Close closes the source iterator.
func (fi *filterIterator) Close() error {
	return fi.source.Close()
}

// MapStream 映射变换装饰流。
type MapStream struct {
	source StreamProtocol
	mapper func(*StreamChunk) *StreamChunk
}

// NewMapStream 创建映射流。
func NewMapStream(source StreamProtocol, mapper func(*StreamChunk) *StreamChunk) *MapStream {
	return &MapStream{
		source: source,
		mapper: mapper,
	}
}

// Emit emits a transformed chunk.
func (ms *MapStream) Emit(ctx context.Context, chunk *StreamChunk) error {
	if ms.mapper != nil {
		chunk = ms.mapper(chunk)
	}
	return ms.source.Emit(ctx, chunk)
}

// Iterator returns a mapped iterator.
func (ms *MapStream) Iterator(ctx context.Context) StreamIterator {
	return &mapIterator{
		source: ms.source.Iterator(ctx),
		mapper: ms.mapper,
	}
}

// Close closes the source stream.
func (ms *MapStream) Close() error {
	return ms.source.Close()
}

// IsClosed returns true if the source stream is closed.
func (ms *MapStream) IsClosed() bool {
	return ms.source.IsClosed()
}

// Mode returns the source stream mode.
func (ms *MapStream) Mode() StreamMode {
	return ms.source.Mode()
}

// mapIterator implements mapped iteration.
type mapIterator struct {
	source StreamIterator
	mapper func(*StreamChunk) *StreamChunk
}

// Next returns the next mapped chunk.
func (mi *mapIterator) Next(ctx context.Context) (*StreamChunk, error) {
	chunk, err := mi.source.Next(ctx)
	if err != nil {
		return nil, err
	}

	if mi.mapper != nil {
		return mi.mapper(chunk), nil
	}

	return chunk, nil
}

// HasMore returns true if there are more chunks.
func (mi *mapIterator) HasMore() bool {
	return mi.source.HasMore()
}

// Close closes the source iterator.
func (mi *mapIterator) Close() error {
	return mi.source.Close()
}

// StreamError 流相关错误（含可选 Code）。
type StreamError struct {
	Message string
	Code    string
}

func (e *StreamError) Error() string {
	if e.Code != "" {
		return e.Code + ": " + e.Message
	}
	return e.Message
}

// Emit 自动填充 Mode 与 Timestamp；关闭后 Emit 返回 StreamError。
