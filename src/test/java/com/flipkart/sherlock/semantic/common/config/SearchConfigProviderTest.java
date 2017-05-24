package com.flipkart.sherlock.semantic.common.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider.*;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 16/04/17.
 */

@RunWith(PowerMockRunner.class)
public class SearchConfigProviderTest {
    @Mock
    private ConfigsDao configsDaoMock;
    @Mock
    private ExecutorService executorServiceMock;
    @Mock
    LoadingCache<String, Map<Key, String>> loadingCacheMock;

    @Test
    public void testPowerMock() throws Exception{

        TypeReference<Set<String>> typeRefStringSet = new TypeReference<Set<String>>() {};
        Map<Key, String> cachedValues = new HashMap<>();
        cachedValues.put(new Key("conf1", "b1"), "val1");
        cachedValues.put(new Key("conf2", "b1"), "[\"pqr\",\"xyz\",\"abc\"]");     //json set of string as values
        cachedValues.put(new Key("conf2", ""), "[\"a\",\"b\",\"c\"]");   //json set of string as values
        cachedValues.put(new Key("conf3", "b1"), "val3");
        cachedValues.put(new Key("conf3", ""), "val3_default");    //empty bucket

        //setup what loading cache should return
        when(loadingCacheMock.get(anyString())).thenReturn(cachedValues);

        SearchConfigProvider searchConfigProvider = new SearchConfigProvider(configsDaoMock, executorServiceMock,30);
        //swap internal loading cache with mocked cache - now loading cache will return what we have set it up with
        Whitebox.setInternalState(searchConfigProvider, "searchConfigCache", loadingCacheMock);

        /**
         * Validate values returned by cache
         */
        Assert.assertEquals("val1", searchConfigProvider.getSearchConfig("conf1", "b1", String.class));

        Set<String> conf2Values = searchConfigProvider.getSearchConfig("conf2", "b1", typeRefStringSet);
        Assert.assertTrue(Sets.newHashSet("pqr", "xyz", "abc").equals(conf2Values));

        Set<String> conf2EmptyBucketValues = searchConfigProvider.getSearchConfig("conf2", typeRefStringSet);
        Assert.assertTrue(Sets.newHashSet("a", "b", "c").equals(conf2EmptyBucketValues));

        Assert.assertEquals("val3_default", searchConfigProvider.getSearchConfig("conf3", String.class));
    }
}