package com.dss006.connect.api.plugin.udpp2p.service;

import com.dss006.connect.api.plugin.udpp2p.mapper.P2pMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@AllArgsConstructor
@Slf4j
public class CleanService {

    private P2pMapper mapper;

    private static final int CLEAN_INACTIVE_USER_MINUTES = 4;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void cleanInactiveUser() {
        log.info("开始清理连接...");
        int deleteCount = mapper.deleteInactiveUserGroup(CLEAN_INACTIVE_USER_MINUTES);
        log.info("已清理[{}]个连接", deleteCount / 2);
    }
}
