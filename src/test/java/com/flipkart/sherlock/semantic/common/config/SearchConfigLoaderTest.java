package com.flipkart.sherlock.semantic.common.config;

import com.flipkart.sherlock.semantic.common.dao.mysql.ConfigsDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.entity.SearchConfig;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider.*;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 16/04/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SearchConfigLoaderTest {

    @Mock
    private ConfigsDao configsDaoMock;
    @Mock
    private ExecutorService executorServiceMock;

    @Test
    public void testLoadSearchConfigs(){
        List<SearchConfig> searchConfigs = new ArrayList<>();
        searchConfigs.add(new SearchConfig("conf1", "string", "val1", "b1", 123l));
        searchConfigs.add(new SearchConfig("conf1", "string", "val2", "b1", 345l));
        searchConfigs.add(new SearchConfig("conf2", "array", "[\"pqr\",\"xyz\",\"abc\"]", "b2", 345l));
        searchConfigs.add(new SearchConfig("conf3", "string", "val3", "", 345l));   //case with empty bucket

        when(configsDaoMock.getAllSearchConfigs()).thenReturn(searchConfigs);

        SearchConfigLoader searchConfigLoader = new SearchConfigLoader(configsDaoMock, executorServiceMock);
        Map<Key, String> configValues = searchConfigLoader.getAllSearchConfigs();

        System.out.println(configValues.keySet());
        System.out.println(configValues);

        //Validate value with latest timestamp is chosen in case key has more than one values
        Assert.assertEquals(3, configValues.keySet().size());
        Assert.assertEquals("val2", configValues.get(new Key("conf1", "b1")));

        Assert.assertEquals("[\"pqr\",\"xyz\",\"abc\"]", configValues.get(new Key("conf2", "b2")));
        Assert.assertEquals("val3", configValues.get(new Key("conf3", "")));
    }
}