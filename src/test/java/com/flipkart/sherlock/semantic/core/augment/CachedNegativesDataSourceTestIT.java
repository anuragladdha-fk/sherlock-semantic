package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by anurag.laddha on 18/04/17.
 */

/**
 * Integration test for negatives data sources.
 * File name must end in 'IT'
 */
public class CachedNegativesDataSourceTestIT {

    @Test
    public void testGetNegatives(){
        CachedNegativesDataSource cachedNegativesDataSource = TestContext.getInstance(CachedNegativesDataSource.class);
        System.out.println(cachedNegativesDataSource.getAllNegatives().size());
        Assert.assertTrue(cachedNegativesDataSource.getAllNegatives().size() > 0);
        Assert.assertTrue(cachedNegativesDataSource.containsNegative("PursEUS"));
    }
}