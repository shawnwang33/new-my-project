package com.springboot.mapper;

import com.springboot.pojo.Admin;
import com.springboot.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginMapper {

    @Select("select userId as userId, userName as userName, userAccount as userAccount, avatar as avatar, Introduction as introduction, password from `user` where userAccount = #{account} limit 1")
    User findUserByAccount(@Param("account") String account);

    @Select("select userId as userId, userName as userName, userAccount as userAccount, avatar as avatar, Introduction as introduction, password from `user` where userId = #{userId} limit 1")
    User findUserById(@Param("userId") Long userId);

    @Select("select AdminId as adminId, AdminName as adminName, AdminAccount as adminAccount, password from admin where AdminAccount = #{account} limit 1")
    Admin findAdminByAccount(@Param("account") String account);

    @Update("update `user` set password = #{password} where userId = #{userId}")
    void updateUserPasswordById(@Param("userId") Long userId, @Param("password") String password);

    @Update("update admin set password = #{password} where AdminId = #{adminId}")
    void updateAdminPasswordById(@Param("adminId") Long adminId, @Param("password") String password);
}
