package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.content.banner.vo.request.BannerPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtBanner;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 *  banner Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtBannerMapper extends BaseMapperX<MtBanner> {

    default PageResult<MtBanner> selectBannerListByPage(BannerPageReqVO pageReqVO){
        return selectPage(pageReqVO,new LambdaQueryWrapperX<MtBanner>()
                .eqIfPresent(MtBanner::getStatus, pageReqVO.getStatus())
                .likeIfPresent(MtBanner::getTitle, pageReqVO.getTitle())
                .eqIfPresent(MtBanner::getPosition, pageReqVO.getPosition())
                .eqIfPresent(MtBanner::getStoreId, pageReqVO.getStoreId())
                .eqIfPresent(MtBanner::getMerchantId, pageReqVO.getMerchantId())
                .and(pageReqVO.getStoreId()!=null , wrapper -> wrapper
                        .eq(MtBanner::getStoreId, pageReqVO.getStoreId())
                        .or()
                        .eq(MtBanner::getStoreId, 0)
                )
                .ne(MtBanner::getStatus, StatusEnum.DISABLE.getKey())
                .orderByAsc(MtBanner::getSort)
                .orderByDesc(MtBanner::getId)
        );
    }
}
