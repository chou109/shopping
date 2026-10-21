package com.neuedu.system.service.impl;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ShopMapper;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IShopService;
import com.neuedu.system.service.ISysUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 店铺信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService
{
    private final ShopMapper shopMapper;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 查询店铺信息
     * 
     * @param id 店铺信息主键
     * @return 店铺信息
     */
    @Override
    public Shop selectShopById(Long id)
    {
        return shopMapper.selectShopById(id);
    }

    /**
     * 查询店铺信息列表
     * 
     * @param shop 店铺信息
     * @return 店铺信息
     */
    @Override
    public List<Shop> selectShopList(Shop shop)
    {
        return shopMapper.selectShopList(shop);
    }

    /**
     * 新增店铺信息
     * 
     * @param shop 店铺信息
     * @return 结果
     */
    @Override
    public int insertShop(Shop shop)
    {
        if (shop.getUserId() == null || sysUserService.selectUserById(shop.getUserId()) == null) {
            throw new ServiceException("店铺所有者不存在，请选择有效的系统用户");
        }

        shop.setCreateTime(DateUtils.getNowDate());
        // 设置默认值
        if (shop.getShopScore() == null) {
            shop.setShopScore(BigDecimal.ZERO);
        }
        if (shop.getSalesCount() == null) {
            shop.setSalesCount(0L);
        }
        if (shop.getProductCount() == null) {
            shop.setProductCount(0L);
        }
        if (shop.getFollowerCount() == null) {
            shop.setFollowerCount(0L);
        }
        if (shop.getDeleteFlag() == null) {
            shop.setDeleteFlag(0);
        }
        if (shop.getStatus() == null) {
            shop.setStatus(1); // 默认正常营业
        }
        return shopMapper.insertShop(shop);
    }

    /**
     * 修改店铺信息
     * 
     * @param shop 店铺信息
     * @return 结果
     */
    @Override
    public int updateShop(Shop shop)
    {
        // 店铺资料接口只维护可编辑资料；所有者、营业状态、统计数据和审计字段
        // 必须由专用流程维护，不能通过通用更新接口由请求体覆盖。
        shop.setCreateTime(null);
        shop.setCreateUser(null);
        shop.setUpdateUser(null);
        shop.setDeleteFlag(null);
        shop.setUserId(null);
        shop.setShopScore(null);
        shop.setSalesCount(null);
        shop.setProductCount(null);
        shop.setFollowerCount(null);
        shop.setStatus(null);
        shop.setUpdateTime(DateUtils.getNowDate());
        return shopMapper.updateShop(shop);
    }

    /**
     * 批量删除店铺信息
     * 
     * @param ids 需要删除的店铺信息主键
     * @return 结果
     */
    @Override
    public int deleteShopByIds(Long[] ids)
    {
        return shopMapper.deleteShopByIds(ids);
    }

    /**
     * 删除店铺信息信息
     * 
     * @param id 店铺信息主键
     * @return 结果
     */
    @Override
    public int deleteShopById(Long id)
    {
        return shopMapper.deleteShopById(id);
    }

    /**
     * 根据用户ID查询店铺
     */
    @Override
    public Shop selectShopByUserId(Long userId) {
        return shopMapper.selectShopByUserId(userId);
    }

    /**
     * 获取我的店铺
     */
    @Override
    public Shop getMyShop() {
        Long userId = SecurityUtils.getUserId();
        return selectShopByUserId(userId);
    }

    /**
     * 增加店铺销量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int increaseSales(Long shopId, Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ServiceException("销量数量必须大于0");
        }
        return shopMapper.increaseSales(shopId, quantity);
    }

    /**
     * 增加店铺粉丝数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int increaseFollower(Long shopId) {
        return shopMapper.increaseFollower(shopId, 1L);
    }

    /**
     * 减少店铺粉丝数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int decreaseFollower(Long shopId) {
        return shopMapper.decreaseFollower(shopId, 1L);
    }

    /**
     * 更新店铺评分
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShopScore(Long shopId, BigDecimal newScore) {
        if (newScore == null || newScore.compareTo(BigDecimal.ZERO) < 0 || newScore.compareTo(new BigDecimal(5)) > 0) {
            throw new ServiceException("评分必须在0-5之间");
        }
        return shopMapper.updateShopScore(shopId, newScore);
    }

    /**
     * 更新店铺状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShopStatus(Long shopId, Integer status) {
        if (status == null || (status != 1 && status != 2 && status != 3)) {
            throw new ServiceException("店铺状态无效");
        }
        Long updateUser = SecurityUtils.getUserId();
        return shopMapper.updateShopStatus(shopId, status, updateUser);
    }

    /**
     * 逻辑删除店铺
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShopLogicById(Long id) {
        return shopMapper.deleteShopLogicById(id);
    }

    /**
     * 批量逻辑删除店铺
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShopLogicByIds(Long[] ids) {
        return shopMapper.deleteShopLogicByIds(ids);
    }

    /**
     * 热门店铺列表
     */
    @Override
    public List<Shop> getHotShopList(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认10个
        }
        return shopMapper.selectHotShopList(limit);
    }

    /**
     * 搜索店铺
     */
    @Override
    public List<Shop> searchShops(String keyword, Integer status) {
        return shopMapper.searchShops(keyword, status);
    }

    /**
     * 获取店铺统计信息
     */
    @Override
    public Map<String, Object> getShopStatistics(Long shopId) {
        Shop shop = selectShopById(shopId);
        if (shop == null) {
            throw new ServiceException("店铺不存在");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("id", shop.getId());
        stats.put("shopName", shop.getShopName());
        stats.put("status", shop.getStatus());
        stats.put("statusDesc", shop.getStatusDesc());
        stats.put("salesCount", shop.getSalesCount());
        stats.put("productCount", shop.getProductCount());
        stats.put("followerCount", shop.getFollowerCount());
        stats.put("shopScore", shop.getShopScore());
        stats.put("createTime", shop.getCreateTime());
        stats.put("updateTime", shop.getUpdateTime());

        // 可以在这里添加更多的统计信息，比如：
        // - 近7天订单数量
        // - 近30天销售额
        // - 商品类目分布
        // - 客户评价统计

        return stats;
    }
}
