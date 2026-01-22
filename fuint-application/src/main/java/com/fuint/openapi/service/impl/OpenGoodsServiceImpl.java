package com.fuint.openapi.service.impl;

import com.fuint.openapi.service.OpenGoodsService;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuRespVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.MtGoods;
import com.fuint.repository.model.MtGoodsSku;
import com.fuint.repository.model.MtGoodsSpec;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fuint.framework.util.collection.CollectionUtils.*;

/**
 * Foot-Fuint-Backend-master
 *
 * @author mjw
 * @since 2026/1/23 0:15
 */
@Service
public class OpenGoodsServiceImpl implements OpenGoodsService {

    @Resource
    private MtGoodsSkuMapper goodsSkuMapper;

    @Resource
    private MtGoodsSpecMapper goodsSpecMapper;

    @Override
    public List<CGoodsListRespVO> getGoodsList(List<MtGoods> goodsLs) {
        List<Integer> goodsIds = convertList(goodsLs, MtGoods::getId);
        List<MtGoodsSku> skuLs = goodsSkuMapper.selectSkuLsByGoodsIds(goodsIds);
        List<MtGoodsSpec> specLs = goodsSpecMapper.selectSpecLsByGoodsIds(goodsIds);
        Map<Integer, List<MtGoodsSku>> skuMap = convertMultiMap(skuLs, MtGoodsSku::getGoodsId);
        Map<Integer, List<MtGoodsSpec>> specMap = convertMultiMap(specLs, MtGoodsSpec::getGoodsId);

        List<CGoodsListRespVO> goodsList = new ArrayList<>();
        for (MtGoods goods : goodsLs) {
            CGoodsListRespVO goodsVO = new CGoodsListRespVO();
            BeanUtils.copyProperties(goods, goodsVO);
            List<MtGoodsSku> skuList = skuMap.getOrDefault(goods.getId(), Collections.emptyList());
            List<GoodsSkuRespVO> skuVOList = new ArrayList<>();
            for (MtGoodsSku sku : skuList) {
                GoodsSkuRespVO skuVO = new GoodsSkuRespVO();
                BeanUtils.copyProperties(sku, skuVO);
                skuVOList.add(skuVO);
            }
            goodsVO.setSkuData(skuVOList);
            goodsList.add(goodsVO);

            List<MtGoodsSpec> specList = specMap.getOrDefault(goods.getId(), Collections.emptyList());
            List<GoodsSpecItemVO> specVOList = new ArrayList<>();
            Map<String, List<MtGoodsSpec>> specNameMap = convertMultiMap(specList, MtGoodsSpec::getName);
            for (String key : specNameMap.keySet()) {
                GoodsSpecItemVO specVO = new GoodsSpecItemVO();
                specVO.setName(key);
                List<MtGoodsSpec> childLs = specNameMap.get(key);
            }

        }
        return goodsList;
    }
}
