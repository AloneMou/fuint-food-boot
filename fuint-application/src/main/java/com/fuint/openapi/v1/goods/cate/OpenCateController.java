package com.fuint.openapi.v1.goods.cate;

import com.fuint.common.dto.GoodsCateDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.CateService;
import com.fuint.common.service.StoreService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.goods.cate.vo.*;
import com.fuint.repository.model.MtGoodsCate;
import com.fuint.repository.model.MtStore;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类管理controller
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-商品分类相关接口")
@RestController
@RequestMapping(value = "/api/v1/goods/cate")
public class OpenCateController extends BaseController {

    @Resource
    private CateService cateService;

    @Resource
    private StoreService storeService;

    /**
     * 创建商品分类
     *
     * @param cateCreateReqVO 创建请求参数
     * @return 分类ID
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "创建商品分类", notes = "创建一个新的商品分类")
    @PostMapping(value = "/create")
    public CommonResult<Integer> createCate(@Valid @RequestBody MtGoodsCateCreateReqVO cateCreateReqVO) throws BusinessCheckException {
        MtGoodsCate mtGoodsCate = BeanUtils.toBean(cateCreateReqVO, MtGoodsCate.class);
        
        // 设置默认值
        if (mtGoodsCate.getMerchantId() == null) {
            mtGoodsCate.setMerchantId(1);
        }
        if (mtGoodsCate.getStoreId() == null) {
            mtGoodsCate.setStoreId(0);
        }
        if (mtGoodsCate.getSort() == null) {
            mtGoodsCate.setSort(0);
        }
        
        MtGoodsCate cate = cateService.addCate(mtGoodsCate);
        return CommonResult.success(cate.getId());
    }

    /**
     * 更新商品分类
     *
     * @param updateReqVO 更新请求参数
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品分类", notes = "根据ID更新商品分类信息")
    @PutMapping(value = "/update")
    public CommonResult<Boolean> updateCate(@Valid @RequestBody MtGoodsCateUpdateReqVO updateReqVO) throws BusinessCheckException {
        MtGoodsCate mtGoodsCate = BeanUtils.toBean(updateReqVO, MtGoodsCate.class);
        
        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(updateReqVO.getId());
        if (existCate == null) {
            return CommonResult.error(404, "商品分类不存在");
        }
        
        cateService.updateCate(mtGoodsCate);
        return CommonResult.success(true);
    }

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "删除商品分类", notes = "根据ID删除商品分类（逻辑删除）")
    @DeleteMapping(value = "/delete/{id}")
    public CommonResult<Boolean> deleteCate(
            @ApiParam(value = "分类ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {
        
        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(id);
        if (existCate == null) {
            return CommonResult.error(404, "商品分类不存在");
        }
        
        cateService.deleteCate(id, "openapi");
        return CommonResult.success(true);
    }

    /**
     * 获取商品分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取商品分类详情", notes = "根据ID获取商品分类详细信息")
    @GetMapping(value = "/detail/{id}")
    public CommonResult<MtGoodsCateRespVO> getCateDetail(
            @ApiParam(value = "分类ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {
        
        MtGoodsCate mtCate = cateService.queryCateById(id);
        if (mtCate == null) {
            return CommonResult.error(404, "商品分类不存在");
        }
        
        MtGoodsCateRespVO respVO = BeanUtils.toBean(mtCate, MtGoodsCateRespVO.class);
        
        // 设置店铺名称
        if (mtCate.getStoreId() != null && mtCate.getStoreId() > 0) {
            MtStore storeInfo = storeService.queryStoreById(mtCate.getStoreId());
            if (storeInfo != null) {
                respVO.setStoreName(storeInfo.getName());
            }
        }
        
        return CommonResult.success(respVO);
    }

    /**
     * 分页查询商品分类列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分类列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "分页查询商品分类列表", notes = "支持按名称、状态、店铺等条件分页查询")
    @GetMapping(value = "/page")
    public CommonResult<MtGoodsCatePageRespVO> getCatePage(@Valid MtGoodsCatePageReqVO pageReqVO) throws BusinessCheckException {
        
        // 构建分页请求
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(pageReqVO.getPage());
        paginationRequest.setPageSize(pageReqVO.getPageSize());
        
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotEmpty(pageReqVO.getName())) {
            params.put("name", pageReqVO.getName());
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
        
        paginationRequest.setSearchParams(params);
        
        // 执行查询
        PaginationResponse<GoodsCateDto> paginationResponse = cateService.queryCateListByPagination(paginationRequest);
        
        // 构建响应
        MtGoodsCatePageRespVO respVO = new MtGoodsCatePageRespVO();
        
        // 转换数据
        List<MtGoodsCateRespVO> list = paginationResponse.getContent().stream()
                .map(dto -> {
                    MtGoodsCateRespVO vo = new MtGoodsCateRespVO();
                    vo.setId(dto.getId());
                    vo.setMerchantId(dto.getMerchantId());
                    vo.setStoreId(dto.getStoreId());
                    vo.setStoreName(dto.getStoreName());
                    vo.setName(dto.getName());
                    vo.setLogo(dto.getLogo());
                    vo.setDescription(dto.getDescription());
                    vo.setSort(dto.getSort());
                    vo.setStatus(dto.getStatus());
                    vo.setCreateTime(dto.getCreateTime());
                    vo.setUpdateTime(dto.getUpdateTime());
                    vo.setOperator(dto.getOperator());
                    return vo;
                })
                .collect(Collectors.toList());
        
        respVO.setList(list);
        respVO.setTotal(paginationResponse.getTotalElements());
        respVO.setTotalPages(paginationResponse.getTotalPages());
        respVO.setCurrentPage(pageReqVO.getPage());
        respVO.setPageSize(pageReqVO.getPageSize());
        
        return CommonResult.success(respVO);
    }

    /**
     * 获取所有启用的商品分类列表
     *
     * @param merchantId 商户ID（可选）
     * @param storeId 店铺ID（可选）
     * @return 分类列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取所有启用的商品分类列表", notes = "获取所有状态为启用的商品分类，不分页")
    @GetMapping(value = "/list")
    public CommonResult<List<MtGoodsCateRespVO>> getCateList(
            @ApiParam(value = "商户ID", example = "1") @RequestParam(required = false) Integer merchantId,
            @ApiParam(value = "店铺ID", example = "1") @RequestParam(required = false) Integer storeId) throws BusinessCheckException {
        
        Map<String, Object> params = new HashMap<>();
        params.put("status", StatusEnum.ENABLED.getKey());
        if (merchantId != null) {
            params.put("merchantId", merchantId.toString());
        }
        if (storeId != null) {
            params.put("storeId", storeId.toString());
        }
        
        List<MtGoodsCate> cateList = cateService.queryCateListByParams(params);
        
        // 转换为响应VO
        List<MtGoodsCateRespVO> respList = new java.util.ArrayList<>();
        for (MtGoodsCate cate : cateList) {
            MtGoodsCateRespVO vo = BeanUtils.toBean(cate, MtGoodsCateRespVO.class);
            // 设置店铺名称
            if (cate.getStoreId() != null && cate.getStoreId() > 0) {
                MtStore storeInfo = storeService.queryStoreById(cate.getStoreId());
                if (storeInfo != null) {
                    vo.setStoreName(storeInfo.getName());
                }
            }
            respList.add(vo);
        }
        
        return CommonResult.success(respList);
    }

    /**
     * 更新商品分类状态
     *
     * @param id 分类ID
     * @param status 状态：A-正常；D-删除
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品分类状态", notes = "更新指定商品分类的状态")
    @PatchMapping(value = "/status/{id}")
    public CommonResult<Boolean> updateCateStatus(
            @ApiParam(value = "分类ID", required = true, example = "1") @PathVariable("id") Integer id,
            @ApiParam(value = "状态：A-正常；D-删除", required = true, example = "A") @RequestParam String status) throws BusinessCheckException {
        
        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(id);
        if (existCate == null) {
            return CommonResult.error(404, "商品分类不存在");
        }
        
        // 更新状态
        MtGoodsCate mtCate = new MtGoodsCate();
        mtCate.setId(id);
        mtCate.setStatus(status);
        mtCate.setOperator("openapi");
        
        cateService.updateCate(mtCate);
        return CommonResult.success(true);
    }
}
