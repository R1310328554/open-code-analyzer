package scheduler

// collector 实现 prometheus.Collector，聚合调度器负载、在途任务/流、连接数与公平队列深度。

import (
	"context"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"golang.org/x/sync/errgroup"

	"github.com/grafana/loki/v3/pkg/engine/internal/util/ewma"
	"github.com/grafana/loki/v3/pkg/engine/internal/workflow"
)

// collector 持有 Scheduler 指针，通过 EWMA 平滑 load 并周期性 Process 更新滑动窗口。
// collector implements [prometheus.Collector], collecting metrics for a
// [Scheduler].
type collector struct {
	sched *Scheduler

	load        prometheus.GaugeFunc
	loadAverage *ewma.EWMA

	tasksInflight   *prometheus.Desc
	streamsInflight *prometheus.Desc

	connections  *prometheus.Desc
	readyWorkers *prometheus.Desc
	taskQueue    *prometheus.Desc
}

var _ prometheus.Collector = (*collector)(nil)

// newCollector returns a new collector for the given scheduler. Load and
// saturation average metrics will only be computed after calling
// [collector.Process].
func newCollector(sched *Scheduler) *collector {
	var (
		loadSource ewma.SourceFunc = func() float64 { return computeLoad(sched) }
	)

	return &collector{
		sched: sched,

		load: prometheus.NewGaugeFunc(prometheus.GaugeOpts{
			Name: "loki_engine_scheduler_load",
			Help: "Current load on the scheduler (count of running and pending tasks)",
		}, loadSource),
		loadAverage: ewma.MustNew(ewma.Options{
			Name: "loki_engine_scheduler_load_average",
			Help: "Average load on the scheduler over time",

			UpdateFrequency: 5 * time.Second,
			Windows:         []time.Duration{1 * time.Minute, 5 * time.Minute, 15 * time.Minute},
		}, loadSource),

		tasksInflight: prometheus.NewDesc(
			"loki_engine_scheduler_tasks_inflight",
			"Number of in-flight tasks by state",
			[]string{"state"},
			nil,
		),
		streamsInflight: prometheus.NewDesc(
			"loki_engine_scheduler_streams_inflight",
			"Number of in-flight streams by state",
			[]string{"state"},
			nil,
		),

		connections: prometheus.NewDesc(
			"loki_engine_scheduler_connections_active",
			"Current number active connections to the scheduler",
			nil,
			nil,
		),

		readyWorkers: prometheus.NewDesc(
			"loki_engine_scheduler_ready_workers",
			"Current number of workers ready to accept tasks",
			nil,
			nil,
		),
		taskQueue: prometheus.NewDesc(
			"loki_engine_scheduler_task_queue_length",
			"Current number of tasks waiting in the assignment queue",
			nil,
			nil,
		),
	}
}

// computeLoad 统计 RUNNING 与 PENDING 任务数，作为当前调度压力 gauge 源。
// computeLoad returns the active load on the scheduler: the sum of running and
// pending tasks.
func computeLoad(sched *Scheduler) float64 {
	sched.resourcesMut.RLock()
	defer sched.resourcesMut.RUnlock()

	var load uint64

	for _, t := range sched.tasks {
		if t.status.State == workflow.TaskStateRunning || t.status.State == workflow.TaskStatePending {
			load++
		}
	}

	return float64(load)
}

// Process 在独立 goroutine 中运行 EWMA Monitor，直到 context 取消。
// Process performs stat computations for EWMA metrics of the collector. Process
// runs until the provided context is canceled.
func (mc *collector) Process(ctx context.Context) error {
	g, ctx := errgroup.WithContext(ctx)

	g.Go(func() error { return mc.loadAverage.Monitor(ctx) })

	return g.Wait()
}

func (mc *collector) Collect(ch chan<- prometheus.Metric) {
	mc.load.Collect(ch)
	mc.loadAverage.Collect(ch)

	mc.collectResourceStats(ch)
	mc.collectConnStats(ch)
	mc.collectAssignStats(ch)
}

func (mc *collector) collectResourceStats(ch chan<- prometheus.Metric) {
	mc.sched.resourcesMut.RLock()
	defer mc.sched.resourcesMut.RUnlock()

	var (
		tasksByState   = make(map[workflow.TaskState]int)
		streamsByState = make(map[workflow.StreamState]int)
	)

	for _, t := range mc.sched.tasks {
		tasksByState[t.status.State]++
	}
	for _, s := range mc.sched.streams {
		streamsByState[s.state]++
	}

	for state, count := range tasksByState {
		ch <- prometheus.MustNewConstMetric(mc.tasksInflight, prometheus.GaugeValue, float64(count), state.String())
	}
	for state, count := range streamsByState {
		ch <- prometheus.MustNewConstMetric(mc.streamsInflight, prometheus.GaugeValue, float64(count), state.String())
	}
}

// collectConnStats 仅统计 control plane 连接，数据面连接不计入 active connections。
func (mc *collector) collectConnStats(ch chan<- prometheus.Metric) {
	var (
		totalConnections int
	)

	mc.sched.connections.Range(func(key, _ any) bool {
		wc := key.(*workerConn)

		// We only want to count connections which have been flagged as control
		// plane connections (meaning they will be assigned work, and are a part
		// of the set of compute).
		if wc.Type() != connectionTypeControlPlane {
			return true
		}

		totalConnections++
		return true
	})

	ch <- prometheus.MustNewConstMetric(mc.connections, prometheus.GaugeValue, float64(totalConnections))
}

func (mc *collector) collectAssignStats(ch chan<- prometheus.Metric) {
	mc.sched.assignMut.RLock()
	defer mc.sched.assignMut.RUnlock()

	ch <- prometheus.MustNewConstMetric(mc.readyWorkers, prometheus.GaugeValue, float64(len(mc.sched.readyWorkers)))
	ch <- prometheus.MustNewConstMetric(mc.taskQueue, prometheus.GaugeValue, float64(mc.sched.taskQueue.Len()))
}

func (mc *collector) Describe(ch chan<- *prometheus.Desc) {
	ch <- mc.tasksInflight
	ch <- mc.streamsInflight
	ch <- mc.connections
	ch <- mc.readyWorkers
	ch <- mc.taskQueue
}
// collectAssignStats 暴露 readyWorkers 与 taskQueue 长度。
