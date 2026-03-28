package com.springboot.mapper;

import com.springboot.pojo.NewsArticle;
import com.springboot.pojo.NewsCategory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NewsMapper {

    @Select("select id, name, sort_no from news_category order by sort_no asc, id asc")
    List<NewsCategory> listCategories();

    @Select({
            "<script>",
            "select count(1)",
            "from news_article a",
            "where a.status = 1",
            "<if test='categoryId != null'>",
            "and a.category_id = #{categoryId}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "</script>"
    })
    Long countRecommendNews(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select count(1)",
            "from user_recommend ur",
            "inner join news_article a on ur.news_id = a.id",
            "where ur.user_id = #{userId}",
            "and a.status = 1",
            "<if test='categoryId != null'>",
            "and a.category_id = #{categoryId}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "</script>"
    })
    Long countPersonalizedRecommendNews(@Param("userId") Long userId,
                                        @Param("categoryId") Long categoryId,
                                        @Param("keyword") String keyword);

    @Select({
            "<script>",
            "select a.id, a.title, a.summary, a.cover_url, a.source, a.category_id, c.name as category_name,",
            "a.publish_time, a.view_count, a.recommend_score, a.status",
            "from news_article a",
            "left join news_category c on a.category_id = c.id",
            "where a.status = 1",
            "<if test='categoryId != null'>",
            "and a.category_id = #{categoryId}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by a.publish_time desc, a.recommend_score desc",
            "limit #{offset}, #{pageSize}",
            "</script>"
    })
    List<NewsArticle> listRecommendNewsPage(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize);

    @Select({
            "<script>",
            "select a.id, a.title, a.summary, a.cover_url, a.source, a.category_id, c.name as category_name,",
            "a.publish_time, a.view_count, a.recommend_score, a.status",
            "from user_recommend ur",
            "inner join news_article a on ur.news_id = a.id",
            "left join news_category c on a.category_id = c.id",
            "where ur.user_id = #{userId}",
            "and a.status = 1",
            "<if test='categoryId != null'>",
            "and a.category_id = #{categoryId}",
            "</if>",
            "<if test='keyword != null and keyword != \"\"'>",
            "and (a.title like concat('%', #{keyword}, '%') or a.summary like concat('%', #{keyword}, '%'))",
            "</if>",
            "order by ur.rank_no asc, ur.score desc, a.publish_time desc",
            "limit #{offset}, #{pageSize}",
            "</script>"
    })
    List<NewsArticle> listPersonalizedRecommendNewsPage(@Param("userId") Long userId,
                                                        @Param("categoryId") Long categoryId,
                                                        @Param("keyword") String keyword,
                                                        @Param("offset") Integer offset,
                                                        @Param("pageSize") Integer pageSize);

    @Select("select a.id, a.title, a.summary, a.cover_url, a.source, a.category_id, c.name as category_name, " +
            "a.publish_time, a.view_count, a.recommend_score, a.status " +
            "from news_article a left join news_category c on a.category_id = c.id " +
            "where a.status = 1 order by a.view_count desc, a.publish_time desc limit 10")
    List<NewsArticle> listHotNews();

    @Update("update news_article set view_count = view_count + 1 where id = #{id}")
    void increaseViewCount(@Param("id") Long id);

    @Select("select ifnull(max(RecordID), 0) + 1 from browse")
    Integer nextBrowseId();

    @Insert("insert into browse(RecordID, userId, newsId, browseTime) values(#{recordId}, #{userId}, #{newsId}, #{browseTime})")
    void insertBrowse(@Param("recordId") Integer recordId,
                      @Param("userId") Long userId,
                      @Param("newsId") Long newsId,
                      @Param("browseTime") LocalDateTime browseTime);

    @Select("select a.id, a.title, a.summary, a.content, a.cover_url, a.source, " +
            "a.category_id, c.name as category_name, a.publish_time, a.view_count, a.recommend_score, a.status " +
            "from news_article a " +
            "left join news_category c on a.category_id = c.id " +
            "where a.id = #{id} and a.status = 1")
    NewsArticle getNewsDetailById(@Param("id") Long id);
}
