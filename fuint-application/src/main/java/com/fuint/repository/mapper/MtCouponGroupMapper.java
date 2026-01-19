package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.marketing.group.vo.CouponGroupPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtCouponGroup;

/**
 * 优惠券组 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtCouponGroupMapper extends BaseMapperX<MtCouponGroup> {



    default PageResult<MtCouponGroup> selectCouponGroupPage(CouponGroupPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<MtCouponGroup>()
                .eqIfPresent(MtCouponGroup::getStatus, StatusEnum.ENABLED.getKey())
                .eqIfPresent(MtCouponGroup::getMerchantId, pageReqVO.getMerchantId())
                .eqIfPresent(MtCouponGroup::getStoreId, pageReqVO.getStoreId())
                .likeIfPresent(MtCouponGroup::getName, pageReqVO.getName())
                .orderByDesc(MtCouponGroup::getCreateTime)
        );
    }
}
