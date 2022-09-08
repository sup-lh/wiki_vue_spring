package com.suplin.demo.job;

import com.suplin.demo.aspect.LogAspect;
import com.suplin.demo.service.DocTable;
import com.suplin.demo.util.SnowFlake;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class DocJob {

    private final static Logger LOG = LoggerFactory.getLogger(LogAspect.class);

    @Resource
    private DocTable docTable;


    @Resource
    private SnowFlake snowFlake;

    /**
     * 每30秒更新电子书信息
     */
    @Scheduled(cron = "5/30 * * * * ?")
    public void cron() {
        // 增加日志流水号
        MDC.put("LOG_ID", String.valueOf(snowFlake.nextId()));

        long start = System.currentTimeMillis();
        LOG.info("定时任务开始");
        docTable.updateEbookInfo();
        LOG.info("定时任务结束，耗时{}毫秒", System.currentTimeMillis() - start);
    }

}