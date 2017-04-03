package com.flipkart.sherlock.semantic.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

/**
 * Created by anurag.laddha on 03/04/17.
 */
public interface SearchConfigsDao {

    @SqlQuery("SELECT value FROM sherlock.search_configs WHERE name = :name AND bucket = :bucket ")
    String getConfig(@Bind("name") String fieldName, @Bind("bucket") String bucket);
}
