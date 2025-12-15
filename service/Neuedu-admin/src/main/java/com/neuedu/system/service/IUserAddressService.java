package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户地址Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IUserAddressService extends IService<UserAddress>
{
    /**
     * 查询用户地址
     * 
     * @param id 用户地址主键
     * @return 用户地址
     */
    public UserAddress selectUserAddressById(Long id);

    /**
     * 查询用户地址列表
     * 
     * @param userAddress 用户地址
     * @return 用户地址集合
     */
    public List<UserAddress> selectUserAddressList(UserAddress userAddress);

    /**
     * 新增用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    public int insertUserAddress(UserAddress userAddress);

    /**
     * 修改用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    public int updateUserAddress(UserAddress userAddress);

    /**
     * 批量删除用户地址
     * 
     * @param ids 需要删除的用户地址主键集合
     * @return 结果
     */
    public int deleteUserAddressByIds(Long[] ids);

    /**
     * 删除用户地址信息
     * 
     * @param id 用户地址主键
     * @return 结果
     */
    public int deleteUserAddressById(Long id);
}
