package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtGoodsSpec;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

    default List<MtGoodsSpec> selectByGoodsId(Integer goodsId) {
        return selectList(MtGoodsSpec::getGoodsId, goodsId, MtGoodsSpec::getStatus, StatusEnum.ENABLED.getKey());
    }
}
