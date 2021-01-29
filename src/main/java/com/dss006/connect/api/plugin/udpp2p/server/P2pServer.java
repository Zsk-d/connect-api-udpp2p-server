package com.dss006.connect.api.plugin.udpp2p.server;

import com.dss006.connect.api.plugin.udpp2p.handler.P2pServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * P2PServer
 *
 * @author daishaoshu
 */
@Component
public class P2pServer extends Thread {

    @Value("${p2p-api.udp.port:9202}")
    private Integer port;

    @Autowired
    private P2pServerHandler p2pServerHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(P2pServer.class);

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(p2pServerHandler);
            LOGGER.info("Server running. port = {}", port);
            b.bind(port).sync().channel().closeFuture().await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
        }
    }

}