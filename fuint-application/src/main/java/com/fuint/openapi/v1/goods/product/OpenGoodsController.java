package com.fuint.openapi.v1.goods.product;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.alibaba.fastjson.JSONArray;
import com.fuint.common.dto.CouponDto;
import com.fuint.common.dto.GoodsDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.framework.util.object.ObjectUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSkuVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecChildVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.openapi.v1.goods.product.vo.request.CGoodsListPageReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsCreateReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsPageReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsUpdateReqVO;
import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.openapi.v1.goods.product.vo.response.MtGoodsPageRespVO;
import com.fuint.openapi.v1.goods.product.vo.response.MtGoodsRespVO;
import com.fuint.repository.model.*;
import com.fuint.framework.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.framework.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.fuint.framework.util.collection.CollectionUtils.convertMap;
import static com.fuint.framework.util.string.StrUtils.splitToInt;
import static com.fuint.openapi.enums.GoodsErrorCodeConstants.GOODS_GET_DETAIL_FAILED;
import static com.fuint.openapi.enums.GoodsErrorCodeConstants.GOODS_NOT_FOUND;

/**
 * 商品管理controller
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-商品相关接口")
@RestController
@RequestMapping(value = "/api/v1/goods")
public class OpenGoodsController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OpenGoodsController.class);
    @Resource
    private GoodsService goodsService;

    @Resource
    private MemberService memberService;

    @Resource
    private UserCouponService userCouponService;

    @ApiOperation(value = "创建商品", notes = "创建一个新的商品")
    @PostMapping(value = "/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Integer> createGoods(@Valid @RequestBody MtGoodsCreateReqVO createReqVO) throws BusinessCheckException {
        Integer goodsId = goodsService.createGoods(createReqVO);
        return CommonResult.success(goodsId);
    }

    @ApiOperation(value = "更新商品", notes = "根据ID更新商品信息")
    @PostMapping(value = "/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> updateGoods(@Valid @RequestBody MtGoodsUpdateReqVO updateReqVO) throws BusinessCheckException {
        goodsService.updateGoods(updateReqVO);
        return CommonResult.success(true);
    }

    @ApiOperation(value = "删除商品", notes = "根据ID删除商品（逻辑删除）")
    @DeleteMapping(value = "/delete/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> deleteGoods(
            @ApiParam(value = "商品ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {
        // 检查商品是否存在
        MtGoods existGoods = goodsService.queryGoodsById(id);
        if (existGoods == null) {
            return CommonResult.error(GOODS_NOT_FOUND);
        }
        goodsService.deleteGoods(id, "openapi");
        return CommonResult.success(true);
    }

    @ApiOperation(value = "获取商品详情", notes = "根据ID获取商品详细信息，包括规格和SKU")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtGoodsRespVO> getGoodsDetail(@PathVariable("id") Integer id) {
        try {
            GoodsDto goodsDto = goodsService.getGoodsDetail(id, false);
            if (goodsDto == null) {
                return CommonResult.error(GOODS_NOT_FOUND);
            }
            MtGoodsRespVO respVO = convertToRespVO(goodsDto);
            return CommonResult.success(respVO);
        } catch (Exception e) {
            return CommonResult.error(GOODS_GET_DETAIL_FAILED);
        }
    }

    @ApiOperation(value = "分页查询商品列表", notes = "支持按名称、编码、类型、分类、状态等条件分页查询")
    @GetMapping(value = "/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtGoodsPageRespVO> getGoodsPage(@Valid MtGoodsPageReqVO pageReqVO) throws BusinessCheckException {
        // 构建分页请求
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(pageReqVO.getPage());
        paginationRequest.setPageSize(pageReqVO.getPageSize());
        // 构建查询参数
        Map<String, Object> params = BeanUtil.beanToMap(pageReqVO, false, true);
        paginationRequest.setSearchParams(params);
        // 执行查询
        PaginationResponse<GoodsDto> paginationResponse = goodsService.queryGoodsListByPagination(paginationRequest);
        // 构建响应
        MtGoodsPageRespVO respVO = new MtGoodsPageRespVO();
        // 转换数据
        List<MtGoodsRespVO> list = paginationResponse.getContent().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
        respVO.setList(list);
        respVO.setTotal(paginationResponse.getTotalElements());
        respVO.setTotalPages(paginationResponse.getTotalPages());
        respVO.setCurrentPage(pageReqVO.getPage());
        respVO.setPageSize(pageReqVO.getPageSize());
        return CommonResult.success(respVO);
    }

    /**
     * 获取所有启用的商品列表
     *
     * @param merchantId 商户ID（可选）
     * @param storeId    店铺ID（可选）
     * @param cateId     分类ID（可选）
     * @return 商品列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取所有启用的商品列表", notes = "获取所有状态为启用的商品，不分页")
    @GetMapping(value = "/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<List<MtGoodsRespVO>> getGoodsList(
            @ApiParam(value = "商户ID", example = "1") @RequestParam(required = false) Integer merchantId,
            @ApiParam(value = "店铺ID", example = "1") @RequestParam(required = false) Integer storeId,
            @ApiParam(value = "分类ID", example = "1") @RequestParam(required = false) Integer cateId) throws BusinessCheckException {

        Map<String, Object> params = new HashMap<>();
        params.put("status", StatusEnum.ENABLED.getKey());
        if (merchantId != null) {
            params.put("merchantId", merchantId.toString());
        }
        if (storeId != null) {
            params.put("storeId", storeId.toString());
        }
        if (cateId != null) {
            params.put("cateId", cateId.toString());
        }

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(1);
        paginationRequest.setPageSize(1000); // 获取所有数据
        paginationRequest.setSearchParams(params);

        PaginationResponse<GoodsDto> paginationResponse = goodsService.queryGoodsListByPagination(paginationRequest);

        // 转换为响应VO
        List<MtGoodsRespVO> respList = paginationResponse.getContent().stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());

        return CommonResult.success(respList);
    }

    /**
     * 更新商品状态
     *
     * @param id     商品ID
     * @param status 状态：A-正常；D-删除
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品状态", notes = "更新指定商品的状态")
    @PatchMapping(value = "/status/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> updateGoodsStatus(
            @ApiParam(value = "商品ID", required = true, example = "1") @PathVariable("id") Integer id,
            @ApiParam(value = "状态：A-正常；D-删除", required = true, example = "A") @RequestParam String status) throws BusinessCheckException {

        // 检查商品是否存在
        MtGoods existGoods = goodsService.queryGoodsById(id);
        if (existGoods == null) {
            return CommonResult.error(GOODS_NOT_FOUND);
        }
        // 更新状态
        MtGoods mtGoods = new MtGoods();
        mtGoods.setId(id);
        mtGoods.setStatus(status);
        mtGoods.setOperator("openapi");
        goodsService.saveGoods(mtGoods);
        return CommonResult.success(true);
    }

    @ApiOperation(value = "C端商品列表（支持动态价格计算）", notes = "获取已上架、可点单的商品列表，包含动态价格（根据营销活动和用户优惠券计算）和划线价格")
    @GetMapping(value = "/calculate-page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<CGoodsListRespVO>> getCGoodsList(@Valid CGoodsListPageReqVO pageReqVO) throws BusinessCheckException {
        Integer userId = ObjectUtils.defaultIfNull(pageReqVO.getUserId(), 0);
        memberService.checkMemberExist(userId);
        if (pageReqVO.getPageSize() > 20) {
            return CommonResult.error(BAD_REQUEST.getCode(), "每页条数不能大于20");
        }
        // 1. 查询已上架商品
        PageResult<MtGoods> pageResult = goodsService.queryGoodsList(pageReqVO);
        List<MtGoods> goodsList = pageResult.getList();
        List<CGoodsListRespVO> goodsLs = new ArrayList<>();
        for (MtGoods goods : goodsList) {
            CGoodsListRespVO cGoods = BeanUtils.toBean(goods, CGoodsListRespVO.class);
            cGoods.setImageUrl(goods.getLogo());
            cGoods.setGoodsId(goods.getId());
            if (StrUtil.equals(YesOrNoEnum.YES.getKey(), goods.getIsSingleSpec())) {
                // 单规格商品，直接使用商品价格
                BigDecimal originalPrice = goods.getPrice();
                BigDecimal dynamicPrice = calculateDynamicPrice(userId, goods.getId(), originalPrice);
                cGoods.setOriginalPrice(originalPrice);
                cGoods.setDynamicPrice(dynamicPrice);
                cGoods.setStock(goods.getStock());
            } else {
                MtGoodsSku sku = goodsService.getLowestPriceSku(goods.getId());
                if (sku == null) {
                    log.error("商品ID：{}，没有找到最低价格SKU", goods.getId());
                    continue;
                }
                // 计算动态价格
                BigDecimal originalPrice = sku.getPrice();
                BigDecimal dynamicPrice = calculateDynamicPrice(userId, goods.getId(), originalPrice);
                cGoods.setOriginalPrice(originalPrice);
                cGoods.setDynamicPrice(dynamicPrice);
                cGoods.setDefaultSkuId(sku.getId());
            }
            goodsLs.add(cGoods);
        }
        PageResult<CGoodsListRespVO> respVO = new PageResult<>();
        respVO.setTotal(pageResult.getTotal());
        respVO.setTotalPages(pageResult.getTotalPages());
        respVO.setCurrentPage(pageResult.getCurrentPage());
        respVO.setPageSize(pageResult.getPageSize());
        respVO.setList(goodsLs);
        return CommonResult.success(respVO);
    }


    /**
     * 计算商品动态价格
     * 根据用户优惠券和营销活动计算实际价格
     *
     * @param userId        用户ID（可为null）
     * @param goodsId       商品ID
     * @param originalPrice 原价
     * @return 动态价格
     */
    private BigDecimal calculateDynamicPrice(Integer userId, Integer goodsId, BigDecimal originalPrice) {
        try {
            CouponDto couponDto = userCouponService.getBestCouponByGoodsAndAmount(userId, goodsId, originalPrice);
            return NumberUtil.sub(originalPrice, couponDto != null ? couponDto.getAmount() : BigDecimal.ZERO);
        } catch (Exception e) {
            // 计算失败，返回原价
            return originalPrice;
        } finally {
            userCouponService.clear();
        }
    }

    /**
     * 转换GoodsDto为响应VO
     */
    private MtGoodsRespVO convertToRespVO(GoodsDto goodsDto) {
        MtGoodsRespVO respVO = new MtGoodsRespVO();

        // 基本信息
        respVO.setId(goodsDto.getId());
        respVO.setName(goodsDto.getName());
        respVO.setGoodsNo(goodsDto.getGoodsNo());
        respVO.setType(goodsDto.getType());
        respVO.setCateId(goodsDto.getCateId());

        // 从关联对象中获取分类名称
        if (goodsDto.getCateInfo() != null) {
            respVO.setCateName(goodsDto.getCateInfo().getName());
        }

        // 店铺信息
        respVO.setStoreId(goodsDto.getStoreId());
        if (goodsDto.getStoreInfo() != null) {
            respVO.setStoreName(goodsDto.getStoreInfo().getName());
            respVO.setMerchantId(goodsDto.getStoreInfo().getMerchantId());
        }

        respVO.setDescription(goodsDto.getDescription());
        respVO.setLogo(goodsDto.getLogo());

        // 图片列表
        if (StringUtils.isNotEmpty(goodsDto.getImages())) {
            try {
                List<String> imageList = JSONArray.parseArray(goodsDto.getImages(), String.class);
                respVO.setImages(imageList);
            } catch (Exception e) {
                respVO.setImages(new ArrayList<>());
            }
        } else {
            respVO.setImages(new ArrayList<>());
        }

        // 价格和库存
        respVO.setPrice(goodsDto.getPrice());
        respVO.setLinePrice(goodsDto.getLinePrice());
        respVO.setWeight(goodsDto.getWeight());
        respVO.setStock(goodsDto.getStock());
        respVO.setInitSale(goodsDto.getInitSale());
        // 销量 = 初始销量（实际销量在MtGoods中可能是计算得出的）
        respVO.setSaleNum(goodsDto.getInitSale());

        // 其他属性
        respVO.setSalePoint(goodsDto.getSalePoint());
        respVO.setCanUsePoint(goodsDto.getCanUsePoint());
        respVO.setIsMemberDiscount(goodsDto.getIsMemberDiscount());
        respVO.setIsSingleSpec(goodsDto.getIsSingleSpec());
        respVO.setServiceTime(goodsDto.getServiceTime());
        respVO.setCouponIds(splitToInt(goodsDto.getCouponIds(), ","));
        respVO.setSort(goodsDto.getSort());
        respVO.setStatus(goodsDto.getStatus());
        respVO.setCreateTime(goodsDto.getCreateTime());
        respVO.setUpdateTime(goodsDto.getUpdateTime());
        respVO.setOperator(goodsDto.getOperator());

        // 规格和SKU处理
        if (goodsDto.getSpecList() != null && !goodsDto.getSpecList().isEmpty()) {
            List<GoodsSpecItemVO> specData = buildSpecData(goodsDto.getSpecList());
            respVO.setSpecData(specData);
        }

        if (goodsDto.getSkuList() != null && !goodsDto.getSkuList().isEmpty()) {
            List<GoodsSkuVO> skuData = goodsDto.getSkuList().stream()
                    .map(this::convertToSkuVO)
                    .collect(Collectors.toList());
            respVO.setSkuData(skuData);
        }

        return respVO;
    }

    /**
     * 构建规格数据
     */
    private List<GoodsSpecItemVO> buildSpecData(List<MtGoodsSpec> specList) {
        Map<String, GoodsSpecItemVO> specMap = new LinkedHashMap<>();

        for (MtGoodsSpec spec : specList) {
            String specName = spec.getName();
            GoodsSpecItemVO specItem = specMap.get(specName);

            if (specItem == null) {
                specItem = new GoodsSpecItemVO();
//                specItem.setId(spec.getId());
                specItem.setName(specName);
                specItem.setChild(new ArrayList<>());
                specMap.put(specName, specItem);
            }

            GoodsSpecChildVO child = new GoodsSpecChildVO();
            child.setId(spec.getId());
            child.setName(spec.getValue());
            specItem.getChild().add(child);
        }

        return new ArrayList<>(specMap.values());
    }

    /**
     * 转换MtGoodsSku为VO
     */
    private GoodsSkuVO convertToSkuVO(MtGoodsSku sku) {
        GoodsSkuVO vo = new GoodsSkuVO();
        vo.setId(sku.getId());
        vo.setSkuNo(sku.getSkuNo());
        vo.setSpecIds(sku.getSpecIds());
        vo.setLogo(sku.getLogo());
        vo.setPrice(sku.getPrice());
        vo.setLinePrice(sku.getLinePrice());
        vo.setWeight(sku.getWeight());
        vo.setStock(sku.getStock());
        return vo;
    }
}
