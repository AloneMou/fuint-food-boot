package com.fuint.repository.mapper;

import com.fuint.repository.model.MtOrderGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品表 Mapper 接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtOrderGoodsMapper extends BaseMapper<MtOrderGoods> {

    /**
     * 批量统计订单商品数量
     * 
     * @param orderIds 订单ID列表
     * @return 商品总数量
     */
    Integer countGoodsByOrderIds(@Param("orderIds") List<Integer> orderIds);

}
