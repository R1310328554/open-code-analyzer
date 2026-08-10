package query

// TailQuery 通过 WebSocket 连接 Loki live tail 端点，持续接收并格式化日志流。

import (
	"context"
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"github.com/fatih/color"
	"github.com/gorilla/websocket"
	"github.com/grafana/dskit/backoff"

	"github.com/grafana/loki/v3/pkg/logcli/client"
	"github.com/grafana/loki/v3/pkg/logcli/output"
	"github.com/grafana/loki/v3/pkg/logcli/util"
	"github.com/grafana/loki/v3/pkg/loghttp"
	"github.com/grafana/loki/v3/pkg/util/unmarshal"
)

// TailQuery 建立 tail 连接，处理 SIGINT 优雅关闭，并在断线时指数退避重连。
// TailQuery connects to the Loki websocket endpoint and tails logs
func (q *Query) TailQuery(delayFor time.Duration, c client.Client, out output.LogOutput) {
	conn, err := c.LiveTailQueryConn(q.QueryString, delayFor, q.Limit, q.Start, q.Quiet)
	if err != nil {
		log.Fatalf("Tailing logs failed: %+v", err)
	}

// 后台 goroutine 监听中断信号，向服务端发送 WebSocket 正常关闭帧。
	go func() {
		stopChan := make(chan os.Signal, 1)
		signal.Notify(stopChan, os.Interrupt, syscall.SIGTERM)
		<-stopChan
		if err := conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, "")); err != nil {
			log.Println("Error closing websocket:", err)
		}
		os.Exit(0)
	}()

	if len(q.IgnoreLabelsKey) > 0 && !q.Quiet {
		log.Println("Ignoring labels key:", color.RedString(strings.Join(q.IgnoreLabelsKey, ",")))
	}

	if len(q.ShowLabelsKey) > 0 && !q.Quiet {
		log.Println("Print only labels key:", color.RedString(strings.Join(q.ShowLabelsKey, ",")))
	}

	lastReceivedTimestamp := q.Start

	for {
		tailResponse := new(loghttp.TailResponse)
		err := unmarshal.ReadTailResponseJSON(tailResponse, conn)
		if err != nil {
// 异常关闭（如 querier 重启）时记录 lastReceivedTimestamp 并重连 tail。
			// Check if the websocket connection closed unexpectedly. If so, retry.
			// The connection might close unexpectedly if the querier handling the tail request
			// in Loki stops running. The following error would be printed:
			// "websocket: close 1006 (abnormal closure): unexpected EOF"
			if websocket.IsCloseError(err, websocket.CloseAbnormalClosure) {
				log.Printf("Remote websocket connection closed unexpectedly (%+v). Connecting again.", err)

				// Close previous connection. If it fails to close the connection it should be fine as it is already broken.
				if err = conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, "")); err != nil {
					log.Printf("Error closing websocket: %+v", err)
				}

// backoff 最多重试 5 次，重连起点为上次收到日志的时间戳。
				// Try to re-establish the connection up to 5 times.
				backoff := backoff.New(context.Background(), backoff.Config{
					MinBackoff: 1 * time.Second,
					MaxBackoff: 10 * time.Second,
					MaxRetries: 5,
				})

				for backoff.Ongoing() {
					conn, err = c.LiveTailQueryConn(q.QueryString, delayFor, q.Limit, lastReceivedTimestamp, q.Quiet)
					if err == nil {
						break
					}

					log.Println("Error recreating tailing connection after unexpected close, will retry:", err)
					backoff.Wait()
				}

				if err = backoff.Err(); err != nil {
					log.Println("Error recreating tailing connection:", err)
					return
				}

				continue
			}

			log.Println("Error reading stream:", err)
			return
		}

		labels := loghttp.LabelSet{}
		for _, stream := range tailResponse.Streams {
			if !q.NoLabels {
				if len(q.IgnoreLabelsKey) > 0 || len(q.ShowLabelsKey) > 0 {

					ls := stream.Labels

					if len(q.ShowLabelsKey) > 0 {
						ls = matchLabels(true, ls, q.ShowLabelsKey)
					}

// tail 流上按 ShowLabelsKey/IgnoreLabelsKey 过滤标签后再输出。
					if len(q.IgnoreLabelsKey) > 0 {
						ls = matchLabels(false, ls, q.ShowLabelsKey)
					}

					labels = ls

				} else {
					labels = stream.Labels
				}
			}

			for _, entry := range stream.Entries {
				out.FormatAndPrintln(entry.Timestamp, labels, 0, entry.Line)
				lastReceivedTimestamp = entry.Timestamp
			}

		}
// 客户端处理过慢时服务端会丢弃流，此处打印 DroppedStreams 告警。
		if len(tailResponse.DroppedStreams) != 0 {
			log.Println("Server dropped following entries due to slow client")
			for _, d := range tailResponse.DroppedStreams {
				log.Println(d.Timestamp, d.Labels)
			}
		}
	}
}

func matchLabels(on bool, l loghttp.LabelSet, names []string) loghttp.LabelSet {
	return util.MatchLabels(on, l, names)
}
// matchLabels 委托 util.MatchLabels，与 print 包标签过滤语义一致。
