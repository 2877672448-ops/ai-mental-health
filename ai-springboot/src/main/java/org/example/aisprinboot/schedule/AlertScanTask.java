package org.example.aisprinboot.schedule;

import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.service.alert.AlertScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 危机预警定时任务
 * <p>
 * 设计要点：
 * 1. 只做触发，业务逻辑全部委托给 AlertScanService（保证手动触发和定时触发逻辑一致）
 * 2. 每天凌晨 2:00 扫描昨日日记
 * 3. 错开整点高峰，但注意：默认 @Scheduled 单线程，长时间任务会阻塞后续调度
 *
 * @author PANJU
 */
@Slf4j
@Component
public class AlertScanTask {

    @Autowired
    private AlertScanService alertScanService;

    /**
     * 每天凌晨 2:00 扫描前一天的所有情绪日记
     * <p>
     * cron 表达式：秒 分 时 日 月 周
     * "0 0 2 * * ?"：每天 02:00:00 执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyScan() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("定时任务触发，开始扫描 date={}", yesterday);
        alertScanService.executeScan(yesterday);
    }
}
