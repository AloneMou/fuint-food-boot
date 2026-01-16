package com.fuint.openapi.v1.member.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fuint.common.dto.UserDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.openapi.enums.UserErrorCodeConstants;
import com.fuint.openapi.v1.member.user.vo.*;
import com.fuint.repository.mapper.MtUserCouponMapper;
import com.fuint.repository.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpenUserController 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenUserControllerTest {

    @InjectMocks
    private OpenUserController openUserController;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberGroupService memberGroupService;

    @Mock
    private UserGradeService userGradeService;

    @Mock
    private StoreService storeService;

    @Mock
    private MtUserCouponMapper mtUserCouponMapper;

    @Mock
    private UserCouponService userCouponService;

    @Mock
    private CouponService couponService;

    private MtUser mockUser;
    private UserDto mockUserDto;
    private MtUserGroup mockGroup;
    private MtUserGrade mockGrade;
    private MtStore mockStore;
    private MtUserCoupon mockUserCoupon;
    private MtCoupon mockCoupon;

    @Before
    public void setUp() {
        // 初始化模拟用户
        mockUser = new MtUser();
        mockUser.setId(1001);
        mockUser.setName("测试用户");
        mockUser.setMobile("13800138000");
        mockUser.setMerchantId(1);
        mockUser.setStoreId(1);
        mockUser.setGroupId(1);
        mockUser.setGradeId("1");
        mockUser.setStatus(StatusEnum.ENABLED.getKey());
        mockUser.setBalance(new BigDecimal("100.00"));
        mockUser.setPoint(1000);
        mockUser.setIsStaff("Y");

        // 初始化模拟用户DTO
        mockUserDto = new UserDto();
        mockUserDto.setId(1001);
        mockUserDto.setName("测试用户");
        mockUserDto.setMobile("138****8000");
//        mockUserDto.setMerchantId(1);
        mockUserDto.setStoreId(1);
        mockUserDto.setGroupId(1);
        mockUserDto.setGradeId("1");
        mockUserDto.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟分组
        mockGroup = new MtUserGroup();
        mockGroup.setId(1);
        mockGroup.setName("测试分组");

        // 初始化模拟等级
        mockGrade = new MtUserGrade();
        mockGrade.setId(1);
        mockGrade.setName("普通会员");

        // 初始化模拟店铺
        mockStore = new MtStore();
        mockStore.setId(1);
        mockStore.setName("测试店铺");

        // 初始化模拟用户优惠券
        mockUserCoupon = new MtUserCoupon();
        mockUserCoupon.setId(1);
        mockUserCoupon.setUserId(1001);
        mockUserCoupon.setCouponId(1);
        mockUserCoupon.setCode("COUPON001");
        mockUserCoupon.setStatus("A");
        mockUserCoupon.setAmount(new BigDecimal("10.00"));
        mockUserCoupon.setBalance(new BigDecimal("10.00"));
        mockUserCoupon.setCreateTime(new Date());

        // 初始化模拟优惠券
        mockCoupon = new MtCoupon();
        mockCoupon.setId(1);
        mockCoupon.setName("测试优惠券");
        mockCoupon.setType("CASH");
        mockCoupon.setOutRule("100");
    }

    /**
     * 测试单个员工数据同步 - 创建新用户成功
     */
    @Test
    public void testSyncUser_CreateSuccess() {
        // 准备请求参数
        MtUserSyncReqVO reqVO = new MtUserSyncReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setName("新用户");
        reqVO.setMerchantId(1);

        // Mock服务调用
        when(memberService.queryMemberByMobile(1, "13800138000")).thenReturn(null);
        when(memberService.addMember(any(MtUser.class))).thenReturn(mockUser);

        // 执行测试
        CommonResult<MtUserSyncRespVO> result = openUserController.syncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getSuccess());
        assertEquals("create", result.getData().getOperationType());
        assertEquals(mockUser.getId(), result.getData().getUserId());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberByMobile(1, "13800138000");
        verify(memberService, times(1)).addMember(any(MtUser.class));
    }

    /**
     * 测试单个员工数据同步 - 更新已存在用户成功
     */
    @Test
    public void testSyncUser_UpdateSuccess() {
        // 准备请求参数
        MtUserSyncReqVO reqVO = new MtUserSyncReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setName("更新后的用户");
        reqVO.setMerchantId(1);

        // Mock服务调用
        when(memberService.queryMemberByMobile(1, "13800138000")).thenReturn(mockUser);
        when(memberService.updateMember(any(MtUser.class), eq(false))).thenReturn(mockUser);

        // 执行测试
        CommonResult<MtUserSyncRespVO> result = openUserController.syncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getSuccess());
        assertEquals("update", result.getData().getOperationType());
        assertEquals(mockUser.getId(), result.getData().getUserId());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberByMobile(1, "13800138000");
        verify(memberService, times(1)).updateMember(any(MtUser.class), eq(false));
    }

    /**
     * 测试单个员工数据同步 - 手机号格式不正确
     */
    @Test
    public void testSyncUser_InvalidMobile() {
        // 准备请求参数（无效手机号）
        MtUserSyncReqVO reqVO = new MtUserSyncReqVO();
        reqVO.setMobile("123456");
        reqVO.setName("测试用户");

        // 执行测试
        CommonResult<MtUserSyncRespVO> result = openUserController.syncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertFalse(result.getData().getSuccess());
        assertEquals("手机号格式不正确", result.getData().getMessage());

        // 验证服务调用
        verify(memberService, never()).queryMemberByMobile(anyInt(), anyString());
        verify(memberService, never()).addMember(any(MtUser.class));
    }

    /**
     * 测试单个员工数据同步 - 创建失败
     */
    @Test
    public void testSyncUser_CreateFailed() {
        // 准备请求参数
        MtUserSyncReqVO reqVO = new MtUserSyncReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setName("新用户");

        // Mock服务调用 - 创建失败
        when(memberService.queryMemberByMobile(anyInt(), eq("13800138000"))).thenReturn(null);
        when(memberService.addMember(any(MtUser.class))).thenReturn(null);

        // 执行测试
        CommonResult<MtUserSyncRespVO> result = openUserController.syncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertFalse(result.getData().getSuccess());
        assertEquals("创建会员失败", result.getData().getMessage());
    }

    /**
     * 测试批量员工数据同步 - 成功场景
     */
    @Test
    public void testBatchSyncUser_Success() {
        // 准备请求参数
        MtUserBatchSyncReqVO reqVO = new MtUserBatchSyncReqVO();
        List<MtUserSyncReqVO> users = new ArrayList<>();
        
        MtUserSyncReqVO user1 = new MtUserSyncReqVO();
        user1.setMobile("13800138001");
        user1.setName("用户1");
        users.add(user1);
        
        MtUserSyncReqVO user2 = new MtUserSyncReqVO();
        user2.setMobile("13800138002");
        user2.setName("用户2");
        users.add(user2);
        
        reqVO.setUsers(users);

        // Mock服务调用
        when(memberService.queryMemberByMobile(anyInt(), anyString())).thenReturn(null);
        when(memberService.addMember(any(MtUser.class))).thenReturn(mockUser);

        // 执行测试
        CommonResult<MtUserBatchSyncRespVO> result = openUserController.batchSyncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getTotalCount().intValue());
        assertEquals(2, result.getData().getSuccessCount().intValue());
        assertEquals(0, result.getData().getFailureCount().intValue());
        assertEquals(2, result.getData().getResults().size());

        // 验证服务调用
        verify(memberService, times(2)).queryMemberByMobile(anyInt(), anyString());
        verify(memberService, times(2)).addMember(any(MtUser.class));
    }

    /**
     * 测试批量员工数据同步 - 空列表
     */
    @Test
    public void testBatchSyncUser_Empty() {
        // 准备请求参数（空列表）
        MtUserBatchSyncReqVO reqVO = new MtUserBatchSyncReqVO();
        reqVO.setUsers(new ArrayList<>());

        // 执行测试
        CommonResult<MtUserBatchSyncRespVO> result = openUserController.batchSyncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberService, never()).queryMemberByMobile(anyInt(), anyString());
    }

    /**
     * 测试批量员工数据同步 - 超过限制
     */
    @Test
    public void testBatchSyncUser_ExceedLimit() {
        // 准备请求参数（超过100条）
        MtUserBatchSyncReqVO reqVO = new MtUserBatchSyncReqVO();
        List<MtUserSyncReqVO> users = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            MtUserSyncReqVO user = new MtUserSyncReqVO();
            user.setMobile("1380013800" + i);
            users.add(user);
        }
        reqVO.setUsers(users);

        // 执行测试
        CommonResult<MtUserBatchSyncRespVO> result = openUserController.batchSyncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberService, never()).queryMemberByMobile(anyInt(), anyString());
    }

    /**
     * 测试获取员工详情 - 成功场景
     */
    @Test
    public void testGetUserDetail_Success() {
        // Mock服务调用
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(memberGroupService.queryMemberGroupById(1)).thenReturn(mockGroup);
        when(userGradeService.queryUserGradeById(anyInt(), anyInt(), anyInt())).thenReturn(mockGrade);
        when(storeService.queryStoreById(1)).thenReturn(mockStore);

        // 执行测试
        CommonResult<MtUserRespVO> result = openUserController.getUserDetail(1001);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockUser.getId(), result.getData().getId());
        assertEquals(mockUser.getName(), result.getData().getName());
        assertEquals(mockGroup.getName(), result.getData().getGroupName());
        assertEquals(mockGrade.getName(), result.getData().getGradeName());
        assertEquals(mockStore.getName(), result.getData().getStoreName());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(1001);
    }

    /**
     * 测试获取员工详情 - 用户不存在
     */
    @Test
    public void testGetUserDetail_NotFound() {
        // Mock服务调用 - 用户不存在
        when(memberService.queryMemberById(9999)).thenReturn(null);

        // 执行测试
        CommonResult<MtUserRespVO> result = openUserController.getUserDetail(9999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(9999);
    }

    /**
     * 测试分页查询员工列表 - 成功场景
     */
    @Test
    public void testGetUserPage_Success() {
        // 准备请求参数
        MtUserPageReqVO reqVO = new MtUserPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setName("测试");

        // 准备分页响应
        List<UserDto> content = new ArrayList<>();
        content.add(mockUserDto);
        PaginationResponse<UserDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberService.queryMemberListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(mtUserCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行测试
        CommonResult<MtUserPageRespVO> result = openUserController.getUserPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal().longValue());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试分页查询员工列表 - 带优惠券状态筛选
     */
    @Test
    public void testGetUserPage_WithCouponStatus() {
        // 准备请求参数
        MtUserPageReqVO reqVO = new MtUserPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setCouponStatus("A");

        // 准备分页响应
        List<UserDto> content = new ArrayList<>();
        content.add(mockUserDto);
        PaginationResponse<UserDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberService.queryMemberListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(mtUserCouponMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createMockUserCouponList());

        // 执行测试
        CommonResult<MtUserPageRespVO> result = openUserController.getUserPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberListByPagination(any(PaginationRequest.class));
        verify(mtUserCouponMapper, atLeastOnce()).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取用户优惠券列表 - 成功场景
     */
    @Test
    public void testGetUserCouponList_Success() {
        // 准备请求参数
        UserCouponListReqVO reqVO = new UserCouponListReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setStatus("A");

        // 准备分页响应
        List<MtUserCoupon> content = new ArrayList<>();
        content.add(mockUserCoupon);
        PaginationResponse<MtUserCoupon> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(userCouponService.queryUserCouponListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(couponService.queryCouponById(1)).thenReturn(mockCoupon);

        // 执行测试
        CommonResult<UserCouponListPageRespVO> result = openUserController.getUserCouponList(1001, reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal().longValue());
        assertEquals(mockCoupon.getName(), result.getData().getList().get(0).getCouponName());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(1001);
        verify(userCouponService, times(1)).queryUserCouponListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试获取用户优惠券列表 - 用户不存在
     */
    @Test
    public void testGetUserCouponList_UserNotFound() {
        // 准备请求参数
        UserCouponListReqVO reqVO = new UserCouponListReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);

        // Mock服务调用 - 用户不存在
        when(memberService.queryMemberById(9999)).thenReturn(null);

        // 执行测试
        CommonResult<UserCouponListPageRespVO> result = openUserController.getUserCouponList(9999, reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(memberService, times(1)).queryMemberById(9999);
        verify(userCouponService, never()).queryUserCouponListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试单个员工数据同步 - 异常场景
     */
    @Test
    public void testSyncUser_Exception() {
        // 准备请求参数
        MtUserSyncReqVO reqVO = new MtUserSyncReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setName("测试用户");

        // Mock服务调用 - 抛出异常
        when(memberService.queryMemberByMobile(anyInt(), anyString()))
                .thenThrow(new RuntimeException("数据库错误"));

        // 执行测试
        CommonResult<MtUserSyncRespVO> result = openUserController.syncUser(reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());
    }

    /**
     * 创建模拟用户优惠券列表
     */
    private List<MtUserCoupon> createMockUserCouponList() {
        List<MtUserCoupon> list = new ArrayList<>();
        list.add(mockUserCoupon);
        return list;
    }
}
