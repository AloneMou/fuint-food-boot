package com.fuint.repository.mapper;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fuint.common.enums.YesOrNoEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.openapi.v1.goods.product.vo.request.CGoodsListPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.bean.GoodsBean;
import com.fuint.repository.bean.GoodsTopBean;
import com.fuint.repository.model.MtGoods;
import com.fuint.framework.pojo.PageResult;
import com.fuint.repository.request.GoodsStatisticsReqVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 商品 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsMapper extends BaseMapperX<MtGoods> {

    List<MtGoods> getStoreGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("cateId") Integer cateId);

    List<MtGoods> searchStoreGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("keyword") String keyword);

    MtGoods getByGoodsNo(@Param("merchantId") Integer merchantId, @Param("goodsNo") String goodsNo);

    Boolean updateInitSale(@Param("goodsId") Integer goodsId);

    List<GoodsBean> selectGoodsList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("cateId") Integer cateId, @Param("keyword") String keyword);

    List<GoodsTopBean> getGoodsSaleTopList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<GoodsTopBean> getGoodsSaleTopListByStore(GoodsStatisticsReqVO reqVO);


    default PageResult<MtGoods> selectPage(CGoodsListPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<MtGoods>()
                .likeIfPresent(MtGoods::getName, pageReqVO.getName())
                .eqIfPresent(MtGoods::getMerchantId, pageReqVO.getMerchantId())
                .eqIfPresent(MtGoods::getStatus, pageReqVO.getStatus())
                .eqIfPresent(MtGoods::getCateId, pageReqVO.getCateId())
                .eqIfPresent(MtGoods::getStatus, pageReqVO.getStatus())
                .eqIfPresent(MtGoods::getType, pageReqVO.getType())
                .eqIfPresent(MtGoods::getMerchantId, pageReqVO.getMerchantId())
                .gt(StrUtil.equals(YesOrNoEnum.YES.getKey(), pageReqVO.getHasStock()),
                        MtGoods::getStock, 0)
                .lt(StrUtil.equals(YesOrNoEnum.YES.getKey(), pageReqVO.getHasStock()),
                        MtGoods::getStock, 1)
                .and(ObjectUtil.isNotNull(pageReqVO.getStoreId()), ew -> ew
                        .eq(MtGoods::getStoreId, pageReqVO.getStoreId())
                        .or()
                        .eq(MtGoods::getStoreId, 0)
                )
                .orderByAsc(MtGoods::getSort)
                .select(
                        MtGoods::getId,
                        MtGoods::getName,
                        MtGoods::getPrice,
                        MtGoods::getStock,
                        MtGoods::getLogo,
                        MtGoods::getStatus,
                        MtGoods::getType,
                        MtGoods::getCateId,
                        MtGoods::getStoreId,
                        MtGoods::getSalePoint,
                        MtGoods::getCreateTime,
                        MtGoods::getUpdateTime
                )
        );
    }

}
