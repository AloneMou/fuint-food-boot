package com.fuint.openapi.v1.content.banner;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.service.BannerService;
import com.fuint.common.service.SettingService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.content.banner.vo.request.BannerPageReqVO;
import com.fuint.openapi.v1.content.banner.vo.response.BannerPageRespVO;
import com.fuint.openapi.v1.content.banner.vo.response.BannerRespVO;
import com.fuint.repository.model.MtBanner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fuint.framework.util.string.StrUtils.isHttp;

@Validated
@Api(tags = "OpenApi-Banner相关接口")
@RestController
@RequestMapping(value = "/api/v1/banner")
public class OpenBannerController extends BaseController {

    @Resource
    private BannerService bannerService;

    @Resource
    private SettingService settingService;

    @ApiOperation(value = "分页查询Banner列表")
    @GetMapping(value = "/page")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<BannerRespVO>> getBannerPage(@Valid BannerPageReqVO pageReqVO) throws BusinessCheckException {
        String imagePath = settingService.getUploadBasePath();
        PageResult<MtBanner> pageResult = bannerService.getBannerListByPage(pageReqVO);
        PageResult<BannerRespVO> result=BeanUtils.toBean(pageResult,BannerRespVO.class);
        List<BannerRespVO> list = result.getList().stream().map(item -> {
            BannerRespVO respVO = BeanUtils.toBean(item, BannerRespVO.class);
            respVO.setImage(isHttp(item.getImage(), imagePath));
            return respVO;
        }).collect(Collectors.toList());
        result.setList(list);
        return CommonResult.success(result);
    }

    @ApiOperation(value = "获取Banner详情")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<BannerRespVO> getBannerDetail(@PathVariable("id") Integer id) {
        MtBanner banner = bannerService.queryBannerById(id);
        if (banner == null) {
            return CommonResult.error(404, "Banner not found");
        }
        String imagePath = settingService.getUploadBasePath();
        return CommonResult.success(convertToRespVO(banner, imagePath));
    }

    private BannerRespVO convertToRespVO(MtBanner banner, String imagePath) {
        BannerRespVO respVO = new BannerRespVO();
        BeanUtil.copyProperties(banner, respVO);
        respVO.setImage(isHttp(banner.getImage(), imagePath));
        return respVO;
    }
}
