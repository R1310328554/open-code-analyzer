package main

// 面向 Docker 的 Loki logger 适配层。
// 封装 Promtail client 与可选 pipeline，将 logger.Message 转为 Loki 条目。

import (
	"bytes"
	"sync"

	"github.com/docker/docker/daemon/logger"
	"github.com/go-kit/log"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/client"

	"github.com/grafana/loki/v3/pkg/logproto"
)

var jobName = "docker"

// Loki 输出适配器，持有 client、标签集与 pipeline handler。
type loki struct {
	client  client.Client
	handler api.EntryHandler
	labels  model.LabelSet
	logger  log.Logger

	closed bool
	mutex  sync.RWMutex

	stop func()
}

// 创建 Loki logger：解析配置、构建 client 与 pipeline 处理器链。
// New create a new Loki logger that forward logs to Loki instance
func New(logCtx logger.Info, logger log.Logger) (logger.Logger, error) {
	logger = log.With(logger, "container_id", logCtx.ContainerID)
	cfg, err := parseConfig(logCtx)
	if err != nil {
		return nil, err
	}
	m := client.NewMetrics(prometheus.DefaultRegisterer)
	c, err := client.New(m, cfg.clientConfig, 0, 0, false, logger)
	if err != nil {
		return nil, err
	}
	var handler api.EntryHandler = c
	var stop = func() {}
	if len(cfg.pipeline.PipelineStages) != 0 {
		pipeline, err := stages.NewPipeline(logger, cfg.pipeline.PipelineStages, &jobName, prometheus.DefaultRegisterer)
		if err != nil {
			return nil, err
		}
		handler = pipeline.Wrap(c)
		stop = handler.Stop
	}
	return &loki{
		client:  c,
		labels:  cfg.labels,
		logger:  logger,
		handler: handler,
		stop:    stop,
	}, nil
}

// 实现 logger.Logger：将非空日志行异步写入 Loki 推送通道。
// Log implements `logger.Logger`
func (l *loki) Log(m *logger.Message) error {
	l.mutex.RLock()
	defer l.mutex.RUnlock()

	if l.closed {
		return errors.New("client closed")
	}

	if len(bytes.Fields(m.Line)) == 0 {
		return nil
	}
	lbs := l.labels.Clone()
	if m.Source != "" {
		lbs["source"] = model.LabelValue(m.Source)
	}
	l.handler.Chan() <- api.Entry{
		Labels: lbs,
		Entry: logproto.Entry{
			Timestamp: m.Timestamp,
			Line:      string(m.Line),
		},
	}
	return nil
}

// Log implements `logger.Logger`
func (l *loki) Name() string {
	return driverName
}

// Log implements `logger.Logger`
func (l *loki) Close() error {
	l.mutex.Lock()
	defer l.mutex.Unlock()
	l.stop()
	l.client.StopNow()
	l.closed = true
	return nil
}
