package jobqueue

// 水平扩展压缩 Worker 端：通过 JobQueue Loop 双向流接收任务，
// 按 JobType 路由到 JobRunner 执行并回传 JobResult。

import (
	"context"
	"flag"
	"fmt"
	"sync"
	"time"

	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"
	"github.com/pkg/errors"
	"github.com/prometheus/client_golang/prometheus"
	"go.uber.org/atomic"

	"github.com/grafana/loki/v3/pkg/compactor/client/grpc"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

var (
	connBackoffConfig = backoff.Config{
		MinBackoff: 500 * time.Millisecond,
		MaxBackoff: 5 * time.Second,
	}
)

const (
	statusSuccess = "success"
	statusFailure = "failure"
)

// CompactorClient 抽象获取 JobQueue gRPC 客户端的 Compactor 连接。
type CompactorClient interface {
	JobQueueClient() grpc.JobQueueClient
}

type JobRunner interface {
	Run(ctx context.Context, job *grpc.Job) ([]byte, error)
}

type WorkerConfig struct {
	NumWorkers int `yaml:"num_sub_workers"`
}

func (c *WorkerConfig) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.IntVar(&c.NumWorkers, prefix+"num-sub-workers", 4, "Number of sub-workers to run for concurrent processing of jobs.")
}

func (c *WorkerConfig) RegisterFlags(f *flag.FlagSet) {
	c.RegisterFlagsWithPrefix("", f)
}

func (c *WorkerConfig) Validate() error {
	if c.NumWorkers <= 0 {
		return errors.New("num_workers must be > 0")
	}

	return nil
}

// WorkerManager 管理多个 worker 协程及 JobType 到 JobRunner 的映射。
type WorkerManager struct {
	cfg        WorkerConfig
	grpcClient CompactorClient
	jobRunners map[grpc.JobType]JobRunner
	metrics    *workerMetrics

	workers []*worker
	wg      sync.WaitGroup
}

func NewWorkerManager(cfg WorkerConfig, grpcClient CompactorClient, r prometheus.Registerer) *WorkerManager {
	wm := &WorkerManager{
		cfg:        cfg,
		grpcClient: grpcClient,
		jobRunners: make(map[grpc.JobType]JobRunner),
	}

	wm.metrics = newWorkerMetrics(r, func() bool {
		if len(wm.workers) != cfg.NumWorkers {
			return false
		}

		for _, w := range wm.workers {
			if !w.isConnected() {
				return false
			}
		}
		return true
	})

	return wm
}

func (w *WorkerManager) RegisterJobRunner(jobType grpc.JobType, jobRunner JobRunner) error {
	if _, exists := w.jobRunners[jobType]; exists {
		return ErrJobTypeAlreadyRegistered
	}

	w.jobRunners[jobType] = jobRunner
	return nil
}

// Start 启动 NumWorkers 个子 Worker，各自独立维护 gRPC Loop 连接。
func (w *WorkerManager) Start(ctx context.Context) error {
	if len(w.jobRunners) == 0 {
		return errors.New("no job runners registered")
	}

	wg := &sync.WaitGroup{}

	for i := 0; i < w.cfg.NumWorkers; i++ {
		worker := newWorker(w.grpcClient, w.jobRunners, w.metrics)
		w.workers = append(w.workers, worker)

		wg.Add(1)
		go func() {
			defer wg.Done()
			worker.Start(ctx)
		}()
	}

	wg.Wait()
	return nil
}

// worker 持有 gRPC 客户端、JobRunner 映射及连接状态 atomic.Bool。
type worker struct {
	grpcClient CompactorClient
	jobRunners map[grpc.JobType]JobRunner
	metrics    *workerMetrics
	connected  atomic.Bool
}

func newWorker(grpcClient CompactorClient, jobRunners map[grpc.JobType]JobRunner, metrics *workerMetrics) *worker {
	return &worker{
		grpcClient: grpcClient,
		jobRunners: jobRunners,
		metrics:    metrics,
	}
}

func (w *worker) isConnected() bool {
	return w.connected.Load()
}

func (w *worker) Start(ctx context.Context) {
	client := w.grpcClient.JobQueueClient()

	backoff := backoff.New(ctx, connBackoffConfig)
	for backoff.Ongoing() {
		c, err := client.Loop(ctx)
		if err != nil {
			level.Warn(util_log.Logger).Log("msg", "error contacting compactor", "err", err)
			backoff.Wait()
			continue
		}

		if err := w.process(c); err != nil {
			level.Error(util_log.Logger).Log("msg", "error running jobs", "err", err)
			backoff.Wait()
			continue
		}

		backoff.Reset()
	}
}

// process 在 lock-step 模式下 Recv 任务、异步 Run 并通过 Send 回传结果。
// process pull jobs from the established stream, processes them and sends back the job result to the stream.
func (w *worker) process(c grpc.JobQueue_LoopClient) error {
	// Build a child context so we can cancel the job when the stream is closed.
	ctx, cancel := context.WithCancelCause(c.Context())
	defer cancel(errors.New("job queue stream closed"))

	w.connected.Store(true)
	defer w.connected.Store(false)

	for {
		job, err := c.Recv()
		if err != nil {
			return err
		}

		// Execute the job on a "background" goroutine, so we go back to
		// blocking on c.Recv(). This allows us to detect the stream closing
		// and cancel the job execution. We don't process jobs in parallel
		// here, as we're running in a lock-step with the server - each Recv is
		// paired with a Send.
		go func() {
			jobFailed := true
			defer func() {
				status := statusSuccess
				if jobFailed {
					status = statusFailure
				}

				w.metrics.jobsProcessed.WithLabelValues(status).Inc()
			}()

			jobResult := &grpc.JobResult{
				JobId:   job.Id,
				JobType: job.Type,
			}

			jobRunner, ok := w.jobRunners[job.Type]
			if !ok {
				level.Error(util_log.Logger).Log("msg", "job runner for job type not registered", "jobType", job.Type)
				jobResult.Error = fmt.Sprintf("unknown job type %s", job.Type)
				if err := c.Send(jobResult); err != nil {
					level.Error(util_log.Logger).Log("msg", "error sending job result", "err", err)
				}
				return
			}

			jobResponse, err := jobRunner.Run(ctx, job)
			if err != nil {
				level.Error(util_log.Logger).Log("msg", "error running job", "job_id", job.Id, "err", err)
				jobResult.Error = err.Error()
				if err := c.Send(jobResult); err != nil {
					level.Error(util_log.Logger).Log("msg", "error sending job result", "job_id", job.Id, "err", err)
				}
				return
			}

			jobResult.Result = jobResponse
			if err := c.Send(jobResult); err != nil {
				level.Error(util_log.Logger).Log("msg", "error sending job result", "job_id", job.Id, "err", err)
				return
			}

			jobFailed = false
		}()
	}
}
