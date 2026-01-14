package com.fuint.openapi.v1.member.group;

import com.fuint.common.Constants;
import com.fuint.common.dto.MemberGroupDto;
import com.fuint.common.dto.UserGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.MemberGroupService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.util.object.BeanUtils;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.member.group.vo.*;
import com.fuint.repository.model.MtUserGroup;
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
 * OpenApi会员分组相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-会员分组相关接口")
@RestController
@RequestMapping(value = "/api/v1/member/group")
public class OpenMemberGroupController extends BaseController {

    @Resource
    private MemberGroupService memberGroupService;

    /**
     * 创建会员分组
     *
     * @param createReqVO 创建请求参数
     * @return 分组ID
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "创建会员分组", notes = "创建一个新的会员分组")
    @PostMapping(value = "/create")
    public CommonResult<Integer> createMemberGroup(@Valid @RequestBody MtMemberGroupCreateReqVO createReqVO) throws BusinessCheckException {
        MemberGroupDto memberGroupDto = BeanUtils.toBean(createReqVO, MemberGroupDto.class);
        
        // 设置默认值
        if (memberGroupDto.getMerchantId() == null) {
            memberGroupDto.setMerchantId(1);
        }
        if (memberGroupDto.getStoreId() == null) {
            memberGroupDto.setStoreId(0);
        }
        if (memberGroupDto.getParentId() == null) {
            memberGroupDto.setParentId(0);
        }
        
        memberGroupDto.setOperator("openapi");
        
        MtUserGroup group = memberGroupService.addMemberGroup(memberGroupDto);
        return CommonResult.success(group.getId());
    }

    /**
     * 更新会员分组
     *
     * @param updateReqVO 更新请求参数
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新会员分组", notes = "根据ID更新会员分组信息")
    @PutMapping(value = "/update")
    public CommonResult<Boolean> updateMemberGroup(@Valid @RequestBody MtMemberGroupUpdateReqVO updateReqVO) throws BusinessCheckException {
        // 检查分组是否存在
        MtUserGroup existGroup = memberGroupService.queryMemberGroupById(updateReqVO.getId());
        if (existGroup == null) {
            return CommonResult.error(404, "会员分组不存在");
        }
        
        MemberGroupDto memberGroupDto = BeanUtils.toBean(updateReqVO, MemberGroupDto.class);
        memberGroupDto.setOperator("openapi");
        
        memberGroupService.updateMemberGroup(memberGroupDto);
        return CommonResult.success(true);
    }

    /**
     * 删除会员分组
     *
     * @param id 分组ID
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "删除会员分组", notes = "根据ID删除会员分组（逻辑删除）")
    @DeleteMapping(value = "/delete/{id}")
    public CommonResult<Boolean> deleteMemberGroup(
            @ApiParam(value = "分组ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {
        
        // 检查分组是否存在
        MtUserGroup existGroup = memberGroupService.queryMemberGroupById(id);
        if (existGroup == null) {
            return CommonResult.error(404, "会员分组不存在");
        }
        
        memberGroupService.deleteMemberGroup(id, "openapi");
        return CommonResult.success(true);
    }

    /**
     * 获取会员分组详情
     *
     * @param id 分组ID
     * @return 分组详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取会员分组详情", notes = "根据ID获取会员分组详细信息")
    @GetMapping(value = "/detail/{id}")
    public CommonResult<MtMemberGroupRespVO> getMemberGroupDetail(
            @ApiParam(value = "分组ID", required = true, example = "1")
            @PathVariable("id") Integer id) throws BusinessCheckException {
        
        MtUserGroup mtUserGroup = memberGroupService.queryMemberGroupById(id);
        if (mtUserGroup == null) {
            return CommonResult.error(404, "会员分组不存在");
        }
        
        MtMemberGroupRespVO respVO = convertToRespVO(mtUserGroup);
        
        return CommonResult.success(respVO);
    }

    /**
     * 分页查询会员分组列表
     *
     * @param pageReqVO 分页查询参数
     * @return 分组列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "分页查询会员分组列表", notes = "支持按名称、状态等条件分页查询")
    @GetMapping(value = "/page")
    public CommonResult<MtMemberGroupPageRespVO> getMemberGroupPage(@Valid MtMemberGroupPageReqVO pageReqVO) throws BusinessCheckException {
        
        // 构建分页请求
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(pageReqVO.getPage());
        paginationRequest.setPageSize(pageReqVO.getPageSize());
        
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isNotEmpty(pageReqVO.getName())) {
            params.put("name", pageReqVO.getName());
        }
        if (pageReqVO.getId() != null) {
            params.put("id", pageReqVO.getId().toString());
        }
        if (StringUtil.isNotEmpty(pageReqVO.getStatus())) {
            params.put("status", pageReqVO.getStatus());
        }
        if (pageReqVO.getMerchantId() != null) {
            params.put("merchantId", pageReqVO.getMerchantId().toString());
        }
        if (pageReqVO.getStoreId() != null) {
            params.put("storeId", pageReqVO.getStoreId().toString());
        }
        
        paginationRequest.setSearchParams(params);
        
        // 执行查询
        PaginationResponse<UserGroupDto> paginationResponse = memberGroupService.queryMemberGroupListByPagination(paginationRequest);
        
        // 构建响应
        MtMemberGroupPageRespVO respVO = new MtMemberGroupPageRespVO();
        
        // 转换数据
        List<MtMemberGroupRespVO> list = paginationResponse.getContent().stream()
                .map(this::convertUserGroupDtoToRespVO)
                .collect(Collectors.toList());
        
        respVO.setList(list);
        respVO.setTotal(paginationResponse.getTotalElements());
        respVO.setTotalPages(paginationResponse.getTotalPages());
        respVO.setCurrentPage(pageReqVO.getPage());
        respVO.setPageSize(pageReqVO.getPageSize());
        
        return CommonResult.success(respVO);
    }

    /**
     * 获取所有启用的会员分组列表
     *
     * @param merchantId 商户ID（可选）
     * @param storeId 店铺ID（可选）
     * @return 分组列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "获取所有启用的会员分组列表", notes = "获取所有状态为启用的会员分组，不分页")
    @GetMapping(value = "/list")
    public CommonResult<List<MtMemberGroupRespVO>> getMemberGroupList(
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
        
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setCurrentPage(1);
        paginationRequest.setPageSize(Constants.MAX_ROWS);
        paginationRequest.setSearchParams(params);
        
        PaginationResponse<UserGroupDto> paginationResponse = memberGroupService.queryMemberGroupListByPagination(paginationRequest);
        
        // 转换为响应VO
        List<MtMemberGroupRespVO> respList = paginationResponse.getContent().stream()
                .map(this::convertUserGroupDtoToRespVO)
                .collect(Collectors.toList());
        
        return CommonResult.success(respList);
    }

    /**
     * 更新会员分组状态
     *
     * @param id 分组ID
     * @param status 状态：A-正常；N-禁用；D-删除
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "更新会员分组状态", notes = "更新指定会员分组的状态")
    @PatchMapping(value = "/status/{id}")
    public CommonResult<Boolean> updateMemberGroupStatus(
            @ApiParam(value = "分组ID", required = true, example = "1") @PathVariable("id") Integer id,
            @ApiParam(value = "状态：A-正常；N-禁用；D-删除", required = true, example = "A") @RequestParam String status) throws BusinessCheckException {
        
        // 检查分组是否存在
        MtUserGroup existGroup = memberGroupService.queryMemberGroupById(id);
        if (existGroup == null) {
            return CommonResult.error(404, "会员分组不存在");
        }
        
        // 更新状态
        MemberGroupDto memberGroupDto = new MemberGroupDto();
        memberGroupDto.setId(id);
        memberGroupDto.setStatus(status);
        memberGroupDto.setOperator("openapi");
        
        memberGroupService.updateMemberGroup(memberGroupDto);
        return CommonResult.success(true);
    }

    /**
     * 转换MtUserGroup为响应VO
     */
    private MtMemberGroupRespVO convertToRespVO(MtUserGroup mtUserGroup) {
        return BeanUtils.toBean(mtUserGroup, MtMemberGroupRespVO.class);
    }

    /**
     * 转换UserGroupDto为响应VO
     */
    private MtMemberGroupRespVO convertUserGroupDtoToRespVO(UserGroupDto userGroupDto) {
        MtMemberGroupRespVO respVO = BeanUtils.toBean(userGroupDto, MtMemberGroupRespVO.class);
        
        // 处理子分组
        if (userGroupDto.getChildren() != null && !userGroupDto.getChildren().isEmpty()) {
            List<MtMemberGroupRespVO> children = userGroupDto.getChildren().stream()
                    .map(this::convertUserGroupDtoToRespVO)
                    .collect(Collectors.toList());
            respVO.setChildren(children);
        }
        
        return respVO;
    }
}
