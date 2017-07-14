package com.flipkart.sherlock.semantic.common.dao.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by anurag.laddha on 03/04/17.
 */
public interface CompleteTableDao {

    @Data
    @AllArgsConstructor
    public static class Stores implements Serializable {
        private String id;
        private String title;
        private String core;
        private String category;
        private String vertical;
        private String filters;
        private String metadata;
        private int active;
        private String options;
    }

    public static class StoreMapper implements ResultSetMapper<Stores> {
        @Override
        public Stores map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new Stores(
                    resultSet.getString("id"),
                    resultSet.getString("title"),
                    resultSet.getString("core"),
                    resultSet.getString("category"),
                    resultSet.getString("vertical"),
                    resultSet.getString("filters"),
                    resultSet.getString("metadata"),
                    resultSet.getInt("active"),
                    resultSet.getString("options")
            );
        }
    }

    @Mapper(StoreMapper.class)
    @SqlQuery("SELECT * FROM stores")
    List<Stores> getStores();


    @Data
    @AllArgsConstructor
    public static class StorePathRedirect implements Serializable {
        private String oldPath;
        private String newPath;
    }

    public static class StorePathRedirectMapper implements ResultSetMapper<StorePathRedirect> {
        @Override
        public StorePathRedirect map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new StorePathRedirect(
                    resultSet.getString("old_path"),
                    resultSet.getString("new_path")
            );
        }
    }

    @Mapper(StorePathRedirectMapper.class)
    @SqlQuery("SELECT * FROM store_path_redirect")
    List<StorePathRedirect> getStorePathRedirect();


    @Data
    @AllArgsConstructor
    public static class AutoSuggestDisabled implements Serializable {
        private String prefix;
    }

    public static class AutoSuggestDisabledQueriesMapper implements ResultSetMapper<AutoSuggestDisabled> {
        @Override
        public AutoSuggestDisabled map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new AutoSuggestDisabled(resultSet.getString("prefix"));
        }
    }

    @Mapper(AutoSuggestDisabledQueriesMapper.class)
    @SqlQuery("SELECT * FROM autosuggest_disabled_queries")
    List<AutoSuggestDisabled> getAutoSuggestDisabledQueries();


    @Data
    @AllArgsConstructor
    public static class SearchConfigs implements Serializable {
        private String name;
        private String data_type;
        private String value;
        private String bucket;
        private String lastModified;
    }

    public static class SearchConfigsMapper implements ResultSetMapper<SearchConfigs> {
        @Override
        public SearchConfigs map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new SearchConfigs(resultSet.getString("name"),
                    resultSet.getString("data_type"),
                    resultSet.getString("value"),
                    resultSet.getString("bucket"),
                    resultSet.getString("last_modified"));
        }
    }

    @Mapper(SearchConfigsMapper.class)
    @SqlQuery("SELECT * FROM search_configs")
    List<SearchConfigs> getSearchConfigs();



    @Data
    @AllArgsConstructor
    public static class StorePathMetaData implements Serializable {
        private String storePath;
        private String metadata;
    }

    public static class StorePathMetaDataMapper implements ResultSetMapper<StorePathMetaData> {
        @Override
        public StorePathMetaData map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new StorePathMetaData(resultSet.getString("path-id"),
                    resultSet.getString("metadata"));
        }
    }

    @Mapper(StorePathMetaDataMapper.class)
    @SqlQuery("SELECT * FROM `store-path`")
    List<StorePathMetaData> getStorePathCanonicalTitles();
}
