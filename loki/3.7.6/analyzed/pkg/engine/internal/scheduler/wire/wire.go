// wire 包定义调度器 peer 之间的线协议抽象：Listener、Dialer 与 Conn 接口。
// Package wire provides the wire protocol for how peers scheduler peers
// communicate.
package wire

import (
	"context"
	"errors"
	"net"
)

// ErrConnClosed 表示 peer 连接已关闭，后续 Send/Recv 应返回此错误。
// ErrConnClosed indicates a closed connection between peers.
var ErrConnClosed = errors.New("connection closed")

// Listener 监听入站连接，Accept 在 ctx 取消或 Close 时返回错误。
// Listener waits for incoming connections from scheduler peers.
type Listener interface {
	// Accept waits for and returns the next connection to the listener. Accept
	// returns an error if the context is canceled or if the listener is closed.
	Accept(ctx context.Context) (Conn, error)

	// Close closes the listener. Any blocked Accept operations will be
	// unblocked and return errors.
	Close(ctx context.Context) error

	// Addr returns the listener's advertised network address. Peers use this
	// address to connect to the listener.
	Addr() net.Addr
}

// Dialer 主动拨号建立连接，from 地址供对端回连，to 为目标 peer 地址。
// A Dialer establishes connections to scheduler peers.
type Dialer interface {
	// Dial connects to the scheduler peer at the provided "to" address. The
	// "from" address is used to establish the address that can be used to
	// connect back to the caller. Dial returns an error if the context is
	// canceled or if the connection cannot be established.
	Dial(ctx context.Context, from, to net.Addr) (Conn, error)
}

// Conn 是双向帧流：Send 不等待确认，Recv 需持续调用以免阻塞对端发送。
// Conn is a communication stream between two peers.
type Conn interface {
	// Send sends the provided Frame to the peer. Send blocks until the Frame
	// has been sent to the peer, but does not wait for the peer to acknowledge
	// receipt of the Frame.
	//
	// Send returns an error if the context is canceled or if the connection is
	// closed.
	Send(context.Context, Frame) error

	// Recv receives the next Frame from the peer. Recv blocks until a Frame is
	// available. Recv returns an error if the context is canceled or if the
	// connection is closed.
	//
	// Callers should take care to avoid long periods of where there is not an
	// active call to Recv to avoid blocking the peer's Send call.
	Recv(context.Context) (Frame, error)

	// Close closes the Conn. Close may be called by either side of the
	// connection. After the connection has been closed, calls to Send or Recv
	// return [ErrConnClosed].
	Close() error

	// LocalAddr returns the address of the local side of the connection.
	LocalAddr() net.Addr

	// RemoteAddr returns the address of the remote side of the connection.
	RemoteAddr() net.Addr
}
// 线协议层与 HTTP/2、本地内存通道等具体传输实现解耦。
