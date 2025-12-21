package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.system.constants.AddressConstants;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserAddressMapper;
import com.neuedu.system.domain.UserAddress;
import com.neuedu.system.service.IUserAddressService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户地址Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements IUserAddressService
{
    private final UserAddressMapper userAddressMapper;

    /**
     * 查询用户地址
     * 
     * @param id 用户地址主键
     * @return 用户地址
     */
    @Override
    public UserAddress selectUserAddressById(Long id)
    {
        UserAddress address = userAddressMapper.selectUserAddressById(id);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;
    }

    /**
     * 根据地址ID查询地址
     *
     * @param addressId 地址ID
     * @return 地址信息
     */
    @Override
    public UserAddress selectUserAddressByAddressId(Long addressId)
    {
        UserAddress address = userAddressMapper.selectUserAddressByAddressId(addressId);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;
    }

    /**
     * 查询用户的默认地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    @Override
    public UserAddress selectDefaultAddressByUserId(Long userId)
    {
        UserAddress address = userAddressMapper.selectDefaultAddressByUserId(userId);
        if (address != null) {
            enrichAddressInfo(address);
        }
        return address;

    }

    /**
     * 查询用户地址列表
     * 
     * @param userAddress 用户地址
     * @return 用户地址
     */
    @Override
    public List<UserAddress> selectUserAddressList(UserAddress userAddress)
    {
        List<UserAddress> list = userAddressMapper.selectUserAddressList(userAddress);
        list.forEach(this::enrichAddressInfo);
        return list;
    }

    /**
     * 查询用户的所有地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    @Override
    public List<UserAddress> selectUserAddressListByUserId(Long userId) {
        UserAddress query = new UserAddress();
        query.setUserId(userId);
        return selectUserAddressList(query);
    }

    /**
     * 新增用户地址
     *
     * @param userAddress 地址信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUserAddress(UserAddress userAddress)
    {
        // 1. 验证地址信息
        if (!validateAddress(userAddress)) {
            throw new RuntimeException("地址信息不完整");
        }
        // 2. 设置默认值
        userAddress.setCreateTime(DateUtils.getNowDate());
        userAddress.setDeleteFlag(AddressConstants.DELETE_FLAG_NO);

        // 3. 如果是设置默认地址，需要取消其他默认地址
        if (userAddress.getDefaultFlag() != null &&
                userAddress.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            cancelOtherDefaultAddress(userAddress.getUserId());
        } else {
            // 如果用户没有地址，则第一个地址自动设为默认
            int addressCount = countUserAddress(userAddress.getUserId());
            if (addressCount == 0) {
                userAddress.setDefaultFlag(AddressConstants.DEFAULT_FLAG_YES);
            } else {
                userAddress.setDefaultFlag(AddressConstants.DEFAULT_FLAG_NO);
            }
        }

        return userAddressMapper.insertUserAddress(userAddress);
    }

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setDefaultAddress(Long addressId, Long userId)
    {
        // 1. 检查地址是否存在且属于该用户
        UserAddress address = userAddressMapper.selectUserAddressById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或不属于该用户");
        }

        // 1. 取消所有默认地址
        int cancelResult = userAddressMapper.cancelAllDefaultAddress(userId);

        // 2. 设置新的默认地址
        int setResult = userAddressMapper.setDefaultAddress(addressId, userId);

        return cancelResult + setResult;
    }
    /**
     * 取消其他默认地址
     */
    private void cancelOtherDefaultAddress(Long userId) {
        userAddressMapper.cancelAllDefaultAddress(userId);
    }
    /**
     * 修改用户地址
     * 
     * @param userAddress 用户地址
     * @return 结果
     */
    @Override
    public int updateUserAddress(UserAddress userAddress)
    {
        // 1. 验证地址信息
        if (!validateAddress(userAddress)) {
            throw new RuntimeException("地址信息不完整");
        }

        // 2. 如果是设置默认地址，需要取消其他默认地址
        if (userAddress.getDefaultFlag() != null &&
                userAddress.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            // 先查询当前地址信息
            UserAddress existingAddress = selectUserAddressById(userAddress.getId());
            if (existingAddress != null &&
                    existingAddress.getDefaultFlag() != AddressConstants.DEFAULT_FLAG_YES) {
                cancelOtherDefaultAddress(existingAddress.getUserId());
            }
        }

        userAddress.setUpdateTime(DateUtils.getNowDate());
        return userAddressMapper.updateUserAddress(userAddress);
    }

    /**
     * 批量删除用户地址
     * 
     * @param ids 需要删除的用户地址主键
     * @return 结果
     */
    @Override
    public int deleteUserAddressByIds(Long[] ids)
    {

        // 检查是否有默认地址，默认地址不能删除
        for (Long id : ids) {
            UserAddress address = selectUserAddressById(id);
            if (address != null && address.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
                throw new RuntimeException("默认地址不能删除，请先设置其他地址为默认");
            }
        }

        return userAddressMapper.deleteUserAddressByIds(ids);
    }

    /**
     * 删除用户地址信息
     * 
     * @param id 用户地址主键
     * @return 结果
     */
    @Override
    public int deleteUserAddressById(Long id)
    {
        UserAddress address = selectUserAddressById(id);
        if (address != null && address.getDefaultFlag() == AddressConstants.DEFAULT_FLAG_YES) {
            throw new RuntimeException("默认地址不能删除，请先设置其他地址为默认");
        }

        return userAddressMapper.deleteUserAddressById(id);
    }

    /**
     * 验证地址信息是否完整
     *
     * @param userAddress 地址信息
     * @return 验证结果
     */
    @Override
    public boolean validateAddress(UserAddress userAddress) {
        if (userAddress == null) {
            return false;
        }

        // 必填字段验证
        if (StringUtils.isEmpty(userAddress.getReceiveName())) {
            return false;
        }

        if (StringUtils.isEmpty(userAddress.getReceivePhone())) {
            return false;
        }

        // 手机号格式验证（简单验证）
        String phone = userAddress.getReceivePhone();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return false;
        }

        // 省市区验证
        if (StringUtils.isEmpty(userAddress.getProvince()) ||
                StringUtils.isEmpty(userAddress.getCity()) ||
                StringUtils.isEmpty(userAddress.getArea())) {
            return false;
        }

        // 详细地址验证
        if (StringUtils.isEmpty(userAddress.getStreet())) {
            return false;
        }

        return true;
    }

    /**
     * 获取用户的地址数量
     *
     * @param userId 用户ID
     * @return 地址数量
     */
    @Override
    public int countUserAddress(Long userId) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("delete_flag", AddressConstants.DELETE_FLAG_NO);
        return Math.toIntExact(count(queryWrapper));
    }

    /**
     * 丰富地址信息（添加扩展字段）
     */
    private void enrichAddressInfo(UserAddress address) {
        // 调用初始化方法设置扩展字段
        address.initExtraFields();
    }
}
