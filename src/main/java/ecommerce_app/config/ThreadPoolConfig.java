package ecommerce_app.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class ThreadPoolConfig {

  private static final int CORE_POOL_SIZE = 10;
  private static final int MAX_POOL_SIZE = 50;
  private static final int QUEUE_CAPACITY = 100;
  private static final int KEEP_ALIVE_SECONDS = 60;

  /** Thread pool for general async tasks */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY);
    executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
    executor.setThreadNamePrefix("Async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  /** Thread pool for I/O intensive tasks (file operations, external API calls) */
  @Bean(name = "ioTaskExecutor")
  public Executor ioTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(200);
    executor.setKeepAliveSeconds(120);
    executor.setThreadNamePrefix("IO-Async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(120);
    executor.initialize();
    return executor;
  }

  /** Thread pool for CPU intensive tasks */
  @Bean(name = "cpuTaskExecutor")
  public Executor cpuTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    int coreCount = Runtime.getRuntime().availableProcessors();
    executor.setCorePoolSize(coreCount);
    executor.setMaxPoolSize(coreCount * 2);
    executor.setQueueCapacity(50);
    executor.setKeepAliveSeconds(30);
    executor.setThreadNamePrefix("CPU-Async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    executor.initialize();
    return executor;
  }

  /** Scheduled task executor */
  @Bean(name = "scheduledTaskExecutor")
  public Executor scheduledTaskExecutor() {
    ScheduledExecutorFactoryBean factoryBean = new ScheduledExecutorFactoryBean();
    factoryBean.setPoolSize(5);
    factoryBean.setThreadNamePrefix("Scheduled-");
    factoryBean.setRejectedExecutionHandler(
        (_, _) -> log.error("Scheduled task rejected"));
    return factoryBean.getObject();
  }
}
