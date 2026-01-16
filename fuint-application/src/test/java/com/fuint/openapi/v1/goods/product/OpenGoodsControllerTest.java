package com.fuint.openapi.v1.goods.product;

import com.alibaba.fastjson.JSONArray;
import com.fuint.common.dto.GoodsDto;
import com.fuint.common.dto.GoodsSpecValueDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.GoodsService;
import com.fuint.common.service.MemberService;
import com.fuint.common.service.OrderService;
import com.fuint.common.service.SettingService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.openapi.v1.goods.product.vo.*;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OpenGoodsController 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenGoodsControllerTest {

    @InjectMocks
    private OpenGoodsController openGoodsController;

    @Mock
    private MtGoodsSpecMapper mtGoodsSpecMapper;

    @Mock
    private MtGoodsSkuMapper mtGoodsSkuMapper;

    @Mock
    private GoodsService goodsService;

    @Mock
    private OrderService orderService;

    @Mock
    private MemberService memberService;

    @Mock
    private SettingService settingService;

    private MtGoods mockGoods;
    private GoodsDto mockGoodsDto;
    private MtUser mockUser;
    private MtGoodsSku mockSku;
    private MtGoodsCate mockCate;
    private MtStore mockStore;

    @Before
    public void setUp() {
        // 初始化模拟商品
        mockGoods = new MtGoods();
        mockGoods.setId(1);
        mockGoods.setName("测试商品");
        mockGoods.setGoodsNo("GOODS001");
        mockGoods.setType("GOODS");
        mockGoods.setCateId(1);
        mockGoods.setStoreId(1);
        mockGoods.setMerchantId(1);
        mockGoods.setDescription("测试商品描述");
        mockGoods.setLogo("/images/goods.jpg");
        mockGoods.setPrice(new BigDecimal("100.00"));
        mockGoods.setLinePrice(new BigDecimal("120.00"));
        mockGoods.setStock(100);
        mockGoods.setInitSale(50);
        mockGoods.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        mockGoods.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟商品DTO
        mockGoodsDto = new GoodsDto();
        mockGoodsDto.setId(1);
        mockGoodsDto.setName("测试商品");
        mockGoodsDto.setGoodsNo("GOODS001");
        mockGoodsDto.setType("GOODS");
        mockGoodsDto.setCateId(1);
        mockGoodsDto.setStoreId(1);
        mockGoodsDto.setDescription("测试商品描述");
        mockGoodsDto.setLogo("/images/goods.jpg");
        mockGoodsDto.setPrice(new BigDecimal("100.00"));
        mockGoodsDto.setLinePrice(new BigDecimal("120.00"));
        mockGoodsDto.setStock(100);
        mockGoodsDto.setInitSale(50);
        mockGoodsDto.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        mockGoodsDto.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟用户
        mockUser = new MtUser();
        mockUser.setId(1001);
        mockUser.setName("测试用户");
        mockUser.setMobile("13800138000");
        mockUser.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟SKU
        mockSku = new MtGoodsSku();
        mockSku.setId(1);
        mockSku.setGoodsId(1);
        mockSku.setSkuNo("SKU001");
        mockSku.setPrice(new BigDecimal("100.00"));
        mockSku.setLinePrice(new BigDecimal("120.00"));
        mockSku.setStock(100);
        mockSku.setStatus(StatusEnum.ENABLED.getKey());

        // 初始化模拟分类
        mockCate = new MtGoodsCate();
        mockCate.setId(1);
        mockCate.setName("测试分类");

        // 初始化模拟店铺
        mockStore = new MtStore();
        mockStore.setId(1);
        mockStore.setName("测试店铺");
        mockStore.setMerchantId(1);

        // 设置关联信息
        mockGoodsDto.setCateInfo(mockCate);
        mockGoodsDto.setStoreInfo(mockStore);
    }

    /**
     * 测试创建商品 - 成功场景
     */
    @Test
    public void testCreateGoods_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCreateReqVO reqVO = new MtGoodsCreateReqVO();
        reqVO.setName("新商品");
        reqVO.setDescription("新商品描述");
        reqVO.setPrice(new BigDecimal("99.00"));

        // Mock服务调用
        when(goodsService.createGoods(any(MtGoodsCreateReqVO.class))).thenReturn(1);

        // 执行测试
        CommonResult<Integer> result = openGoodsController.createGoods(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(Integer.valueOf(1), result.getData());

        // 验证服务调用
        verify(goodsService, times(1)).createGoods(any(MtGoodsCreateReqVO.class));
    }

    /**
     * 测试更新商品 - 成功场景
     */
    @Test
    public void testUpdateGoods_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsUpdateReqVO reqVO = new MtGoodsUpdateReqVO();
        reqVO.setId(1);
        reqVO.setName("更新后的商品");
        reqVO.setDescription("更新后的描述");

        // Mock服务调用
        doNothing().when(goodsService).updateGoods(any(MtGoodsUpdateReqVO.class));

        // 执行测试
        CommonResult<Boolean> result = openGoodsController.updateGoods(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(goodsService, times(1)).updateGoods(any(MtGoodsUpdateReqVO.class));
    }

    /**
     * 测试删除商品 - 成功场景
     */
    @Test
    public void testDeleteGoods_Success() throws BusinessCheckException {
        // Mock服务调用
        when(goodsService.queryGoodsById(1)).thenReturn(mockGoods);
        doNothing().when(goodsService).deleteGoods(1, "openapi");

        // 执行测试
        CommonResult<Boolean> result = openGoodsController.deleteGoods(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsById(1);
        verify(goodsService, times(1)).deleteGoods(1, "openapi");
    }

    /**
     * 测试删除商品 - 商品不存在
     */
    @Test
    public void testDeleteGoods_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 商品不存在
        when(goodsService.queryGoodsById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openGoodsController.deleteGoods(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsById(999);
        verify(goodsService, never()).deleteGoods(anyInt(), anyString());
    }

    /**
     * 测试获取商品详情 - 成功场景
     */
    @Test
    public void testGetGoodsDetail_Success() throws Exception {
        // Mock服务调用
        when(goodsService.getGoodsDetail(1, false)).thenReturn(mockGoodsDto);

        // 执行测试
        CommonResult<MtGoodsRespVO> result = openGoodsController.getGoodsDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockGoodsDto.getId(), result.getData().getId());
        assertEquals(mockGoodsDto.getName(), result.getData().getName());

        // 验证服务调用
        verify(goodsService, times(1)).getGoodsDetail(1, false);
    }

    /**
     * 测试获取商品详情 - 商品不存在
     */
    @Test
    public void testGetGoodsDetail_NotFound() throws Exception {
        // Mock服务调用 - 商品不存在
        when(goodsService.getGoodsDetail(999, false)).thenReturn(null);

        // 执行测试
        CommonResult<MtGoodsRespVO> result = openGoodsController.getGoodsDetail(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(goodsService, times(1)).getGoodsDetail(999, false);
    }

    /**
     * 测试获取商品详情 - 异常场景
     */
    @Test
    public void testGetGoodsDetail_Exception() throws Exception {
        // Mock服务调用 - 抛出异常
        when(goodsService.getGoodsDetail(1, false)).thenThrow(new RuntimeException("数据库错误"));

        // 执行测试
        CommonResult<MtGoodsRespVO> result = openGoodsController.getGoodsDetail(1);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());
    }

    /**
     * 测试分页查询商品列表 - 成功场景
     */
    @Test
    public void testGetGoodsPage_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsPageReqVO reqVO = new MtGoodsPageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setName("测试");
        reqVO.setStatus(StatusEnum.ENABLED.getKey());

        // 准备分页响应
        List<GoodsDto> content = new ArrayList<>();
        content.add(mockGoodsDto);
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtGoodsPageRespVO> result = openGoodsController.getGoodsPage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal().longValue());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试获取所有启用的商品列表 - 成功场景
     */
    @Test
    public void testGetGoodsList_Success() throws BusinessCheckException {
        // 准备分页响应
        List<GoodsDto> content = new ArrayList<>();
        content.add(mockGoodsDto);
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<List<MtGoodsRespVO>> result = openGoodsController.getGoodsList(1, 1, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试更新商品状态 - 成功场景
     */
    @Test
    public void testUpdateGoodsStatus_Success() throws BusinessCheckException {
        // Mock服务调用
        when(goodsService.queryGoodsById(1)).thenReturn(mockGoods);
        when(goodsService.saveGoods(any(MtGoods.class))).thenReturn(mockGoods);

        // 执行测试
        CommonResult<Boolean> result = openGoodsController.updateGoodsStatus(1, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsById(1);
        verify(goodsService, times(1)).saveGoods(any(MtGoods.class));
    }

    /**
     * 测试更新商品状态 - 商品不存在
     */
    @Test
    public void testUpdateGoodsStatus_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 商品不存在
        when(goodsService.queryGoodsById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openGoodsController.updateGoodsStatus(999, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsById(999);
        verify(goodsService, never()).saveGoods(any(MtGoods.class));
    }

    /**
     * 测试C端商品列表 - 成功场景（单规格商品）
     */
    @Test
    public void testGetCGoodsList_SingleSpec() throws BusinessCheckException {
        // 准备分页响应
        List<GoodsDto> content = new ArrayList<>();
        mockGoodsDto.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        content.add(mockGoodsDto);
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(settingService.getUploadBasePath()).thenReturn("https://example.com");

        // 执行测试
        CommonResult<CGoodsListPageRespVO> result = openGoodsController.getCGoodsList(1001, 1, 1, 1, 1, 20);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getItems().size());
        assertNotNull(result.getData().getItems().get(0).getOriginalPrice());
        assertNotNull(result.getData().getItems().get(0).getDynamicPrice());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsListByPagination(any(PaginationRequest.class));
        verify(settingService, times(1)).getUploadBasePath();
    }

    /**
     * 测试C端商品列表 - 成功场景（多规格商品）
     */
    @Test
    public void testGetCGoodsList_MultiSpec() throws BusinessCheckException {
        // 准备多规格商品
        mockGoodsDto.setIsSingleSpec(YesOrNoEnum.NO.getKey());
        List<MtGoodsSku> skuList = new ArrayList<>();
        skuList.add(mockSku);
        mockGoodsDto.setSkuList(skuList);

        // 准备分页响应
        List<GoodsDto> content = new ArrayList<>();
        content.add(mockGoodsDto);
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(settingService.getUploadBasePath()).thenReturn("https://example.com");
        when(memberService.queryMemberById(1001)).thenReturn(mockUser);
        when(orderService.calculateCartGoods(anyInt(), anyInt(), anyList(), anyInt(), anyBoolean(), anyString(), anyString()))
                .thenReturn(createMockCartData());
        when(goodsService.getSpecListBySkuId(1)).thenReturn(new ArrayList<>());

        // 执行测试
        CommonResult<CGoodsListPageRespVO> result = openGoodsController.getCGoodsList(1001, 1, 1, 1, 1, 20);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getItems().size());
        assertNotNull(result.getData().getItems().get(0).getSkus());
        assertEquals(1, result.getData().getItems().get(0).getSkus().size());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsListByPagination(any(PaginationRequest.class));
        verify(settingService, times(1)).getUploadBasePath();
    }

    /**
     * 测试C端商品列表 - 无用户ID
     */
    @Test
    public void testGetCGoodsList_NoUserId() throws BusinessCheckException {
        // 准备分页响应
        List<GoodsDto> content = new ArrayList<>();
        mockGoodsDto.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        content.add(mockGoodsDto);
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);
        when(settingService.getUploadBasePath()).thenReturn("https://example.com");

        // 执行测试（无用户ID）
        CommonResult<CGoodsListPageRespVO> result = openGoodsController.getCGoodsList(null, 1, 1, 1, 1, 20);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());

        // 验证服务调用
        verify(goodsService, times(1)).queryGoodsListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试C端商品列表 - 异常场景
     */
    @Test
    public void testGetCGoodsList_Exception() throws BusinessCheckException {
        // Mock服务调用 - 抛出异常
        when(goodsService.queryGoodsListByPagination(any(PaginationRequest.class)))
                .thenThrow(new RuntimeException("数据库错误"));

        // 执行测试
        CommonResult<CGoodsListPageRespVO> result = openGoodsController.getCGoodsList(null, 1, 1, 1, 1, 20);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());
    }

    /**
     * 创建模拟购物车数据
     */
    private Map<String, Object> createMockCartData() {
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("totalPrice", new BigDecimal("100.00"));
        cartData.put("discountAmount", new BigDecimal("0.00"));
        cartData.put("payableAmount", new BigDecimal("100.00"));
        return cartData;
    }
}
