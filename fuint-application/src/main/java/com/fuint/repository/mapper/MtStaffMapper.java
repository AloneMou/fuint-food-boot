package com.fuint.repository.mapper;

import com.fuint.common.enums.StatusEnum;
import com.fuint.common.mybatis.query.LambdaQueryWrapperX;
import com.fuint.repository.base.BaseMapperX;
import com.fuint.repository.model.MtStaff;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 店铺员工表 Mapper 接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
public interface MtStaffMapper extends BaseMapperX<MtStaff> {

    int updateStatus(@Param("id") Integer id, @Param("status") String status, @Param("updateTime") Date updateTime);

    MtStaff queryStaffByMobile(@Param("mobile") String mobile);

    MtStaff queryStaffByUserId(@Param("userId") Integer userId);

    default MtStaff selectByMobile(String mobile) {
        return selectOne(new LambdaQueryWrapperX<MtStaff>().eq(MtStaff::getMobile, mobile));
    }

    default MtStaff selectByUserId(Integer userId) {
        return selectOne(new LambdaQueryWrapperX<MtStaff>().eq(MtStaff::getUserId, userId));
    }

    default List<MtStaff> selectListByUserIds(Collection<Integer> userIds) {
        return selectList(new LambdaQueryWrapperX<MtStaff>()
                .in(MtStaff::getUserId, userIds)
                .ne(MtStaff::getAuditedStatus, StatusEnum.DISABLE.getKey())
        );
    }
}
