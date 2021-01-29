package com.dss006.connect.api.plugin.udpp2p;

import com.dss006.connect.api.plugin.udpp2p.server.P2pServer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author daishaoshu
 */
@Component
@AllArgsConstructor
public class ServerRun {

    private P2pServer p2pServer;

    @PostConstruct
    public void runP2pServer() {
        p2pServer.start();
    }
}
