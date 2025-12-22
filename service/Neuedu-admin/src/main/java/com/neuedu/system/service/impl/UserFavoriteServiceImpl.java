package com.neuedu.system.service.impl;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.system.service.IShopService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserFavoriteMapper;
import com.neuedu.system.domain.UserFavorite;
import com.neuedu.system.service.IUserFavoriteService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户收藏Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements IUserFavoriteService
{
    private final UserFavoriteMapper userFavoriteMapper;
    private final IShopService shopService; // 注入店铺服务
    /**
     * 查询用户收藏
     * 
     * @param favoriteId 用户收藏主键
     * @return 用户收藏
     */
    @Override
    public UserFavorite selectUserFavoriteByFavoriteId(Long favoriteId)
    {
        return userFavoriteMapper.selectUserFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询用户收藏列表
     * 
     * @param userFavorite 用户收藏
     * @return 用户收藏
     */
    @Override
    public List<UserFavorite> selectUserFavoriteList(UserFavorite userFavorite)
    {
        return userFavoriteMapper.selectUserFavoriteList(userFavorite);
    }

    /**
     * 新增用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    @Override
    public int insertUserFavorite(UserFavorite userFavorite)
    {
        userFavorite.setCreateTime(DateUtils.getNowDate());
        return userFavoriteMapper.insertUserFavorite(userFavorite);
    }

    /**
     * 修改用户收藏
     * 
     * @param userFavorite 用户收藏
     * @return 结果
     */
    @Override
    public int updateUserFavorite(UserFavorite userFavorite)
    {
        return userFavoriteMapper.updateUserFavorite(userFavorite);
    }

    /**
     * 批量删除用户收藏
     * 
     * @param favoriteIds 需要删除的用户收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return userFavoriteMapper.deleteUserFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除用户收藏信息
     * 
     * @param favoriteId 用户收藏主键
     * @return 结果
     */
    @Override
    public int deleteUserFavoriteByFavoriteId(Long favoriteId)
    {
        return userFavoriteMapper.deleteUserFavoriteByFavoriteId(favoriteId);
    }


    // ============ 新增的方法实现 ============

    /**
     * 添加收藏（商品或店铺）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFavorite(Integer targetType, Long targetId) {
        Long userId = SecurityUtils.getUserId();

        // 检查是否已收藏
        boolean exists = userFavoriteMapper.existsFavorite(userId, targetType, targetId);
        if (exists) {
            throw new ServiceException("已经收藏过了");
        }

        // 如果是店铺收藏，检查店铺是否存在且正常营业
        if (targetType == 2) { // 2代表店铺
            var shop = shopService.selectShopById(targetId);
            if (shop == null) {
                throw new ServiceException("店铺不存在");
            }
            if (!shop.isNormal()) {
                throw new ServiceException("店铺暂未营业，无法收藏");
            }
        }

        // 创建收藏记录
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setCreateTime(DateUtils.getNowDate());

        int result = insertUserFavorite(favorite);

        // 如果是店铺收藏，更新店铺粉丝数
        if (result > 0 && targetType == 2) {
            shopService.increaseFollower(targetId);
        }

        return result > 0;
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Integer targetType, Long targetId) {
        Long userId = SecurityUtils.getUserId();

        // 检查是否已收藏
        boolean exists = userFavoriteMapper.existsFavorite(userId, targetType, targetId);
        if (!exists) {
            throw new ServiceException("还未收藏，无法取消");
        }

        int result = userFavoriteMapper.deleteByUserAndTarget(userId, targetType, targetId);

        // 如果是店铺收藏，更新店铺粉丝数
        if (result > 0 && targetType == 2) {
            shopService.decreaseFollower(targetId);
        }

        return result > 0;
    }

    /**
     * 检查是否已收藏
     */
    @Override
    public boolean isFavorite(Integer targetType, Long targetId) {
        Long userId = SecurityUtils.getUserId();
        return userFavoriteMapper.existsFavorite(userId, targetType, targetId);
    }

    /**
     * 获取用户的商品收藏列表
     */
    @Override
    public List<UserFavorite> getUserProductFavorites() {
        Long userId = SecurityUtils.getUserId();
        return userFavoriteMapper.selectUserFavorites(userId, 1); // 1代表商品
    }

    /**
     * 获取用户的店铺收藏列表
     */
    @Override
    public List<UserFavorite> getUserShopFavorites() {
        Long userId = SecurityUtils.getUserId();
        return userFavoriteMapper.selectUserFavorites(userId, 2); // 2代表店铺
    }

    /**
     * 获取用户的收藏数量
     */
    @Override
    public Long getUserFavoriteCount(Integer targetType) {
        Long userId = SecurityUtils.getUserId();
        return userFavoriteMapper.countUserFavorites(userId, targetType);
    }

    /**
     * 获取目标被收藏数量
     */
    @Override
    public Long getTargetFavoriteCount(Integer targetType, Long targetId) {
        return userFavoriteMapper.countTargetFavorites(targetType, targetId);
    }
}
