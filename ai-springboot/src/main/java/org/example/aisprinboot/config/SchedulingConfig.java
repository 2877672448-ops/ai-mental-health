package org.example.aisprinboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务 + 异步任务配置
 * <p>
 * 设计要点：
 * 1. @EnableScheduling 启用定时任务，让 @Scheduled 注解生效
 * 2. @EnableAsync 启用异步，让 @Async 注解生效
 * 3. 自定义 alertMailExecutor 线程池（核心2、最大5、队列100），避免用默认的 SimpleAsyncTaskExecutor
 *
 * @author PANJU
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

    /**
     * 预警邮件发送专用线程池
     * - 核心线程 2：日常并发邮件发送足够
     * - 最大线程 5：突发流量时扩容
     * - 队列 100：堆积时缓冲
     * - 拒绝策略 CallerRunsPolicy：队列满时由调用线程执行（兜底，不丢任务）
     */
    @Bean("alertMailExecutor")
    public Executor alertMailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("alert-mail-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
