package org.example.url_shortener.service.impl;

// Spring框架相关导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Java标准库导入
import java.net.URL;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// 项目内部类导入
import org.example.url_shortener.entity.UrlMapping;
import org.example.url_shortener.repository.UrlMappingRepository;
import org.example.url_shortener.service.UrlShortenerService;

/**
 * URL短链服务实现类
 *
 * 这是Service接口的具体实现，包含所有业务逻辑
 *
 * 关键注解说明：
 * @Service - 标记为Spring服务组件，会被自动扫描和管理
 * @Transactional - 声明式事务管理，确保数据一致性
 */
@Service  // 告诉Spring这是一个业务逻辑组件
@Transactional  // 为这个类的所有public方法启用事务管理
public class UrlShortenerServiceImpl implements UrlShortenerService {

    // ==================== 依赖注入 ====================

    /**
     * 数据访问层依赖
     *
     * @Autowired注解告诉Spring自动注入UrlMappingRepository的实例
     * Spring会自动找到UrlMappingRepository的实现并注入进来
     * 这就是依赖注入（Dependency Injection，DI）的核心概念
     */
    @Autowired
    private UrlMappingRepository urlMappingRepository;

    // ==================== 常量定义 ====================

    /**
     * 短码生成用的字符集
     * 包含大小写字母和数字，共62个字符
     * 这种编码方式称为Base62编码
     */
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * 短码默认长度
     * 6位Base62编码可以生成 62^6 ≈ 568亿 种不同组合
     */
    private static final int SHORT_CODE_LENGTH = 6;

    /**
     * 生成短码时的最大重试次数
     * 防止无限循环，如果重试太多次说明可能有问题
     */
    private static final int MAX_RETRY_ATTEMPTS = 10;

    /**
     * 随机数生成器
     * 用于生成随机短码
     */
    private final Random random = new Random();

    // ==================== 核心业务方法实现 ====================

    /**
     * 将长URL转换为短链接
     *
     * 这是最重要的业务方法，实现步骤：
     * 1. 验证URL格式
     * 2. 检查是否已存在
     * 3. 生成唯一短码
     * 4. 保存到数据库
     */
    @Override
    public UrlMapping shortenUrl(String originalUrl) {
        // 调用重载方法，过期时间设为null（永不过期）
        return shortenUrl(originalUrl, null);
    }

    /**
     * 将长URL转换为短链接，支持设置过期时间
     */
    @Override
    public UrlMapping shortenUrl(String originalUrl, LocalDateTime expiresAt) {
        // 步骤1: 验证URL格式
        if (!isValidUrl(originalUrl)) {
            // 抛出异常，Spring会自动回滚事务
            throw new IllegalArgumentException("无效的URL格式: " + originalUrl);
        }

        // 步骤2: 检查是否已经存在相同的URL映射
        // 这里可以选择返回已存在的映射，或者创建新的映射
        List<UrlMapping> existingMappings = urlMappingRepository.findAllByOriginalUrl(originalUrl);
        if (!existingMappings.isEmpty()) {
            // 如果已存在且未过期，可以返回第一个
            UrlMapping existing = existingMappings.get(0);
            if (!existing.isExpired()) {
                return existing;
            }
        }

        // 步骤3: 生成唯一的短码
        String shortCode = generateUniqueShortCode();

        // 步骤4: 创建并保存URL映射
        UrlMapping urlMapping = new UrlMapping(originalUrl, shortCode);
        urlMapping.setExpiresAt(expiresAt);

        // 保存到数据库，Spring Data JPA会自动生成SQL
        return urlMappingRepository.save(urlMapping);
    }

    /**
     * 根据短码查找URL映射
     */
    @Override
    public Optional<UrlMapping> findByShortCode(String shortCode) {
        // 参数验证
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return Optional.empty();
        }

