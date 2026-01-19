package com.fuint.repository.mapper;

import cn.hutool.core.collection.CollUtil;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.order.vo.EvaluationPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtUserAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 会员行为 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserActionMapper extends BaseMapperX<MtUserAction> {

    Long getActiveUserCount(@Param("merchantId") Integer merchantId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    Long getStoreActiveUserCount(@Param("storeId") Integer storeId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    /**
     * 分页查询用户行为评价（使用 MyBatis Plus）
     *
     * @param pageReqVO 分页查询参数
     * @return 分页结果
     */
    default PageResult<MtUserAction> selectUserActionPage(EvaluationPageReqVO pageReqVO) {
        LambdaQueryWrapperX<MtUserAction> queryWrapper = new LambdaQueryWrapperX<MtUserAction>()
                .eq(MtUserAction::getAction, "NPS_EVALUATION")
                .geIfPresent(MtUserAction::getCreateTime, StringUtils.isNotEmpty(pageReqVO.getStartTime()) ? pageReqVO.getStartTime() : null)
                .leIfPresent(MtUserAction::getCreateTime, StringUtils.isNotEmpty(pageReqVO.getEndTime()) ? pageReqVO.getEndTime() : null);

        // SKU筛选逻辑：如果传了SKU，则通过JSON字段进行简单匹配
        if (CollUtil.isNotEmpty(pageReqVO.getSkuIds())) {
            queryWrapper.and(wrapper -> {
                for (int i = 0; i < pageReqVO.getSkuIds().size(); i++) {
                    Integer skuId = pageReqVO.getSkuIds().get(i);
                    if (i == 0) {
                        wrapper.like(MtUserAction::getParam, "\"skuId\":" + skuId);
                    } else {
                        wrapper.or().like(MtUserAction::getParam, "\"skuId\":" + skuId);
                    }
                }
                return wrapper;
            });
        }

        queryWrapper.orderByDesc(MtUserAction::getId);
        return selectPage(pageReqVO, queryWrapper);
    }

}
