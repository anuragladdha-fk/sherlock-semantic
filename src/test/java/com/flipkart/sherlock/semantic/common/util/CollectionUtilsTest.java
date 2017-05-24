package com.flipkart.sherlock.semantic.common.util;

import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by anurag.laddha on 13/04/17.
 */
public class CollectionUtilsTest {

    @Test
    public void testAddSingleEntryToTargetMapValueSet(){

        Map<String, Set<String>> origMap = new HashMap<>();
        origMap.put("k1", Sets.newHashSet("v1"));

         /* Test if a key value pair is added if the key already exists in map */
        CollectionUtils.addSingleEntryToTargetMapValueSet(origMap, "k1", "v2");
        Assert.assertEquals(1, origMap.keySet().size());
        Assert.assertTrue(Sets.newHashSet("v1", "v2").equals(origMap.get("k1")));

         /* Test if a key value pair is added if a new key the key does not exist in the mapm*/
        CollectionUtils.addSingleEntryToTargetMapValueSet(origMap, "k2", "v");
        Assert.assertEquals(2, origMap.keySet().size());
        Assert.assertTrue(Sets.newHashSet("v").equals(origMap.get("k2")));

        /* Test if exception is thrown for null map */
        boolean exceptionThrowForNullMap = false;
        try {
            CollectionUtils.addSingleEntryToTargetMapValueSet(null, "k1", "v2");
        }
        catch(Exception e){
            exceptionThrowForNullMap = true;
        }

        Assert.assertTrue(exceptionThrowForNullMap);
    }


    @Test
    public void testAddMultipleEntriesToTargetMapValueSet(){

        Map<String, Set<String>> origMap = new HashMap<>();

        //Add some elements to a key (k1)
        CollectionUtils.addEntriesToTargetMapValueSet(origMap, "k1", Sets.newHashSet("a", "b"));
        Assert.assertEquals(1, origMap.keySet().size());
        Assert.assertTrue(Sets.newHashSet("a", "b").equals(origMap.get("k1")));

        //Add some more elements to the same key (k1)
        CollectionUtils.addEntriesToTargetMapValueSet(origMap, "k1", Sets.newHashSet("c", "d"));
        Assert.assertEquals(1, origMap.keySet().size());
        Assert.assertTrue(Sets.newHashSet("a", "b", "c", "d").equals(origMap.get("k1")));

        //Add some elements to another key (k2)
        CollectionUtils.addEntriesToTargetMapValueSet(origMap, "k2", Sets.newHashSet("a", "b"));
        Assert.assertTrue(Sets.newHashSet("k1", "k2").equals(origMap.keySet()));
        Assert.assertTrue(Sets.newHashSet("a", "b").equals(origMap.get("k2")));

        /* Test if exception is thrown for null map */
        boolean exceptionThrowForNullMap = false;
        try {
            CollectionUtils.addEntriesToTargetMapValueSet(null, "k1", Sets.newHashSet("v1"));
        }
        catch(Exception e){
            exceptionThrowForNullMap = true;
        }

        Assert.assertTrue(exceptionThrowForNullMap);
    }


    @Test
    public void testMergeMapWithValueSet(){
        /**
         * Test merging of map1 into map1
         * map 1 should have all the k,v pairs
         */
        Map<String, Set<String>> map1 = new HashMap<>();
        map1.put("k1", Sets.newHashSet("a"));
        map1.put("k2", Sets.newHashSet("a"));

        Map<String, Set<String>> map2 = new HashMap<>();
        map2.put("k1", Sets.newHashSet("b"));
        map2.put("k3", Sets.newHashSet("a"));

        CollectionUtils.mergeMapWithValueSet(map1, map2);

        Assert.assertEquals(3, map1.keySet().size());
        Assert.assertTrue(Sets.newHashSet("k1", "k2", "k3").equals(map1.keySet()));
        Assert.assertTrue(Sets.newHashSet("a", "b").equals(map1.get("k1")));
        Assert.assertTrue(Sets.newHashSet("a").equals(map1.get("k2")));
        Assert.assertTrue(Sets.newHashSet("a").equals(map1.get("k3")));
    }

}