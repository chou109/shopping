package com.neuedu.system.mapper;

import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.ProductComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品评论Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment>
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
     * 删除商品评论
     * 
     * @param commentId 商品评论主键
     * @return 结果
     */
    public int deleteProductCommentByCommentId(Long commentId);

    /**
     * 批量删除商品评论
     * 
     * @param commentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductCommentByCommentIds(Long[] commentIds);


    // ============ 新增的方法 ============

    /**
     * 查询商品评论列表（带分页）
     */
    List<ProductComment> selectProductCommentPage(@Param("productId") Long productId,
                                                  @Param("userId") Long userId,
                                                  @Param("score") Integer score,
                                                  @Param("hasImages") Boolean hasImages,
                                                  @Param("status") Integer status);

    /**
     * 查询商品的根评论列表
     */
    List<ProductComment> selectRootCommentsByProductId(@Param("productId") Long productId,
                                                       @Param("status") Integer status);

    /**
     * 查询评论的回复列表
     */
    List<ProductComment> selectRepliesByParentId(@Param("parentId") Long parentId,
                                                 @Param("status") Integer status);

    /**
     * 统计商品评论数量
     */
    Long countCommentsByProductId(@Param("productId") Long productId,
                                  @Param("score") Integer score,
                                  @Param("hasImages") Boolean hasImages,
                                  @Param("status") Integer status);

    /**
     * 统计商品的评分分布
     */
    List<Map<String, Object>> countScoreDistribution(@Param("productId") Long productId);

    /**
     * 计算商品平均评分
     */
    Double calculateAverageScore(@Param("productId") Long productId);

    /** 计算店铺所有正常根评价的平均分 */
    Double calculateShopAverageScore(@Param("shopId") Long shopId);

    /**
     * 增加评论点赞数
     */
    int increaseLikeCount(@Param("commentId") Long commentId);

    /**
     * 减少评论点赞数
     */
    int decreaseLikeCount(@Param("commentId") Long commentId);

    int insertCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int deleteCommentLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int countCommentLikesByUser(@Param("commentId") Long commentId, @Param("userId") Long userId);

    List<Long> selectLikedCommentIds(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    /**
     * 增加评论回复数
     */
    int increaseReplyCount(@Param("commentId") Long commentId);

    /**
     * 减少评论回复数
     */
    int decreaseReplyCount(@Param("commentId") Long commentId);

    /**
     * 更新评论状态
     */
    int updateCommentStatus(@Param("commentId") Long commentId,
                            @Param("status") Integer status);

    /**
     * 查询用户对商品的评论
     */
    ProductComment selectUserCommentOnProduct(@Param("userId") Long userId,
                                              @Param("productId") Long productId,
                                              @Param("orderId") Long orderId);
}
