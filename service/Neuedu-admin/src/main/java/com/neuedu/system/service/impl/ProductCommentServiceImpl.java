package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.SecurityUtils;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import com.neuedu.system.service.ISysUserService;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.mapper.OrderInfoMapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Autowired
    private OrderInfoMapper orderInfoMapper;
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
        // 设置用户ID
        if (productComment.getUserId() == null) {
            productComment.setUserId(SecurityUtils.getUserId());
        }
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
    @Transactional(rollbackFor = Exception.class)
    public int updateProductComment(ProductComment productComment)
    {
        ProductComment existing = productCommentMapper.selectProductCommentByCommentId(productComment.getCommentId());
        productComment.setUpdateTime(DateUtils.getNowDate());
        int result = productCommentMapper.updateProductComment(productComment);
        if (result > 0) {
            Set<Long> affectedProductIds = new HashSet<>();
            if (existing != null && existing.isRootComment() && existing.isNormal()
                    && existing.getProductId() != null) {
                affectedProductIds.add(existing.getProductId());
            }

            boolean updatedRoot = existing == null
                    ? productComment.isRootComment()
                    : productComment.getParentId() == null
                        ? existing.isRootComment()
                        : productComment.isRootComment();
            boolean updatedNormal = existing == null
                    ? productComment.isNormal()
                    : productComment.getStatus() == null
                        ? existing.isNormal()
                        : productComment.isNormal();
            Long updatedProductId = productComment.getProductId() == null && existing != null
                    ? existing.getProductId()
                    : productComment.getProductId();
            if (updatedRoot && updatedNormal && updatedProductId != null) {
                affectedProductIds.add(updatedProductId);
            }
            affectedProductIds.forEach(this::updateProductScoreStatistics);
        }
        return result;
    }

    /**
     * 批量删除商品评论
     * 
     * @param commentIds 需要删除的商品评论主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductCommentByCommentIds(Long[] commentIds)
    {
        Set<Long> affectedProductIds = new HashSet<>();
        if (commentIds != null) {
            for (Long commentId : commentIds) {
                ProductComment comment = productCommentMapper.selectProductCommentByCommentId(commentId);
                if (comment != null && comment.isRootComment() && comment.getProductId() != null) {
                    affectedProductIds.add(comment.getProductId());
                }
            }
        }
        int result = productCommentMapper.deleteProductCommentByCommentIds(commentIds);
        if (result > 0) {
            affectedProductIds.forEach(this::updateProductScoreStatistics);
        }
        return result;
    }

    /**
     * 删除商品评论信息
     * 
     * @param commentId 商品评论主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProductCommentByCommentId(Long commentId)
    {
        ProductComment comment = productCommentMapper.selectProductCommentByCommentId(commentId);
        int result = productCommentMapper.deleteProductCommentByCommentId(commentId);
        if (result > 0 && comment != null && comment.isRootComment()) {
            updateProductScoreStatistics(comment.getProductId());
        }
        return result;
    }

    // ============ 新增的方法实现 ============

    /**
     * 发表商品评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishComment(ProductComment comment) {
        Long userId = SecurityUtils.getUserId();

        if (comment == null || comment.getProductId() == null || comment.getOrderId() == null) {
            throw new ServiceException("评价必须关联有效订单和商品");
        }
        if (comment.getScore() == null || comment.getScore() < 1 || comment.getScore() > 5) {
            throw new ServiceException("商品评分必须为1到5星");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()
                || comment.getContent().length() > 500) {
            throw new ServiceException("评价内容不能为空且不能超过500字");
        }
        if (comment.getIsAnonymous() != null
                && !Integer.valueOf(0).equals(comment.getIsAnonymous())
                && !Integer.valueOf(1).equals(comment.getIsAnonymous())) {
            throw new ServiceException("匿名评价设置无效");
        }

        // 验证商品是否存在
        var product = productService.selectProductById(comment.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 锁定并校验订单归属及商品明细，跨实例的同订单商品评价会在此串行处理。
        OrderInfo order = orderInfoMapper.lockOrderForProductReview(
                userId, comment.getOrderId(), comment.getProductId());
        if (order == null) {
            throw new ServiceException("订单不存在、无权评价或订单不包含该商品");
        }

        // 同一订单商品重复提交按幂等成功处理，包含待审核记录。
        ProductComment existing = productCommentMapper.selectReviewForOrderProduct(
                userId, comment.getProductId(), comment.getOrderId());
        if (existing != null) {
            return true;
        }
        if (!OrderStatusConstants.WAIT_REVIEW.equals(order.getStatus())) {
            throw new ServiceException("当前订单状态不允许评价");
        }

        // 仅接收评价正文和匿名选项，评论层级、审核状态及计数由服务端决定。
        comment.setParentId(0L);
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setStatus(1);
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
        if (parentId == null || userId == null || content == null
                || content.trim().isEmpty() || content.length() > 500) {
            throw new ServiceException("回复内容不能为空且不能超过500字");
        }
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
        List<ProductComment> comments = productCommentMapper.selectProductCommentPage(productId, null, score, hasImages, 1);
        populateLikeStatus(comments, SecurityUtils.getOptionalUserId());
        return comments;
    }

    /**
     * 获取商品的根评论列表
     */
    @Override
    public List<ProductComment> getRootComments(Long productId) {
        List<ProductComment> comments = productCommentMapper.selectRootCommentsByProductId(productId, 1);
        populateLikeStatus(comments, SecurityUtils.getOptionalUserId());
        return comments;
    }

    /**
     * 获取评论的回复列表
     */
    @Override
    public List<ProductComment> getCommentReplies(Long parentId) {
        List<ProductComment> comments = productCommentMapper.selectRepliesByParentId(parentId, 1);
        populateLikeStatus(comments, SecurityUtils.getOptionalUserId());
        return comments;
    }

    /**
     * 点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeComment(Long commentId, Long userId) {
        if (userId == null) {
            throw new ServiceException("请先登录");
        }
        ProductComment comment = selectProductCommentByCommentId(commentId);
        if (comment == null || !comment.isNormal()) {
            throw new ServiceException("评论不存在或已被删除");
        }
        int inserted = productCommentMapper.insertCommentLike(commentId, userId);
        if (inserted > 0) {
            if (productCommentMapper.increaseLikeCount(commentId) < 1) {
                throw new ServiceException("评论已不可点赞");
            }
            return true;
        }
        if (productCommentMapper.countCommentLikesByUser(commentId, userId) > 0) return false;
        throw new ServiceException("评论已不可点赞");
    }

    /**
     * 取消点赞评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlikeComment(Long commentId, Long userId) {
        if (userId == null) {
            throw new ServiceException("请先登录");
        }
        ProductComment comment = selectProductCommentByCommentId(commentId);
        if (comment == null || !comment.isNormal()) {
            throw new ServiceException("评论不存在或已被删除");
        }
        int removed = productCommentMapper.deleteCommentLike(commentId, userId);
        if (removed < 1) {
            return false;
        }
        if (productCommentMapper.decreaseLikeCount(commentId) < 1) {
            throw new ServiceException("评论已不可取消点赞");
        }
        return true;
    }

    @Override
    public List<Long> getLikedCommentIds(Long userId, List<Long> commentIds) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) return new ArrayList<>();
        List<Long> likedIds = productCommentMapper.selectLikedCommentIds(userId, commentIds);
        return likedIds != null ? likedIds : new ArrayList<>();
    }

    private void populateLikeStatus(List<ProductComment> comments, Long userId) {
        if (comments == null || comments.isEmpty()) return;
        maskAnonymousIdentity(comments);
        comments.forEach(comment -> comment.setOrderId(null));
        List<Long> commentIds = comments.stream().map(ProductComment::getCommentId)
            .filter(id -> id != null).collect(java.util.stream.Collectors.toList());
        Set<Long> likedIds = new HashSet<>(getLikedCommentIds(userId, commentIds));
        comments.forEach(comment -> comment.setLikedByCurrentUser(likedIds.contains(comment.getCommentId())));
    }

    private void maskAnonymousIdentity(List<ProductComment> comments) {
        if (comments == null) return;
        for (ProductComment comment : comments) {
            if (comment != null && Integer.valueOf(1).equals(comment.getIsAnonymous())) {
                comment.setUserId(null);
                comment.setNickName("匿名用户");
                comment.setUserAvatar(null);
            }
        }
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

        // 审核通过或撤销正常状态都会改变店铺评分统计集合
        if (result > 0 && comment.isRootComment() && (status == 1 || comment.isNormal())) {
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
        maskAnonymousIdentity(comments);
        comments.forEach(comment -> comment.setOrderId(null));

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

        Long shopId = product.getShopId();
        if (shopId != null) {
            updateShopAverageScore(shopId);
        }
    }
    /**
     * 更新店铺平均评分
     */
    private void updateShopAverageScore(Long shopId) {
        Double averageScore = productCommentMapper.calculateShopAverageScore(shopId);
        BigDecimal score = averageScore == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(averageScore).setScale(2, RoundingMode.HALF_UP);
        shopService.updateShopScore(shopId, score);
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

        List<Long> commentIds = allComments.stream().map(ProductComment::getCommentId)
            .filter(id -> id != null).collect(java.util.stream.Collectors.toList());
        Set<Long> likedIds = new HashSet<>(getLikedCommentIds(SecurityUtils.getOptionalUserId(), commentIds));

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
            node.put("likedByCurrentUser", likedIds.contains(comment.getCommentId()));
            node.put("createTime", comment.getCreateTime());
            boolean anonymous = Integer.valueOf(1).equals(comment.getIsAnonymous());
            node.put("userId", anonymous ? null : comment.getUserId());
            node.put("images", comment.getImages());
            node.put("children", new ArrayList<Map<String, Object>>()); // 初始化children
            node.put("isAnonymous", comment.getIsAnonymous());

            if (anonymous) {
                node.put("nickName", "匿名用户");
                node.put("userAvatar", null);
            } else {
                SysUser user = sysUserService.selectUserById(comment.getUserId());
                if (user != null) {
                    node.put("nickName", user.getNickName());
                    node.put("userAvatar", user.getAvatar());
                }
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
