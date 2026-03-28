package com.springboot.mapper;

import com.springboot.pojo.Admin;
import com.springboot.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegisterMapper {

    @Select("select count(1) from `user` where userAccount = #{account}")
    Integer countUserByAccount(@Param("account") String account);

    @Select("select count(1) from `user` where userName = #{name}")
    Integer countUserByName(@Param("name") String name);

    @Select("select ifnull(max(userId), 10000) from `user`")
    Long maxUserId();

    @Insert("insert into `user`(userId, userName, userAccount, avatar, Introduction, password) values(#{userId}, #{userName}, #{userAccount}, #{avatar}, #{introduction}, #{password})")
    void insertUser(User user);

    @Select("select count(1) from admin where AdminAccount = #{account}")
    Integer countAdminByAccount(@Param("account") String account);

    @Select("select count(1) from admin where AdminName = #{name}")
    Integer countAdminByName(@Param("name") String name);

    @Select("select ifnull(max(AdminId), 9000) from admin")
    Long maxAdminId();

    @Insert("insert into admin(AdminId, AdminName, AdminAccount, password) values(#{adminId}, #{adminName}, #{adminAccount}, #{password})")
    void insertAdmin(Admin admin);
}
