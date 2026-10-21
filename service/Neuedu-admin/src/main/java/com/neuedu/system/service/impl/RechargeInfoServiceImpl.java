package com.neuedu.system.service.impl;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.system.constants.RechargeConstants;
import com.neuedu.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.RechargeInfoMapper;
import com.neuedu.system.domain.RechargeInfo;
import com.neuedu.system.service.IRechargeInfoService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RechargeInfoServiceImpl extends ServiceImpl<RechargeInfoMapper, RechargeInfo> implements IRechargeInfoService
{
    private final RechargeInfoMapper rechargeInfoMapper;

    @Autowired
    private ISysUserService sysUserService; // 用户服务，用于更新余额

    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public RechargeInfo selectRechargeInfoById(Long id)
    {
        return rechargeInfoMapper.selectRechargeInfoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<RechargeInfo> selectRechargeInfoList(RechargeInfo rechargeInfo)
    {
        return rechargeInfoMapper.selectRechargeInfoList(rechargeInfo);
    }

    /**
     * 根据用户ID查询充值记录
     */
    @Override
    public List<RechargeInfo> selectRechargeInfoByUserId(Long userId)
    {
        return rechargeInfoMapper.selectRechargeInfoByUserId(userId);
    }

    /**
     * 根据订单号查询充值记录
     */
    @Override
    public RechargeInfo selectRechargeInfoByOrderNo(String orderNo)
    {
        return rechargeInfoMapper.selectRechargeInfoByOrderNo(orderNo);
    }

    /**
     * 根据支付流水号查询充值记录
     */
    @Override
    public RechargeInfo selectRechargeInfoByPaymentNo(String paymentNo)
    {
        return rechargeInfoMapper.selectRechargeInfoByPaymentNo(paymentNo);
    }

    /**
     * 统计用户充值总金额
     */
    @Override
    public BigDecimal selectTotalRechargeByUserId(Long userId)
    {
        return rechargeInfoMapper.selectTotalRechargeByUserId(userId);
    }

    @Override
    public Map<String, Object> selectRechargeStatistics(String beginDate, String endDate)
    {
        return rechargeInfoMapper.selectRechargeStatistics(beginDate, endDate);
    }

    /**
     * 创建充值订单
     */
    @Override
    public Long createRechargeOrder(RechargeInfo rechargeInfo)
    {
        // 验证参数
        if (rechargeInfo.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }
        if (rechargeInfo.getBalance() == null || rechargeInfo.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("充值金额必须大于0");
        }

        // 设置默认状态
        if (rechargeInfo.getStatus() == null) {
            rechargeInfo.setStatus(RechargeConstants.RechargeStatus.WAIT_PAY);
        }

        // 生成订单号（规则：RC + 时间戳 + 随机数）
        if (StringUtils.isEmpty(rechargeInfo.getOrderNo())) {
            String orderNo = "RC" + System.currentTimeMillis() + (int)((Math.random() * 9 + 1) * 1000);
            rechargeInfo.setOrderNo(orderNo);
        }

        // 设置创建时间
        rechargeInfo.setCreateTime(DateUtils.getNowDate());

        // 插入充值记录
        int result = rechargeInfoMapper.insertRechargeInfo(rechargeInfo);
        if (result > 0) {
            return rechargeInfo.getId();
        }
        return null;
    }

    /**
     * 处理支付成功回调
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(String orderNo, String paymentNo, Integer paymentType)
    {
        // 查询充值记录
        RechargeInfo rechargeInfo = rechargeInfoMapper.selectRechargeInfoByOrderNo(orderNo);
        if (rechargeInfo == null) {
            throw new ServiceException("充值订单不存在：" + orderNo);
        }

        // 检查状态
        if (rechargeInfo.getStatus() != RechargeConstants.RechargeStatus.WAIT_PAY) {
            throw new ServiceException("订单状态不正确，当前状态：" +
                    RechargeConstants.RechargeStatus.getStatusDesc(rechargeInfo.getStatus()));
        }

        // 更新充值记录状态
        int result = rechargeInfoMapper.updateRechargeStatus(
                rechargeInfo.getId(),
                RechargeConstants.RechargeStatus.SUCCESS,
                DateUtils.getNowDate(),
                paymentNo
        );

        if (result > 0) {
            // 更新用户余额
            try {
                boolean updateBalance = sysUserService.updateUserBalance(
                        rechargeInfo.getUserId(),
                        rechargeInfo.getBalance()
                );

                if (!updateBalance) {
                    throw new ServiceException("更新用户余额失败");
                }
            } catch (Exception e) {
                // 如果更新余额失败，需要回滚充值状态
                rechargeInfoMapper.updateRechargeStatus(
                        rechargeInfo.getId(),
                        RechargeConstants.RechargeStatus.FAILED,
                        DateUtils.getNowDate(),
                        paymentNo + "_ROLLBACK"
                );
                throw new ServiceException("更新用户余额异常：" + e.getMessage());
            }
            // 这里可以添加其他业务逻辑，如发送通知等

            return true;
        }

        return false;
    }

    /**
     * 处理支付失败
     */
    @Override
    public boolean handlePaymentFailed(String orderNo, String paymentNo)
    {
        RechargeInfo rechargeInfo = rechargeInfoMapper.selectRechargeInfoByOrderNo(orderNo);
        if (rechargeInfo == null) {
            throw new ServiceException("充值订单不存在：" + orderNo);
        }

        return rechargeInfoMapper.updateRechargeStatus(
                rechargeInfo.getId(),
                RechargeConstants.RechargeStatus.FAILED,
                DateUtils.getNowDate(),
                paymentNo
        ) > 0;
    }

    /**
     * 退款处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRefund(Long id)
    {
        RechargeInfo rechargeInfo = rechargeInfoMapper.selectRechargeInfoById(id);
        if (rechargeInfo == null) {
            throw new ServiceException("充值记录不存在");
        }

        // 只有支付成功的才能退款
        if (rechargeInfo.getStatus() != RechargeConstants.RechargeStatus.SUCCESS) {
            throw new ServiceException("只有支付成功的订单才能退款");
        }

        // 检查用户余额是否足够
        BigDecimal currentBalance = sysUserService.getUserBalance(rechargeInfo.getUserId());
        if (currentBalance.compareTo(rechargeInfo.getBalance()) < 0) {
            throw new ServiceException("用户余额不足，无法退款");
        }

        // 更新充值记录状态为已退款
        int result = rechargeInfoMapper.updateRechargeStatus(
                rechargeInfo.getId(),
                RechargeConstants.RechargeStatus.REFUNDED,
                DateUtils.getNowDate(),
                rechargeInfo.getPaymentNo() + "_REFUND"
        );

        if (result > 0) {
            // 扣除用户余额
            try {
                boolean updateBalance = sysUserService.updateUserBalance(
                        rechargeInfo.getUserId(),
                        rechargeInfo.getBalance().negate() // 负值表示扣除
                );

                if (!updateBalance) {
                    throw new ServiceException("更新用户余额失败");
                }
            } catch (Exception e) {
                // 如果扣除余额失败，需要回滚退款状态
                rechargeInfoMapper.updateRechargeStatus(
                        rechargeInfo.getId(),
                        RechargeConstants.RechargeStatus.SUCCESS,
                        DateUtils.getNowDate(),
                        rechargeInfo.getPaymentNo() + "_REFUND_ROLLBACK"
                );
                throw new ServiceException("更新用户余额异常：" + e.getMessage());
            }

            return true;
        }

        return false;
    }


    /**
     * 新增【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertRechargeInfo(RechargeInfo rechargeInfo)
    {
        rechargeInfo.setCreateTime(DateUtils.getNowDate());
        return rechargeInfoMapper.insertRechargeInfo(rechargeInfo);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateRechargeInfo(RechargeInfo rechargeInfo)
    {
        rechargeInfo.setUpdateTime(DateUtils.getNowDate());
        return rechargeInfoMapper.updateRechargeInfo(rechargeInfo);
    }

    /**
     * 更新充值状态
     */
    @Override
    public int updateRechargeStatus(Long id, Integer status, java.util.Date paymentTime, String paymentNo)
    {
        return rechargeInfoMapper.updateRechargeStatus(id, status, paymentTime, paymentNo);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteRechargeInfoByIds(Long[] ids)
    {
        return rechargeInfoMapper.deleteRechargeInfoByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteRechargeInfoById(Long id)
    {
        return rechargeInfoMapper.deleteRechargeInfoById(id);
    }
}