        // 调用Repository方法查询数据库
        return urlMappingRepository.findByShortCode(shortCode.trim());
    }

    /**
     * 获取原始URL并增加点击次数
     * 这是短链接重定向的核心方法
     */
    @Override
    public String getOriginalUrlAndIncrementClick(String shortCode) {
        // 查找URL映射
        Optional<UrlMapping> optionalMapping = findByShortCode(shortCode);

        if (optionalMapping.isPresent()) {
            UrlMapping mapping = optionalMapping.get();

            // 检查是否过期
            if (mapping.isExpired()) {
                return null; // 已过期，返回null
            }

            // 增加点击次数
            mapping.incrementClickCount();

            // 保存更新（由于@Transactional，这会自动提交）
            urlMappingRepository.save(mapping);

            // 返回原始URL
            return mapping.getOriginalUrl();
        }

        return null; // 未找到
    }

    // ==================== 辅助和工具方法 ====================

    /**
     * 生成唯一的短码
     *
     * 核心算法：使用随机数生成Base62编码的字符串
     * 并检查数据库确保唯一性
     */
    private String generateUniqueShortCode() {
        int attempts = 0;

        // 循环生成，直到找到唯一的短码
        while (attempts < MAX_RETRY_ATTEMPTS) {
            String shortCode = generateRandomShortCode();

            // 检查数据库中是否已存在
            if (!urlMappingRepository.existsByShortCode(shortCode)) {
                return shortCode;
            }

            attempts++;
        }

        // 如果重试多次仍失败，抛出异常
        throw new RuntimeException("无法生成唯一的短码，请稍后重试");
    }

    /**
     * 生成随机短码
     *
     * 算法：从字符集中随机选择字符组成指定长度的字符串
     */
    private String generateRandomShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);

        // 循环生成指定长度的随机字符串
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            // 随机选择一个字符
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    /**
     * 验证URL格式是否有效
     *
     * 使用Java标准库的URL类进行验证
     */
    @Override
    public boolean isValidUrl(String url) {
        // 基本非空检查
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 添加协议前缀（如果没有的话）
        String urlToValidate = url.trim();
        if (!urlToValidate.startsWith("http://") && !urlToValidate.startsWith("https://")) {
            urlToValidate = "http://" + urlToValidate;
        }

        try {
            // 使用URL类验证格式
            new URL(urlToValidate);
            return true;
        } catch (MalformedURLException e) {
            // URL格式不正确
            return false;
        }
    }

    // ==================== 查询和统计方法 ====================

    @Override
    public Optional<UrlMapping> getUrlStatistics(String shortCode) {
        return findByShortCode(shortCode);
    }

    @Override
    public List<UrlMapping> getRecentUrls(int limit) {
        // 查找最近创建的URL
        // 注意：这里需要Repository支持，实际实现中可能需要添加相应的查询方法
        return urlMappingRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 按创建时间降序
                .limit(limit)
                .toList();
    }

    @Override
    public List<UrlMapping> getTopClickedUrls(int limit) {
        return urlMappingRepository.findTopClickedUrls(limit);
    }

    @Override
    public int cleanupExpiredUrls() {
        // 查找所有过期的URL
        List<UrlMapping> expiredUrls = urlMappingRepository
                .findAllByExpiresAtIsNotNullAndExpiresAtBefore(LocalDateTime.now());

        // 批量删除
        urlMappingRepository.deleteAll(expiredUrls);

        return expiredUrls.size();
    }

    @Override
    public SystemStatistics getSystemStatistics() {
        // 总URL数量
        long totalUrls = urlMappingRepository.count();

        // 计算总点击数
        long totalClicks = urlMappingRepository.findAll()
                .stream()
                .mapToLong(UrlMapping::getClickCount)
                .sum();

        // 平均点击数
        double averageClicks = totalUrls > 0 ? (double) totalClicks / totalUrls : 0;

        // 今日创建的URL数量
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long urlsToday = urlMappingRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        return new SystemStatistics(totalUrls, totalClicks, averageClicks, urlsToday);
    }
}