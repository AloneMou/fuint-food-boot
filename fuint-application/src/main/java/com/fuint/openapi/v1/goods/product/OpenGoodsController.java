package com.fuint.openapi.v1.goods.product;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fuint.common.dto.*;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
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
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags="OpenApi-商品相关接口")
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
    private CateService cateService;

    @Resource
    private StoreService storeService;

    @Resource
    private SettingService settingService;

    /**
     * 创建商品
     *
     * @param createReqVO 创建请求参数
     * @return 商品ID
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "创建商品", notes = "创建一个新的商品")
    @PostMapping(value = "/create")
    public CommonResult<Integer> createGoods(@Valid @RequestBody MtGoodsCreateReqVO createReqVO) throws BusinessCheckException {
        MtGoods mtGoods = new MtGoods();
        
        // 基本信息
        mtGoods.setName(createReqVO.getName());
        mtGoods.setGoodsNo(createReqVO.getGoodsNo());
        mtGoods.setType(createReqVO.getType());
        mtGoods.setCateId(createReqVO.getCateId());
        mtGoods.setDescription(createReqVO.getDescription());
        
        // 价格和库存
        mtGoods.setPrice(createReqVO.getPrice());
        mtGoods.setLinePrice(createReqVO.getLinePrice());
        mtGoods.setWeight(createReqVO.getWeight());
        mtGoods.setStock(createReqVO.getStock() != null ? createReqVO.getStock() : 0);
        mtGoods.setInitSale(createReqVO.getInitSale() != null ? createReqVO.getInitSale() : 0);
        
        // 其他属性
        mtGoods.setSalePoint(createReqVO.getSalePoint());
        mtGoods.setCanUsePoint(createReqVO.getCanUsePoint() != null ? createReqVO.getCanUsePoint() : YesOrNoEnum.YES.getKey());
        mtGoods.setIsMemberDiscount(createReqVO.getIsMemberDiscount() != null ? createReqVO.getIsMemberDiscount() : YesOrNoEnum.YES.getKey());
        mtGoods.setIsSingleSpec(createReqVO.getIsSingleSpec() != null ? createReqVO.getIsSingleSpec() : YesOrNoEnum.YES.getKey());
        mtGoods.setServiceTime(createReqVO.getServiceTime());
        mtGoods.setCouponIds(createReqVO.getCouponIds());
        mtGoods.setSort(createReqVO.getSort() != null ? createReqVO.getSort() : 0);
        
        // 商户和店铺
        if (createReqVO.getMerchantId() != null) {
            mtGoods.setMerchantId(createReqVO.getMerchantId());
        } else {
            mtGoods.setMerchantId(1);
        }
        if (createReqVO.getStoreId() != null) {
            mtGoods.setStoreId(createReqVO.getStoreId());
        } else {
            mtGoods.setStoreId(0);
        }
        
        // 图片处理
        if (createReqVO.getImages() != null && !createReqVO.getImages().isEmpty()) {
            mtGoods.setLogo(createReqVO.getImages().get(0));
            mtGoods.setImages(JSONObject.toJSONString(createReqVO.getImages()));
        } else if (StringUtil.isNotEmpty(createReqVO.getLogo())) {
            mtGoods.setLogo(createReqVO.getLogo());
            List<String> imageList = new ArrayList<>();
            imageList.add(createReqVO.getLogo());
            mtGoods.setImages(JSONObject.toJSONString(imageList));
        }
        
        mtGoods.setStatus(StatusEnum.ENABLED.getKey());
        mtGoods.setOperator("openapi");
        
        // 保存商品
        MtGoods savedGoods = goodsService.saveGoods(mtGoods);
        
        // 保存SKU和规格
        if (createReqVO.getSkuData() != null && !createReqVO.getSkuData().isEmpty()) {
            saveGoodsSkus(savedGoods.getId(), createReqVO.getSkuData());
        }
        
        return CommonResult.success(savedGoods.getId());
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
    public CommonResult<Boolean> updateGoods(@Valid @RequestBody MtGoodsUpdateReqVO updateReqVO) throws BusinessCheckException {
        // 检查商品是否存在
        MtGoods existGoods = goodsService.queryGoodsById(updateReqVO.getId());
        if (existGoods == null) {
            return CommonResult.error(404, "商品不存在");
        }
        
        MtGoods mtGoods = new MtGoods();
        mtGoods.setId(updateReqVO.getId());
        
        // 只更新传入的字段
        if (StringUtil.isNotEmpty(updateReqVO.getName())) {
            mtGoods.setName(updateReqVO.getName());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getGoodsNo())) {
            mtGoods.setGoodsNo(updateReqVO.getGoodsNo());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getType())) {
            mtGoods.setType(updateReqVO.getType());
        }
        if (updateReqVO.getCateId() != null) {
            mtGoods.setCateId(updateReqVO.getCateId());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getDescription())) {
            mtGoods.setDescription(updateReqVO.getDescription());
        }
        if (updateReqVO.getPrice() != null) {
            mtGoods.setPrice(updateReqVO.getPrice());
        }
        if (updateReqVO.getLinePrice() != null) {
            mtGoods.setLinePrice(updateReqVO.getLinePrice());
        }
        if (updateReqVO.getWeight() != null) {
            mtGoods.setWeight(updateReqVO.getWeight());
        }
        if (updateReqVO.getStock() != null) {
            mtGoods.setStock(updateReqVO.getStock());
        }
        if (updateReqVO.getInitSale() != null) {
            mtGoods.setInitSale(updateReqVO.getInitSale());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getSalePoint())) {
            mtGoods.setSalePoint(updateReqVO.getSalePoint());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getCanUsePoint())) {
            mtGoods.setCanUsePoint(updateReqVO.getCanUsePoint());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getIsMemberDiscount())) {
            mtGoods.setIsMemberDiscount(updateReqVO.getIsMemberDiscount());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(updateReqVO.getIsSingleSpec());
        }
        if (updateReqVO.getServiceTime() != null) {
            mtGoods.setServiceTime(updateReqVO.getServiceTime());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getCouponIds())) {
            mtGoods.setCouponIds(updateReqVO.getCouponIds());
        }
        if (updateReqVO.getSort() != null) {
            mtGoods.setSort(updateReqVO.getSort());
        }
        if (StringUtil.isNotEmpty(updateReqVO.getStatus())) {
            mtGoods.setStatus(updateReqVO.getStatus());
        }
        if (updateReqVO.getMerchantId() != null) {
            mtGoods.setMerchantId(updateReqVO.getMerchantId());
        }
        if (updateReqVO.getStoreId() != null) {
            mtGoods.setStoreId(updateReqVO.getStoreId());
        }
        
        // 图片处理
        if (updateReqVO.getImages() != null && !updateReqVO.getImages().isEmpty()) {
            mtGoods.setLogo(updateReqVO.getImages().get(0));
            mtGoods.setImages(JSONObject.toJSONString(updateReqVO.getImages()));
        } else if (StringUtil.isNotEmpty(updateReqVO.getLogo())) {
            mtGoods.setLogo(updateReqVO.getLogo());
        }
        
        mtGoods.setOperator("openapi");
        
        // 更新商品
        goodsService.saveGoods(mtGoods);
        
        // 更新SKU和规格
        if (updateReqVO.getSkuData() != null && !updateReqVO.getSkuData().isEmpty()) {
            saveGoodsSkus(updateReqVO.getId(), updateReqVO.getSkuData());
        }
        
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
     * @param storeId 店铺ID（可选）
     * @param cateId 分类ID（可选）
     * @return 商品列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取所有启用的商品列表", notes = "获取所有状态为启用的商品，不分页")
    @GetMapping(value = "/list")
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
     * @param id 商品ID
     * @param status 状态：A-正常；D-删除
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品状态", notes = "更新指定商品的状态")
    @PatchMapping(value = "/status/{id}")
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
     * 保存商品SKU列表
     */
    private void saveGoodsSkus(Integer goodsId, List<GoodsSkuVO> skuList) {
        for (GoodsSkuVO skuVO : skuList) {
            Map<String, Object> params = new HashMap<>();
            params.put("goods_id", goodsId);
            params.put("spec_ids", skuVO.getSpecIds());
            
            // 查询是否已存在
            List<MtGoodsSku> existSkuList = mtGoodsSkuMapper.selectByMap(params);
            MtGoodsSku sku;
            if (existSkuList != null && !existSkuList.isEmpty()) {
                sku = existSkuList.get(0);
            } else {
                sku = new MtGoodsSku();
            }
            
            sku.setSkuNo(skuVO.getSkuNo());
            sku.setLogo(skuVO.getLogo());
            sku.setGoodsId(goodsId);
            sku.setSpecIds(skuVO.getSpecIds());
            sku.setStock(skuVO.getStock() != null ? skuVO.getStock() : 0);
            sku.setPrice(skuVO.getPrice() != null ? skuVO.getPrice() : BigDecimal.ZERO);
            sku.setLinePrice(skuVO.getLinePrice() != null ? skuVO.getLinePrice() : BigDecimal.ZERO);
            sku.setWeight(skuVO.getWeight() != null ? skuVO.getWeight() : BigDecimal.ZERO);
            sku.setStatus(StatusEnum.ENABLED.getKey());
            
            if (sku.getId() != null && sku.getId() > 0) {
                mtGoodsSkuMapper.updateById(sku);
            } else {
                mtGoodsSkuMapper.insert(sku);
            }
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
