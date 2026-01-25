package com.fuint.common.service;

import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.goods.comment.vo.CommentBatchCreateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentCreateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentPageReqVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentRespVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentStatisticsVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentUpdateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.GoodsCommentCreateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.OrderCommentCreateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.PriceCommentCreateReqVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentDeleteReqVO;
import com.fuint.openapi.v1.goods.comment.vo.CommentReplyReqVO;
import com.fuint.repository.model.MtGoodsComment;

import java.util.List;

/**
 * 商品评价业务接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface GoodsCommentService {

    /**
     * 分页查询评价列表
     *
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    PageResult<CommentRespVO> queryCommentPageList(CommentPageReqVO pageReqVO);

    /**
     * 根据ID查询评价详情
     *
     * @param commentId 评价ID
     * @return 评价详情
     */
    CommentRespVO queryCommentById(Integer commentId);

    /**
     * 创建商品评价
     *
     * @param createReqVO 创建请求
     * @return 评价ID
     * @throws BusinessCheckException 业务异常
     */
    Integer createGoodsComment(GoodsCommentCreateReqVO createReqVO) throws BusinessCheckException;

    /**
     * 创建订单NPS评价
     *
     * @param createReqVO 创建请求
     * @return 评价ID
     * @throws BusinessCheckException 业务异常
     */
    Integer createOrderComment(OrderCommentCreateReqVO createReqVO) throws BusinessCheckException;

    /**
     * 创建价格合理性评价
     *
     * @param createReqVO 创建请求
     * @return 评价ID
     * @throws BusinessCheckException 业务异常
     */
    Integer createPriceComment(PriceCommentCreateReqVO createReqVO) throws BusinessCheckException;

    /**
     * 批量创建评价（支持商品评价、订单评价、价格评价一次性提交）
     *
     * @param batchCreateReqVO 批量创建请求
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean createBatchComment(CommentBatchCreateReqVO batchCreateReqVO) throws BusinessCheckException;

    /**
     * 创建评价（兼容旧接口，内部路由到具体方法）
     *
     * @param createReqVO 创建请求
     * @return 评价ID
     * @throws BusinessCheckException 业务异常
     */
    @Deprecated
    Integer createComment(CommentCreateReqVO createReqVO) throws BusinessCheckException;

    /**
     * 更新评价
     *
     * @param updateReqVO 更新请求
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean updateComment(CommentUpdateReqVO updateReqVO) throws BusinessCheckException;

    /**
     * 删除评价（逻辑删除）
     *
     * @param deleteReqVO 删除请求
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean deleteComment(CommentDeleteReqVO deleteReqVO) throws BusinessCheckException;

    /**
     * 商家回复评价
     *
     * @param replyReqVO 回复请求
     * @return 是否成功
     * @throws BusinessCheckException 业务异常
     */
    Boolean replyComment(CommentReplyReqVO replyReqVO) throws BusinessCheckException;

    /**
     * 获取商品评价统计信息
     *
     * @param goodsId 商品ID
     * @return 统计信息
     */
    CommentStatisticsVO getCommentStatistics(Integer goodsId);

    /**
     * 获取店铺NPS评价统计信息
     *
     * @param storeId 店铺ID
     * @return 统计信息
     */
    CommentStatisticsVO getStoreNpsStatistics(Integer storeId);

    /**
     * 获取商户NPS评价统计信息
     *
     * @param merchantId 商户ID
     * @return 统计信息
     */
    CommentStatisticsVO getMerchantNpsStatistics(Integer merchantId);

    /**
     * 获取商户价格评价统计信息
     *
     * @param merchantId 商户ID
     * @return 统计信息
     */
    CommentStatisticsVO getMerchantPriceStatistics(Integer merchantId);

    /**
     * 查询用户的评价列表
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    List<CommentRespVO> queryUserCommentList(Integer userId);

    /**
     * 查询商品的评价列表（仅显示中的）
     *
     * @param goodsId 商品ID
     * @return 评价列表
     */
    List<CommentRespVO> queryGoodsCommentList(Integer goodsId);

    /**
     * 检查订单商品是否已评价
     *
     * @param orderId 订单ID
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 是否已评价
     */
    Boolean checkCommentExists(Integer orderId, Integer goodsId, Integer userId);

    /**
     * 点赞评价
     *
     * @param commentId 评价ID
     * @return 是否成功
     */
    Boolean likeComment(Integer commentId);

}
