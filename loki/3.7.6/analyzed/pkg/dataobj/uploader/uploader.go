package uploader

// dataobj 对象上传器：按 SHA-224 前缀生成对象存储键，带指数退避重试将 dataobj 写入 bucket。

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"flag"
	"fmt"
	"io"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/thanos-io/objstore"

	"github.com/grafana/loki/v3/pkg/dataobj"
)

// Config 控制对象键路径中 SHA 前缀长度，用于在对象存储中分散热点。
type Config struct {
	// SHAPrefixSize is the size of the SHA prefix used for splitting object storage keys
	SHAPrefixSize int
}

// RegisterFlagsWithPrefix 注册 sha-prefix-size 等上传相关命令行参数。
// RegisterFlagsWithPrefix registers flags with the given prefix.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.IntVar(&cfg.SHAPrefixSize, prefix+"sha-prefix-size", 2, "The size of the SHA prefix to use for generating object storage keys for data objects.")
}

func (cfg *Config) Validate() error {
	if cfg.SHAPrefixSize <= 0 {
		return fmt.Errorf("SHAPrefixSize must be greater than 0")
	}
	return nil
}

// Uploader 封装 bucket 客户端、Prometheus 指标与 SHA 前缀配置。
type Uploader struct {
	SHAPrefixSize int
	bucket        objstore.Bucket
	metrics       *metrics
	logger        log.Logger
}

func New(cfg Config, bucket objstore.Bucket, logger log.Logger) *Uploader {
	metrics := newMetrics(cfg.SHAPrefixSize)

	return &Uploader{
		SHAPrefixSize: cfg.SHAPrefixSize,
		bucket:        bucket,
		metrics:       metrics,
		logger:        logger,
	}
}

func (d *Uploader) RegisterMetrics(reg prometheus.Registerer) error {
	return d.metrics.register(reg)
}

func (d *Uploader) UnregisterMetrics(reg prometheus.Registerer) {
	d.metrics.unregister(reg)
}

// getKey 读取对象内容计算 SHA-224，生成 objects/<前缀>/<完整哈希> 存储路径。
// getKey determines the key in object storage to upload the object to, based on our path scheme.
func (d *Uploader) getKey(ctx context.Context, object *dataobj.Object) (string, error) {
	hash := sha256.New224()

	reader, err := object.Reader(ctx)
	if err != nil {
		return "", err
	}
	defer reader.Close()

	if _, err := io.Copy(hash, reader); err != nil {
		return "", err
	}

	var sumBytes [sha256.Size224]byte
	sum := hash.Sum(sumBytes[:0])
	sumStr := hex.EncodeToString(sum)

	return fmt.Sprintf("objects/%s/%s", sumStr[:d.SHAPrefixSize], sumStr[d.SHAPrefixSize:]), nil
}

// Upload 在最多 20 次指数退避重试内将 dataobj 写入 bucket 并记录耗时与大小指标。
// Upload uploads an object to the configured bucket and returns the key.
func (d *Uploader) Upload(ctx context.Context, object *dataobj.Object) (key string, err error) {
	start := time.Now()

	timer := prometheus.NewTimer(d.metrics.uploadTime)
	defer timer.ObserveDuration()

	objectPath, err := d.getKey(ctx, object)
	if err != nil {
		d.metrics.uploadFailures.Inc()
		d.metrics.uploadSize.WithLabelValues(statusFailure).Observe(float64(object.Size()))
		return "", fmt.Errorf("generating object key: %w", err)
	}

	backoff := backoff.New(ctx, backoff.Config{
		MinBackoff: 100 * time.Millisecond,
		MaxBackoff: 10 * time.Second,
		MaxRetries: 20,
	})

	size := object.Size()

	logger := log.With(d.logger, "key", objectPath, "size", size)

	for backoff.Ongoing() {
		level.Debug(logger).Log("msg", "attempting to upload dataobj to object storage", "attempt", backoff.NumRetries())

		err = func() error {
			reader, err := object.Reader(ctx)
			if err != nil {
				return err
			}
			defer reader.Close()
			return d.bucket.Upload(ctx, objectPath, reader)
		}()
		if err == nil {
			break
		}
		level.Warn(logger).Log("msg", "failed to upload dataobj to object storage; will retry", "err", err, "attempt", backoff.NumRetries())
		backoff.Wait()
	}

	d.metrics.uploadTotal.Inc()

	if err != nil {
		d.metrics.uploadFailures.Inc()
		d.metrics.uploadSize.WithLabelValues(statusFailure).Observe(float64(size))
		return "", fmt.Errorf("uploading object after %d retries: %w", backoff.NumRetries(), err)
	}

	d.metrics.uploadSize.WithLabelValues(statusSuccess).Observe(float64(size))
	level.Info(logger).Log("msg", "uploaded dataobj to object storage", "duration", time.Since(start))
	return objectPath, nil
}
// 上传失败时递增 uploadFailures 并按成功/失败标签记录对象字节大小。
