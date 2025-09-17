package org.example.url_shortener.dto;

import java.time.LocalDateTime;

/**
 * 缩短URL的请求数据传输对象 (DTO - Data Transfer Object)
 *
 * DTO的作用：
 * 1. 数据传输 - 在Controller和Service之间传递数据
 * 2. 数据验证 - 可以添加验证注解
 * 3. 数据封装 - 隐藏内部实体结构
 * 4. 版本控制 - API变化时不影响内部实体
 *
 * 为什么要用DTO而不直接用Entity？
 * - Entity是数据库映射，包含ID、创建时间等内部信息
 * - DTO只包含客户端需要的数据，更安全和简洁
 * - DTO可以组合多个Entity的数据
 */
public class ShortenUrlRequest {

    /**
     * 要缩短的原始URL
     * 这是客户端必须提供的字段
     */
    private String url;

    /**
     * 可选的过期时间
     * 如果不设置，表示永不过期
     */
    private LocalDateTime expiresAt;

    // ==================== 构造函数 ====================

    /**
     * 无参构造函数
     * JSON反序列化需要无参构造函数
     * Jackson（SpringBoot默认的JSON处理库）会使用这个构造函数
     */
    public ShortenUrlRequest() {
    }

    /**
     * 全参构造函数
     * 方便创建对象，特别是在测试中
     */
    public ShortenUrlRequest(String url, LocalDateTime expiresAt) {
        this.url = url;
        this.expiresAt = expiresAt;
    }

    /**
     * 只包含URL的构造函数
     * 最常用的情况 - 大多数请求不需要设置过期时间
     */
    public ShortenUrlRequest(String url) {
        this.url = url;
        this.expiresAt = null;
    }

    // ==================== Getter和Setter方法 ====================
    // JSON序列化/反序列化需要这些方法

    /**
     * 获取URL
     * @return 原始URL字符串
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置URL
     * @param url 原始URL字符串
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取过期时间
     * @return 过期时间，null表示永不过期
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间
     * @param expiresAt 过期时间
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    // ==================== 工具方法 ====================

    /**
     * 验证请求数据是否有效
     * 可以在Controller中调用这个方法进行基本验证
     *
     * @return true表示数据有效，false表示无效
     */
    public boolean isValid() {
        return url != null && !url.trim().isEmpty();
    }

    /**
     * toString方法
     * 方便调试和日志记录
     * 注意：实际项目中要小心不要在日志中暴露敏感信息
     */
    @Override
    public String toString() {
        return "ShortenUrlRequest{" +
                "url='" + url + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}