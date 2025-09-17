package org.example.url_shortener.entity;

// Java持久化API相关注解，用于对象关系映射(ORM)
import jakarta.persistence.*;
// Java 8+的日期时间API
import java.time.LocalDateTime;

/**
 * URL映射实体类
 * 这是一个Java Bean，代表数据库中的一条URL映射记录
 * 用于存储长URL和短码的对应关系
 */
@Entity  // 告诉SpringBoot这是一个数据库实体类，需要映射到数据库表
@Table(name = "url_mappings")  // 指定对应的数据库表名
public class UrlMapping {

    /**
     * 主键ID - 每条记录的唯一标识
     * @Id 表示这是主键字段
     * @GeneratedValue 表示这个值由数据库自动生成
     * IDENTITY策略表示使用数据库的自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 原始的长URL
     * nullable = false 表示这个字段不能为空
     * length = 2000 表示最大长度为2000字符（URL可能很长）
     */
    @Column(nullable = false, length = 2000)
    private String originalUrl;

    /**
     * 短码 - 生成的短链接的关键部分
     * unique = true 表示这个字段必须唯一，不能重复
     * length = 10 表示短码最长10个字符
     */
    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    /**
     * 创建时间 - 记录何时创建的这个URL映射
     * LocalDateTime是Java 8+推荐的日期时间类型
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 过期时间 - 可选字段，如果设置了则链接会在此时间后失效
     * 没有nullable = false，所以这个字段可以为空
     */
    @Column
    private LocalDateTime expiresAt;

    /**
     * 点击次数统计 - 记录这个短链接被访问了多少次
     * 默认值为0L（L表示这是一个Long类型的字面量）
     */
    @Column(nullable = false)
    private Long clickCount = 0L;

    /**
     * 无参构造函数 - Java Bean的要求
     * SpringBoot的JPA需要有无参构造函数来创建对象
     */
    public UrlMapping() {
        // 创建对象时自动设置创建时间为当前时间
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 有参构造函数 - 方便创建对象时直接设置主要字段
     * this() 调用上面的无参构造函数，这样createdAt会被自动设置
     */
    public UrlMapping(String originalUrl, String shortCode) {
        this();  // 先调用无参构造函数
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
    }

    // ==================== Getter和Setter方法 ====================
    // Java Bean规范要求每个private字段都要有对应的getter和setter方法
    // SpringBoot会使用这些方法来访问和设置字段值

    /**
     * 获取ID
     * @return 记录的唯一标识ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置ID（通常由数据库自动设置，不需要手动调用）
     * @param id 记录ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取原始URL
     * @return 完整的长URL
     */
    public String getOriginalUrl() {
        return originalUrl;
    }

    /**
     * 设置原始URL
     * @param originalUrl 要缩短的原始URL
     */
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    /**
     * 获取短码
     * @return 生成的短码字符串
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * 设置短码
     * @param shortCode 生成的短码
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * 获取创建时间
     * @return 记录的创建时间
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
     * @return 过期时间，如果为null表示永不过期
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * 设置过期时间
     * @param expiresAt 过期时间，可以为null
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * 获取点击次数
     * @return 当前的点击统计数
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

    // ==================== 业务方法 ====================
    // 这些方法包含业务逻辑，不是简单的getter/setter

    /**
     * 增加点击次数
     * 每次有人访问短链接时调用此方法
     */
    public void incrementClickCount() {
        this.clickCount++;  // 点击次数+1
    }

    /**
     * 检查链接是否已过期
     * @return true表示已过期，false表示未过期或永不过期
     */
    public boolean isExpired() {
        // 如果expiresAt为null，表示永不过期，返回false
        // 如果expiresAt不为null，比较当前时间是否晚于过期时间
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}