package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtGoodsSpec;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 规格表 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsSpecMapper extends BaseMapperX<MtGoodsSpec> {

    List<MtGoodsSpec> getGoodsSpecCountList(@Param("goodsId") Integer goodsId);

    int saveBatch(@Param("list") List<MtGoodsSpec> list);


    default List<MtGoodsSpec> selectByGoodsId(Integer goodsId) {
        return selectList(new LambdaQueryWrapperX<MtGoodsSpec>()
                .eqIfPresent(MtGoodsSpec::getGoodsId, goodsId)
                .eq(MtGoodsSpec::getStatus, StatusEnum.ENABLED.getKey())
        );
    }

    default void deleteByGoodsId(Integer goodsId) {
        delete(MtGoodsSpec::getGoodsId, goodsId);
    }

    default List<MtGoodsSpec> selectSpecLsByGoodsIds(List<Integer> goodsIds) {
        return selectList(new LambdaQueryWrapperX<MtGoodsSpec>()
                .inIfPresent(MtGoodsSpec::getGoodsId, goodsIds)
                .eq(MtGoodsSpec::getStatus, StatusEnum.ENABLED.getKey()));
    }

}
