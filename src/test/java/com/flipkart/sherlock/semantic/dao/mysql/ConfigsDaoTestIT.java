package com.flipkart.sherlock.semantic.dao.mysql;

import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SearchConfig;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SolrEntities;
import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by anurag.laddha on 26/04/17.
 */

/**
 * Integration test for configs dao.
 * File name must end in 'IT'
 */
public class ConfigsDaoTestIT {

    @Test
    public void testGetSolrFacetFieldMappings(){
        ConfigsDao configsDao = TestContext.getInstance(ConfigsDao.class);
        List<SolrEntities.SolrFacetField> solrFacetFields = configsDao.getAllSolrFacetFieldMappings();
        System.out.println(solrFacetFields);
        Assert.assertTrue(solrFacetFields.size() > 0);
    }

    @Test
    public void testGetSearchConfigs(){
        ConfigsDao configsDao = TestContext.getInstance(ConfigsDao.class);
        List<SearchConfig> allSearchConfigs = configsDao.getAllSearchConfigs();
        System.out.println("Number of search configs: " + allSearchConfigs.size());
        Assert.assertTrue(allSearchConfigs.size() > 0);
    }
}