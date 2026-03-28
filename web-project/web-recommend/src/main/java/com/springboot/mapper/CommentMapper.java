package com.springboot.mapper;

import com.springboot.pojo.NewsComment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Select("select commentId as id, userId as userId, newsId as newsId, " +
            "ifnull(userAccount, concat('用户', userId)) as userAccount, " +
            "commentContent as content, ifnull(likeCount, 0) as likeCount, commentTime as createTime " +
            "from `comment` where newsId = #{newsId} order by commentTime desc, commentId desc")
    List<NewsComment> listByNewsId(@Param("newsId") Long newsId);

    @Select("select count(1) from `comment` where newsId = #{newsId}")
    Integer countByNewsId(@Param("newsId") Long newsId);

    @Insert("insert into `comment`(userId, newsId, commentTime, commentContent, userAccount, likeCount) " +
            "values(#{userId}, #{newsId}, #{createTime}, #{content}, #{userAccount}, #{likeCount})")
    void insert(NewsComment comment);
}
