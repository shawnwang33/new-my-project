package com.springboot.mapper;

import com.springboot.pojo.BrowseHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BrowseMapper {

    @Select({
            "<script>",
            "select b.RecordID as recordId, b.userId as userId, b.newsId as newsId, b.browseTime as browseTime,",
            "a.title as title, a.summary as summary, a.cover_url as coverUrl, a.source as source,",
            "nc.name as categoryName, a.publish_time as publishTime",
            "from browse b",
            "left join news_article a on b.newsId = a.id",
            "left join news_category nc on a.category_id = nc.id",
            "where b.userId = #{userId}",
            "and (a.status = 1 or a.status is null)",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by b.browseTime",
            "<choose>",
            "<when test='ascOrder != null and ascOrder'>asc</when>",
            "<otherwise>desc</otherwise>",
            "</choose>",
            "</script>"
    })
    List<BrowseHistoryVO> listByUser(@Param("userId") Long userId,
                                     @Param("keyword") String keyword,
                                     @Param("ascOrder") Boolean ascOrder);

    @Update("delete from browse where userId = #{userId}")
    Integer clearByUser(@Param("userId") Long userId);
}
