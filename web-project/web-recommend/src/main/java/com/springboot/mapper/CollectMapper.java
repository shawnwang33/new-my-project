package com.springboot.mapper;

import com.springboot.pojo.CollectArticleVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CollectMapper {

    @Select("select count(1) from collect where userId = #{userId} and newsId = #{newsId}")
    Integer existsByUserAndNews(@Param("userId") Long userId, @Param("newsId") Long newsId);

    @Select("select count(1) from collect where newsId = #{newsId}")
    Integer countByNewsId(@Param("newsId") Long newsId);

    @Select("select ifnull(max(CollectID), 0) + 1 from collect")
    Integer nextCollectId();

    @Insert("insert into collect(CollectID, userId, newsId, collectTime) values(#{collectId}, #{userId}, #{newsId}, #{collectTime})")
    void insert(@Param("collectId") Integer collectId,
                @Param("userId") Long userId,
                @Param("newsId") Long newsId,
                @Param("collectTime") LocalDateTime collectTime);

    @Update("delete from collect where userId = #{userId} and newsId = #{newsId}")
    void deleteByUserAndNews(@Param("userId") Long userId, @Param("newsId") Long newsId);

    @Update({
            "<script>",
            "delete from collect",
            "where userId = #{userId}",
            "and newsId in",
            "<foreach collection='newsIds' item='newsId' open='(' separator=',' close=')'>",
            "#{newsId}",
            "</foreach>",
            "</script>"
    })
    Integer deleteBatchByUserAndNewsIds(@Param("userId") Long userId, @Param("newsIds") List<Long> newsIds);

    @Select({
            "<script>",
            "select c.CollectID as collectId, c.userId as userId, c.newsId as newsId, c.collectTime as collectTime,",
            "a.title as title, a.summary as summary, a.cover_url as coverUrl, a.source as source,",
            "nc.name as categoryName, a.publish_time as publishTime",
            "from collect c",
            "left join news_article a on c.newsId = a.id",
            "left join news_category nc on a.category_id = nc.id",
            "where c.userId = #{userId}",
            "and (a.status = 1 or a.status is null)",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by c.collectTime desc",
            "</script>"
    })
    List<CollectArticleVO> listByUser(@Param("userId") Long userId, @Param("keyword") String keyword);
}
