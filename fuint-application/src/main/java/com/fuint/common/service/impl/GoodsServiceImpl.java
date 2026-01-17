package com.fuint.common.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.Constants;
import com.fuint.common.dto.GoodsDto;
import com.fuint.common.dto.GoodsSpecValueDto;
import com.fuint.common.dto.GoodsTopDto;
import com.fuint.common.enums.GoodsTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.service.*;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.exception.ServiceException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecChildVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemCreateReqVO;
import com.fuint.openapi.v1.goods.product.vo.model.GoodsSpecItemVO;
import com.fuint.openapi.v1.goods.product.vo.request.CGoodsListPageReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.GoodsSkuCreateReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsCreateReqVO;
import com.fuint.openapi.v1.goods.product.vo.request.MtGoodsUpdateReqVO;
import com.fuint.repository.bean.GoodsBean;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.mapper.MtGoodsMapper;
import com.fuint.repository.mapper.MtGoodsSkuMapper;
import com.fuint.repository.mapper.MtGoodsSpecMapper;
import com.fuint.repository.model.*;
import com.fuint.framework.pojo.PageResult;
import com.fuint.repository.request.GoodsStatisticsReqVO;
import com.fuint.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.fuint.framework.exception.util.ServiceExceptionUtil.exception;
import static com.fuint.framework.util.collection.CollectionUtils.convertMap;
import static com.fuint.openapi.enums.GoodsErrorCodeConstants.*;

