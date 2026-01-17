package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.model.MtGoodsSku;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SKU表 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsSkuMapper extends BaseMapper<MtGoodsSku> {

    List<MtGoodsSku> getBySkuNo(@Param("skuNo") String skuNo);

    default List<MtGoodsSku> selectSkuLsByGoodsId(Integer goodsId) {
        return selectList(new LambdaQueryWrapperX<MtGoodsSku>()
                .eqIfPresent(MtGoodsSku::getGoodsId, goodsId)
                .eq(MtGoodsSku::getStatus, StatusEnum.ENABLED.getKey())
        );
    }

    default MtGoodsSku selectLowestPriceSku(Integer goodsId) {
        return selectOne(new LambdaQueryWrapperX<MtGoodsSku>()
                .eqIfPresent(MtGoodsSku::getGoodsId, goodsId)
                .eq(MtGoodsSku::getStatus, StatusEnum.ENABLED.getKey())
                .orderByAsc(MtGoodsSku::getPrice)
                .last("limit 1")
        );
    }

    default void deleteByGoodsId(Integer goodsId) {
        delete(new LambdaQueryWrapperX<MtGoodsSku>()
                .eqIfPresent(MtGoodsSku::getGoodsId, goodsId)
        );
    }
}
