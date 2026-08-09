package com.taobao.arthas.grpc.server;

/**
 * Arthas gRPC 服务独立启动入口。
 * <p>
 * 默认在 9091 端口启动 {@link ArthasGrpcServer}，供 labs 模块本地调试。
 *
 * @author: FengYe
 * @date: 2024/10/13 02:40
 * @description: ArthasGrpcServerBootstrap
 */
public class ArthasGrpcBootstrap {
    /** 以默认端口 9091 启动 gRPC 服务并阻塞至通道关闭。 */
    public static void main(String[] args) {
        ArthasGrpcServer arthasGrpcServer = new ArthasGrpcServer(9091, null);
        arthasGrpcServer.start();
    }
}
