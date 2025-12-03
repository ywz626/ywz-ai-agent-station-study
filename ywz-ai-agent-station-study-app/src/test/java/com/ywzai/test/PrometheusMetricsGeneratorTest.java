package com.ywzai.test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Prometheus指标模拟生成器的Java实现 基于 generate-metrics.sh 脚本转换而来 用途：生成模拟的Prometheus格式指标数据，供Node
 * Exporter的textfile收集器采集
 *
 * @author xiaofuge
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PrometheusMetricsGeneratorTest {

  // 指标文件路径
  private static final String METRICS_FILE = "/tmp/custom_metrics.prom";

  // 指标更新间隔（毫秒）
  private static final int UPDATE_INTERVAL_MS = 15000;

  // 小数格式化器
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

  // 运行标志
  private volatile boolean running = true;

  @Test
  public void testGeneratePrometheusMetrics() {
    log.info("=== Prometheus指标模拟生成器 ===");
    log.info(
        "开始时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    log.info("指标文件路径: {}", METRICS_FILE);

    // 检查依赖
    if (!checkDependencies()) {
      return;
    }

    log.info("");
    log.info("🚀 开始生成模拟指标数据...");
    log.info("💡 运行测试方法来停止脚本");
    log.info("📊 指标更新间隔: {}秒", UPDATE_INTERVAL_MS / 1000);
    log.info("");

    // 注册关闭钩子
    Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

    // 持续生成指标（测试环境下运行有限次数）
    int maxIterations = 10; // 测试环境下限制运行次数
    int iteration = 0;

    while (running && iteration < maxIterations) {
      try {
        if (generateMetrics()) {
          log.info("{}: 📈 指标数据更新成功", getCurrentTimestamp());
        } else {
          log.warn("{}: ⚠️ 指标数据更新失败", getCurrentTimestamp());
        }

        iteration++;
        if (iteration < maxIterations) {
          Thread.sleep(UPDATE_INTERVAL_MS);
        }
      } catch (InterruptedException e) {
        log.info("接收到中断信号，停止生成指标");
        Thread.currentThread().interrupt();
        break;
      }
    }

    log.info("指标生成测试完成，共运行 {} 次", iteration);
    cleanup();
  }

  /** 检查必要的依赖 */
  private boolean checkDependencies() {
    try {
      // 检查是否可以创建文件
      Path metricsPath = Paths.get(METRICS_FILE);
      Path parentDir = metricsPath.getParent();

      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir);
      }

      log.info("✅ 依赖检查通过");
      return true;
    } catch (Exception e) {
      log.error("❌ 依赖检查失败: {}", e.getMessage());
      return false;
    }
  }

  /** 生成模拟指标 */
  private boolean generateMetrics() {
    try (FileWriter writer = new FileWriter(METRICS_FILE)) {
      StringBuilder metrics = new StringBuilder();

      // HTTP请求响应时间指标
      generateHttpRequestMetrics(metrics);

      // JVM内存指标
      generateJvmMemoryMetrics(metrics);

      // JVM GC指标
      generateJvmGcMetrics(metrics);

      // 系统指标
      generateSystemMetrics(metrics);

      // 业务指标
      generateBusinessMetrics(metrics);

      // 数据库和Redis指标
      generateDatabaseMetrics(metrics);

      // 应用指标
      generateApplicationMetrics(metrics);

      // 写入文件
      writer.write(metrics.toString());
      writer.flush();

      // 设置文件权限（Unix系统）
      setFilePermissions();

      // 验证文件
      return verifyMetricsFile();

    } catch (IOException e) {
      log.error("生成指标文件失败", e);
      return false;
    }
  }

  /** 生成HTTP请求响应时间指标 */
  private void generateHttpRequestMetrics(StringBuilder metrics) {
    metrics.append("# HELP http_server_requests_seconds HTTP请求响应时间\n");
    metrics.append("# TYPE http_server_requests_seconds histogram\n");

    // lock_market_pay_order 接口
    String uri1 = "/api/v1/lock_market_pay_order";
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.001\"} %d\n",
            uri1, generateRandom(50, 100)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.01\"} %d\n",
            uri1, generateRandom(100, 300)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.1\"} %d\n",
            uri1, generateRandom(300, 800)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"1.0\"} %d\n",
            uri1, generateRandom(800, 1200)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"+Inf\"} %d\n",
            uri1, generateRandom(1200, 1500)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_sum{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n",
            uri1, generateRandomDecimal(5.0, 50.0)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_count{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %d\n\n",
            uri1, generateRandom(1000, 5000)));

    // group_buy/progress 接口
    String uri2 = "/api/v1/group_buy/progress";
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.001\"} %d\n",
            uri2, generateRandom(100, 200)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.01\"} %d\n",
            uri2, generateRandom(200, 500)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.1\"} %d\n",
            uri2, generateRandom(500, 1000)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"1.0\"} %d\n",
            uri2, generateRandom(1000, 1500)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"+Inf\"} %d\n",
            uri2, generateRandom(1500, 2000)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_sum{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n",
            uri2, generateRandomDecimal(2.0, 20.0)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_count{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %d\n\n",
            uri2, generateRandom(2000, 8000)));

    // team/create 接口
    String uri3 = "/api/v1/team/create";
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.001\"} %d\n",
            uri3, generateRandom(30, 80)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.01\"} %d\n",
            uri3, generateRandom(80, 200)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"0.1\"} %d\n",
            uri3, generateRandom(200, 500)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"1.0\"} %d\n",
            uri3, generateRandom(500, 800)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_bucket{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\",le=\"+Inf\"} %d\n",
            uri3, generateRandom(800, 1000)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_sum{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n",
            uri3, generateRandomDecimal(3.0, 30.0)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_count{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %d\n\n",
            uri3, generateRandom(500, 2000)));

    // HTTP请求最大响应时间
    metrics.append("# HELP http_server_requests_seconds_max HTTP请求最大响应时间\n");
    metrics.append("# TYPE http_server_requests_seconds_max gauge\n");
    metrics.append(
        String.format(
            "http_server_requests_seconds_max{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n",
            uri1, generateRandomDecimal(0.1, 2.5)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_max{exception=\"None\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n",
            uri2, generateRandomDecimal(0.05, 1.8)));
    metrics.append(
        String.format(
            "http_server_requests_seconds_max{exception=\"None\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"} %s\n\n",
            uri3, generateRandomDecimal(0.08, 2.0)));
  }

  /** 生成JVM内存指标 */
  private void generateJvmMemoryMetrics(StringBuilder metrics) {
    metrics.append("# HELP jvm_memory_used_bytes JVM内存使用量\n");
    metrics.append("# TYPE jvm_memory_used_bytes gauge\n");
    metrics.append(
        String.format(
            "jvm_memory_used_bytes{area=\"heap\",id=\"PS Eden Space\"} %d\n",
            generateRandom(100000000, 500000000)));
    metrics.append(
        String.format(
            "jvm_memory_used_bytes{area=\"heap\",id=\"PS Old Gen\"} %d\n",
            generateRandom(200000000, 800000000)));
    metrics.append(
        String.format(
            "jvm_memory_used_bytes{area=\"heap\",id=\"PS Survivor Space\"} %d\n",
            generateRandom(10000000, 50000000)));
    metrics.append(
        String.format(
            "jvm_memory_used_bytes{area=\"nonheap\",id=\"Metaspace\"} %d\n",
            generateRandom(50000000, 150000000)));
    metrics.append(
        String.format(
            "jvm_memory_used_bytes{area=\"nonheap\",id=\"Code Cache\"} %d\n\n",
            generateRandom(20000000, 80000000)));

    metrics.append("# HELP jvm_memory_max_bytes JVM内存最大值\n");
    metrics.append("# TYPE jvm_memory_max_bytes gauge\n");
    metrics.append("jvm_memory_max_bytes{area=\"heap\",id=\"PS Eden Space\"} 715653120\n");
    metrics.append("jvm_memory_max_bytes{area=\"heap\",id=\"PS Old Gen\"} 1431655765\n");
    metrics.append("jvm_memory_max_bytes{area=\"heap\",id=\"PS Survivor Space\"} 71565312\n");
    metrics.append("jvm_memory_max_bytes{area=\"nonheap\",id=\"Metaspace\"} -1\n");
    metrics.append("jvm_memory_max_bytes{area=\"nonheap\",id=\"Code Cache\"} 251658240\n\n");
  }

  /** 生成JVM GC指标 */
  private void generateJvmGcMetrics(StringBuilder metrics) {
    metrics.append("# HELP jvm_gc_pause_seconds GC暂停时间\n");
    metrics.append("# TYPE jvm_gc_pause_seconds histogram\n");
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_bucket{action=\"end of minor GC\",cause=\"Allocation Failure\",le=\"0.001\"} %d\n",
            generateRandom(10, 50)));
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_bucket{action=\"end of minor GC\",cause=\"Allocation Failure\",le=\"0.01\"} %d\n",
            generateRandom(50, 150)));
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_bucket{action=\"end of minor GC\",cause=\"Allocation Failure\",le=\"0.1\"} %d\n",
            generateRandom(150, 300)));
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_bucket{action=\"end of minor GC\",cause=\"Allocation Failure\",le=\"+Inf\"} %d\n",
            generateRandom(300, 400)));
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_sum{action=\"end of minor GC\",cause=\"Allocation Failure\"} %s\n",
            generateRandomDecimal(0.5, 5.0)));
    metrics.append(
        String.format(
            "jvm_gc_pause_seconds_count{action=\"end of minor GC\",cause=\"Allocation Failure\"} %d\n\n",
            generateRandom(300, 400)));
  }

  /** 生成系统指标 */
  private void generateSystemMetrics(StringBuilder metrics) {
    metrics.append("# HELP system_cpu_usage 系统CPU使用率\n");
    metrics.append("# TYPE system_cpu_usage gauge\n");
    metrics.append(String.format("system_cpu_usage %s\n\n", generateRandomDecimal(0.1, 0.8)));

    metrics.append("# HELP process_cpu_usage 进程CPU使用率\n");
    metrics.append("# TYPE process_cpu_usage gauge\n");
    metrics.append(String.format("process_cpu_usage %s\n\n", generateRandomDecimal(0.05, 0.6)));

    metrics.append("# HELP jvm_threads_live JVM活跃线程数\n");
    metrics.append("# TYPE jvm_threads_live gauge\n");
    metrics.append(String.format("jvm_threads_live %d\n\n", generateRandom(20, 80)));

    metrics.append("# HELP jvm_threads_peak JVM峰值线程数\n");
    metrics.append("# TYPE jvm_threads_peak gauge\n");
    metrics.append(String.format("jvm_threads_peak %d\n\n", generateRandom(80, 150)));
  }

  /** 生成业务指标 */
  private void generateBusinessMetrics(StringBuilder metrics) {
    metrics.append("# HELP group_buy_active_teams 活跃拼团数量\n");
    metrics.append("# TYPE group_buy_active_teams gauge\n");
    metrics.append(String.format("group_buy_active_teams %d\n\n", generateRandom(10, 100)));

    metrics.append("# HELP group_buy_completed_teams 已完成拼团数量\n");
    metrics.append("# TYPE group_buy_completed_teams counter\n");
    metrics.append(String.format("group_buy_completed_teams %d\n\n", generateRandom(500, 2000)));

    metrics.append("# HELP market_pay_orders_total 营销支付订单总数\n");
    metrics.append("# TYPE market_pay_orders_total counter\n");
    metrics.append(
        String.format("market_pay_orders_total{status=\"CREATE\"} %d\n", generateRandom(100, 500)));
    metrics.append(
        String.format("market_pay_orders_total{status=\"PAID\"} %d\n", generateRandom(800, 3000)));
    metrics.append(
        String.format(
            "market_pay_orders_total{status=\"CANCEL\"} %d\n\n", generateRandom(50, 200)));

    metrics.append("# HELP group_buy_participants 拼团参与人数\n");
    metrics.append("# TYPE group_buy_participants gauge\n");
    metrics.append(String.format("group_buy_participants %d\n\n", generateRandom(50, 500)));
  }

  /** 生成数据库和Redis指标 */
  private void generateDatabaseMetrics(StringBuilder metrics) {
    metrics.append("# HELP database_connections_active 数据库活跃连接数\n");
    metrics.append("# TYPE database_connections_active gauge\n");
    metrics.append(
        String.format(
            "database_connections_active{pool=\"HikariPool-1\"} %d\n\n", generateRandom(5, 20)));

    metrics.append("# HELP database_connections_max 数据库最大连接数\n");
    metrics.append("# TYPE database_connections_max gauge\n");
    metrics.append("database_connections_max{pool=\"HikariPool-1\"} 20\n\n");

    metrics.append("# HELP redis_connections_active Redis活跃连接数\n");
    metrics.append("# TYPE redis_connections_active gauge\n");
    metrics.append(String.format("redis_connections_active %d\n\n", generateRandom(2, 10)));
  }

  /** 生成应用指标 */
  private void generateApplicationMetrics(StringBuilder metrics) {
    metrics.append("# HELP application_ready_time 应用启动时间\n");
    metrics.append("# TYPE application_ready_time gauge\n");
    metrics.append(
        String.format(
            "application_ready_time{main_application_class=\"cn.bugstack.xfg.dev.tech.Application\"} %s\n",
            generateRandomDecimal(8.0, 15.0)));
  }

  /** 生成随机整数 */
  private int generateRandom(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  /** 生成随机小数 */
  private String generateRandomDecimal(double min, double max) {
    double random = ThreadLocalRandom.current().nextDouble(min, max);
    return DECIMAL_FORMAT.format(random);
  }

  /** 设置文件权限 */
  private void setFilePermissions() {
    try {
      Path path = Paths.get(METRICS_FILE);
      Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-r--r--");
      Files.setPosixFilePermissions(path, permissions);
    } catch (Exception e) {
      // 在Windows系统上可能不支持POSIX权限，忽略错误
      log.debug("设置文件权限失败（可能是Windows系统）: {}", e.getMessage());
    }
  }

  /** 验证指标文件 */
  private boolean verifyMetricsFile() {
    try {
      Path path = Paths.get(METRICS_FILE);
      if (Files.exists(path)) {
        long fileSize = Files.size(path);
        log.debug(
            "{}: ✅ Generated metrics to {} with proper permissions",
            getCurrentTimestamp(),
            METRICS_FILE);
        log.debug("{}: File size: {} bytes", getCurrentTimestamp(), fileSize);
        return true;
      } else {
        log.error("{}: ❌ Failed to create metrics file", getCurrentTimestamp());
        return false;
      }
    } catch (IOException e) {
      log.error("验证指标文件失败", e);
      return false;
    }
  }

  /** 获取当前时间戳 */
  private String getCurrentTimestamp() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }

  /** 清理函数 */
  private void cleanup() {
    running = false;
    log.info("");
    log.info("{}: 🛑 接收到停止信号，正在清理...", getCurrentTimestamp());

    try {
      Path path = Paths.get(METRICS_FILE);
      if (Files.exists(path)) {
        Files.delete(path);
        log.info("{}: 🗑️ 删除指标文件: {}", getCurrentTimestamp(), METRICS_FILE);
      }
    } catch (IOException e) {
      log.warn("删除指标文件失败: {}", e.getMessage());
    }

    log.info("{}: ✅ 清理完成，脚本已停止", getCurrentTimestamp());
  }

  /** 手动停止生成器（用于测试） */
  public void stop() {
    running = false;
  }
}