/**
 * 商品业务实现类
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<MtGoodsMapper, MtGoods> implements GoodsService {

    private MtGoodsMapper mtGoodsMapper;

    private MtGoodsSpecMapper mtGoodsSpecMapper;

    private MtGoodsSkuMapper mtGoodsSkuMapper;

    /**
     * 系统设置服务接口
     */
    private SettingService settingService;

    /**
     * 商品分类服务接口
     */
    private CateService cateService;

    /**
     * 店铺服务接口
     */
    private StoreService storeService;

    /**
     * 卡券服务接口
     */
    private CouponService couponService;

    /**
     * 分页查询商品列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<GoodsDto> queryGoodsListByPagination(PaginationRequest paginationRequest) throws BusinessCheckException {
        Page<MtGoods> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtGoods> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtGoods::getStatus, StatusEnum.DISABLE.getKey());

        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtGoods::getName, name);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtGoods::getStatus, status);
        }
        String goodsNo = paginationRequest.getSearchParams().get("goodsNo") == null ? "" : paginationRequest.getSearchParams().get("goodsNo").toString();
        if (StringUtils.isNotBlank(goodsNo)) {
            lambdaQueryWrapper.eq(MtGoods::getGoodsNo, goodsNo);
        }
        String isSingleSpec = paginationRequest.getSearchParams().get("isSingleSpec") == null ? "" : paginationRequest.getSearchParams().get("isSingleSpec").toString();
        if (StringUtils.isNotBlank(isSingleSpec)) {
            lambdaQueryWrapper.eq(MtGoods::getIsSingleSpec, isSingleSpec);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtGoods::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.and(wq -> wq
                    .eq(MtGoods::getStoreId, 0)
                    .or()
                    .eq(MtGoods::getStoreId, storeId));
        }
        String type = paginationRequest.getSearchParams().get("type") == null ? "" : paginationRequest.getSearchParams().get("type").toString();
        if (StringUtils.isNotBlank(type)) {
            lambdaQueryWrapper.eq(MtGoods::getType, type);
        }
        String cateId = paginationRequest.getSearchParams().get("cateId") == null ? "" : paginationRequest.getSearchParams().get("cateId").toString();
        if (StringUtils.isNotBlank(cateId)) {
            lambdaQueryWrapper.eq(MtGoods::getCateId, cateId);
        }
        String hasStock = paginationRequest.getSearchParams().get("stock") == null ? "" : paginationRequest.getSearchParams().get("stock").toString();
        if (StringUtils.isNotBlank(hasStock)) {
            if (hasStock.equals(YesOrNoEnum.YES.getKey())) {
                lambdaQueryWrapper.gt(MtGoods::getStock, 0);
            } else {
                lambdaQueryWrapper.lt(MtGoods::getStock, 1);
            }
        }
        String hasPrice = paginationRequest.getSearchParams().get("hasPrice") == null ? "" : paginationRequest.getSearchParams().get("hasPrice").toString();
        if (StringUtils.isNotBlank(hasPrice)) {
            if (hasPrice.equals(YesOrNoEnum.YES.getKey())) {
                lambdaQueryWrapper.gt(MtGoods::getPrice, 0);
            }
        }
        lambdaQueryWrapper.orderByAsc(MtGoods::getSort);
        List<MtGoods> goodsList = mtGoodsMapper.selectList(lambdaQueryWrapper);
        List<GoodsDto> dataList = new ArrayList<>();
        String basePath = settingService.getUploadBasePath();
        for (MtGoods mtGoods : goodsList) {
            MtGoodsCate cateInfo = null;
            if (mtGoods.getCateId() != null) {
                cateInfo = cateService.queryCateById(mtGoods.getCateId());
            }
            GoodsDto item = com.fuint.framework.util.object.BeanUtils.toBean(mtGoods, GoodsDto.class);
            item.setId(mtGoods.getId());
            item.setInitSale(mtGoods.getInitSale());
            if (StringUtil.isNotEmpty(mtGoods.getLogo())) {
                item.setLogo(basePath + mtGoods.getLogo());
            }
            item.setStoreId(mtGoods.getStoreId());
            if (mtGoods.getStoreId() != null) {
                MtStore storeInfo = storeService.queryStoreById(mtGoods.getStoreId());
                item.setStoreInfo(storeInfo);
            }
            item.setName(mtGoods.getName());
            item.setGoodsNo(mtGoods.getGoodsNo());
            item.setCateId(mtGoods.getCateId());
            item.setStock(mtGoods.getStock());
            item.setCateInfo(cateInfo);
            item.setType(mtGoods.getType());
            item.setPrice(mtGoods.getPrice());
            item.setLinePrice(mtGoods.getLinePrice());
            item.setSalePoint(mtGoods.getSalePoint());
            item.setDescription(mtGoods.getDescription());
            item.setCreateTime(mtGoods.getCreateTime());
            item.setUpdateTime(mtGoods.getUpdateTime());
            item.setStatus(mtGoods.getStatus());
            item.setOperator(mtGoods.getOperator());
            // 规格列表
            Map<String, Object> param = new HashMap<>();
            param.put("goods_id", item.getId());
            param.put("status", StatusEnum.ENABLED.getKey());
            List<MtGoodsSpec> goodsSpecList = mtGoodsSpecMapper.selectByMap(param);
            item.setSpecList(goodsSpecList);

            // sku列表
            if (item.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
                List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
                item.setSkuList(goodsSkuList);
                // 多规格商品的价格、库存数量
                if (!goodsSkuList.isEmpty()) {
                    item.setPrice(goodsSkuList.get(0).getPrice());
                    item.setLinePrice(goodsSkuList.get(0).getLinePrice());
                    int stock = 0;
                    for (MtGoodsSku mtGoodsSku : goodsSkuList) {
                        stock = stock + mtGoodsSku.getStock();
                    }
                    item.setStock(stock);
                } else {
                    item.setStock(0);
                }
            } else {
                item.setSkuList(new ArrayList<>());
            }
            dataList.add(item);
        }

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse(pageImpl, GoodsDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 保存商品信息
     *
     * @param reqDto 商品参数
     * @return
     * @throws BusinessCheckException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "保存商品信息")
    public MtGoods saveGoods(MtGoods reqDto) throws BusinessCheckException {
        MtGoods mtGoods = new MtGoods();
        if (reqDto.getId() != null && reqDto.getId() > 0) {
            mtGoods = queryGoodsById(reqDto.getId());
            reqDto.setMerchantId(mtGoods.getMerchantId());
        }
        if (reqDto.getMerchantId() != null) {
            mtGoods.setMerchantId(reqDto.getMerchantId() >= 0 ? reqDto.getMerchantId() : 0);
        }
        if (reqDto.getStoreId() != null) {
            mtGoods.setStoreId(reqDto.getStoreId() >= 0 ? reqDto.getStoreId() : 0);
        }
        Integer storeId = reqDto.getStoreId() == null ? 0 : reqDto.getStoreId();
        if (reqDto.getMerchantId() == null || reqDto.getMerchantId() <= 0) {
            MtStore mtStore = storeService.queryStoreById(storeId);
            if (mtStore != null) {
                mtGoods.setMerchantId(mtStore.getMerchantId());
            }
        }
        if (mtGoods.getId() == null && (mtGoods.getMerchantId() == null || mtGoods.getMerchantId() < 1)) {
            throw new BusinessCheckException("平台方帐号无法执行该操作，请使用商户帐号操作");
        }
        if (StringUtil.isNotEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(reqDto.getIsSingleSpec());
        }
        if (reqDto.getId() != null && reqDto.getId() <= 0 && StringUtil.isEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(YesOrNoEnum.YES.getKey());
        }
        if (StringUtil.isNotEmpty(reqDto.getName())) {
            mtGoods.setName(reqDto.getName());
        }
        if (StringUtil.isNotEmpty(reqDto.getStatus())) {
            mtGoods.setStatus(reqDto.getStatus());
        }
        if (StringUtil.isNotEmpty(reqDto.getLogo())) {
            mtGoods.setLogo(reqDto.getLogo());
        }
        if (StringUtil.isNotEmpty(reqDto.getIsSingleSpec())) {
            mtGoods.setIsSingleSpec(reqDto.getIsSingleSpec());
        }
        if (StringUtil.isNotEmpty(reqDto.getDescription())) {
            mtGoods.setDescription(reqDto.getDescription());
        }
        if (StringUtil.isNotEmpty(reqDto.getOperator())) {
            mtGoods.setOperator(reqDto.getOperator());
        }
        if (StringUtil.isNotEmpty(reqDto.getType())) {
            mtGoods.setType(reqDto.getType());
        }
        if (reqDto.getCateId() != null && reqDto.getCateId() > 0) {
            mtGoods.setCateId(reqDto.getCateId());
        }
        if (reqDto.getServiceTime() != null && reqDto.getServiceTime() > 0) {
            mtGoods.setServiceTime(reqDto.getServiceTime());
        }
        if (StringUtil.isNotEmpty(reqDto.getGoodsNo())) {
            mtGoods.setGoodsNo(reqDto.getGoodsNo());
        }
        if (reqDto.getSort() != null) {
            mtGoods.setSort(reqDto.getSort());
        }
        if (reqDto.getPrice() != null) {
            mtGoods.setPrice(reqDto.getPrice());
        }
        if (reqDto.getPrice() == null && reqDto.getId() != null && reqDto.getId() <= 0) {
            mtGoods.setPrice(new BigDecimal("0.00"));
        }
        if (reqDto.getLinePrice() != null) {
            mtGoods.setLinePrice(reqDto.getLinePrice());
        }
        if (reqDto.getLinePrice() == null && reqDto.getId() != null && reqDto.getId() <= 0) {
            mtGoods.setLinePrice(new BigDecimal("0.00"));
        }
        if (StringUtil.isNotEmpty(reqDto.getCouponIds())) {
            mtGoods.setCouponIds(reqDto.getCouponIds());
        }
        if (reqDto.getWeight() != null) {
            mtGoods.setWeight(reqDto.getWeight());
        }
        if (reqDto.getInitSale() != null) {
            mtGoods.setInitSale(reqDto.getInitSale());
        }
        if (reqDto.getStock() != null) {
            mtGoods.setStock(reqDto.getStock());
        }
        if (StringUtil.isNotEmpty(reqDto.getSalePoint())) {
            mtGoods.setSalePoint(reqDto.getSalePoint());
        }
        if (StringUtil.isEmpty(reqDto.getSalePoint()) && reqDto.getId() != null && reqDto.getId() <= 0) {
            reqDto.setSalePoint("");
        }
        if (StringUtil.isNotEmpty(reqDto.getCanUsePoint())) {
            mtGoods.setCanUsePoint(reqDto.getCanUsePoint());
        }
        if (StringUtil.isNotEmpty(reqDto.getIsMemberDiscount())) {
            mtGoods.setIsMemberDiscount(reqDto.getIsMemberDiscount());
        }
        if (StringUtil.isNotEmpty(reqDto.getImages())) {
            mtGoods.setImages(reqDto.getImages());
        }
        if (!mtGoods.getType().equals(GoodsTypeEnum.COUPON.getKey())) {
            mtGoods.setCouponIds("");
        }
        if (mtGoods.getCouponIds() != null && StringUtil.isNotEmpty(mtGoods.getCouponIds())) {
            String[] couponIds = mtGoods.getCouponIds().split(",");
            for (String couponId : couponIds) {
                MtCoupon mtCoupon = couponService.queryCouponById(Integer.parseInt(couponId));
                if (mtCoupon == null) {
                    throw new BusinessCheckException("卡券ID等于“" + couponId + "”的虚拟卡券不存在.");
                }
            }
        }
        mtGoods.setUpdateTime(new Date());
        if (reqDto.getId() == null || reqDto.getId() <= 0) {
            mtGoods.setCreateTime(new Date());
            this.save(mtGoods);
        } else {
            this.updateById(mtGoods);
        }

        return mtGoods;
    }

    /**
     * 根据ID获取商品信息
     *
     * @param id 商品ID
     * @return
     * @throws BusinessCheckException
     */
    @Override
    public MtGoods queryGoodsById(Integer id) {
        MtGoods mtGoods = mtGoodsMapper.selectById(id);
        if (mtGoods == null) {
            return null;
        }
        return mtGoods;
    }

    /**
     * 根据编码获取商品信息
     *
     * @param merchantId 商户ID
     * @param goodsNo    商品编码
     * @return
     * @throws BusinessCheckException
     */
    @Override
    public MtGoods queryGoodsByGoodsNo(Integer merchantId, String goodsNo) {
        return mtGoodsMapper.getByGoodsNo(merchantId, goodsNo);
    }

    /**
     * 根据条码获取sku信息
     *
     * @param skuNo skuNo
     * @throws BusinessCheckException
     */
    @Override
    public MtGoodsSku getSkuInfoBySkuNo(String skuNo) {
        List<MtGoodsSku> mtGoodsSkuList = mtGoodsSkuMapper.getBySkuNo(skuNo);
        if (mtGoodsSkuList.size() > 0) {
            return mtGoodsSkuList.get(0);
        }
        return null;
    }

    /**
     * 根据ID获取商品详情
     *
     * @param id 商品ID
     * @throws BusinessCheckException
     */
    @Override
    public GoodsDto getGoodsDetail(Integer id, boolean getDeleteSpec) {
        if (id == null || id < 1) {
            return null;
        }

        MtGoods mtGoods = mtGoodsMapper.selectById(id);
        GoodsDto goodsInfo = new GoodsDto();

        if (mtGoods != null) {
            try {
                BeanUtils.copyProperties(mtGoods, goodsInfo);
            } catch (Exception e) {
                goodsInfo.setId(mtGoods.getId());
                goodsInfo.setType(mtGoods.getType());
                goodsInfo.setStoreId(mtGoods.getStoreId());
                goodsInfo.setName(mtGoods.getName());
                goodsInfo.setCateId(mtGoods.getCateId());
                goodsInfo.setGoodsNo(mtGoods.getGoodsNo());
                goodsInfo.setIsSingleSpec(mtGoods.getIsSingleSpec());
                goodsInfo.setLogo(mtGoods.getLogo());
                goodsInfo.setImages(mtGoods.getImages());
                goodsInfo.setStatus(mtGoods.getStatus());
                goodsInfo.setSort(mtGoods.getSort());
                goodsInfo.setPrice(mtGoods.getPrice());
                goodsInfo.setLinePrice(mtGoods.getLinePrice());
                goodsInfo.setServiceTime(mtGoods.getServiceTime());
                goodsInfo.setCouponIds(mtGoods.getCouponIds());
            }
        }

        String basePath = settingService.getUploadBasePath();
        if (StringUtil.isNotEmpty(goodsInfo.getLogo())) {
            goodsInfo.setLogo(basePath + goodsInfo.getLogo());
        }

        // 规格列表
        Map<String, Object> param = new HashMap<>();
        param.put("goods_id", id.toString());
        if (getDeleteSpec == false) {
            param.put("status", StatusEnum.ENABLED.getKey());
        }
        List<MtGoodsSpec> goodsSpecList = mtGoodsSpecMapper.selectByMap(param);
        goodsInfo.setSpecList(goodsSpecList);

        // sku列表
        if (goodsInfo.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
            List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
            goodsInfo.setSkuList(goodsSkuList);
            // 多规格商品的价格、库存数量
            if (goodsSkuList.size() > 0) {
                goodsInfo.setPrice(goodsSkuList.get(0).getPrice());
                goodsInfo.setLinePrice(goodsSkuList.get(0).getLinePrice());
                Integer stock = 0;
                for (MtGoodsSku mtGoodsSku : goodsSkuList) {
                    stock = stock + mtGoodsSku.getStock();
                }
                goodsInfo.setStock(stock);
            } else {
                goodsInfo.setStock(0);
            }
        } else {
            goodsInfo.setSkuList(new ArrayList<>());
        }

        return goodsInfo;
    }

    /**
     * 根据ID删除商品信息
     *
     * @param id       ID
     * @param operator 操作人
     * @return
     * @throws BusinessCheckException
     */
    @Override
    @OperationServiceLog(description = "删除商品信息")
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Integer id, String operator) throws BusinessCheckException {
        MtGoods cateInfo = queryGoodsById(id);
        if (null == cateInfo) {
            throw new BusinessCheckException("该商品不存在");
        }
        cateInfo.setStatus(StatusEnum.DISABLE.getKey());
        cateInfo.setUpdateTime(new Date());
        mtGoodsMapper.updateById(cateInfo);
    }

    /**
     * 获取店铺的商品列表
     *
     * @param storeId  店铺ID
     * @param keyword  关键字
     * @param cateId   分类ID
     * @param page     当前页码
     * @param pageSize 每页页数
     * @return
     * @throws BusinessCheckException
     */
    @Override
    public Map<String, Object> getStoreGoodsList(Integer storeId, String keyword, Integer cateId, Integer page, Integer pageSize) throws BusinessCheckException {
        MtStore mtStore = storeService.queryStoreById(storeId);
        if (mtStore == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("goodsList", new ArrayList<>());
            result.put("total", 0);
            return result;
        }
        Integer merchantId = mtStore.getMerchantId() == null ? 0 : mtStore.getMerchantId();
        Page<MtGoods> pageHelper = PageHelper.startPage(page, pageSize);
        List<MtGoods> goodsList = new ArrayList<>();
        List<MtGoodsSku> skuList = new ArrayList<>();
        if (StringUtil.isNotEmpty(keyword)) {
            skuList = mtGoodsSkuMapper.getBySkuNo(keyword);
        }
        if (skuList != null && skuList.size() > 0) {
            MtGoods goods = mtGoodsMapper.selectById(skuList.get(0).getGoodsId());
            goodsList.add(goods);
        } else {
            pageHelper = PageHelper.startPage(page, pageSize);
            if (keyword != null && StringUtil.isNotEmpty(keyword)) {
                goodsList = mtGoodsMapper.searchStoreGoodsList(merchantId, storeId, keyword);
            } else {
                goodsList = mtGoodsMapper.getStoreGoodsList(merchantId, storeId, cateId);
            }
        }
        List<MtGoods> dataList = new ArrayList<>();
        if (goodsList.size() > 0) {
            for (MtGoods mtGoods : goodsList) {
                // 多规格商品价格、库存数量
                if (mtGoods != null && mtGoods.getIsSingleSpec().equals(YesOrNoEnum.NO.getKey())) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("goods_id", mtGoods.getId().toString());
                    param.put("status", StatusEnum.ENABLED.getKey());
                    List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
                    if (goodsSkuList.size() > 0) {
                        mtGoods.setPrice(goodsSkuList.get(0).getPrice());
                        mtGoods.setLinePrice(goodsSkuList.get(0).getLinePrice());
                        Integer stock = 0;
                        for (MtGoodsSku mtGoodsSku : goodsSkuList) {
                            stock = stock + mtGoodsSku.getStock();
                        }
                        mtGoods.setStock(stock);
                    } else {
                        mtGoods.setStock(0);
                    }
                }
                dataList.add(mtGoods);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goodsList", dataList);
        data.put("total", pageHelper.getTotal());

        return data;
    }

    /**
     * 通过SKU获取规格列表
     *
     * @param skuId skuID
     * @return
     */
    @Override
    public List<GoodsSpecValueDto> getSpecListBySkuId(Integer skuId) {
        if (skuId < 0 || skuId == null) {
            return new ArrayList<>();
        }
        List<GoodsSpecValueDto> result = new ArrayList<>();

        MtGoodsSku goodsSku = mtGoodsSkuMapper.selectById(skuId);
        if (goodsSku == null) {
            return result;
        }

        String specIds = goodsSku.getSpecIds();
        String specIdArr[] = specIds.split("-");
        for (String specId : specIdArr) {
            MtGoodsSpec mtGoodsSpec = mtGoodsSpecMapper.selectById(Integer.parseInt(specId));
            GoodsSpecValueDto dto = new GoodsSpecValueDto();
            dto.setSpecValueId(mtGoodsSpec.getId());
            dto.setSpecName(mtGoodsSpec.getName());
            dto.setSpecValue(mtGoodsSpec.getValue());
            result.add(dto);
        }

        return result;
    }

    /**
     * 获取商品规格详情
     *
     * @param specId 规格ID
     * @return
     */
    @Override
    public MtGoodsSpec getSpecDetail(Integer specId) {
        MtGoodsSpec mtGoodsSpec = mtGoodsSpecMapper.selectById(specId);
        return mtGoodsSpec;
    }

    /**
     * 更新已售数量
     *
     * @param goodsId 商品ID
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateInitSale(Integer goodsId) {
        return mtGoodsMapper.updateInitSale(goodsId);
    }

    /**
     * 获取选择商品列表
     *
     * @param params 查询参数
     * @return
     */
    @Override
    public PaginationResponse<GoodsDto> selectGoodsList(Map<String, Object> params) throws BusinessCheckException {
        Integer page = params.get("page") == null ? Constants.PAGE_NUMBER : Integer.parseInt(params.get("page").toString());
        Integer pageSize = params.get("pageSize") == null ? Constants.PAGE_SIZE : Integer.parseInt(params.get("pageSize").toString());
        Integer merchantId = (params.get("merchantId") == null || StringUtil.isEmpty(params.get("merchantId").toString())) ? 0 : Integer.parseInt(params.get("merchantId").toString());
        Integer storeId = (params.get("storeId") == null || StringUtil.isEmpty(params.get("storeId").toString())) ? 0 : Integer.parseInt(params.get("storeId").toString());
        Integer cateId = (params.get("cateId") == null || StringUtil.isEmpty(params.get("cateId").toString())) ? 0 : Integer.parseInt(params.get("cateId").toString());
        String keyword = params.get("keyword") == null ? "" : params.get("keyword").toString();

        MtStore mtStore = storeService.queryStoreById(storeId);
        if (mtStore != null && mtStore.getMerchantId() != null) {
            merchantId = mtStore.getMerchantId();
        }
        Page<MtGoods> pageHelper = PageHelper.startPage(page, pageSize);
        List<GoodsDto> dataList = new ArrayList<>();

        List<GoodsBean> goodsList = mtGoodsMapper.selectGoodsList(merchantId, storeId, cateId, keyword);

        for (GoodsBean goodsBean : goodsList) {
            GoodsDto goodsDto = new GoodsDto();
            goodsDto.setId(goodsBean.getGoodsId());
            goodsDto.setLogo(goodsBean.getLogo());
            goodsDto.setName(goodsBean.getName());
            goodsDto.setGoodsNo(goodsBean.getGoodsNo());
            goodsDto.setStoreId(goodsBean.getStoreId());
            goodsDto.setPrice(goodsBean.getPrice());
            goodsDto.setCateId(goodsBean.getCateId());
            goodsDto.setStock(goodsBean.getStock());
            if (goodsBean.getSpecIds() != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("GOODS_ID", goodsBean.getGoodsId());
                param.put("SPEC_IDS", goodsBean.getSpecIds());
                param.put("STATUS", StatusEnum.ENABLED.getKey());
                List<MtGoodsSku> goodsSkuList = mtGoodsSkuMapper.selectByMap(param);
                if (goodsSkuList != null && goodsSkuList.size() > 0) {
                    goodsDto.setSkuId(goodsSkuList.get(0).getId());
                    goodsDto.setPrice(goodsSkuList.get(0).getPrice());
                    if (goodsSkuList.get(0).getLogo() != null && StringUtil.isNotEmpty(goodsSkuList.get(0).getLogo())) {
                        goodsDto.setLogo(goodsSkuList.get(0).getLogo());
                    }
                    goodsDto.setStock(goodsSkuList.get(0).getStock());
                    List<MtGoodsSpec> specList = new ArrayList<>();
                    String[] specIds = goodsBean.getSpecIds().split("-");
                    if (specIds.length > 0) {
                        for (String specId : specIds) {
                            MtGoodsSpec mtGoodsSpec = mtGoodsSpecMapper.selectById(Integer.parseInt(specId));
                            if (mtGoodsSpec != null) {
                                specList.add(mtGoodsSpec);
                            }
                        }
                    }
                    goodsDto.setSpecList(specList);
                }
            }
            dataList.add(goodsDto);
        }

        PageRequest pageRequest = PageRequest.of(page, pageSize);
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<GoodsDto> paginationResponse = new PaginationResponse(pageImpl, GoodsDto.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 获取商品销售排行榜
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return
     */
    @Override
    public List<GoodsTopDto> getGoodsSaleTopList(Integer merchantId, Integer storeId, Date startTime, Date endTime) {
        List<GoodsTopBean> dataList = mtGoodsMapper.getGoodsSaleTopList(merchantId, storeId, startTime, endTime);
        List<GoodsTopDto> goodsList = new ArrayList<>();
        if (dataList != null && dataList.size() > 0) {
            for (GoodsTopBean bean : dataList) {
                GoodsTopDto dto = new GoodsTopDto();
                BeanUtils.copyProperties(bean, dto);
                goodsList.add(dto);
            }
        }
        return goodsList;
    }

    @Override
    public List<GoodsTopBean> getGoodsSaleTopListByStore(GoodsStatisticsReqVO reqVO) {
        return mtGoodsMapper.getGoodsSaleTopListByStore(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createGoods(MtGoodsCreateReqVO createReqVO) {
        if (CollUtil.isNotEmpty(createReqVO.getCouponIds())) {
            if (createReqVO.getCouponIds().size() > 10) {
                throw exception(GOODS_COUPON_TOO_MANY, 10);
            }
            List<MtCoupon> mtCoupons = couponService.queryCouponListByIds(createReqVO.getCouponIds());
            Map<Integer, MtCoupon> couponMap = convertMap(mtCoupons, MtCoupon::getId);
            for (Integer couponId : createReqVO.getCouponIds()) {
                MtCoupon mtCoupon = couponMap.get(couponId);
                if (mtCoupon == null) {
                    throw exception(GOODS_COUPON_NOT_FOUND, couponId);
                }
            }
        }

        MtGoods mtGoods = new MtGoods();
        BeanUtils.copyProperties(createReqVO, mtGoods);
        // 处理优惠券ID
        mtGoods.setCouponIds(CollUtil.join(createReqVO.getCouponIds(), ","));
        // 处理图片
        if (createReqVO.getImages() != null && !createReqVO.getImages().isEmpty()) {
            mtGoods.setLogo(createReqVO.getImages().get(0));
            mtGoods.setImages(JSONObject.toJSONString(createReqVO.getImages()));
        } else if (StringUtil.isNotEmpty(createReqVO.getLogo())) {
            mtGoods.setLogo(createReqVO.getLogo());
            mtGoods.setImages(JSONObject.toJSONString(Collections.singletonList(createReqVO.getLogo())));
        }
        // 设置默认值
        if (mtGoods.getMerchantId() == null) mtGoods.setMerchantId(1);
        if (mtGoods.getStoreId() == null) mtGoods.setStoreId(0);
        mtGoods.setStatus(StatusEnum.ENABLED.getKey());
        mtGoods.setOperator("openapi");
        mtGoods.setCreateTime(new Date());
        mtGoods.setUpdateTime(new Date());
        mtGoodsMapper.insert(mtGoods);
        Integer goodsId = mtGoods.getId();
        if (createReqVO.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
            return goodsId;
        }
        List<MtGoodsSpec> specLs = new ArrayList<>();
        for (GoodsSpecItemCreateReqVO spec : createReqVO.getSpecData()) {
            for (String value : spec.getChild()) {
                MtGoodsSpec specBean = new MtGoodsSpec();
                BeanUtils.copyProperties(spec, specBean);
                specBean.setGoodsId(goodsId);
                specBean.setStatus(StatusEnum.ENABLED.getKey());
                specBean.setValue(value);
                mtGoodsSpecMapper.insert(specBean);
                specLs.add(specBean);
            }
        }
        // 处理规格
        Map<String, Integer> specMap = new HashMap<>();
        for (MtGoodsSpec spec : specLs) {
            specMap.put(spec.getName() + "^" + spec.getValue(), spec.getId());
        }
        Integer stock = 0;
        for (GoodsSkuCreateReqVO sku : createReqVO.getSkuData()) {
            List<GoodsSkuCreateReqVO.Spec> specList = sku.getSpecLs();
            List<Integer> specIds = new ArrayList<>();
            for (GoodsSkuCreateReqVO.Spec spec : specList) {
                Integer specId = specMap.get(spec.getName() + "^" + spec.getValue());
                if (specId != null) {
                    specIds.add(specId);
                }
            }
            MtGoodsSku skuBean = new MtGoodsSku();
            BeanUtils.copyProperties(sku, skuBean);
            skuBean.setGoodsId(goodsId);
            skuBean.setSpecIds(CollUtil.join(specIds, "-"));
            skuBean.setStatus(StatusEnum.ENABLED.getKey());
            mtGoodsSkuMapper.insert(skuBean);
            stock += skuBean.getStock();
        }
        MtGoods updateObj = new MtGoods();
        updateObj.setId(goodsId);
        updateObj.setStock(stock);
        mtGoodsMapper.updateById(updateObj);
        return goodsId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(MtGoodsUpdateReqVO updateReqVO) throws BusinessCheckException {
        MtGoods mtGoods = this.queryGoodsById(updateReqVO.getId());
        if (mtGoods == null) {
            throw exception(GOODS_NOT_FOUND);
        }
        if (CollUtil.isNotEmpty(updateReqVO.getCouponIds())) {
            if (updateReqVO.getCouponIds().size() > 10) {
                throw exception(GOODS_COUPON_TOO_MANY, 10);
            }
            List<MtCoupon> mtCoupons = couponService.queryCouponListByIds(updateReqVO.getCouponIds());
            Map<Integer, MtCoupon> couponMap = convertMap(mtCoupons, MtCoupon::getId);
            for (Integer couponId : updateReqVO.getCouponIds()) {
                MtCoupon mtCoupon = couponMap.get(couponId);
                if (mtCoupon == null) {
                    throw exception(GOODS_COUPON_NOT_FOUND, couponId);
                }
            }
        }
        MtGoods updateObj = new MtGoods();
        BeanUtils.copyProperties(updateReqVO, updateObj);
        mtGoods.setCouponIds(CollUtil.join(updateReqVO.getCouponIds(), ","));
        // 图片处理
        if (updateReqVO.getImages() != null && !updateReqVO.getImages().isEmpty()) {
            updateObj.setLogo(updateReqVO.getImages().get(0));
            updateObj.setImages(JSONObject.toJSONString(updateReqVO.getImages()));
        } else if (StringUtil.isNotEmpty(updateReqVO.getLogo())) {
            updateObj.setLogo(updateReqVO.getLogo());
        }
        updateObj.setUpdateTime(new Date());
        updateObj.setOperator("openapi");
        this.updateById(mtGoods);

        if (!StrUtil.equals(updateReqVO.getIsSingleSpec(), mtGoods.getIsSingleSpec())) {
            if (updateReqVO.getIsSingleSpec().equals(YesOrNoEnum.YES.getKey())) {
                mtGoodsSpecMapper.deleteByGoodsId(mtGoods.getId());
                mtGoodsSkuMapper.deleteByGoodsId(mtGoods.getId());
                return;
            }
            List<MtGoodsSpec> specLs = queryGoodsSpecList(mtGoods.getId());

        }


        // 如果传了规格，则更新规格
        if (updateReqVO.getSpecData() != null && !updateReqVO.getSpecData().isEmpty()) {
            mtGoodsSpecMapper.delete(Wrappers.<MtGoodsSpec>lambdaQuery().eq(MtGoodsSpec::getGoodsId, mtGoods.getId()));
            for (GoodsSpecItemVO specItem : updateReqVO.getSpecData()) {
                if (specItem.getChild() != null) {
                    for (GoodsSpecChildVO child : specItem.getChild()) {
                        MtGoodsSpec spec = new MtGoodsSpec();
                        spec.setGoodsId(mtGoods.getId());
                        spec.setName(specItem.getName());
                        spec.setValue(child.getName());
                        spec.setStatus(StatusEnum.ENABLED.getKey());
                        mtGoodsSpecMapper.insert(spec);
                    }
                }
            }
        }

    }

    @Override
    public PageResult<MtGoods> queryGoodsList(CGoodsListPageReqVO pageReqVO) {
        return mtGoodsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MtGoodsSku> queryGoodsSkuList(Integer goodsId) {
        return mtGoodsSkuMapper.selectSkuLsByGoodsId(goodsId);
    }

    @Override
    public MtGoodsSku getLowestPriceSku(Integer goodsId) {
        return mtGoodsSkuMapper.selectLowestPriceSku(goodsId);
    }

    @Override
    public List<MtGoodsSpec> queryGoodsSpecList(Integer goodsId) {
        return mtGoodsSpecMapper.selectByGoodsId(goodsId);
    }
}
