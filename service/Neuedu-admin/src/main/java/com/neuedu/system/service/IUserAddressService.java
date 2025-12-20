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
     * 根据地址ID查询地址
     *
     * @param addressId 地址ID
     * @return 地址信息
     */
    UserAddress selectUserAddressByAddressId(Long addressId);

    /**
     * 查询用户地址列表
     * 
     * @param userAddress 用户地址
     * @return 用户地址集合
     */
    public List<UserAddress> selectUserAddressList(UserAddress userAddress);

    /**
     * 查询用户的所有地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> selectUserAddressListByUserId(Long userId);


    /**
     * 新增用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    public int insertUserAddress(UserAddress userAddress);

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress selectDefaultAddressByUserId(Long userId);

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

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 结果
     */
    int setDefaultAddress(Long addressId, Long userId);


    /**
     * 验证地址信息是否完整
     *
     * @param userAddress 地址信息
     * @return 验证结果
     */
    boolean validateAddress(UserAddress userAddress);

    /**
     * 获取用户的地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    int countUserAddress(Long userId);

}
