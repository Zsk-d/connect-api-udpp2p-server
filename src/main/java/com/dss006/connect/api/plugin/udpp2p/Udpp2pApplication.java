package com.dss006.connect.api.plugin.udpp2p;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author daishaoshu
 */
@SpringBootApplication
@EnableScheduling
public class Udpp2pApplication {

    public static void main(String[] args) {
        SpringApplication.run(Udpp2pApplication.class, args);
    }

}
