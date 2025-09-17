package org.example.url_shortener.dto;

import org.example.url_shortener.entity.UrlMapping;
import java.time.LocalDateTime;

/**
 * 缩短URL的响应数据传输对象
 *
 * 这个类封装了创建短链接后返回给客户端的数据
 * 只包含客户端需要的信息，不暴露内部实现细节
 */
public class ShortenUrlResponse {

    /**
     * 生成的短码
     * 这是短链接的核心部分，客户端会用它构建完整的短链接
     */
    private String shortCode;

    /**
     * 完整的短链接URL
     * 方便客户端直接使用，无需自己拼接
     */
    private String shortUrl;

    /**
     * 原始的长URL
     * 让客户端确认是否正确
     */
    private String originalUrl;

    /**
     * 创建时间
     * 告诉客户端什么时候创建的
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间
     * null表示永不过期
     */
    private LocalDateTime expiresAt;

    /**
     * 当前点击次数
     * 提供基础统计信息
     */
    private Long clickCount;

    // ==================== 构造函数 ====================

    /**
     * 无参构造函数
     * JSON序列化需要
     */
    public ShortenUrlResponse() {
    }

    /**
     * 从UrlMapping实体创建响应对象
     * 这是一个非常实用的构造函数，避免手动设置每个字段
     *
     * @param urlMapping 数据库实体对象
     * @param baseUrl 系统的基础URL（如：http://localhost:8080）
     */
    public ShortenUrlResponse(UrlMapping urlMapping, String baseUrl) {
        this.shortCode = urlMapping.getShortCode();
        this.shortUrl = baseUrl + "/" + urlMapping.getShortCode();
        this.originalUrl = urlMapping.getOriginalUrl();
        this.createdAt = urlMapping.getCreatedAt();
        this.expiresAt = urlMapping.getExpiresAt();
        this.clickCount = urlMapping.getClickCount();
    }

    /**
     * 全参构造函数
     * 在某些情况下可能需要手动构建响应
     */
    public ShortenUrlResponse(String shortCode, String shortUrl, String originalUrl,
                             LocalDateTime createdAt, LocalDateTime expiresAt, Long clickCount) {
        this.shortCode = shortCode;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取短码
     * @return 短码字符串
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * 设置短码
     * @param shortCode 短码字符串
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * 获取完整短链接
     * @return 完整的短链接URL
     */
    public String getShortUrl() {
        return shortUrl;
    }

    /**
     * 设置完整短链接
     * @param shortUrl 完整的短链接URL
     */
    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    /**
     * 获取原始URL
     * @return 原始长URL
     */
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * 设置原始URL
     * @param originalUrl 原始长URL
     */
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    /**
     * 获取点击次数
     * @return 当前点击统计
     */
    public Long getClickCount() {
        return clickCount;
    }

    /**
     * 设置点击次数
     * @param clickCount 点击次数
     */
    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    // ==================== 工具方法 ====================

    /**
     * 检查短链接是否已过期
     * @return true表示已过期，false表示未过期或永不过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 获取剩余有效时间（以小时为单位）
     * @return 剩余小时数，如果永不过期则返回-1
     */
    public long getRemainingHours() {
        if (expiresAt == null) {
            return -1; // 永不过期
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0; // 已过期
        }
        return java.time.Duration.between(now, expiresAt).toHours();
    }

    @Override
    public String toString() {
        return "ShortenUrlResponse{" +
                "shortCode='" + shortCode + '\'' +
                ", shortUrl='" + shortUrl + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", clickCount=" + clickCount +
                '}';
    }
}