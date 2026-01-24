package com.fuint.openapi.service.impl;

import com.fuint.common.enums.OrderModeEnum;
import com.fuint.common.enums.PlatformTypeEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.openapi.service.OpenGoodsService;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuRespVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecChildVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtGoods;
import com.fuint.repository.model.MtGoodsSku;
import com.fuint.repository.model.MtGoodsSpec;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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

    @Resource
    private OpenApiOrderService openApiOrderService;

    @Resource(name = "goodsPriceExecutor")
    private ThreadPoolExecutor goodsPriceExecutor;

    @Override
    public List<CGoodsListRespVO> getGoodsList(List<MtGoods> goodsLs, Integer userId) {
        List<Integer> goodsIds = convertList(goodsLs, MtGoods::getId);
        List<MtGoodsSku> skuLs = goodsSkuMapper.selectSkuLsByGoodsIds(goodsIds);
        List<MtGoodsSpec> specLs = goodsSpecMapper.selectSpecLsByGoodsIds(goodsIds);
        Map<Integer, List<MtGoodsSku>> skuMap = convertMultiMap(skuLs, MtGoodsSku::getGoodsId);
        Map<Integer, List<MtGoodsSpec>> specMap = convertMultiMap(specLs, MtGoodsSpec::getGoodsId);

        List<CompletableFuture<CGoodsListRespVO>> futures = goodsLs.stream()
                .map(goods -> CompletableFuture.supplyAsync(
                        () -> buildGoodsVO(goods, userId, skuMap, specMap),
                        goodsPriceExecutor
                ))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private CGoodsListRespVO buildGoodsVO(MtGoods goods, Integer userId,
                                          Map<Integer, List<MtGoodsSku>> skuMap,
                                          Map<Integer, List<MtGoodsSpec>> specMap) {
        CGoodsListRespVO goodsVO = new CGoodsListRespVO();
        BeanUtils.copyProperties(goods, goodsVO);
        if (goodsVO.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
            List<MtCart> cartList = new ArrayList<>();
            MtCart cart = new MtCart();
            cart.setGoodsId(goods.getId());
            cart.setMerchantId(goods.getMerchantId());
            cart.setStoreId(goods.getStoreId());
            cart.setUserId(userId);
            cart.setNum(1);
            cartList.add(cart);
            Map<String, Object> goodsPriceMap = openApiOrderService.calculateCartGoods(goods.getMerchantId(), userId, cartList, null, false, PlatformTypeEnum.OPEN_API.getCode(), OrderModeEnum.ONESELF.getKey());
            goodsVO.setDynamicPrice((BigDecimal) goodsPriceMap.get("payPrice"));
        }
        List<MtGoodsSku> skuList = skuMap.getOrDefault(goods.getId(), Collections.emptyList());
        List<GoodsSkuRespVO> skuVOList = new ArrayList<>();
        for (MtGoodsSku sku : skuList) {
            GoodsSkuRespVO skuVO = new GoodsSkuRespVO();
            BeanUtils.copyProperties(sku, skuVO);
            List<MtCart> cartList = new ArrayList<>();
            MtCart cart = new MtCart();
            cart.setGoodsId(goods.getId());
            cart.setSkuId(sku.getId());
            cart.setMerchantId(goods.getMerchantId());
            cart.setStoreId(goods.getStoreId());
            cart.setUserId(userId);
            cart.setNum(1);
            cartList.add(cart);
            Map<String, Object> priceMap = openApiOrderService.calculateCartGoods(goods.getMerchantId(), userId, cartList, null, false, PlatformTypeEnum.OPEN_API.getCode(), OrderModeEnum.ONESELF.getKey());
            skuVO.setDynamicPrice((BigDecimal) priceMap.get("payPrice"));
            skuVOList.add(skuVO);
        }
        goodsVO.setSkuData(skuVOList);
        List<MtGoodsSpec> specList = specMap.getOrDefault(goods.getId(), Collections.emptyList());
        List<GoodsSpecItemVO> specVOList = new ArrayList<>();
        Map<String, List<MtGoodsSpec>> specNameMap = convertMultiMap(specList, MtGoodsSpec::getName);
        for (String key : specNameMap.keySet()) {
            GoodsSpecItemVO specVO = new GoodsSpecItemVO();
            specVO.setName(key);
            List<MtGoodsSpec> childLs = specNameMap.get(key);
            List<GoodsSpecChildVO> childVOList = new ArrayList<>();
            for (MtGoodsSpec child : childLs) {
                GoodsSpecChildVO childVO = new GoodsSpecChildVO();
                BeanUtils.copyProperties(child, childVO);
                childVOList.add(childVO);
            }
            specVO.setChild(childVOList);
            specVOList.add(specVO);
        }
        goodsVO.setSpecData(specVOList);
        return goodsVO;
    }
}
