package com.neuedu.system.service;

import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.ProductComment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品评论Service接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
public interface IProductCommentService extends IService<ProductComment>
{
    /**
     * 查询商品评论
     * 
     * @param commentId 商品评论主键
     * @return 商品评论
     */
    public ProductComment selectProductCommentByCommentId(Long commentId);

    /**
     * 查询商品评论列表
     * 
     * @param productComment 商品评论
     * @return 商品评论集合
     */
    public List<ProductComment> selectProductCommentList(ProductComment productComment);

    /**
     * 新增商品评论
     * 
     * @param productComment 商品评论
     * @return 结果
     */
    public int insertProductComment(ProductComment productComment);

    /**
     * 修改商品评论
     * 
     * @param productComment 商品评论
     * @return 结果
     */
    public int updateProductComment(ProductComment productComment);

    /**
     * 批量删除商品评论
     * 
     * @param commentIds 需要删除的商品评论主键集合
     * @return 结果
     */
    public int deleteProductCommentByCommentIds(Long[] commentIds);

    /**
     * 删除商品评论信息
     * 
     * @param commentId 商品评论主键
     * @return 结果
     */
    public int deleteProductCommentByCommentId(Long commentId);

    // ============ 新增的方法 ============

    /**
     * 发表商品评论
     */
    boolean publishComment(ProductComment comment);

    /**
     * 回复评论
     */
    boolean replyComment(Long parentId, String content, Long userId);

    /**
     * 获取商品评论列表（前台展示）
     */
    List<ProductComment> getProductComments(Long productId, Integer score, Boolean hasImages);

    /**
     * 获取商品的根评论列表
     */
    List<ProductComment> getRootComments(Long productId);

    /**
     * 获取评论的回复列表
     */
    List<ProductComment> getCommentReplies(Long parentId);

    /**
     * 点赞评论
     */
    boolean likeComment(Long commentId);

    /**
     * 取消点赞评论
     */
    boolean unlikeComment(Long commentId);

    /**
     * 获取商品评论统计信息
     */
    Map<String, Object> getCommentStatistics(Long productId);

    /**
     * 获取商品评分分布
     */
    Map<Integer, Long> getScoreDistribution(Long productId);

    /**
     * 获取商品平均评分
     */
    Double getAverageScore(Long productId);

    /**
     * 审核评论
     */
    boolean auditComment(Long commentId, Integer status, String reason);

    /**
     * 获取用户对商品的评论
     */
    ProductComment getUserProductComment(Long userId, Long productId, Long orderId);

    /**
     * 获取我的评论列表
     */
    List<ProductComment> getMyComments();

    /**
     * 获取带图片的评论列表
     */
    List<ProductComment> getCommentsWithImages(Long productId, Integer limit);

    /**
     * 获取商品评论树形结构（最简单版本）
     */
    List<Map<String, Object>> getCommentTreeSimple(Long productId);
}
