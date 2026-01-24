package com.fuint.openapi.service;

import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.repository.model.MtGoods;

import java.util.List;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/23 0:12
 */
public interface OpenGoodsService {

    /**
     * 商品列表
     *
     * @param goodsLs 商品列表
     * @return 商品列表
     */
    List<CGoodsListRespVO> getGoodsList(List<MtGoods> goodsLs, Integer userId);
}
