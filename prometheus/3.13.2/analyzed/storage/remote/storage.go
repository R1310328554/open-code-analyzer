// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Remote Storage 门面：实现 storage.Storage，聚合 remote read 客户端与 WriteStorage，ApplyConfig 按配置哈希增删读写端点。

package remote

import (
	"context"
	"crypto/md5"
	"encoding/hex"
	"fmt"
	"log/slog"
	"sync"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
	"github.com/prometheus/common/promslog"
	"go.yaml.in/yaml/v2"

	"github.com/prometheus/prometheus/config"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/scrape"
	"github.com/prometheus/prometheus/storage"
	"github.com/prometheus/prometheus/util/logging"
)

// String constants for instrumentation.
const (
	namespace  = "prometheus"
	subsystem  = "remote_storage"
	remoteName = "remote_name"
	endpoint   = "url"
)

type ReadyScrapeManager interface {
	Get() (*scrape.Manager, error)
}

// startTimeCallback is a callback func that return the oldest timestamp stored in a storage.
type startTimeCallback func() (int64, error)

// Storage 组合 WriteStorage 与多个 remote read queryable，供 fanout 使用。
// Storage represents all the remote read and write endpoints.  It implements
// storage.Storage.
type Storage struct {
	deduper *logging.Deduper
	logger  *slog.Logger
	mtx     sync.Mutex

	rws *WriteStorage

	// For reads.
	queryables             []storage.SampleAndChunkQueryable
	localStartTimeCallback startTimeCallback
}

var _ storage.Storage = &Storage{}

// NewStorage 创建 remote storage 并初始化 WriteStorage 与日志 deduper。
// NewStorage returns a remote.Storage.
func NewStorage(l *slog.Logger, reg prometheus.Registerer, stCallback startTimeCallback, walDir string, flushDeadline time.Duration, sm ReadyScrapeManager, enableTypeAndUnitLabels bool) *Storage {
	if l == nil {
		l = promslog.NewNopLogger()
	}
	deduper := logging.Dedupe(l, 1*time.Minute)
	logger := slog.New(deduper)

	s := &Storage{
		logger:                 logger,
		deduper:                deduper,
		localStartTimeCallback: stCallback,
	}
	s.rws = NewWriteStorage(s.logger, reg, walDir, flushDeadline, sm, enableTypeAndUnitLabels)
	return s
}

func (s *Storage) Notify() {
	s.rws.Notify()
}

// ApplyConfig updates the state as the new config requires.
// ApplyConfig 刷新 remote write 队列与 read 客户端列表，禁止重复 read 配置。
func (s *Storage) ApplyConfig(conf *config.Config) error {
	s.mtx.Lock()
	defer s.mtx.Unlock()

	if err := s.rws.ApplyConfig(conf); err != nil {
		return err
	}

	// Update read clients
	readHashes := make(map[string]struct{})
	queryables := make([]storage.SampleAndChunkQueryable, 0, len(conf.RemoteReadConfigs))
	for _, rrConf := range conf.RemoteReadConfigs {
		hash, err := toHash(rrConf)
		if err != nil {
			return err
		}

		// Don't allow duplicate remote read configs.
		if _, ok := readHashes[hash]; ok {
			return fmt.Errorf("duplicate remote read configs are not allowed, found duplicate for URL: %s", rrConf.URL)
		}
		readHashes[hash] = struct{}{}

		// Set the queue name to the config hash if the user has not set
		// a name in their remote write config so we can still differentiate
		// between queues that have the same remote write endpoint.
		name := hash[:6]
		if rrConf.Name != "" {
			name = rrConf.Name
		}

		c, err := NewReadClient(name, &ClientConfig{
			URL:              rrConf.URL,
			Timeout:          rrConf.RemoteTimeout,
			ChunkedReadLimit: rrConf.ChunkedReadLimit,
			HTTPClientConfig: rrConf.HTTPClientConfig,
			Headers:          rrConf.Headers,
		})
		if err != nil {
			return err
		}

		externalLabels := conf.GlobalConfig.ExternalLabels
		if !rrConf.FilterExternalLabels {
			externalLabels = labels.EmptyLabels()
		}
		queryables = append(queryables, NewSampleAndChunkQueryableClient(
			c,
			externalLabels,
			labelsToEqualityMatchers(rrConf.RequiredMatchers),
			rrConf.ReadRecent,
			s.localStartTimeCallback,
		))
	}
	s.queryables = queryables

	return nil
}

