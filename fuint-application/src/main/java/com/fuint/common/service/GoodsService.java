package com.fuint.common.service;

import com.fuint.common.dto.GoodsDto;
import com.fuint.common.dto.GoodsSpecValueDto;
import com.fuint.common.dto.GoodsTopDto;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.openapi.v1.goods.product.vo.request.CGoodsListPageReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsCreateReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsUpdateReqVO;
import com.fuint.openapi.v1.goods.product.vo.response.CGoodsListRespVO;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.model.MtGoods;
import com.fuint.repository.model.MtGoodsSku;
import com.fuint.repository.model.MtGoodsSpec;
import com.fuint.framework.pojo.PageResult;
import com.fuint.repository.request.GoodsStatisticsReqVO;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品业务接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface GoodsService {

    /**
     * 分页查询商品列表
     *
     * @param paginationRequest
     * @return
     */
    PaginationResponse<GoodsDto> queryGoodsListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException;

    /**
     * 保存商品
     *
     * @param reqDto
     * @return
     * @throws BusinessCheckException
     */
    MtGoods saveGoods(MtGoods reqDto) throws BusinessCheckException;

    /**
     * 根据ID获取商品信息
     *
     * @param id 商品ID
     * @return
     * @throws BusinessCheckException
     */
    MtGoods queryGoodsById(Integer id) throws BusinessCheckException;

    /**
     * 根据编码获取商品信息
     *
     * @param merchantId
     * @param goodsNo
     * @return
     * @throws BusinessCheckException
     */
    MtGoods queryGoodsByGoodsNo(Integer merchantId, String goodsNo) throws BusinessCheckException;

    /**
     * 根据条码获取sku信息
     *
     * @param skuNo skuNo
     * @return
     * @throws BusinessCheckException
     */
    MtGoodsSku getSkuInfoBySkuNo(String skuNo) throws BusinessCheckException;

    /**
     * 根据ID获取商品SKU信息
     *
     * @param id SKU ID
     * @return
     * @throws BusinessCheckException
     */
    MtGoodsSku getSkuInfoById(Integer id);

    /**
     * 根据ID获取商品详情
     *
     * @param id
     * @return
     * @throws BusinessCheckException
     */
    GoodsDto getGoodsDetail(Integer id, boolean getDeleteSpec) throws InvocationTargetException, IllegalAccessException;

    /**
     * 根据ID删除
     *
     * @param id       ID
     * @param operator 操作人
     * @return
     * @throws BusinessCheckException
     */
    void deleteGoods(Integer id, String operator) throws BusinessCheckException;

    /**
     * 获取店铺的商品列表
     *
     * @param storeId
     * @param keyword
     * @param cateId
     * @param page
     * @param pageSize
     * @return
     */
    Map<String, Object> getStoreGoodsList(Integer storeId, String keyword, Integer cateId, Integer page, Integer pageSize) throws BusinessCheckException;

    /**
     * 根据skuId获取规格列表
     *
     * @param skuId
     * @return
     */
    List<GoodsSpecValueDto> getSpecListBySkuId(Integer skuId) throws BusinessCheckException;

    /**
     * 获取规格详情
     *
     * @param specId
     * @return
     */
    MtGoodsSpec getSpecDetail(Integer specId);

    /**
     * 更新已售数量
     *
     * @param goodsId 商品ID
     * @return
     */
    Boolean updateInitSale(Integer goodsId);

    /**
     * 获取选择商品列表
     *
     * @param params 查询参数
     * @return
     */
    PaginationResponse<GoodsDto> selectGoodsList(Map<String, Object> params) throws BusinessCheckException;

    /**
     * 获取商品销售排行榜
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return
     */
    List<GoodsTopDto> getGoodsSaleTopList(Integer merchantId, Integer storeId, Date startTime, Date endTime);

    /**
     * 获取店铺商品销售排行榜
     *
     * @param reqVO 筛选条件
     * @return 排行记录
     */
    List<GoodsTopBean> getGoodsSaleTopListByStore(GoodsStatisticsReqVO reqVO);


    /**
     * 创建商品
     *
     * @param mtGoods 商品信息
     * @return 商品ID
     */
    Integer createGoods(MtGoodsCreateReqVO mtGoods);

    /**
     * 更新商品
     *
     * @param updateReqVO 商品信息
     */
    void updateGoods(MtGoodsUpdateReqVO updateReqVO) throws BusinessCheckException;


    /**
     * 商品列表分页查询
     *
     * @param pageReqVO 请求参数
     * @return 商品列表
     */
    PageResult<MtGoods> queryGoodsList(CGoodsListPageReqVO pageReqVO);

    /**
     * 获取商品SKU列表
     *
     * @param goodsId 商品ID
     * @return 商品SKU列表
     */
    List<MtGoodsSku> queryGoodsSkuList(Integer goodsId);


    /**
     * 获取最低价格SKU
     */
    MtGoodsSku getLowestPriceSku(Integer goodsId);

    /**
     * 获取商品规格列表
     *
     * @param goodsId 商品ID
     * @return 商品规格列表
     */
    List<MtGoodsSpec> queryGoodsSpecList(Integer goodsId);

    /**
     * 获取商品列表
     *
     * @param pageReqVO 请求参数
     * @return 商品列表
     */
    List<MtGoods> getGoodsList(CGoodsListPageReqVO pageReqVO);
}