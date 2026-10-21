package com.neuedu.system.service.impl;
import com.neuedu.system.domain.Activity;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IActivityService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserFootprintMapper;
import com.neuedu.system.domain.UserFootprint;
import com.neuedu.system.service.IUserFootprintService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户足迹Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-22
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFootprintServiceImpl extends ServiceImpl<UserFootprintMapper, UserFootprint> implements IUserFootprintService
{
    private final UserFootprintMapper userFootprintMapper;
    private final IProductService productService;
    private final IShopService shopService;
    private final IActivityService activityService;
    /**
     * 查询用户足迹
     * 
     * @param id 用户足迹主键
     * @return 用户足迹
     */
    @Override
    public UserFootprint selectUserFootprintById(Long id)
    {
        return userFootprintMapper.selectUserFootprintById(id);
    }

    /**
     * 查询用户足迹列表
     * 
     * @param userFootprint 用户足迹
     * @return 用户足迹
     */
    @Override
    public List<UserFootprint> selectUserFootprintList(UserFootprint userFootprint)
    {
        // 默认只查询未删除的记录
        if (userFootprint.getDeleteFlag() == null) {
            userFootprint.setDeleteFlag(0);
        }
        return userFootprintMapper.selectUserFootprintList(userFootprint);
    }

    /**
     * 记录用户足迹（核心方法）
     *
     * @param userId 用户ID
     * @param targetType 目标类型 1-商品 2-店铺 3-活动
     * @param targetId 目标ID
     * @return 结果
     */
    @Override
    @Transactional
    public int recordFootprint(Long userId, Integer targetType, Long targetId)
    {
        // 1. 先查询是否已有记录
        UserFootprint existing = userFootprintMapper.selectFootprintByUserAndTarget(userId, targetType, targetId);

        // 2. 获取目标信息（需要调用商品或店铺服务，这里假设有外部方法获取）
        FootprintTargetInfo targetInfo = getTargetInfo(targetType, targetId);

        if (existing != null) {
            // 3. 更新已有记录：增加浏览次数，更新最后浏览时间
            return userFootprintMapper.updateBrowseInfo(
                    existing.getId(),
                    existing.getBrowseCount() + 1,
                    DateUtils.getNowDate()
            );
        } else {
            // 4. 新增记录
            UserFootprint footprint = new UserFootprint();
            footprint.setUserId(userId);
            footprint.setTargetType(targetType);
            footprint.setTargetId(targetId);

            // 设置目标信息
            if (targetInfo != null) {
                footprint.setTargetName(targetInfo.getName());
                footprint.setTargetImage(targetInfo.getImage());
                footprint.setPrice(targetInfo.getPrice());
                footprint.setCategoryId(targetInfo.getCategoryId());
                footprint.setCategoryName(targetInfo.getCategoryName());
            }

            footprint.setBrowseCount(1L);
            footprint.setLastBrowseTime(DateUtils.getNowDate());
            footprint.setCreateTime(DateUtils.getNowDate());
            footprint.setUpdateTime(DateUtils.getNowDate());
            footprint.setDeleteFlag(0);
            footprint.setCreateUser(userId);
            footprint.setUpdateUser(userId);

            return userFootprintMapper.insertUserFootprint(footprint);
        }
    }

    /**
     * 查询用户的足迹列表（按最后浏览时间倒序）
     *
     * @param userId 用户ID
     * @return 足迹列表
     */
    @Override
    public List<UserFootprint> selectFootprintByUserId(Long userId, Integer targetType)
    {
        return userFootprintMapper.selectFootprintByUserId(userId, targetType);
    }

    /**
     * 查询用户最近浏览的足迹（前N条）
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 足迹列表
     */
    @Override
    public List<UserFootprint> selectRecentFootprint(Long userId, Integer limit)
    {
        return userFootprintMapper.selectRecentFootprint(userId, limit);
    }



    /**
     * 新增用户足迹
     * 
     * @param userFootprint 用户足迹
     * @return 结果
     */
    @Override
    public int insertUserFootprint(UserFootprint userFootprint)
    {
        userFootprint.setCreateTime(DateUtils.getNowDate());
        // 设置默认值
        if (userFootprint.getDeleteFlag() == null) {
            userFootprint.setDeleteFlag(0);
        }
        if (userFootprint.getBrowseCount() == null) {
            userFootprint.setBrowseCount(1L);
        }
        if (userFootprint.getLastBrowseTime() == null) {
            userFootprint.setLastBrowseTime(DateUtils.getNowDate());
        }
        return userFootprintMapper.insertUserFootprint(userFootprint);
    }

    /**
     * 修改用户足迹
     * 
     * @param userFootprint 用户足迹
     * @return 结果
     */
    @Override
    public int updateUserFootprint(UserFootprint userFootprint)
    {
        userFootprint.setUpdateTime(DateUtils.getNowDate());
        return userFootprintMapper.updateUserFootprint(userFootprint);
    }

    /**
     * 批量删除用户足迹
     *
     * @param ids 需要删除的用户足迹主键
     * @return 结果
     */
    @Override
    public int deleteUserFootprintByIds(Long[] ids)
    {
        // 改为逻辑删除
        for (Long id : ids) {
            UserFootprint footprint = new UserFootprint();
            footprint.setId(id);
            footprint.setDeleteFlag(1);
            footprint.setUpdateTime(DateUtils.getNowDate());
            userFootprintMapper.updateUserFootprint(footprint);
        }
        return ids.length;
    }

    /**
     * 删除用户足迹信息
     * 
     * @param id 用户足迹主键
     * @return 结果
     */
    @Override
    public int deleteUserFootprintById(Long id)
    {
        // 改为逻辑删除
        UserFootprint footprint = new UserFootprint();
        footprint.setId(id);
        footprint.setDeleteFlag(1);
        footprint.setUpdateTime(DateUtils.getNowDate());
        return userFootprintMapper.updateUserFootprint(footprint);
    }

    @Override
    public int deleteUserFootprintByIdForUser(Long id, Long userId)
    {
        if (id == null || userId == null) return 0;
        return userFootprintMapper.deleteUserFootprintByIdForUser(id, userId);
    }
    /**
     * 清空用户的足迹
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int clearUserFootprint(Long userId)
    {
        return userFootprintMapper.clearUserFootprint(userId);
    }

    /**
     * 统计用户足迹数量
     *
     * @param userId 用户ID
     * @return 足迹数量
     */
    @Override
    public int countUserFootprint(Long userId)
    {
        return userFootprintMapper.countUserFootprint(userId);
    }

    /**
     * 删除指定目标的足迹
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 结果
     */
    @Override
    public int deleteSpecificFootprint(Long userId, Integer targetType, Long targetId)
    {
        return userFootprintMapper.deleteSpecificFootprint(userId, targetType, targetId);
    }

    /**
     * 获取目标信息（模拟方法，需要根据实际情况实现）
     */
    private FootprintTargetInfo getTargetInfo(Integer targetType, Long targetId) {
        FootprintTargetInfo info = new FootprintTargetInfo();

        try {
            switch (targetType) {
                case 1: // 商品
                    Product product = productService.selectProductById(targetId);
                    if (product != null) {
                        info.setName(product.getName());
                        info.setImage(product.getMainImage());
                        info.setPrice(product.getPrice());
                        info.setCategoryId(product.getCategoryId());
                        // 这里需要获取分类名称，可能需要调用分类服务
                        // 暂时先设置为空，后续可以完善
                        info.setCategoryName("");
                    }
                    break;

                case 2: // 店铺
                    Shop shop = shopService.selectShopById(targetId);
                    if (shop != null) {
                        info.setName(shop.getShopName());
                        info.setImage(shop.getShopLogo());
                        info.setPrice(null); // 店铺没有价格
                        info.setCategoryId(null);
                        info.setCategoryName("");
                    }
                    break;

                case 3: // 活动
                    Activity activity = activityService.selectActivityById(targetId);
                    if (activity != null) {
                        info.setName(activity.getName());
                        info.setImage(activity.getMainImage());
                        info.setPrice(null); // 活动没有价格
                        info.setCategoryId(null);
                        info.setCategoryName("");
                    }
                    break;

                default:
                    // 未知类型
                    break;
            }
        } catch (Exception e) {
            // 记录日志，但不影响主要功能
//            log.warn("获取目标信息失败: targetType={}, targetId={}, error={}", targetType, targetId, e.getMessage());
        }

        return info;
    }

    /**
     * 目标信息内部类
     */
    private static class FootprintTargetInfo {
        private String name;
        private String image;
        private java.math.BigDecimal price;
        private Long categoryId;
        private String categoryName;

        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    }
}
