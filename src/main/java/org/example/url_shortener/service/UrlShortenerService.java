package org.example.url_shortener.service;

import org.example.url_shortener.entity.UrlMapping;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * URL短链服务接口
 *
 * 在Java和SpringBoot中，通常会定义Service接口然后实现它
 * 这样做的好处：
 * 1. 面向接口编程 - 降低耦合度
 * 2. 便于单元测试 - 可以创建Mock实现
 * 3. 便于扩展 - 可以有多种不同的实现
 * 4. 符合SpringBoot的依赖注入规范
 */
public interface UrlShortenerService {

    /**
     * 将长URL转换为短链接
     *
     * 这是最核心的业务方法，包含以下步骤：
     * 1. 验证URL格式是否正确
     * 2. 检查URL是否已经存在短码
     * 3. 生成唯一的短码
     * 4. 保存到数据库
     *
     * @param originalUrl 原始的长URL
     * @return 创建成功的URL映射对象
     * @throws IllegalArgumentException 当URL格式不正确时抛出
     * @throws RuntimeException 当生成短码失败时抛出
     */
    UrlMapping shortenUrl(String originalUrl);

    /**
     * 将长URL转换为短链接，并设置过期时间
     *
     * 重载方法 - Java允许同名方法有不同的参数列表
     *
     * @param originalUrl 原始的长URL
     * @param expiresAt 过期时间，可以为null表示永不过期
     * @return 创建成功的URL映射对象
     */
    UrlMapping shortenUrl(String originalUrl, LocalDateTime expiresAt);

    /**
     * 根据短码查找原始URL
     *
     * 这个方法用于短链接重定向功能
     * 当用户访问短链接时，需要找到对应的原始URL
     *
     * @param shortCode 短码字符串
     * @return Optional包装的URL映射，如果找不到则为空
     */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * 根据短码获取原始URL并增加点击次数
     *
     * 这是重定向时使用的主要方法
     * 除了查找URL，还会自动增加点击统计
     *
     * @param shortCode 短码字符串
     * @return 原始URL字符串，如果不存在或已过期则返回null
     */
    String getOriginalUrlAndIncrementClick(String shortCode);

    /**
     * 获取URL的访问统计信息
     *
     * @param shortCode 短码字符串
     * @return URL映射对象，包含点击次数等信息
     */
    Optional<UrlMapping> getUrlStatistics(String shortCode);

    /**
     * 获取最近创建的URL列表
     *
     * @param limit 返回数量限制
     * @return 最近创建的URL映射列表
     */
    List<UrlMapping> getRecentUrls(int limit);

    /**
     * 获取点击次数最多的URL列表
     *
     * @param limit 返回数量限制
     * @return 点击次数最高的URL映射列表
     */
    List<UrlMapping> getTopClickedUrls(int limit);

    /**
     * 清理已过期的URL映射
     *
     * 定期清理任务，删除已过期的短链接
     *
     * @return 删除的记录数量
     */
    int cleanupExpiredUrls();

    /**
     * 检查URL是否有效（格式验证）
     *
     * 验证URL格式是否正确，是否可以访问等
     *
     * @param url 要验证的URL字符串
     * @return true表示URL有效，false表示无效
     */
    boolean isValidUrl(String url);

    /**
     * 获取系统统计信息
     *
     * @return 包含总URL数量、总点击数等信息的统计对象
     */
    SystemStatistics getSystemStatistics();

    /**
     * 系统统计信息内部类
     *
     * 使用内部类来封装统计数据
     * 这是一种常见的设计模式，用于返回复杂的数据结构
     */
    class SystemStatistics {
        private final long totalUrls;           // 总URL数量
        private final long totalClicks;         // 总点击数
        private final double averageClicks;     // 平均点击数
        private final long urlsToday;          // 今日创建的URL数量

        /**
         * 统计信息构造函数
         *
         * @param totalUrls 总URL数量
         * @param totalClicks 总点击数
         * @param averageClicks 平均点击数
         * @param urlsToday 今日URL数量
         */
        public SystemStatistics(long totalUrls, long totalClicks, double averageClicks, long urlsToday) {
            this.totalUrls = totalUrls;
            this.totalClicks = totalClicks;
            this.averageClicks = averageClicks;
            this.urlsToday = urlsToday;
        }

        // Getter方法
        public long getTotalUrls() { return totalUrls; }
        public long getTotalClicks() { return totalClicks; }
        public double getAverageClicks() { return averageClicks; }
        public long getUrlsToday() { return urlsToday; }
    }
}