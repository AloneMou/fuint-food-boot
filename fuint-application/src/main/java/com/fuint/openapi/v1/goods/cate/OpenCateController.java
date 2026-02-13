package com.fuint.openapi.v1.goods.cate;

import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.dto.GoodsCateDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.CateService;
import com.fuint.common.service.SettingService;
import com.fuint.common.service.StoreService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.goods.cate.vo.*;
import com.fuint.repository.model.MtGoodsCate;
import com.fuint.repository.model.MtStore;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fuint.framework.util.string.StrUtils.isHttp;
import static com.fuint.openapi.enums.GoodsCateErrorCodeConstants.GOODS_CATE_NOT_FOUND;

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

    @Resource
    private SettingService settingService;


    @ApiOperation(value = "创建商品分类", notes = "创建一个新的商品分类")
    @PostMapping(value = "/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)创建商品分类")
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


    @ApiOperation(value = "更新商品分类", notes = "根据ID更新商品分类信息")
    @PutMapping(value = "/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)更新商品分类")
    public CommonResult<Boolean> updateCate(@Valid @RequestBody MtGoodsCateUpdateReqVO updateReqVO) throws BusinessCheckException {
        MtGoodsCate mtGoodsCate = BeanUtils.toBean(updateReqVO, MtGoodsCate.class);

        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(updateReqVO.getId());
        if (existCate == null) {
            return CommonResult.error(GOODS_CATE_NOT_FOUND);
        }

        cateService.updateCate(mtGoodsCate);
        return CommonResult.success(true);
    }

    @ApiOperation(value = "删除商品分类", notes = "根据ID删除商品分类（逻辑删除）")
    @DeleteMapping(value = "/delete/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)删除商品分类")
    public CommonResult<Boolean> deleteCate(
            @ApiParam(value = "分类ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {

        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(id);
        if (existCate == null) {
            return CommonResult.error(GOODS_CATE_NOT_FOUND);
        }

        cateService.deleteCate(id, "openapi");
        return CommonResult.success(true);
    }


    @ApiOperation(value = "获取商品分类详情", notes = "根据ID获取商品分类详细信息")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtGoodsCateRespVO> getCateDetail(
            @ApiParam(value = "分类ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {

        MtGoodsCate mtCate = cateService.queryCateById(id);
        if (mtCate == null) {
            return CommonResult.error(GOODS_CATE_NOT_FOUND);
        }

        MtGoodsCateRespVO respVO = BeanUtils.toBean(mtCate, MtGoodsCateRespVO.class);
        String imagePath = settingService.getUploadBasePath();
        // 设置店铺名称
        if (mtCate.getStoreId() != null && mtCate.getStoreId() > 0) {
            MtStore storeInfo = storeService.queryStoreById(mtCate.getStoreId());
            if (storeInfo != null) {
                respVO.setStoreName(storeInfo.getName());
            }
        }
        respVO.setLogo(isHttp(mtCate.getLogo(), imagePath));

        return CommonResult.success(respVO);
    }

    @ApiOperation(value = "分页查询商品分类列表", notes = "支持按名称、状态、店铺等条件分页查询")
    @PostMapping(value = "/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<MtGoodsCateRespVO>> getCatePage(@RequestBody @Valid MtGoodsCatePageReqVO pageReqVO) throws BusinessCheckException {
        String imagePath = settingService.getUploadBasePath();
        PageResult<MtGoodsCateRespVO> pageResult = cateService.getCatePage(pageReqVO);
        List<MtGoodsCateRespVO> list = pageResult.getList().stream().peek(item -> {
            MtGoodsCateRespVO respVO = BeanUtils.toBean(item, MtGoodsCateRespVO.class);
            respVO.setLogo(isHttp(item.getLogo(), imagePath));
        }).collect(Collectors.toList());
        pageResult.setList(list);
        return CommonResult.success(pageResult);
    }

    @ApiOperation(value = "获取所有启用的商品分类列表", notes = "获取所有状态为启用的商品分类，不分页")
    @GetMapping(value = "/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<List<MtGoodsCateListRespVO>> getCateList(
            @ApiParam(value = "商户ID", example = "1") @RequestParam(required = false) Integer merchantId,
            @ApiParam(value = "店铺ID", example = "1") @RequestParam(required = false) Integer storeId) throws BusinessCheckException {
        String imagePath = settingService.getUploadBasePath();
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
        List<MtGoodsCateListRespVO> respList = new ArrayList<>();
        for (MtGoodsCate cate : cateList) {
            MtGoodsCateListRespVO vo = BeanUtils.toBean(cate, MtGoodsCateListRespVO.class);
            // 设置店铺名称
            if (cate.getStoreId() != null && cate.getStoreId() > 0) {
                MtStore storeInfo = storeService.queryStoreById(cate.getStoreId());
                if (storeInfo != null) {
                    vo.setStoreName(storeInfo.getName());
                }
            }
            vo.setLogo(isHttp(cate.getLogo(), imagePath));
            respList.add(vo);
        }

        return CommonResult.success(respList);
    }

    /**
     * 更新商品分类状态
     *
     * @param id     分类ID
     * @param status 状态：A-正常；D-删除
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新商品分类状态", notes = "更新指定商品分类的状态")
    @PutMapping(value = "/status/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    @OperationServiceLog(description = "(OpenApi)更新商品分类状态")
    public CommonResult<Boolean> updateCateStatus(
            @ApiParam(value = "分类ID", required = true, example = "1") @PathVariable("id") Integer id,
            @ApiParam(value = "状态：A-正常；D-删除", required = true, example = "A") @RequestParam String status) throws BusinessCheckException {

        // 检查分类是否存在
        MtGoodsCate existCate = cateService.queryCateById(id);
        if (existCate == null) {
            return CommonResult.error(GOODS_CATE_NOT_FOUND);
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
