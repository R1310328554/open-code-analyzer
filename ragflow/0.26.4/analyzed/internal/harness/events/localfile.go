package events

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strings"
	"sync"
	"sync/atomic"
	"time"
)

// localfile.go — 本地 JSONL 文件事件存储：单实例持久化，支持分段轮转。

const (
	defaultMaxSegmentSize int64 = 64 * 1024 * 1024 // 每段最大 64 MB
	segmentFilePattern          = "events_*.jsonl"
)

// LocalFileEventStore 将事件持久化到 JSONL 文件，自动分段轮转，适合单实例部署。
type LocalFileEventStore struct {
	dir     string
	segment int          // 当前写入段编号
	maxSize int64        // 段大小上限，超出则轮转
	mu      sync.RWMutex
	cached  []*Event     // 当前段内存缓存
	clock   atomic.Uint64
}

// NewLocalFileEventStore 在指定目录创建或打开事件存储，启动时加载已有分段。
func NewLocalFileEventStore(dir string) (*LocalFileEventStore, error) {
	if err := os.MkdirAll(dir, 0755); err != nil {
		return nil, fmt.Errorf("create events dir: %w", err)
	}

	s := &LocalFileEventStore{
		dir:     dir,
		segment: 0,
		maxSize: defaultMaxSegmentSize,
		cached:  make([]*Event, 0),
	}

	if err := s.loadExisting(); err != nil {
		return nil, fmt.Errorf("load existing segments: %w", err)
	}

	return s, nil
}

// loadExisting 扫描目录中已有分段文件并加载到内存。
func (s *LocalFileEventStore) loadExisting() error {
	entries, err := os.ReadDir(s.dir)
	if err != nil {
		return err
	}

	var segmentFiles []string
	for _, entry := range entries {
		if !entry.IsDir() && strings.HasPrefix(entry.Name(), "events_") && strings.HasSuffix(entry.Name(), ".jsonl") {
			segmentFiles = append(segmentFiles, entry.Name())
		}
	}

	sort.Strings(segmentFiles)

	allEvents := make([]*Event, 0)
	var maxClock uint64

	for _, fname := range segmentFiles {
		fpath := filepath.Join(s.dir, fname)
		events, err := readSegmentFile(fpath)
		if err != nil {
			return fmt.Errorf("read segment %s: %w", fname, err)
		}
		for _, ev := range events {
			if ev.Clock > maxClock {
				maxClock = ev.Clock
			}
		}
		allEvents = append(allEvents, events...)
	}

	s.cached = allEvents
	if maxClock > 0 {
		s.clock.Store(maxClock)
	}
	return nil
}

// readSegmentFile 从 JSONL 文件读取全部事件。
func readSegmentFile(path string) ([]*Event, error) {
	f, err := os.Open(path)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	var events []*Event
	scanner := bufio.NewScanner(f)
	scanner.Buffer(make([]byte, 1024*1024), 1024*1024) // 1 MB 行缓冲
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}
		var ev Event
		if err := json.Unmarshal([]byte(line), &ev); err != nil {
			return nil, fmt.Errorf("unmarshal event: %w", err)
		}
		events = append(events, &ev)
	}
	return events, scanner.Err()
}

// Append 追加事件到内存缓存并写入当前分段文件。
func (s *LocalFileEventStore) Append(ctx context.Context, events ...*Event) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	for _, ev := range events {
		if ev.Clock == 0 {
			ev.Clock = s.clock.Add(1)
		}
		ev.Seal()
		s.cached = append(s.cached, ev)

		if err := s.appendToFileLocked(ev); err != nil {
			return err
		}
	}
	return nil
}

// appendToFileLocked 将单条事件追加到当前分段；调用方须持有 s.mu。
func (s *LocalFileEventStore) appendToFileLocked(ev *Event) error {
	fname := fmt.Sprintf("events_%s_%04d.jsonl", ev.TraceID, s.segment)
	fpath := filepath.Join(s.dir, fname)

	if info, err := os.Stat(fpath); err == nil && info.Size() > s.maxSize {
		s.segment++
		fname = fmt.Sprintf("events_%s_%04d.jsonl", ev.TraceID, s.segment)
		fpath = filepath.Join(s.dir, fname)
	}

	f, err := os.OpenFile(fpath, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return fmt.Errorf("open segment file: %w", err)
	}
	defer f.Close()

	data, err := json.Marshal(ev)
	if err != nil {
		return fmt.Errorf("marshal event: %w", err)
	}

	if _, err := f.Write(data); err != nil {
		return err
	}
	if _, err := f.Write([]byte("\n")); err != nil {
		return err
	}
	return nil
}

