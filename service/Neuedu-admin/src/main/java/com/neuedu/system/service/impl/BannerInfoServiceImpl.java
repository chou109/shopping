package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.BannerInfoMapper;
import com.neuedu.system.domain.BannerInfo;
import com.neuedu.system.service.IBannerInfoService;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BannerInfoServiceImpl extends ServiceImpl<BannerInfoMapper, BannerInfo> implements IBannerInfoService
{
    private final BannerInfoMapper bannerInfoMapper;

    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public BannerInfo selectBannerInfoById(Long id)
    {
        return bannerInfoMapper.selectBannerInfoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<BannerInfo> selectBannerInfoList(BannerInfo bannerInfo)
    {
        return bannerInfoMapper.selectBannerInfoList(bannerInfo);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertBannerInfo(BannerInfo bannerInfo)
    {
        bannerInfo.setCreateTime(DateUtils.getNowDate());
        return bannerInfoMapper.insertBannerInfo(bannerInfo);
    }

    /**
     * 修改【请填写功能名称】
     * 
     * @param bannerInfo 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateBannerInfo(BannerInfo bannerInfo)
    {
        bannerInfo.setUpdateTime(DateUtils.getNowDate());
        return bannerInfoMapper.updateBannerInfo(bannerInfo);
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteBannerInfoByIds(Long[] ids)
    {
        return bannerInfoMapper.deleteBannerInfoByIds(ids);
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteBannerInfoById(Long id)
    {
        return bannerInfoMapper.deleteBannerInfoById(id);
    }
}
