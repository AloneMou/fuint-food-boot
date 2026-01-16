package com.fuint.openapi.v1.goods.product;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.alibaba.fastjson.JSONArray;
import com.fuint.common.dto.*;
import com.fuint.common.enums.*;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.goods.product.vo.*;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.*;
import com.fuint.common.param.OrderListParam;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private MtGoodsSpecMapper mtGoodsSpecMapper;

    @Resource
    private MtGoodsSkuMapper mtGoodsSkuMapper;

    @Resource
    private GoodsService goodsService;

    @Resource
    private OrderService orderService;

    @Resource
    private MemberService memberService;

    @Resource
    private SettingService settingService;


    @ApiOperation(value = "创建商品", notes = "创建一个新的商品")
    @PostMapping(value = "/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Integer> createGoods(@Valid @RequestBody MtGoodsCreateReqVO createReqVO) throws BusinessCheckException {
        Integer goodsId = goodsService.createGoods(createReqVO);
        return CommonResult.success(goodsId);
    }

    /**
     * 更新商品
     *
     * @param updateReqVO 更新请求参数
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品", notes = "根据ID更新商品信息")
    @PutMapping(value = "/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> updateGoods(@Valid @RequestBody MtGoodsUpdateReqVO updateReqVO) throws BusinessCheckException {
        goodsService.updateGoods(updateReqVO);
        return CommonResult.success(true);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
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
            return CommonResult.error(404, "商品不存在");
        }

        goodsService.deleteGoods(id, "openapi");
        return CommonResult.success(true);
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取商品详情", notes = "根据ID获取商品详细信息，包括规格和SKU")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtGoodsRespVO> getGoodsDetail(
            @ApiParam(value = "商品ID", required = true, example = "1")
            @PathVariable("id") Integer id) {

        try {
            GoodsDto goodsDto = goodsService.getGoodsDetail(id, false);
            if (goodsDto == null) {
                return CommonResult.error(404, "商品不存在");
            }

            MtGoodsRespVO respVO = convertToRespVO(goodsDto);

            return CommonResult.success(respVO);
        } catch (Exception e) {
            return CommonResult.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询商品列表
     *
     * @param pageReqVO 分页查询参数
     * @return 商品列表
     * @throws BusinessCheckException 业务异常
     */
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
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotEmpty(pageReqVO.getName())) {
            params.put("name", pageReqVO.getName());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getGoodsNo())) {
            params.put("goodsNo", pageReqVO.getGoodsNo());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getType())) {
            params.put("type", pageReqVO.getType());
        }
        if (pageReqVO.getCateId() != null) {
            params.put("cateId", pageReqVO.getCateId().toString());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getStatus())) {
            params.put("status", pageReqVO.getStatus());
        }
        if (pageReqVO.getStoreId() != null) {
            params.put("storeId", pageReqVO.getStoreId().toString());
        }
        if (pageReqVO.getMerchantId() != null) {
            params.put("merchantId", pageReqVO.getMerchantId().toString());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getIsSingleSpec())) {
            params.put("isSingleSpec", pageReqVO.getIsSingleSpec());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getStock())) {
            params.put("stock", pageReqVO.getStock());
        }

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
            return CommonResult.error(404, "商品不存在");
        }

        // 更新状态
        MtGoods mtGoods = new MtGoods();
        mtGoods.setId(id);
        mtGoods.setStatus(status);
        mtGoods.setOperator("openapi");

        goodsService.saveGoods(mtGoods);
        return CommonResult.success(true);
    }

    /**
     * C端商品列表（支持动态价格计算）
     *
     * @param userId 用户ID（可选，用于计算个性化价格）
     * @param storeId 店铺ID（可选）
     * @param merchantId 商户ID（可选）
     * @param cateId 分类ID（可选）
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页数量
     * @return C端商品列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "C端商品列表（支持动态价格计算）", notes = "获取已上架、可点单的商品列表，包含动态价格（根据营销活动和用户优惠券计算）和划线价格")
    @GetMapping(value = "/c-end")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<CGoodsListPageRespVO> getCGoodsList(
            @ApiParam(value = "用户ID（用于计算个性化价格）", example = "1") @RequestParam(required = false) Integer userId,
            @ApiParam(value = "店铺ID", example = "1") @RequestParam(required = false) Integer storeId,
            @ApiParam(value = "商户ID", example = "1") @RequestParam(required = false) Integer merchantId,
            @ApiParam(value = "分类ID", example = "1") @RequestParam(required = false) Integer cateId,
            @ApiParam(value = "页码", example = "1") @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(required = false, defaultValue = "20") Integer pageSize) throws BusinessCheckException {

        try {
            // 1. 查询已上架商品
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
            paginationRequest.setCurrentPage(pageNo);
            paginationRequest.setPageSize(pageSize);
            paginationRequest.setSearchParams(params);

            PaginationResponse<GoodsDto> paginationResponse = goodsService.queryGoodsListByPagination(paginationRequest);

            // 2. 转换为C端商品列表，并计算动态价格
            List<CGoodsListRespVO> cGoodsList = new ArrayList<>();
            String basePath = settingService.getUploadBasePath();

            for (GoodsDto goodsDto : paginationResponse.getContent()) {
                CGoodsListRespVO cGoods = new CGoodsListRespVO();
                cGoods.setGoodsId(goodsDto.getId());
                cGoods.setName(goodsDto.getName());
                cGoods.setDescription(goodsDto.getDescription());
                cGoods.setStatus(goodsDto.getStatus());

                // 设置商品图片
                String logo = goodsDto.getLogo();
                if (StringUtil.isNotEmpty(logo) && !logo.startsWith("http")) {
                    logo = basePath + logo;
                }
                cGoods.setImageUrl(logo);

                // 处理单规格商品
                if (goodsDto.getIsSingleSpec() != null && goodsDto.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
                    // 单规格商品，直接使用商品价格
                    BigDecimal originalPrice = goodsDto.getPrice();
                    BigDecimal dynamicPrice = calculateDynamicPrice(merchantId != null ? merchantId : 1, userId, goodsDto.getId(), 0, originalPrice);
                    cGoods.setOriginalPrice(originalPrice);
                    cGoods.setDynamicPrice(dynamicPrice);
                    cGoods.setStock(goodsDto.getStock());
                } else {
                    // 多规格商品，处理SKU列表
                    List<CGoodsSkuVO> skuList = new ArrayList<>();
                    if (goodsDto.getSkuList() != null && !goodsDto.getSkuList().isEmpty()) {
                        for (MtGoodsSku sku : goodsDto.getSkuList()) {
                            CGoodsSkuVO cSku = new CGoodsSkuVO();
                            cSku.setSkuId(sku.getId());
                            cSku.setSkuNo(sku.getSkuNo());
                            cSku.setStock(sku.getStock());
                            cSku.setStatus(sku.getStatus());

                            // 设置SKU图片
                            String skuLogo = sku.getLogo();
                            if (StringUtil.isNotEmpty(skuLogo) && !skuLogo.startsWith("http")) {
                                skuLogo = basePath + skuLogo;
                            } else if (StringUtil.isEmpty(skuLogo)) {
                                skuLogo = logo;
                            }
                            cSku.setLogo(skuLogo);

                            // 计算动态价格
                            BigDecimal originalPrice = sku.getPrice();
                            BigDecimal dynamicPrice = calculateDynamicPrice(merchantId != null ? merchantId : 1, userId, goodsDto.getId(), sku.getId(), originalPrice);
                            cSku.setOriginalPrice(originalPrice);
                            cSku.setDynamicPrice(dynamicPrice);

                            // 获取规格信息
                            if (StringUtil.isNotEmpty(sku.getSpecIds())) {
                                List<GoodsSpecValueDto> specList = goodsService.getSpecListBySkuId(sku.getId());
                                Map<String, String> specs = new HashMap<>();
                                for (GoodsSpecValueDto spec : specList) {
                                    specs.put(spec.getSpecName(), spec.getSpecValue());
                                }
                                cSku.setSpecs(specs);
                            }

                            skuList.add(cSku);
                        }
                    }
                    cGoods.setSkus(skuList);
                }

                cGoodsList.add(cGoods);
            }

            // 3. 构建分页响应
            CGoodsListPageRespVO respVO = new CGoodsListPageRespVO();
            respVO.setPageNo(pageNo);
            respVO.setPageSize(pageSize);
            respVO.setTotalCount(paginationResponse.getTotalElements());
            respVO.setTotalPages(paginationResponse.getTotalPages());
            respVO.setItems(cGoodsList);

            return CommonResult.success(respVO);
        } catch (Exception e) {
            return CommonResult.error(500, "获取C端商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 计算商品动态价格
     * 根据用户优惠券和营销活动计算实际价格
     *
     * @param merchantId 商户ID
     * @param userId 用户ID（可为null）
     * @param goodsId 商品ID
     * @param skuId SKU ID（单规格商品为0）
     * @param originalPrice 原价
     * @return 动态价格
     */
    private BigDecimal calculateDynamicPrice(Integer merchantId, Integer userId, Integer goodsId, Integer skuId, BigDecimal originalPrice) {
        try {
            // 如果没有用户ID，直接返回原价
            if (userId == null || userId <= 0) {
                return originalPrice;
            }

            // 验证用户是否存在
            MtUser userInfo = memberService.queryMemberById(userId);
            if (userInfo == null) {
                return originalPrice;
            }

            // 构建购物车项（单个商品，数量为1）
            List<MtCart> cartList = new ArrayList<>();
            MtCart cart = new MtCart();
            cart.setGoodsId(goodsId);
            cart.setSkuId(skuId != null ? skuId : 0);
            cart.setNum(1);
            cart.setUserId(userId);
            cart.setStatus(StatusEnum.ENABLED.getKey());
            cartList.add(cart);

            // 调用价格计算逻辑（不使用优惠券，只计算基础价格和会员折扣）
            Map<String, Object> cartData = orderService.calculateCartGoods(
                    merchantId, userId, cartList, 0, false, "MP-WEIXIN", OrderModeEnum.ONESELF.getKey()
            );

            BigDecimal totalPrice = new BigDecimal(cartData.get("totalPrice").toString());
            // 动态价格 = 计算后的总价（已考虑会员折扣等）
            // 由于是单个商品，总价就是单价
            return totalPrice;
        } catch (Exception e) {
            // 计算失败，返回原价
            return originalPrice;
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
        if (StringUtil.isNotEmpty(goodsDto.getImages())) {
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
        respVO.setCouponIds(goodsDto.getCouponIds());
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
                specItem.setId(spec.getId());
                specItem.setName(specName);
                specItem.setChild(new ArrayList<>());
                specMap.put(specName, specItem);
            }

            GoodsSpecChildVO child = new GoodsSpecChildVO();
            child.setId(spec.getId());
            child.setName(spec.getValue());
            child.setChecked(true);
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
        vo.setGoodsId(sku.getGoodsId());
        vo.setSpecIds(sku.getSpecIds());
        vo.setLogo(sku.getLogo());
        vo.setPrice(sku.getPrice());
        vo.setLinePrice(sku.getLinePrice());
        vo.setWeight(sku.getWeight());
        vo.setStock(sku.getStock());
        vo.setStatus(sku.getStatus());
        return vo;
    }
}
