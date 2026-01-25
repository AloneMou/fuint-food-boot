package com.fuint.repository.mapper;

import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.goods.comment.vo.CommentPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtGoodsComment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品评价 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtGoodsCommentMapper extends BaseMapperX<MtGoodsComment> {

    /**
     * 分页查询评价列表
     *
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    default PageResult<MtGoodsComment> selectCommentPage(CommentPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<MtGoodsComment>()
                .eqIfPresent(MtGoodsComment::getMerchantId, pageReqVO.getMerchantId())
                .eqIfPresent(MtGoodsComment::getStoreId, pageReqVO.getStoreId())
                .eqIfPresent(MtGoodsComment::getGoodsId, pageReqVO.getGoodsId())
                .eqIfPresent(MtGoodsComment::getOrderId, pageReqVO.getOrderId())
                .eqIfPresent(MtGoodsComment::getUserId, pageReqVO.getUserId())
                .eqIfPresent(MtGoodsComment::getCommentType, pageReqVO.getCommentType())
//                .eqIfPresent(MtGoodsComment::getScore, pageReqVO.getScore())
                .eqIfPresent(MtGoodsComment::getIsShow, pageReqVO.getIsShow())
                .eqIfPresent(MtGoodsComment::getStatus, pageReqVO.getStatus())
                .geIfPresent(MtGoodsComment::getCreateTime, pageReqVO.getStartTime())
                .leIfPresent(MtGoodsComment::getCreateTime, pageReqVO.getEndTime())
                .orderByDesc(MtGoodsComment::getCreateTime)
        );
    }

    /**
     * 查询订单下某商品是否已评价
     *
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 评价记录
     */
    default MtGoodsComment selectByOrderAndGoods(Integer orderId, Integer goodsId, Integer userId) {
        return selectOne(new LambdaQueryWrapperX<MtGoodsComment>()
                .eq(MtGoodsComment::getOrderId, orderId)
                .eq(MtGoodsComment::getGoodsId, goodsId)
                .eq(MtGoodsComment::getUserId, userId)
                .eq(MtGoodsComment::getCommentType, 1)
                .ne(MtGoodsComment::getStatus, "D")
        );
    }

    /**
     * 查询订单是否已进行NPS评价
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 评价记录
     */
    default MtGoodsComment selectByOrderAndNps(Integer orderId, Integer userId) {
        return selectOne(new LambdaQueryWrapperX<MtGoodsComment>()
                .eq(MtGoodsComment::getOrderId, orderId)
                .eq(MtGoodsComment::getCommentType, 2)
                .eq(MtGoodsComment::getUserId, userId)
                .ne(MtGoodsComment::getStatus, "D")
        );
    }

    /**
     * 查询订单是否已进行价格评价
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 评价记录
     */
    default MtGoodsComment selectByOrderAndPrice(Integer orderId, Integer userId) {
        return selectOne(new LambdaQueryWrapperX<MtGoodsComment>()
                .eq(MtGoodsComment::getOrderId, orderId)
                .eq(MtGoodsComment::getCommentType, 3)
                .eq(MtGoodsComment::getUserId, userId)
                .ne(MtGoodsComment::getStatus, "D")
        );
    }

    /**
     * 查询商品的平均评分
     *
     * @param goodsId 商品ID
     * @return 平均评分
     */
    @Select("SELECT AVG(SCORE) FROM mt_goods_comment WHERE GOODS_ID = #{goodsId} AND COMMENT_TYPE = 1 AND STATUS != 'D' AND IS_SHOW = 'Y'")
    BigDecimal selectAvgScoreByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 查询商品的评价数量
     *
     * @param goodsId 商品ID
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM mt_goods_comment WHERE GOODS_ID = #{goodsId} AND COMMENT_TYPE = 1 AND STATUS != 'D' AND IS_SHOW = 'Y'")
    Integer selectCountByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 根据评分查询商品的评价数量
     *
     * @param goodsId 商品ID
     * @param score   评分
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM mt_goods_comment WHERE GOODS_ID = #{goodsId} AND COMMENT_TYPE = 1 AND SCORE = #{score} AND STATUS != 'D' AND IS_SHOW = 'Y'")
    Integer selectCountByGoodsIdAndScore(@Param("goodsId") Integer goodsId, @Param("score") Integer score);

    /**
     * 查询用户的评价列表
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    default List<MtGoodsComment> selectListByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapperX<MtGoodsComment>()
                .eq(MtGoodsComment::getUserId, userId)
                .ne(MtGoodsComment::getStatus, "D")
                .orderByDesc(MtGoodsComment::getCreateTime)
        );
    }

    /**
     * 查询店铺的NPS统计
     *
     * @param storeId 店铺ID
     * @return NPS统计数据
     */
    @Select("SELECT COUNT(*) as totalCount, AVG(SCORE) as avgScore FROM mt_goods_comment WHERE STORE_ID = #{storeId} AND COMMENT_TYPE = 2 AND STATUS != 'D' AND IS_SHOW = 'Y'")
    java.util.Map<String, Object> selectNpsStatsByStoreId(@Param("storeId") Integer storeId);

    /**
     * 根据评分范围查询店铺的NPS评价数量
     *
     * @param storeId 店铺ID
     * @param minScore 最小评分
     * @param maxScore 最大评分
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM mt_goods_comment WHERE STORE_ID = #{storeId} AND COMMENT_TYPE = 2 AND SCORE >= #{minScore} AND SCORE <= #{maxScore} AND STATUS != 'D' AND IS_SHOW = 'Y'")
    Integer selectCountByStoreIdAndScoreRange(@Param("storeId") Integer storeId, @Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);

    /**
     * 查询商户的NPS统计
     *
     * @param merchantId 商户ID
     * @return NPS统计数据
     */
    @Select("SELECT COUNT(*) as totalCount, AVG(SCORE) as avgScore FROM mt_goods_comment WHERE MERCHANT_ID = #{merchantId} AND COMMENT_TYPE = 2 AND STATUS != 'D' AND IS_SHOW = 'Y'")
    java.util.Map<String, Object> selectNpsStatsByMerchantId(@Param("merchantId") Integer merchantId);

    /**
     * 根据评分范围查询商户的NPS评价数量
     *
     * @param merchantId 商户ID
     * @param minScore 最小评分
     * @param maxScore 最大评分
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM mt_goods_comment WHERE MERCHANT_ID = #{merchantId} AND COMMENT_TYPE = 2 AND SCORE >= #{minScore} AND SCORE <= #{maxScore} AND STATUS != 'D' AND IS_SHOW = 'Y'")
    Integer selectCountByMerchantIdAndScoreRange(@Param("merchantId") Integer merchantId, @Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore);

    /**
     * 查询商品的评价列表（显示中的）
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    default List<MtGoodsComment> selectShowListByGoodsId(Integer goodsId) {
        return selectList(new LambdaQueryWrapperX<MtGoodsComment>()
                .eq(MtGoodsComment::getGoodsId, goodsId)
                .eq(MtGoodsComment::getCommentType, 1)
                .eq(MtGoodsComment::getIsShow, "Y")
                .ne(MtGoodsComment::getStatus, "D")
                .orderByDesc(MtGoodsComment::getCreateTime)
        );
    }

}
