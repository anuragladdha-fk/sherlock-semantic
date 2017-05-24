package com.flipkart.sherlock.semantic.common.dao.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by anurag.laddha on 16/04/17.
 */

@AllArgsConstructor
@Getter
@ToString
public class SearchConfig {
    /**
     * Config name
     */
    private String name;
    /**
     * Type of config value: 'int','double','float','boolean','string','array','assoc'
     */
    private String valueType;
    /**
     * String value of config
     */
    private String configValue;
    /**
     * Experiment name. Config value can be different depending on experiment.
     */
    private String bucket;

    private long lastModifiedTs;

    public static class SearchConfigMapper implements ResultSetMapper<SearchConfig> {
        @Override
        public SearchConfig map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new SearchConfig(r.getString("name"), r.getString("data_type"), r.getString("value"), r.getString("bucket"),
                r.getTimestamp("last_modified").getTime());
        }
    }
}
