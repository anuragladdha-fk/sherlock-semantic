package com.flipkart.sherlock.semantic.core.augment;

import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.dao.mysql.AugmentationDao;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by anurag.laddha on 18/04/17.
 */

@RunWith(PowerMockRunner.class)
public class CachedNegativesDataSourceTest {

    @Mock
    AugmentationDao augmentationDaoMock;
    @Mock
    SearchConfigProvider searchConfigProviderMock;
    @Mock
    ExecutorService executorServiceMock;
    @Mock
    LoadingCache<String, Set<String>> negativeTermsCacheMock;

    @Test
    public void testGettingNegatives() throws Exception{

        when(negativeTermsCacheMock.get(anyString())).thenReturn(Sets.newHashSet("neg1", "neg2", "neg3", "neg4"));

        CachedNegativesDataSource negativesDataSource = new CachedNegativesDataSource(augmentationDaoMock, searchConfigProviderMock,
            executorServiceMock, 30);

        //swap internal loading cache with mocked cache - now loading cache will return what we have set it up with
        Whitebox.setInternalState(negativesDataSource, "negativeTermsCache", negativeTermsCacheMock);

        System.out.println(negativesDataSource.getAllNegatives());
        Assert.assertTrue(negativesDataSource.containsNegative("neg1"));
        Assert.assertTrue(Sets.newHashSet("neg1", "neg2", "neg3", "neg4").equals(negativesDataSource.getAllNegatives()));
    }
}