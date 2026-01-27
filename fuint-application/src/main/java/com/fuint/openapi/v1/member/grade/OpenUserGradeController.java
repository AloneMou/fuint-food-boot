package com.fuint.openapi.v1.member.grade;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.UserGradeService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.repository.mapper.MtUserMapper;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.model.MtUserGrade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miao
 * @date 2026/1/27
 */
@Slf4j
@Validated
@Api(tags = "OpenApi-会员等级管理相关接口")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/v1/member/grade")
public class OpenUserGradeController extends BaseController {

    @Resource
    private UserGradeService userGradeService;

    @Resource
    private MtUserMapper mtUserMapper;

    @ApiOperation(value = "新增会员等级", notes = "新增一个会员等级")
    @PostMapping("/add")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserGrade> addUserGrade(@Valid @RequestBody MtUserGrade mtUserGrade) throws BusinessCheckException {
        log.info("Add user grade request: {}", mtUserGrade);
        if (StringUtils.isBlank(mtUserGrade.getName())) {
            throw new BusinessCheckException("等级名称不能为空");
        }
        if (mtUserGrade.getGrade() == null || mtUserGrade.getGrade() <= 0) {
            throw new BusinessCheckException("等级代码(grade)必须为正整数");
        }
        if (ObjectUtil.isEmpty(mtUserGrade.getMerchantId())) {
            throw new BusinessCheckException("商户ID不能为空");
        }
        // 设置默认状态
        if (StringUtils.isBlank(mtUserGrade.getStatus())) {
            mtUserGrade.setStatus(StatusEnum.ENABLED.getKey());
        }
        return CommonResult.success(userGradeService.addUserGrade(mtUserGrade));
    }

    @ApiOperation(value = "创建/更新会员等级", notes = "更新会员等级信息")
    @PutMapping("/update")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserGrade> updateUserGrade(@Valid @RequestBody MtUserGrade mtUserGrade) throws BusinessCheckException {
        log.info("Update user grade request: {}", mtUserGrade);
        if (ObjectUtil.isEmpty(mtUserGrade.getId())) {
            throw new BusinessCheckException("会员等级ID不能为空");
        }
        if (StringUtils.isBlank(mtUserGrade.getName())) {
            throw new BusinessCheckException("等级名称不能为空");
        }
        if (mtUserGrade.getGrade() == null || mtUserGrade.getGrade() <= 0) {
            throw new BusinessCheckException("等级代码(grade)必须为正整数");
        }

        return CommonResult.success(userGradeService.updateUserGrade(mtUserGrade));
    }

    @ApiOperation(value = "查询会员等级详情", notes = "根据ID查询会员等级详情")
    @GetMapping("/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<MtUserGrade> getUserGrade(@PathVariable("id") Integer id) throws BusinessCheckException {
        return CommonResult.success(userGradeService.queryUserGradeById(0, id, 0));
    }

    @ApiOperation(value = "分页查询会员等级列表", notes = "分页查询会员等级列表")
    @GetMapping("/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PaginationResponse<MtUserGrade>> getUserGradeList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "merchantId", required = false) Integer merchantId) throws BusinessCheckException {

        PaginationRequest request = new PaginationRequest();
        request.setCurrentPage(page);
        request.setPageSize(pageSize);
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(name)) {
            params.put("name", name);
        }
        if (StringUtils.isNotBlank(status)) {
            params.put("status", status);
        }
        if (merchantId != null) {
            params.put("merchantId", merchantId);
        }
        request.setSearchParams(params);

        return CommonResult.success(userGradeService.queryUserGradeListByPagination(request));
    }

    @ApiOperation(value = "删除会员等级", notes = "根据ID删除会员等级")
    @DeleteMapping("/delete/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> deleteUserGrade(@PathVariable("id") Integer id) throws BusinessCheckException {
        // 验证等级ID有效性
        MtUserGrade existGrade = userGradeService.queryUserGradeById(0, id, 0);
        if (existGrade == null) {
            throw new BusinessCheckException("会员等级不存在");
        }

        // 检查该等级是否已被会员使用
        LambdaQueryWrapper<MtUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MtUser::getGradeId, id.toString());
        wrapper.ne(MtUser::getStatus, StatusEnum.DISABLE.getKey());
        Integer count = mtUserMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessCheckException("该等级已被会员使用，无法删除");
        }
        userGradeService.deleteUserGrade(id, "openapi");
        return CommonResult.success(true);
    }
}
