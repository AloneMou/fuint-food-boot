package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.framework.pojo.PageResult;
import com.fuint.openapi.v1.member.user.vo.MtUserPageReqVO;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.bean.MemberTopBean;
import com.fuint.repository.model.MtUser;
import com.fuint.repository.request.MemberStatisticsReqVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 会员个人信息 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtUserMapper extends BaseMapperX<MtUser> {

    List<MtUser> queryMemberByMobile(@Param("merchantId") Integer merchantId, @Param("mobile") String mobile);

    List<MtUser> queryMemberByUnionId(@Param("merchantId") Integer merchantId, @Param("unionId") String unionId);

    List<MtUser> queryMemberByMpOpenId(@Param("merchantId") Integer merchantId, @Param("mpOpenId") String mpOpenId);

    List<MtUser> queryMemberByName(@Param("merchantId") Integer merchantId, @Param("name") String name);

    MtUser queryMemberByOpenId(@Param("merchantId") Integer merchantId, @Param("openId") String openId);

    List<MtUser> findMembersByUserNo(@Param("merchantId") Integer merchantId, @Param("userNo") String userNo);

    void updateActiveTime(@Param("userId") Integer userId, @Param("updateTime") Date updateTime);

    void updateUserBalance(@Param("merchantId") Integer merchantId, @Param("userIds") List<Integer> userIds, @Param("amount") BigDecimal amount);

    void resetMobile(@Param("mobile") String mobile, @Param("userId") Integer userId);

    Long getUserCount(@Param("merchantId") Integer merchantId);

    Long getStoreUserCount(@Param("storeId") Integer storeId);

    Long getUserCountByTime(@Param("merchantId") Integer merchantId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    Long getStoreUserCountByTime(@Param("storeId") Integer storeId, @Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    List<MemberTopBean> getMemberConsumeTopList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId, @Param("startTime") Date startTime, @Param("endTime") Date endTime);

    List<Integer> getUserIdList(@Param("merchantId") Integer merchantId, @Param("storeId") Integer storeId);

    List<MtUser> searchMembers(@Param("merchantId") Integer merchantId, @Param("keyword") String keyword);


    List<MemberTopBean> selectMembersConsumeTopList(MemberStatisticsReqVO reqVO);

    default PageResult<MtUser> selectMemberPage(MtUserPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<MtUser>()
                .eqIfPresent(MtUser::getId, pageReqVO.getId())
                .eqIfPresent(MtUser::getMerchantId, pageReqVO.getMerchantId())
                .eqIfPresent(MtUser::getStoreId, pageReqVO.getStoreId())
                .likeIfPresent(MtUser::getUserNo, pageReqVO.getUserNo())
                .likeIfPresent(MtUser::getMobile, pageReqVO.getMobile())
                .likeIfPresent(MtUser::getName, pageReqVO.getName())
                .eqIfPresent(MtUser::getGroupId, pageReqVO.getGroupId())
                .eqIfPresent(MtUser::getGradeId, pageReqVO.getGradeId())
                .eqIfPresent(MtUser::getStatus, pageReqVO.getStatus())
                .eqIfPresent(MtUser::getIsStaff, pageReqVO.getIsStaff())
                .betweenIfPresent(MtUser::getCreateTime, pageReqVO.getStartTime(), pageReqVO.getEndTime())
                .ne(MtUser::getStatus, StatusEnum.DISABLE.getKey())
                .orderByDesc(MtUser::getCreateTime)
                .orderByDesc(MtUser::getId)
        );
    }

    default List<MtUser> selectUserLsByMobiles(List<String> mobiles) {
        return selectList(new LambdaQueryWrapperX<MtUser>()
                .in(MtUser::getMobile, mobiles)
        );
    }



}
