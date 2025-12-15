package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.RechargeInfoMapper;
import com.neuedu.system.domain.RechargeInfo;
import com.neuedu.system.service.IRechargeInfoService;

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
