package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.ProductComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
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
}
