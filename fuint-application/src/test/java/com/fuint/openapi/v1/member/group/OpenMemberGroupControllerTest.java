package com.fuint.openapi.v1.member.group;

import com.fuint.common.dto.MemberGroupDto;
import com.fuint.common.dto.UserGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.MemberGroupService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.openapi.v1.member.group.vo.*;
import com.fuint.repository.model.MtUserGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpenMemberGroupController 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenMemberGroupControllerTest {

    @InjectMocks
    private OpenMemberGroupController openMemberGroupController;

    @Mock
    private MemberGroupService memberGroupService;

    private MtUserGroup mockGroup;
    private UserGroupDto mockGroupDto;

    @Before
    public void setUp() {
        // 初始化模拟分组
        mockGroup = new MtUserGroup();
        mockGroup.setId(1);
        mockGroup.setName("测试分组");
        mockGroup.setMerchantId(1);
        mockGroup.setStoreId(1);
        mockGroup.setParentId(0);
        mockGroup.setDescription("测试分组描述");
        mockGroup.setStatus(StatusEnum.ENABLED.getKey());
        mockGroup.setCreateTime(new Date());
        mockGroup.setUpdateTime(new Date());
        mockGroup.setOperator("admin");

        // 初始化模拟分组DTO
        mockGroupDto = new UserGroupDto();
        mockGroupDto.setId(1);
        mockGroupDto.setName("测试分组");
        mockGroupDto.setMerchantId(1);
        mockGroupDto.setStoreId(1);
        mockGroupDto.setParentId(0);
        mockGroupDto.setDescription("测试分组描述");
        mockGroupDto.setStatus(StatusEnum.ENABLED.getKey());
        mockGroupDto.setCreateTime(new Date());
        mockGroupDto.setUpdateTime(new Date());
        mockGroupDto.setOperator("admin");
    }

    /**
     * 测试创建会员分组 - 成功场景
     */
    @Test
    public void testCreateMemberGroup_Success() throws BusinessCheckException {
        // 准备请求参数
        MtMemberGroupCreateReqVO reqVO = new MtMemberGroupCreateReqVO();
        reqVO.setName("新分组");
        reqVO.setDescription("新分组描述");
//        reqVO.setStatus(StatusEnum.ENABLED.getKey());

        // Mock服务调用
        when(memberGroupService.addMemberGroup(any(MemberGroupDto.class))).thenReturn(mockGroup);

        // 执行测试
        CommonResult<Integer> result = openMemberGroupController.createMemberGroup(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockGroup.getId(), result.getData());

        // 验证服务调用
        verify(memberGroupService, times(1)).addMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试创建会员分组 - 默认值设置
     */
    @Test
    public void testCreateMemberGroup_WithDefaults() throws BusinessCheckException {
        // 准备请求参数（不设置merchantId和storeId）
        MtMemberGroupCreateReqVO reqVO = new MtMemberGroupCreateReqVO();
        reqVO.setName("新分组");

        // Mock服务调用
        when(memberGroupService.addMemberGroup(any(MemberGroupDto.class))).thenReturn(mockGroup);

        // 执行测试
        CommonResult<Integer> result = openMemberGroupController.createMemberGroup(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberGroupService, times(1)).addMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试更新会员分组 - 成功场景
     */
    @Test
    public void testUpdateMemberGroup_Success() throws BusinessCheckException {
        // 准备请求参数
        MtMemberGroupUpdateReqVO reqVO = new MtMemberGroupUpdateReqVO();
        reqVO.setId(1);
        reqVO.setName("更新后的分组");
        reqVO.setDescription("更新后的描述");

        // Mock服务调用
        when(memberGroupService.queryMemberGroupById(1)).thenReturn(mockGroup);
        when(memberGroupService.updateMemberGroup(any(MemberGroupDto.class))).thenReturn(mockGroup);

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.updateMemberGroup(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(1);
        verify(memberGroupService, times(1)).updateMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试更新会员分组 - 分组不存在
     */
    @Test
    public void testUpdateMemberGroup_NotFound() throws BusinessCheckException {
        // 准备请求参数
        MtMemberGroupUpdateReqVO reqVO = new MtMemberGroupUpdateReqVO();
        reqVO.setId(999);

        // Mock服务调用 - 分组不存在
        when(memberGroupService.queryMemberGroupById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.updateMemberGroup(reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(999);
        verify(memberGroupService, never()).updateMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试删除会员分组 - 成功场景
     */
    @Test
    public void testDeleteMemberGroup_Success() throws BusinessCheckException {
        // Mock服务调用
        when(memberGroupService.queryMemberGroupById(1)).thenReturn(mockGroup);
        doNothing().when(memberGroupService).deleteMemberGroup(1, "openapi");

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.deleteMemberGroup(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(1);
        verify(memberGroupService, times(1)).deleteMemberGroup(1, "openapi");
    }

    /**
     * 测试删除会员分组 - 分组不存在
     */
    @Test
    public void testDeleteMemberGroup_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分组不存在
        when(memberGroupService.queryMemberGroupById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.deleteMemberGroup(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(999);
        verify(memberGroupService, never()).deleteMemberGroup(anyInt(), anyString());
    }

    /**
     * 测试获取会员分组详情 - 成功场景
     */
    @Test
    public void testGetMemberGroupDetail_Success() throws BusinessCheckException {
        // Mock服务调用
        when(memberGroupService.queryMemberGroupById(1)).thenReturn(mockGroup);

        // 执行测试
        CommonResult<MtMemberGroupRespVO> result = openMemberGroupController.getMemberGroupDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockGroup.getId(), result.getData().getId());
        assertEquals(mockGroup.getName(), result.getData().getName());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(1);
    }

    /**
     * 测试获取会员分组详情 - 分组不存在
     */
    @Test
    public void testGetMemberGroupDetail_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分组不存在
        when(memberGroupService.queryMemberGroupById(999)).thenReturn(null);

        // 执行测试
        CommonResult<MtMemberGroupRespVO> result = openMemberGroupController.getMemberGroupDetail(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(999);
    }

    /**
     * 测试分页查询会员分组列表 - 成功场景
     */
    @Test
    public void testGetMemberGroupPage_Success() throws BusinessCheckException {
        // 准备请求参数
        MtMemberGroupPageReqVO reqVO = new MtMemberGroupPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setName("测试");
        reqVO.setStatus(StatusEnum.ENABLED.getKey());

        // 准备分页响应
        List<UserGroupDto> content = new ArrayList<>();
        content.add(mockGroupDto);
        PaginationResponse<UserGroupDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberGroupService.queryMemberGroupListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtMemberGroupPageRespVO> result = openMemberGroupController.getMemberGroupPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal().longValue());
        assertEquals(1, result.getData().getTotalPages().intValue());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试分页查询会员分组列表 - 空结果
     */
    @Test
    public void testGetMemberGroupPage_Empty() throws BusinessCheckException {
        // 准备请求参数
        MtMemberGroupPageReqVO reqVO = new MtMemberGroupPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);

        // 准备空分页响应
        PaginationResponse<UserGroupDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(new ArrayList<>());
        paginationResponse.setTotalElements(0L);
        paginationResponse.setTotalPages(0);

        // Mock服务调用
        when(memberGroupService.queryMemberGroupListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtMemberGroupPageRespVO> result = openMemberGroupController.getMemberGroupPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getList().size());
        assertEquals(0L, result.getData().getTotal().longValue());
    }

    /**
     * 测试获取所有启用的会员分组列表 - 成功场景
     */
    @Test
    public void testGetMemberGroupList_Success() throws BusinessCheckException {
        // 准备分页响应
        List<UserGroupDto> content = new ArrayList<>();
        content.add(mockGroupDto);
        PaginationResponse<UserGroupDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberGroupService.queryMemberGroupListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<List<MtMemberGroupRespVO>> result = openMemberGroupController.getMemberGroupList(1, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(mockGroupDto.getName(), result.getData().get(0).getName());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试获取所有启用的会员分组列表 - 无参数
     */
    @Test
    public void testGetMemberGroupList_NoParams() throws BusinessCheckException {
        // 准备分页响应
        List<UserGroupDto> content = new ArrayList<>();
        content.add(mockGroupDto);
        PaginationResponse<UserGroupDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberGroupService.queryMemberGroupListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<List<MtMemberGroupRespVO>> result = openMemberGroupController.getMemberGroupList(null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试更新会员分组状态 - 成功场景
     */
    @Test
    public void testUpdateMemberGroupStatus_Success() throws BusinessCheckException {
        // Mock服务调用
        when(memberGroupService.queryMemberGroupById(1)).thenReturn(mockGroup);
        when(memberGroupService.updateMemberGroup(any(MemberGroupDto.class))).thenReturn(mockGroup);

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.updateMemberGroupStatus(1, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(1);
        verify(memberGroupService, times(1)).updateMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试更新会员分组状态 - 分组不存在
     */
    @Test
    public void testUpdateMemberGroupStatus_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分组不存在
        when(memberGroupService.queryMemberGroupById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openMemberGroupController.updateMemberGroupStatus(999, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberGroupService, times(1)).queryMemberGroupById(999);
        verify(memberGroupService, never()).updateMemberGroup(any(MemberGroupDto.class));
    }

    /**
     * 测试分页查询会员分组列表 - 带子分组
     */
    @Test
    public void testGetMemberGroupPage_WithChildren() throws BusinessCheckException {
        // 准备子分组
        UserGroupDto childDto = new UserGroupDto();
        childDto.setId(2);
        childDto.setName("子分组");
        childDto.setParentId(1);
        childDto.setStatus(StatusEnum.ENABLED.getKey());

        // 准备父分组（带子分组）
        mockGroupDto.setChildren(new ArrayList<>());
        mockGroupDto.getChildren().add(childDto);

        // 准备请求参数
        MtMemberGroupPageReqVO reqVO = new MtMemberGroupPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);

        // 准备分页响应
        List<UserGroupDto> content = new ArrayList<>();
        content.add(mockGroupDto);
        PaginationResponse<UserGroupDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberGroupService.queryMemberGroupListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtMemberGroupPageRespVO> result = openMemberGroupController.getMemberGroupPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertNotNull(result.getData().getList().get(0).getChildren());
        assertEquals(1, result.getData().getList().get(0).getChildren().size());
    }
}
