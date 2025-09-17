package org.example.url_shortener.repository;

// Spring Data JPA相关导入
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// 导入我们的实体类
import org.example.url_shortener.entity.UrlMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * URL映射的数据访问层接口
 *
 * Repository是Spring Data JPA的核心概念，用于封装数据访问逻辑
 * 继承JpaRepository后，Spring会自动提供基本的CRUD操作方法
 *
 * JpaRepository<UrlMapping, Long>的含义：
 * - UrlMapping: 操作的实体类型
 * - Long: 主键的数据类型
 */
@Repository  // 标记为Repository组件，Spring会自动管理这个接口的实现
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    // ==================== Spring Data JPA自动提供的方法 ====================
    // 继承JpaRepository后，以下方法会自动提供，无需手动实现：
    // - save(entity) : 保存实体
    // - findById(id) : 根据ID查找
    // - findAll() : 查找所有记录
    // - deleteById(id) : 根据ID删除
    // - count() : 统计总数
    // 还有更多方法...

    // ==================== 自定义查询方法 ====================
    // Spring Data JPA支持通过方法名自动生成查询
    // 方法名遵循特定规则，Spring会自动解析并生成SQL

    /**
     * 根据短码查找URL映射
     *
     * 方法名解析：
     * - find: 查询操作
     * - By: 查询条件的开始
     * - ShortCode: 按shortCode字段查询
     *
     * Spring会自动生成SQL: SELECT * FROM url_mappings WHERE short_code = ?
     *
     * @param shortCode 短码字符串
     * @return Optional包装的结果，可能存在也可能不存在
     */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * 根据原始URL查找所有映射
     *
     * 方法名解析：
     * - findAll: 查找多个结果
     * - By: 查询条件
     * - OriginalUrl: 按originalUrl字段查询
     *
     * @param originalUrl 原始URL
     * @return 匹配的URL映射列表
     */
    List<UrlMapping> findAllByOriginalUrl(String originalUrl);

    /**
     * 检查短码是否已存在
     *
     * 方法名解析：
     * - exists: 存在性检查，返回boolean
     * - By: 查询条件
     * - ShortCode: 按shortCode字段查询
     *
     * Spring会生成SQL: SELECT COUNT(*) > 0 FROM url_mappings WHERE short_code = ?
     *
     * @param shortCode 要检查的短码
     * @return true表示已存在，false表示不存在
     */
    boolean existsByShortCode(String shortCode);

    /**
     * 查找在指定时间之后创建的URL映射
     *
     * 方法名解析：
     * - findAll: 查找多个
     * - By: 条件开始
     * - CreatedAt: 按createdAt字段
     * - After: 时间条件，晚于指定时间
     *
     * @param dateTime 指定的时间点
     * @return 在指定时间后创建的映射列表
     */
    List<UrlMapping> findAllByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * 查找已过期的URL映射
     *
     * 方法名解析：
     * - findAll: 查找多个
     * - By: 条件开始
     * - ExpiresAt: 按expiresAt字段
     * - IsNotNull: 过期时间不为空
     * - And: 逻辑与
     * - ExpiresAt: 再次使用过期时间字段
     * - Before: 早于指定时间
     *
     * @param now 当前时间
     * @return 已过期的映射列表
     */
    List<UrlMapping> findAllByExpiresAtIsNotNullAndExpiresAtBefore(LocalDateTime now);

    // ==================== 使用@Query注解的自定义查询 ====================
    // 当方法名查询无法满足需求时，可以使用@Query注解写自定义JPQL或SQL

    /**
     * 查找点击次数最高的前N个URL映射
     *
     * @Query注解允许我们写自定义的JPQL查询
     * JPQL是面向对象的查询语言，类似SQL但操作的是Java对象
     *
     * ORDER BY clickCount DESC: 按点击次数降序排列
     *
     * @param limit 返回的记录数量上限
     * @return 点击次数最高的URL映射列表
     */
    @Query("SELECT u FROM UrlMapping u ORDER BY u.clickCount DESC LIMIT :limit")
    List<UrlMapping> findTopClickedUrls(@Param("limit") int limit);

    /**
     * 统计指定时间段内创建的URL数量
     *
     * 使用聚合函数COUNT进行统计
     * BETWEEN用于时间范围查询
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 该时间段内创建的URL数量
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * 批量更新点击次数
     *
     * @Query注解也可以用于更新操作
     * 需要配合@Modifying注解表示这是修改操作
     *
     * 注意：这个方法需要在Service层调用时加上@Transactional注解
     */
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    int incrementClickCountByShortCode(@Param("shortCode") String shortCode);

    // ==================== 使用原生SQL查询 ====================
    // 有时候需要使用数据库特定的功能，可以用原生SQL

    /**
     * 使用原生SQL查询统计信息
     *
     * nativeQuery = true 表示使用原生SQL而不是JPQL
     * 原生SQL直接操作数据库表，而不是Java对象
     *
     * @return 包含总数、平均点击数等统计信息的对象数组
     */
    @Query(value = "SELECT COUNT(*) as total_urls, AVG(click_count) as avg_clicks, MAX(click_count) as max_clicks FROM url_mappings",
           nativeQuery = true)
    Object[] getUrlStatistics();
}