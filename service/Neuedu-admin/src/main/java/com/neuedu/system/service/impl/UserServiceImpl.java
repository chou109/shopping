package com.neuedu.system.service.impl;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.UserMapper;
import com.neuedu.system.domain.User;
import com.neuedu.system.service.IUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService
{
    private final UserMapper userMapper;

    /**
     * 查询用户信息
     * 
     * @param id 用户信息主键
     * @return 用户信息
     */
    @Override
    public User selectUserById(Long id)
    {
        return userMapper.selectUserById(id);
    }

    /**
     * 查询用户信息列表
     * 
     * @param user 用户信息
     * @return 用户信息
     */
    @Override
    public List<User> selectUserList(User user)
    {
        return userMapper.selectUserList(user);
    }

    /**
     * 新增用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int insertUser(User user)
    {
        user.setCreateTime(DateUtils.getNowDate());
        return userMapper.insertUser(user);
    }

    /**
     * 修改用户信息
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUser(User user)
    {
        user.setUpdateTime(DateUtils.getNowDate());
        return userMapper.updateUser(user);
    }

    /**
     * 批量删除用户信息
     * 
     * @param ids 需要删除的用户信息主键
     * @return 结果
     */
    @Override
    public int deleteUserByIds(Long[] ids)
    {
        return userMapper.deleteUserByIds(ids);
    }

    /**
     * 删除用户信息信息
     * 
     * @param id 用户信息主键
     * @return 结果
     */
    @Override
    public int deleteUserById(Long id)
    {
        return userMapper.deleteUserById(id);
    }


    /**
     * 根据用户名查询用户
     */
    @Override
    public User selectUserByUserName(String userName) {
        return lambdaQuery()
                .eq(User::getUserName, userName)
                .eq(User::getDeleteFlag, 0)
                .one();
    }

    /**
     * 根据手机号查询用户
     */
    @Override
    public User selectUserByPhone(String phone) {
        return lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getDeleteFlag, 0)
                .one();
    }

    /**
     * 更新用户余额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserBalance(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            throw new ServiceException("用户ID和金额不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);

        // 检查余额是否足够（当减少余额时）
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException("用户余额不足");
        }

        // 更新余额
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setBalance(newBalance);

        return updateById(updateUser);
    }

    /**
     * 获取用户余额
     */
    @Override
    public BigDecimal getUserBalance(Long userId) {
        if (userId == null) {
            return BigDecimal.ZERO;
        }

        User user = getById(userId);
        if (user == null) {
            return BigDecimal.ZERO;
        }

        return user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
    }

    /**
     * 用户注册
     */
    @Override
    public boolean register(User user) {
        if (user == null) {
            throw new ServiceException("用户信息不能为空");
        }

        // 检查用户名是否已存在
        if (checkUserNameExist(user.getUserName())) {
            throw new ServiceException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (checkPhoneExist(user.getPhone())) {
            throw new ServiceException("手机号已存在");
        }

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO); // 默认余额为0
        }
        if (StringUtils.isEmpty(user.getType())) {
            user.setType("user"); // 默认用户类型
        }

        // 密码加密（这里需要根据实际情况实现）
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        return save(user);
    }

    /**
     * 修改密码
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 验证旧密码（这里需要根据实际情况实现）
        // if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        //     throw new ServiceException("原密码错误");
        // }

        // 更新密码（需要加密）
        // user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassword(newPassword); // 临时方案，实际需要加密

        return updateById(user);
    }

    /**
     * 重置密码
     */
    @Override
    public boolean resetPassword(String phone, String newPassword) {
        User user = selectUserByPhone(phone);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 更新密码（需要加密）
        // user.setPassword(passwordEncoder.encode(newPassword));
        user.setPassword(newPassword); // 临时方案，实际需要加密

        return updateById(user);
    }

    /**
     * 更新用户状态
     */
    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            throw new ServiceException("参数不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);

        return updateById(updateUser);
    }

    /**
     * 用户登录
     */
    @Override
    public User login(String username, String password) {
        // 根据用户名或手机号查询用户
        User user = lambdaQuery()
                .and(q -> q.eq(User::getUserName, username).or().eq(User::getPhone, username))
                .eq(User::getDeleteFlag, 0)
                .one();

        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new ServiceException("账号已被禁用");
        }

        // 验证密码（这里需要根据实际情况实现）
        // if (!passwordEncoder.matches(password, user.getPassword())) {
        if (!password.equals(user.getPassword())) { // 临时方案
            throw new ServiceException("密码错误");
        }

        return user;
    }

    /**
     * 统计用户数量
     */
    @Override
    public Long countUsers() {
        return lambdaQuery()
                .eq(User::getDeleteFlag, 0)
                .count();
    }

    /**
     * 检查用户名是否存在
     */
    @Override
    public boolean checkUserNameExist(String userName) {
        return lambdaQuery()
                .eq(User::getUserName, userName)
                .eq(User::getDeleteFlag, 0)
                .exists();
    }

    /**
     * 检查手机号是否存在
     */
    @Override
    public boolean checkPhoneExist(String phone) {
        return lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getDeleteFlag, 0)
                .exists();
    }
}
