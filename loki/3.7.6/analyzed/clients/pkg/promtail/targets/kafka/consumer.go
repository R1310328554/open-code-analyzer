package kafka

// Kafka ConsumerGroup 封装：实现 sarama.ConsumerGroupHandler，
// 为每个 partition claim 创建 Target 并维护 active/dropped 列表，rebalance 时重置。

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/IBM/sarama"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"

	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

var defaultBackOff = backoff.Config{
	MinBackoff: 1 * time.Second,
	MaxBackoff: 60 * time.Second,
	MaxRetries: 20,
}

// 可运行 target 扩展 Target 接口，增加 run() 消费消息循环。
type RunnableTarget interface {
	target.Target
	run()
}

type TargetDiscoverer interface {
	NewTarget(sarama.ConsumerGroupSession, sarama.ConsumerGroupClaim) (RunnableTarget, error)
}

// 嵌入 ConsumerGroup，通过 TargetDiscoverer 为每个 claim 实例化 target。
// consumer handle a group consumer instance.
// It will create a new target for every consumer claim using the `TargetDiscoverer`.
type consumer struct {
	sarama.ConsumerGroup
	discoverer TargetDiscoverer
	logger     log.Logger

	ctx    context.Context
	cancel context.CancelFunc
	wg     sync.WaitGroup

	mutex          sync.Mutex // used during rebalancing setup and tear down
	activeTargets  []target.Target
	droppedTargets []target.Target
}

// 后台 goroutine 无限循环 Consume，失败时指数退避重试至多 MaxRetries 次。
// start starts the consumer for a given list of topics.
func (c *consumer) start(ctx context.Context, topics []string) {
	c.wg.Wait()
	c.wg.Add(1)

	c.ctx, c.cancel = context.WithCancel(ctx)
	level.Info(c.logger).Log("msg", "starting consumer", "topics", fmt.Sprintf("%+v", topics))

	go func() {
		defer c.wg.Done()
		backoff := backoff.New(c.ctx, defaultBackOff)
		for {
			// Calling Consume in an infinite loop in case rebalancing is kicking in.
			// In which case all claims will be renewed.
			err := c.Consume(c.ctx, topics, c)
			if err != nil && err != context.Canceled {
				level.Error(c.logger).Log("msg", "error from the consumer, retrying...", "err", err)
				// backoff before re-trying.
				backoff.Wait()
				if backoff.Ongoing() {
					continue
				}
				level.Error(c.logger).Log("msg", "maximun error from the consumer reached", "last_err", err)
				return
			}
			if c.ctx.Err() != nil || err == context.Canceled {
				level.Info(c.logger).Log("msg", "stopping consumer", "topics", fmt.Sprintf("%+v", topics))
				return
			}
			backoff.Reset()
		}
	}()
}

// 标签为空时归入 droppedTargets 仍消费以推进 offset，否则加入 activeTargets。
// ConsumeClaim creates a target for the given received claim and start reading message from it.
func (c *consumer) ConsumeClaim(session sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	c.wg.Add(1)
	defer c.wg.Done()

	t, err := c.discoverer.NewTarget(session, claim)
	if err != nil {
		return err
	}
	if len(t.Labels()) == 0 {
		c.addDroppedTarget(t)
		t.run()
		return nil
	}
	c.addTarget(t)
	level.Info(c.logger).Log("msg", "consuming topic", "details", t.Details())
	t.run()

	return nil
}

// Setup is run at the beginning of a new session, before ConsumeClaim
func (c *consumer) Setup(_ sarama.ConsumerGroupSession) error {
	c.resetTargets()
	return nil
}

// Cleanup is run at the end of a session, once all ConsumeClaim goroutines have exited
func (c *consumer) Cleanup(sarama.ConsumerGroupSession) error {
	c.resetTargets()
	return nil
}

// stop stops the consumer.
func (c *consumer) stop() {
	c.cancel()
	c.wg.Wait()
	c.resetTargets()
}

// rebalance Setup/Cleanup 时清空 active 与 dropped 列表。
func (c *consumer) resetTargets() {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	c.activeTargets = nil
	c.droppedTargets = nil
}

func (c *consumer) getActiveTargets() []target.Target {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	return c.activeTargets
}

func (c *consumer) getDroppedTargets() []target.Target {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	return c.droppedTargets
}

func (c *consumer) addTarget(t target.Target) {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	c.activeTargets = append(c.activeTargets, t)
}

func (c *consumer) addDroppedTarget(t target.Target) {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	c.droppedTargets = append(c.droppedTargets, t)
}
