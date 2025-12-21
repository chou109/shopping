package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.service.IProductCommentService;

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
}
