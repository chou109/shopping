package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.SecurityUtils;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import com.neuedu.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.service.IProductCommentService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品评论Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements IProductCommentService
{
    private final ProductCommentMapper productCommentMapper;
    private final IProductService productService; // 注入商品服务
    private final IShopService shopService; // 注入店铺服务
    @Autowired
    private ISysUserService sysUserService;
    /**
     * 查询商品评论
     * 
     * @param commentId 商品评论主键
     * @return 商品评论
     */
    @Override
    public ProductComment selectProductCommentByCommentId(Long commentId)
    {
        return productCommentMapper.selectProductCommentByCommentId(commentId);
    }

    /**
     * 查询商品评论列表
     * 
     * @param productComment 商品评论
     * @return 商品评论
     */
    @Override
    public List<ProductComment> selectProductCommentList(ProductComment productComment)
    {
        return productCommentMapper.selectProductCommentList(productComment);
    }

    /**
     * 新增商品评论
     * 
     * @param productComment 商品评论
     * @return 结果
     */
    @Override
    public int insertProductComment(ProductComment productComment)
    {
        productComment.setCreateTime(DateUtils.getNowDate());
        return productCommentMapper.insertProductComment(productComment);
    }

    /**
     * 修改商品评论
     * 
     * @param productComment 商品评论
     * @return 结果
     */
    @Override
    public int updateProductComment(ProductComment productComment)
    {
        productComment.setUpdateTime(DateUtils.getNowDate());
        return productCommentMapper.updateProductComment(productComment);
    }

    /**
     * 批量删除商品评论
     * 
     * @param commentIds 需要删除的商品评论主键
     * @return 结果
     */
    @Override
    public int deleteProductCommentByCommentIds(Long[] commentIds)
    {
        return productCommentMapper.deleteProductCommentByCommentIds(commentIds);
    }

    /**
     * 删除商品评论信息
     * 
     * @param commentId 商品评论主键
     * @return 结果
     */
    @Override
    public int deleteProductCommentByCommentId(Long commentId)
    {
        return productCommentMapper.deleteProductCommentByCommentId(commentId);
    }

    // ============ 新增的方法实现 ============

    /**
     * 发表商品评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishComment(ProductComment comment) {
        Long userId = SecurityUtils.getUserId();

        // 验证商品是否存在
        var product = productService.selectProductById(comment.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 检查是否已评论过（根据业务需求，可以一个订单评论一次）
        if (comment.getOrderId() != null) {
            ProductComment existing = productCommentMapper.selectUserCommentOnProduct(
                    userId, comment.getProductId(), comment.getOrderId());
            if (existing != null) {
                throw new ServiceException("您已经评论过该商品");
            }
        }

        // 设置默认值
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0L);
        }
        if (comment.getReplyCount() == null) {
            comment.setReplyCount(0L);
        }
        if (comment.getStatus() == null) {
            comment.setStatus(2); // 默认审核中
        }
        if (comment.getIsAnonymous() == null) {
            comment.setIsAnonymous(0); // 默认不匿名
        }

        comment.setUserId(userId);
        comment.setCreateTime(DateUtils.getNowDate());

        int result = productCommentMapper.insertProductComment(comment);

        // 如果是根评论且审核通过，更新商品评分统计
        if (result > 0 && comment.isRootComment() && comment.isNormal()) {
            updateProductScoreStatistics(comment.getProductId());
        }

        return result > 0;
    }

    /**
     * 回复评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyComment(Long parentId, String content, Long userId) {
        ProductComment parentComment = selectProductCommentByCommentId(parentId);
        if (parentComment == null || !parentComment.isNormal()) {
            throw new ServiceException("评论不存在或已被删除");
        }

        ProductComment reply = new ProductComment();
        reply.setProductId(parentComment.getProductId());
        reply.setUserId(userId);
        reply.setParentId(parentId);
        reply.setContent(content);
        reply.setStatus(1); // 回复默认正常
        reply.setCreateTime(DateUtils.getNowDate());

        int result = productCommentMapper.insertProductComment(reply);

        // 更新父评论的回复数
        if (result > 0) {
            productCommentMapper.increaseReplyCount(parentId);
        }

        return result > 0;
    }

    /**
     * 获取商品评论列表（前台展示）
     */
    @Override
    public List<ProductComment> getProductComments(Long productId, Integer score, Boolean hasImages) {
        return productCommentMapper.selectProductCommentPage(productId, null, score, hasImages, 1);
    }

    /**
     * 获取商品的根评论列表
     */
    @Override
    public List<ProductComment> getRootComments(Long productId) {
        return productCommentMapper.selectRootCommentsByProductId(productId, 1);
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public List<ProductComment> getCommentReplies(Long parentId) {
        return productCommentMapper.selectRepliesByParentId(parentId, 1);
    }

    /**
     * 点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId) {
        ProductComment comment = selectProductCommentByCommentId(commentId);
        if (comment == null || !comment.isNormal()) {
            throw new ServiceException("评论不存在或已被删除");
        }

        // TODO: 这里可以添加用户点赞记录，防止重复点赞

        int result = productCommentMapper.increaseLikeCount(commentId);
        return result > 0;
    }

    /**
     * 取消点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId) {
        ProductComment comment = selectProductCommentByCommentId(commentId);
        if (comment == null || !comment.isNormal()) {
            throw new ServiceException("评论不存在或已被删除");
        }

        // TODO: 这里可以检查用户是否点赞过

        int result = productCommentMapper.decreaseLikeCount(commentId);
        return result > 0;
    }

    /**
     * 获取商品评论统计信息
     */
    @Override
    public Map<String, Object> getCommentStatistics(Long productId) {
        Map<String, Object> stats = new HashMap<>();

        // 总评论数
        Long totalCount = productCommentMapper.countCommentsByProductId(productId, null, null, 1);
        stats.put("totalCount", totalCount);

        // 有图评论数
        Long imageCount = productCommentMapper.countCommentsByProductId(productId, null, true, 1);
        stats.put("imageCount", imageCount);

        // 好评数（4-5星）
        Long goodCount = productCommentMapper.countCommentsByProductId(productId, 5, null, 1)
                + productCommentMapper.countCommentsByProductId(productId, 4, null, 1);
        stats.put("goodCount", goodCount);

        // 中评数（3星）
        Long mediumCount = productCommentMapper.countCommentsByProductId(productId, 3, null, 1);
        stats.put("mediumCount", mediumCount);

        // 差评数（1-2星）
        Long badCount = productCommentMapper.countCommentsByProductId(productId, 2, null, 1)
                + productCommentMapper.countCommentsByProductId(productId, 1, null, 1);
        stats.put("badCount", badCount);

        // 平均评分
        Double averageScore = productCommentMapper.calculateAverageScore(productId);
        stats.put("averageScore", averageScore != null ? String.format("%.1f", averageScore) : "0.0");

        // 评分分布
        Map<Integer, Long> scoreDistribution = getScoreDistribution(productId);
        stats.put("scoreDistribution", scoreDistribution);

        return stats;
    }

    /**
     * 获取商品评分分布
     */
    @Override
    public Map<Integer, Long> getScoreDistribution(Long productId) {
        List<Map<String, Object>> distribution = productCommentMapper.countScoreDistribution(productId);

        Map<Integer, Long> result = new HashMap<>();
        // 初始化所有评分
        for (int i = 1; i <= 5; i++) {
            result.put(i, 0L);
        }

        // 填充实际数据
        for (Map<String, Object> map : distribution) {
            Integer score = ((Number) map.get("score")).intValue();
            Long count = ((Number) map.get("count")).longValue();
            result.put(score, count);
        }

        return result;
    }

    /**
     * 获取商品平均评分
     */
    @Override
    public Double getAverageScore(Long productId) {
        return productCommentMapper.calculateAverageScore(productId);
    }

    /**
     * 审核评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditComment(Long commentId, Integer status, String reason) {
        ProductComment comment = selectProductCommentByCommentId(commentId);
        if (comment == null) {
            throw new ServiceException("评论不存在");
        }

        // 更新状态
        int result = productCommentMapper.updateCommentStatus(commentId, status);

        // 如果状态变为正常，更新商品评分统计
        if (result > 0 && status == 1 && comment.isRootComment()) {
            updateProductScoreStatistics(comment.getProductId());
        }

        return result > 0;
    }

    /**
     * 获取用户对商品的评论
     */
    @Override
    public ProductComment getUserProductComment(Long userId, Long productId, Long orderId) {
        return productCommentMapper.selectUserCommentOnProduct(userId, productId, orderId);
    }

    /**
     * 获取我的评论列表
     */
    @Override
    public List<ProductComment> getMyComments() {
        Long userId = SecurityUtils.getUserId();
        ProductComment query = new ProductComment();
        query.setUserId(userId);
        return selectProductCommentList(query);
    }

    /**
     * 获取带图片的评论列表
     */
    @Override
    public List<ProductComment> getCommentsWithImages(Long productId, Integer limit) {
        List<ProductComment> comments = productCommentMapper.selectProductCommentPage(
                productId, null, null, true, 1);

        if (limit != null && limit > 0 && comments.size() > limit) {
            return comments.subList(0, limit);
        }

        return comments;
    }

    /**
     * 更新商品评分统计（更新到商品表和店铺表）
     */
    private void updateProductScoreStatistics(Long productId) {
        // 获取商品信息
        var product = productService.selectProductById(productId);
        if (product == null) {
            return;
        }

        // 计算商品平均评分
        Double averageScore = getAverageScore(productId);
        if (averageScore != null) {
            // 更新商品评分（如果需要）
            // 这里假设商品表有评分字段，如果没有可以跳过

            // 计算店铺平均评分
            Long shopId = product.getShopId();
            if (shopId != null) {
                updateShopAverageScore(shopId);
            }
        }
    }
    /**
     * 更新店铺平均评分
     */
    private void updateShopAverageScore(Long shopId) {
        // TODO: 这里需要实现计算店铺所有商品的平均评分
        // 可以根据业务需求，计算店铺下所有商品评分的平均值
        // 然后调用 shopService.updateShopScore(shopId, newScore)
    }

    @Override
    public List<Map<String, Object>> getCommentTreeSimple(Long productId) {
        // 1. 获取商品的所有评论
        LambdaQueryWrapper<ProductComment> query = new LambdaQueryWrapper<>();
        query.eq(ProductComment::getProductId, productId)
                .eq(ProductComment::getStatus, 1)
                .orderByAsc(ProductComment::getCreateTime);

        List<ProductComment> allComments = list(query);

        // 2. 如果没有评论，返回空列表
        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 准备数据
        Map<Long, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> rootNodes = new ArrayList<>();

        // 4. 第一遍：创建所有节点
        for (ProductComment comment : allComments) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", comment.getCommentId());
            node.put("parentId", comment.getParentId());
            node.put("content", comment.getContent());
            node.put("score", comment.getScore());
            node.put("likeCount", comment.getLikeCount());
            node.put("createTime", comment.getCreateTime());
            node.put("userId", comment.getUserId());
            node.put("images", comment.getImages());
            node.put("children", new ArrayList<Map<String, Object>>()); // 初始化children

            // 获取用户信息
            SysUser user = sysUserService.selectUserById(comment.getUserId());
            if (user != null) {
                node.put("userName", user.getNickName());
                node.put("userAvatar", user.getAvatar());
            }

            nodeMap.put(comment.getCommentId(), node);
        }

        // 5. 第二遍：构建树形关系
        for (ProductComment comment : allComments) {
            Map<String, Object> node = nodeMap.get(comment.getCommentId());
            Long parentId = comment.getParentId();

            if (parentId == null || parentId == 0) {
                // 根节点
                rootNodes.add(node);
            } else {
                // 子节点，找到父节点
                Map<String, Object> parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentNode.get("children");
                    if (children != null) {
                        children.add(node);
                    }
                } else {
                    // 如果父节点不存在（可能被删除了），也作为根节点
                    rootNodes.add(node);
                }
            }
        }

        // 6. 返回根节点列表
        return rootNodes;
    }

}
