package com.fuint.openapi.v1.goods.cate;

import com.fuint.common.dto.GoodsCateDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.CateService;
import com.fuint.common.service.StoreService;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.openapi.v1.goods.cate.vo.*;
import com.fuint.repository.model.MtGoodsCate;
import com.fuint.repository.model.MtStore;
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
 * OpenCateController 测试类
 * 
 * @author Test
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenCateControllerTest {

    @InjectMocks
    private OpenCateController openCateController;

    @Mock
    private CateService cateService;

    @Mock
    private StoreService storeService;

    private MtGoodsCate mockCate;
    private MtStore mockStore;
    private GoodsCateDto mockCateDto;

    @Before
    public void setUp() {
        // 初始化模拟分类
        mockCate = new MtGoodsCate();
        mockCate.setId(1);
        mockCate.setName("测试分类");
        mockCate.setMerchantId(1);
        mockCate.setStoreId(1);
        mockCate.setLogo("/images/cate.jpg");
        mockCate.setDescription("测试分类描述");
        mockCate.setSort(0);
        mockCate.setStatus(StatusEnum.ENABLED.getKey());
        mockCate.setCreateTime(new Date());
        mockCate.setUpdateTime(new Date());
        mockCate.setOperator("admin");

        // 初始化模拟店铺
        mockStore = new MtStore();
        mockStore.setId(1);
        mockStore.setName("测试店铺");
        mockStore.setMerchantId(1);

        // 初始化模拟分类DTO
        mockCateDto = new GoodsCateDto();
        mockCateDto.setId(1);
        mockCateDto.setName("测试分类");
        mockCateDto.setMerchantId(1);
        mockCateDto.setStoreId(1);
        mockCateDto.setStoreName("测试店铺");
        mockCateDto.setLogo("/images/cate.jpg");
        mockCateDto.setDescription("测试分类描述");
        mockCateDto.setSort(0);
        mockCateDto.setStatus(StatusEnum.ENABLED.getKey());
        mockCateDto.setCreateTime(new Date());
        mockCateDto.setUpdateTime(new Date());
        mockCateDto.setOperator("admin");
    }

    /**
     * 测试创建商品分类 - 成功场景
     */
    @Test
    public void testCreateCate_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCateCreateReqVO reqVO = new MtGoodsCateCreateReqVO();
        reqVO.setName("新分类");
        reqVO.setDescription("新分类描述");
        reqVO.setLogo("/images/new.jpg");
//        reqVO.setStatus(StatusEnum.ENABLED.getKey());

        // Mock服务调用
        when(cateService.addCate(any(MtGoodsCate.class))).thenReturn(mockCate);

        // 执行测试
        CommonResult<Integer> result = openCateController.createCate(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockCate.getId(), result.getData());

        // 验证服务调用
        verify(cateService, times(1)).addCate(any(MtGoodsCate.class));
    }

    /**
     * 测试创建商品分类 - 默认值设置
     */
    @Test
    public void testCreateCate_WithDefaults() throws BusinessCheckException {
        // 准备请求参数（不设置merchantId和storeId）
        MtGoodsCateCreateReqVO reqVO = new MtGoodsCateCreateReqVO();
        reqVO.setName("新分类");

        // Mock服务调用
        when(cateService.addCate(any(MtGoodsCate.class))).thenReturn(mockCate);

        // 执行测试
        CommonResult<Integer> result = openCateController.createCate(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(cateService, times(1)).addCate(any(MtGoodsCate.class));
    }

    /**
     * 测试更新商品分类 - 成功场景
     */
    @Test
    public void testUpdateCate_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCateUpdateReqVO reqVO = new MtGoodsCateUpdateReqVO();
        reqVO.setId(1);
        reqVO.setName("更新后的分类");
        reqVO.setDescription("更新后的描述");

        // Mock服务调用
        when(cateService.queryCateById(1)).thenReturn(mockCate);
        doNothing().when(cateService).updateCate(any(MtGoodsCate.class));

        // 执行测试
        CommonResult<Boolean> result = openCateController.updateCate(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(1);
        verify(cateService, times(1)).updateCate(any(MtGoodsCate.class));
    }

    /**
     * 测试更新商品分类 - 分类不存在
     */
    @Test
    public void testUpdateCate_NotFound() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCateUpdateReqVO reqVO = new MtGoodsCateUpdateReqVO();
        reqVO.setId(999);

        // Mock服务调用 - 分类不存在
        when(cateService.queryCateById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openCateController.updateCate(reqVO);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(999);
        verify(cateService, never()).updateCate(any(MtGoodsCate.class));
    }

    /**
     * 测试删除商品分类 - 成功场景
     */
    @Test
    public void testDeleteCate_Success() throws BusinessCheckException {
        // Mock服务调用
        when(cateService.queryCateById(1)).thenReturn(mockCate);
        doNothing().when(cateService).deleteCate(1, "openapi");

        // 执行测试
        CommonResult<Boolean> result = openCateController.deleteCate(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(1);
        verify(cateService, times(1)).deleteCate(1, "openapi");
    }

    /**
     * 测试删除商品分类 - 分类不存在
     */
    @Test
    public void testDeleteCate_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分类不存在
        when(cateService.queryCateById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openCateController.deleteCate(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(999);
        verify(cateService, never()).deleteCate(anyInt(), anyString());
    }

    /**
     * 测试获取商品分类详情 - 成功场景
     */
    @Test
    public void testGetCateDetail_Success() throws BusinessCheckException {
        // Mock服务调用
        when(cateService.queryCateById(1)).thenReturn(mockCate);
        when(storeService.queryStoreById(1)).thenReturn(mockStore);

        // 执行测试
        CommonResult<MtGoodsCateRespVO> result = openCateController.getCateDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(mockCate.getId(), result.getData().getId());
        assertEquals(mockCate.getName(), result.getData().getName());
        assertEquals(mockStore.getName(), result.getData().getStoreName());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(1);
        verify(storeService, times(1)).queryStoreById(1);
    }

    /**
     * 测试获取商品分类详情 - 分类不存在
     */
    @Test
    public void testGetCateDetail_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分类不存在
        when(cateService.queryCateById(999)).thenReturn(null);

        // 执行测试
        CommonResult<MtGoodsCateRespVO> result = openCateController.getCateDetail(999);

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(999);
        verify(storeService, never()).queryStoreById(anyInt());
    }

    /**
     * 测试获取商品分类详情 - 无店铺信息
     */
    @Test
    public void testGetCateDetail_NoStore() throws BusinessCheckException {
        // 设置分类无店铺ID
        mockCate.setStoreId(0);
        
        // Mock服务调用
        when(cateService.queryCateById(1)).thenReturn(mockCate);

        // 执行测试
        CommonResult<MtGoodsCateRespVO> result = openCateController.getCateDetail(1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(1);
        verify(storeService, never()).queryStoreById(anyInt());
    }

    /**
     * 测试分页查询商品分类列表 - 成功场景
     */
    @Test
    public void testGetCatePage_Success() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCatePageReqVO reqVO = new MtGoodsCatePageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);
        reqVO.setName("测试");
        reqVO.setStatus(StatusEnum.ENABLED.getKey());

        // 准备分页响应
        List<GoodsCateDto> content = new ArrayList<>();
        content.add(mockCateDto);
        PaginationResponse<GoodsCateDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(content);
        paginationResponse.setTotalElements(1L);
        paginationResponse.setTotalPages(1);

        // Mock服务调用
        when(cateService.queryCateListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtGoodsCatePageRespVO> result = openCateController.getCatePage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal().longValue());
        assertEquals(1, result.getData().getTotalPages().intValue());

        // 验证服务调用
        verify(cateService, times(1)).queryCateListByPagination(any(PaginationRequest.class));
    }

    /**
     * 测试分页查询商品分类列表 - 空结果
     */
    @Test
    public void testGetCatePage_Empty() throws BusinessCheckException {
        // 准备请求参数
        MtGoodsCatePageReqVO reqVO = new MtGoodsCatePageReqVO();
        reqVO.setPage(1);
        reqVO.setPageSize(10);

        // 准备空分页响应
        PaginationResponse<GoodsCateDto> paginationResponse = new PaginationResponse<>();
        paginationResponse.setContent(new ArrayList<>());
        paginationResponse.setTotalElements(0L);
        paginationResponse.setTotalPages(0);

        // Mock服务调用
        when(cateService.queryCateListByPagination(any(PaginationRequest.class)))
                .thenReturn(paginationResponse);

        // 执行测试
        CommonResult<MtGoodsCatePageRespVO> result = openCateController.getCatePage(reqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getList().size());
        assertEquals(0L, result.getData().getTotal().longValue());
    }

    /**
     * 测试获取所有启用的商品分类列表 - 成功场景
     */
    @Test
    public void testGetCateList_Success() throws BusinessCheckException {
        // 准备分类列表
        List<MtGoodsCate> cateList = new ArrayList<>();
        cateList.add(mockCate);

        // Mock服务调用
        when(cateService.queryCateListByParams(any())).thenReturn(cateList);
        when(storeService.queryStoreById(1)).thenReturn(mockStore);

        // 执行测试
        CommonResult<List<MtGoodsCateListRespVO>> result = openCateController.getCateList(1, 1);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(mockCate.getName(), result.getData().get(0).getName());
        assertEquals(mockStore.getName(), result.getData().get(0).getStoreName());

        // 验证服务调用
        verify(cateService, times(1)).queryCateListByParams(any());
        verify(storeService, times(1)).queryStoreById(1);
    }

    /**
     * 测试获取所有启用的商品分类列表 - 无参数
     */
    @Test
    public void testGetCateList_NoParams() throws BusinessCheckException {
        // 准备分类列表
        List<MtGoodsCate> cateList = new ArrayList<>();
        cateList.add(mockCate);

        // Mock服务调用
        when(cateService.queryCateListByParams(any())).thenReturn(cateList);

        // 执行测试
        CommonResult<List<MtGoodsCateListRespVO>> result = openCateController.getCateList(null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());

        // 验证服务调用
        verify(cateService, times(1)).queryCateListByParams(any());
    }

    /**
     * 测试更新商品分类状态 - 成功场景
     */
    @Test
    public void testUpdateCateStatus_Success() throws BusinessCheckException {
        // Mock服务调用
        when(cateService.queryCateById(1)).thenReturn(mockCate);
        doNothing().when(cateService).updateCate(any(MtGoodsCate.class));

        // 执行测试
        CommonResult<Boolean> result = openCateController.updateCateStatus(1, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertEquals(Integer.valueOf(200), result.getCode());
        assertTrue(result.getData());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(1);
        verify(cateService, times(1)).updateCate(any(MtGoodsCate.class));
    }

    /**
     * 测试更新商品分类状态 - 分类不存在
     */
    @Test
    public void testUpdateCateStatus_NotFound() throws BusinessCheckException {
        // Mock服务调用 - 分类不存在
        when(cateService.queryCateById(999)).thenReturn(null);

        // 执行测试
        CommonResult<Boolean> result = openCateController.updateCateStatus(999, StatusEnum.DISABLE.getKey());

        // 验证结果
        assertNotNull(result);
        assertNotEquals(Integer.valueOf(200), result.getCode());

        // 验证服务调用
        verify(cateService, times(1)).queryCateById(999);
        verify(cateService, never()).updateCate(any(MtGoodsCate.class));
    }
}
