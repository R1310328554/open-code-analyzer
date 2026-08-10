package util //nolint:revive

// util 包 DNS 监视器封装 grpcutil Watcher，在地址增删时通过 DNSNotifications 回调通知上层 ring 或负载均衡。

import (
	"context"
	"fmt"
	"time"

	"github.com/grafana/dskit/grpcutil"
	"github.com/grafana/dskit/services"
	"github.com/pkg/errors"

	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// DNSNotifications 描述解析结果变更；所有回调均在 watchDNSLoop 同一 goroutine 触发。
// Notifications about address resolution. All notifications are sent on the same goroutine.
type DNSNotifications interface {
	// AddressAdded 在新后端实例出现在 DNS 记录时调用。
// New address has been discovered by DNS watcher for supplied hostname.
	AddressAdded(address string)

	// AddressRemoved 在旧地址从解析结果中消失时调用，便于摘除不健康副本。
// Previously-discovered address is no longer resolved for the hostname.
	AddressRemoved(address string)
}

type dnsWatcher struct {
	watcher       grpcutil.Watcher
	notifications DNSNotifications
}

// NewDNSWatcher 创建 resolver 并包装为 dskit BasicService，便于统一启停。
// NewDNSWatcher creates a new DNS watcher and returns a service that is wrapping it.
func NewDNSWatcher(address string, dnsLookupPeriod time.Duration, notifications DNSNotifications) (services.Service, error) {
	resolver, err := grpcutil.NewDNSResolverWithFreq(dnsLookupPeriod, util_log.Logger)
	if err != nil {
		return nil, err
	}

	watcher, err := resolver.Resolve(address, "")
	if err != nil {
		return nil, err
	}

	w := &dnsWatcher{
		watcher:       watcher,
		notifications: notifications,
	}
	return services.NewBasicService(nil, w.watchDNSLoop, nil), nil
}

// watchDNSLoop 迭代 watcher.Next，将 Add/Delete 操作映射为通知接口调用。
// watchDNSLoop watches for changes in DNS and sends notifications.
func (w *dnsWatcher) watchDNSLoop(servCtx context.Context) error {
	go func() {
		// Close the watcher, when this service is asked to stop.
		// Closing the watcher makes watchDNSLoop exit, since it only iterates on watcher updates, and has no other
		// way to stop. We cannot close the watcher in `stopping` method, because it is only called *after*
		// watchDNSLoop exits.
		<-servCtx.Done()
		w.watcher.Close()
	}()

	for {
		updates, err := w.watcher.Next()
		if err != nil {
			// watcher.Next returns error when Close is called, but we call Close when our context is done.
			// we don't want to report error in that case.
			if servCtx.Err() != nil {
				return nil
			}
			return errors.Wrapf(err, "error from DNS watcher")
		}

		for _, update := range updates {
			switch update.Op {
			case grpcutil.Add:
				w.notifications.AddressAdded(update.Addr)

			case grpcutil.Delete:
				w.notifications.AddressRemoved(update.Addr)

			default:
				return fmt.Errorf("unknown op: %v", update.Op)
			}
		}
	}
}
// 服务停止时在独立 goroutine 关闭 watcher，使 Next 返回后 watchDNSLoop 正常退出。
