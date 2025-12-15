package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.RechargeInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * 【请填写功能名称】Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface RechargeInfoMapper extends BaseMapper<RechargeInfo>
{
    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    public RechargeInfo selectRechargeInfoById(Long id);

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<RechargeInfo> selectRechargeInfoList(RechargeInfo rechargeInfo);

    /**
     * 新增【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    public int insertRechargeInfo(RechargeInfo rechargeInfo);

    /**
     * 修改【请填写功能名称】
     * 
     * @param rechargeInfo 【请填写功能名称】
     * @return 结果
     */
    public int updateRechargeInfo(RechargeInfo rechargeInfo);

    /**
     * 删除【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    public int deleteRechargeInfoById(Long id);

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRechargeInfoByIds(Long[] ids);
}