// Stream 返回匹配过滤器的事件迭代器。
func (s *LocalFileEventStore) Stream(ctx context.Context, filter EventFilter) EventIterator {
	s.mu.RLock()
	defer s.mu.RUnlock()

	filtered := make([]*Event, 0)
	for _, ev := range s.cached {
		if filter.Matches(ev) {
			filtered = append(filtered, ev)
		}
	}
	return &sliceIterator{events: filtered, pos: 0}
}

// Get 按 ID 检索事件。
func (s *LocalFileEventStore) Get(ctx context.Context, id EventID) (*Event, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	for _, ev := range s.cached {
		if ev.ID == id {
			return ev, nil
		}
	}
	return nil, nil
}

// Range 返回逻辑时钟区间内匹配过滤器的事件。
func (s *LocalFileEventStore) Range(ctx context.Context, from, to uint64, filter EventFilter) ([]*Event, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Event, 0)
	for _, ev := range s.cached {
		if ev.Clock < from {
			continue
		}
		if to > 0 && ev.Clock > to {
			continue
		}
		if filter.Matches(ev) {
			result = append(result, ev)
		}
	}
	return result, nil
}

// Seek 从指定逻辑时钟位置开始迭代。
func (s *LocalFileEventStore) Seek(ctx context.Context, clock uint64) (EventIterator, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	pos := 0
	for i, ev := range s.cached {
		if ev.Clock >= clock {
			pos = i
			break
		}
		_ = i
	}
	return &sliceIterator{events: s.cached[pos:], pos: 0}, nil
}

// Length 返回缓存中的事件总数。
func (s *LocalFileEventStore) Length(ctx context.Context) (uint64, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return uint64(len(s.cached)), nil
}

// CreateSnapshot 序列化当前缓存为快照。
func (s *LocalFileEventStore) CreateSnapshot(ctx context.Context, traceID string) (*Snapshot, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	clock := s.clock.Load()
	data, err := json.Marshal(s.cached)
	if err != nil {
		return nil, fmt.Errorf("marshal snapshot: %w", err)
	}
	return &Snapshot{
		ID:        fmt.Sprintf("snap-%s-%d", traceID, clock),
		TraceID:   traceID,
		Clock:     clock,
		CreatedAt: time.Now(),
		Data:      data,
	}, nil
}

// RestoreSnapshot 从快照恢复；本地文件存储直接 Seek 到起点。
func (s *LocalFileEventStore) RestoreSnapshot(ctx context.Context, snapshotID string) (EventIterator, error) {
	return s.Seek(ctx, 0)
}

// Subscribe 本地文件存储不支持实时订阅；分布式场景请用 NATSEventStore。
func (s *LocalFileEventStore) Subscribe(ctx context.Context, filter EventFilter) (<-chan *Event, error) {
	ch := make(chan *Event)
	close(ch)
	return ch, nil
}

// GC 删除早于保留期的事件，并重写保留事件到新分段文件。
func (s *LocalFileEventStore) GC(ctx context.Context, retention time.Duration) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	cutoff := time.Now().Add(-retention)
	keep := make([]*Event, 0, len(s.cached))
	for _, ev := range s.cached {
		if ev.Timestamp.After(cutoff) {
			keep = append(keep, ev)
		}
	}
	s.cached = keep
	s.segment = 0

	entries, _ := os.ReadDir(s.dir)
	for _, entry := range entries {
		if !entry.IsDir() && strings.HasPrefix(entry.Name(), "events_") && strings.HasSuffix(entry.Name(), ".jsonl") {
			os.Remove(filepath.Join(s.dir, entry.Name()))
		}
	}

	for _, ev := range keep {
		if err := s.appendToFileLocked(ev); err != nil {
			return err
		}
	}
	return nil
}
