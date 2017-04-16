package com.flipkart.sherlock.semantic.dao.mysql;

import com.flipkart.sherlock.semantic.dao.mysql.entity.SearchConfig;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

/**
 * Created by anurag.laddha on 03/04/17.
 */
public interface ConfigsDao {

    @SqlQuery("SELECT value FROM sherlock.search_configs WHERE name = :name AND bucket = :bucket ")
    String getConfig(@Bind("name") String fieldName, @Bind("bucket") String bucket);


    @RegisterMapper(SearchConfig.SearchConfigMapper.class)
    @SqlQuery("SELECT * FROM sherlock.search_configs ")
    List<SearchConfig> getAllSearchConfigs();
}
