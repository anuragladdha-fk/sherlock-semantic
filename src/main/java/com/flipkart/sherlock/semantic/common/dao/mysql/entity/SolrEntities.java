package com.flipkart.sherlock.semantic.common.dao.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by anurag.laddha on 26/04/17.
 */
public class SolrEntities {

    /**
     * Pojo representing data from solr_facet_field_mapping
     */
    @AllArgsConstructor
    @Getter
    @ToString
    public static class SolrFacetField{
        private String field;
        private String solrField;
        private String metaData;
    }

    public static class SolrFacetFieldMapper implements ResultSetMapper<SolrFacetField> {
        @Override
        public SolrFacetField map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new SolrFacetField(r.getString("field"), r.getString("solr_field"), r.getString("metaData"));
        }
    }
}
