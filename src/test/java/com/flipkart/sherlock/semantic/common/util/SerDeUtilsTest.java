package com.flipkart.sherlock.semantic.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by anurag.laddha on 15/04/17.
 */
public class SerDeUtilsTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup(){
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }


    @Test
    public void testCast() throws Exception{
        String intString = "1";
        String floatString = "10.0";
        String booleanString = "true";

        Assert.assertEquals(1, SerDeUtils.cast(intString, Integer.class).intValue());
        Assert.assertEquals(1, SerDeUtils.cast(intString, Short.class).intValue());
        Assert.assertEquals(10f, SerDeUtils.cast(floatString, Float.class));
        Assert.assertEquals(10d, SerDeUtils.cast(floatString, Double.class));
        Assert.assertTrue(SerDeUtils.cast(booleanString, Boolean.class));
        Assert.assertEquals("abc", SerDeUtils.cast("abc", String.class));
    }

    @Test
    public void testCastCollections() throws Exception{
        Set<String> stringSet = Sets.newHashSet("abc", "pqr", "xyz");
        System.out.println(objectMapper.writeValueAsString(stringSet));
        Set<String> deserialisedStringSet = SerDeUtils.castToGeneric(objectMapper.writeValueAsString(stringSet), new TypeReference<Set<String>>() {});
        System.out.println(deserialisedStringSet);
        Assert.assertTrue(stringSet.equals(deserialisedStringSet));
    }

    @Test
    public void testMapSerde() throws Exception{
        Map<String, List<Integer>> stringToIntListMap = new HashMap<>();
        stringToIntListMap.put("1", Lists.newArrayList(1, 2));

        Map<String, Map<String, Integer>> mapOfMap = new HashMap<>();
        mapOfMap.put("1", ImmutableMap.of("a", 1, "b", 2, "c", 3));

        Map<String, List<Integer>> deserialisedMapOfIntList = SerDeUtils.castToGeneric(objectMapper.writeValueAsString(stringToIntListMap), new TypeReference<Map<String, List<Integer>>>() {});
        Assert.assertTrue(stringToIntListMap.equals(deserialisedMapOfIntList));
        System.out.println(deserialisedMapOfIntList);

        Map<String, Map<String, Integer>> deserialisedMapOfMap = SerDeUtils.castToGeneric(objectMapper.writeValueAsString(mapOfMap), new TypeReference<Map<String, Map<String, Integer>>>() {});
        Assert.assertTrue(mapOfMap.equals(deserialisedMapOfMap));
        System.out.println(deserialisedMapOfMap);
    }
}