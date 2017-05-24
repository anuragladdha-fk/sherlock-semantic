package com.flipkart.sherlock.semantic;

import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.core.augment.AugmentationConfigProvider;
import com.flipkart.sherlock.semantic.core.augment.CachedNegativesDataSource;
import com.flipkart.sherlock.semantic.core.augment.LocalCachedTermAlternativesDataSource;
import com.flipkart.sherlock.semantic.common.util.TestContext;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by anurag.laddha on 22/04/17.
 */
public class SingletonTestIT {

    @Test
    public void testSearchConfigProviderSingleTon(){
        SearchConfigProvider searchConfigProvider1 = TestContext.getInstance(SearchConfigProvider.class);
        SearchConfigProvider searchConfigProvider2 = TestContext.getInstance(SearchConfigProvider.class);
        Assert.assertTrue(searchConfigProvider1 == searchConfigProvider2);
    }

    @Test
    public void testAugmentationConfigProviderSingleTon(){
        AugmentationConfigProvider augmentationConfigProvider1 = TestContext.getInstance(AugmentationConfigProvider.class);
        AugmentationConfigProvider augmentationConfigProvider2 = TestContext.getInstance(AugmentationConfigProvider.class);
        Assert.assertTrue(augmentationConfigProvider1 == augmentationConfigProvider2);
    }

    @Test
    public void testCachedNegativesDataSourceSingleTon(){
        CachedNegativesDataSource cachedNegativesDataSource1 = TestContext.getInstance(CachedNegativesDataSource.class);
        CachedNegativesDataSource cachedNegativesDataSource2 = TestContext.getInstance(CachedNegativesDataSource.class);
        Assert.assertTrue(cachedNegativesDataSource1 == cachedNegativesDataSource2);
    }

    @Test
    public void testLocalCachedAugmentDataSourceSingleTon(){
        LocalCachedTermAlternativesDataSource localCachedTermAlternativesDataSource1 = TestContext.getInstance(LocalCachedTermAlternativesDataSource.class);
        LocalCachedTermAlternativesDataSource localCachedTermAlternativesDataSource2 = TestContext.getInstance(LocalCachedTermAlternativesDataSource.class);
        Assert.assertTrue(localCachedTermAlternativesDataSource1 == localCachedTermAlternativesDataSource2);
    }
}
