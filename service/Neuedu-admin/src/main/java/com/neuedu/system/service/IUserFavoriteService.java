package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.UserFavorite;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户收藏Service接口
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
public interface IUserFavoriteService extends IService<UserFavorite>
{
    /**
     * 查询用户收藏
     * 
     * @param favoriteId 用户收藏主键
     * @return 用户收藏
     */
    public UserFavorite selectUserFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询用户收藏列表
     * 
     * @param userFavorite 用户收藏
     * @return 用户收藏集合
     */
    public List<UserFavorite> selectUserFavoriteList(UserFavorite userFavorite);

    /**
     * 新增用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    public int insertUserFavorite(UserFavorite userFavorite);

    /**
     * 修改用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    public int updateUserFavorite(UserFavorite userFavorite);

    /**
     * 批量删除用户收藏
     * 
     * @param favoriteIds 需要删除的用户收藏主键集合
     * @return 结果
     */
    public int deleteUserFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 删除用户收藏信息
     * 
     * @param favoriteId 用户收藏主键
     * @return 结果
     */
    public int deleteUserFavoriteByFavoriteId(Long favoriteId);


    // ============ 新增的方法 ============

    /**
     * 添加收藏（商品或店铺）
     */
    boolean addFavorite(Integer targetType, Long targetId);

    /**
     * 取消收藏
     */
    boolean removeFavorite(Integer targetType, Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Integer targetType, Long targetId);

    /**
     * 获取用户的商品收藏列表
     */
    List<UserFavorite> getUserProductFavorites();

    /**
     * 获取用户的店铺收藏列表
     */
    List<UserFavorite> getUserShopFavorites();

    /**
     * 获取用户的收藏数量
     */
    Long getUserFavoriteCount(Integer targetType);

    /**
     * 获取目标被收藏数量
     */
    Long getTargetFavoriteCount(Integer targetType, Long targetId);
}
