package com.flipkart.sherlock.semantic.core.augment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Created by anurag.laddha on 21/04/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class AugmentationConfigProviderTest {

    @Mock
    SearchConfigProvider searchConfigProviderMock;

    @Test
    public void testGetAllDisabledContext(){

        //k4 is disabled by default, others are enabled
        Map<String, String> configMap = ImmutableMap.of("k1", "true", "k2", "true", "k3", "true", "k4", "false");
        when(searchConfigProviderMock.getSearchConfig(eq(AugmentationConfigProvider.augConfigDbKey),
            Matchers.<TypeReference<Map<String, String>>>any())).thenReturn(configMap);

        AugmentationConfigProvider augmentationConfigProvider = new AugmentationConfigProvider(searchConfigProviderMock);

        //Test if context disabled in db show up as disabled
        System.out.println(augmentationConfigProvider.getAllDisabledContext(""));
        Assert.assertTrue(Sets.newHashSet("k4").equals(augmentationConfigProvider.getAllDisabledContext(""))); //k4 is disabled by default

        /**
         *  Test if context enabled state gets toggled.
         *  Now k1,k2 should be disabled and k4 should get enabled.
         *  k3 stays as-is (enabled)
         */

        Set<String> contextsToToggle = Sets.newHashSet("k1", "k2", "k4");
        String contextKey = Joiner.on(AugmentationConfigProvider.CONTEXT_KEY_SEPARATOR).join(contextsToToggle);
        System.out.println(augmentationConfigProvider.getAllDisabledContext(contextKey));
        Assert.assertTrue(Sets.newHashSet("k1", "k2").equals(augmentationConfigProvider.getAllDisabledContext(contextKey)));
    }
}