// StartTime implements the Storage interface.
func (*Storage) StartTime() (int64, error) {
	return int64(model.Latest), nil
}

// Querier 合并各 remote read 端点的 Querier，secondary 路径 best-effort。
// Querier returns a storage.MergeQuerier combining the remote client queriers
// of each configured remote read endpoint.
// Returned querier will never return error as all queryables are assumed best effort.
// Additionally all returned queriers ensure that its Select's SeriesSets have ready data after first `Next` invoke.
// This is because Prometheus (fanout and secondary queries) can't handle the stream failing half way through by design.
func (s *Storage) Querier(mint, maxt int64) (storage.Querier, error) {
	s.mtx.Lock()
	queryables := s.queryables
	s.mtx.Unlock()

	queriers := make([]storage.Querier, 0, len(queryables))
	for _, queryable := range queryables {
		q, err := queryable.Querier(mint, maxt)
		if err != nil {
			return nil, err
		}
		queriers = append(queriers, q)
	}
	return storage.NewMergeQuerier(nil, queriers, storage.ChainedSeriesMerge), nil
}

// ChunkQuerier returns a storage.MergeQuerier combining the remote client queriers
// of each configured remote read endpoint.
// ChunkQuerier 合并 remote chunk 查询，使用 CompactingChunkSeriesMerger。
func (s *Storage) ChunkQuerier(mint, maxt int64) (storage.ChunkQuerier, error) {
	s.mtx.Lock()
	queryables := s.queryables
	s.mtx.Unlock()

	queriers := make([]storage.ChunkQuerier, 0, len(queryables))
	for _, queryable := range queryables {
		q, err := queryable.ChunkQuerier(mint, maxt)
		if err != nil {
			return nil, err
		}
		queriers = append(queriers, q)
	}
	return storage.NewMergeChunkQuerier(nil, queriers, storage.NewCompactingChunkSeriesMerger(storage.ChainedSeriesMerge)), nil
}

// Appender implements storage.Storage.
func (s *Storage) Appender(ctx context.Context) storage.Appender {
	return s.rws.Appender(ctx)
}

// AppenderV2 implements storage.Storage.
func (s *Storage) AppenderV2(ctx context.Context) storage.AppenderV2 {
	return s.rws.AppenderV2(ctx)
}

// LowestSentTimestamp returns the lowest sent timestamp across all queues.
func (s *Storage) LowestSentTimestamp() int64 {
	return s.rws.LowestSentTimestamp()
}

// Close the background processing of the storage queues.
func (s *Storage) Close() error {
	s.deduper.Stop()
	s.mtx.Lock()
	defer s.mtx.Unlock()
	return s.rws.Close()
}

func labelsToEqualityMatchers(ls model.LabelSet) []*labels.Matcher {
	ms := make([]*labels.Matcher, 0, len(ls))
	for k, v := range ls {
		ms = append(ms, &labels.Matcher{
			Type:  labels.MatchEqual,
			Name:  string(k),
			Value: string(v),
		})
	}
	return ms
}

// Used for hashing configs and diff'ing hashes in ApplyConfig.
// toHash 对配置 YAML 做 MD5，用于 diff 与队列命名。
func toHash(data any) (string, error) {
	bytes, err := yaml.Marshal(data)
	if err != nil {
		return "", err
	}
	hash := md5.Sum(bytes)
	return hex.EncodeToString(hash[:]), nil
}
