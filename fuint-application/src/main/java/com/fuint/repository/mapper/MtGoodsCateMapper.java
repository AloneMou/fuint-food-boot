package com.fuint.repository.mapper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.dto.GoodsCateDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.goods.cate.vo.MtGoodsCatePageReqVO;
import com.fuint.openapi.v1.goods.cate.vo.MtGoodsCateRespVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtGoodsCate;
import com.fuint.repository.model.MtMerchant;
import com.fuint.repository.model.MtStore;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

import java.util.Arrays;

/**
 * 商品分类 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsCateMapper extends BaseMapperX<MtGoodsCate> {
    // empty

    default PageResult<MtGoodsCateRespVO> selectCateByPage(MtGoodsCatePageReqVO pageReqVO) {
        MPJLambdaWrapper<MtGoodsCate> wrapper = new MPJLambdaWrapper<MtGoodsCate>()
                .selectAll(MtGoodsCate.class)
                .selectAs(MtStore::getName, MtGoodsCateRespVO::getStoreName)
                .selectAs(MtMerchant::getName, MtGoodsCateRespVO::getMerchantName)
                .leftJoin(MtStore.class, MtStore::getId, MtGoodsCate::getStoreId)
                .leftJoin(MtMerchant.class, MtMerchant::getId, MtGoodsCate::getMerchantId)
                .ne(MtGoodsCate::getStatus, StatusEnum.DISABLE.getKey())
                .eq(pageReqVO.getMerchantId() != null, MtGoodsCate::getMerchantId, pageReqVO.getMerchantId())
                .in(pageReqVO.getStoreId() != null, MtGoodsCate::getStoreId,
                        Arrays.asList(pageReqVO.getStoreId(), 0));
        if (CollUtil.isEmpty(pageReqVO.getSortingFields())) {
            wrapper.orderByAsc(MtGoodsCate::getSort);
        }
        return selectJoinPage(pageReqVO, MtGoodsCateRespVO.class, wrapper);
    }
}
