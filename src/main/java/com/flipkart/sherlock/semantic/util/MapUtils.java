package com.flipkart.sherlock.semantic.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by anurag.laddha on 13/04/17.
 */
public class MapUtils {

    /**
     * Add value for given key to given map.
     * Given map must be initialised already.
     * @param destinationMap: map to add k,v pair to
     * @return true if k,v pair could be added to map, else false
     */
    public static <K, V> boolean addSingleEntryToTargetMapValueSet(Map<K, Set<V>> destinationMap, K key, V value){
        if (destinationMap == null) {
            throw new RuntimeException("Destination map must already be initialised");
        }

        if (key != null && value != null){
            if (!destinationMap.containsKey(key)){
                destinationMap.put(key, new HashSet<>());
            }
            destinationMap.get(key).add(value);
            return true;
        }
        return false;
    }


    /**
     * Add a set of values for given key into given map
     * Given map must be initialised already.
     * @param destinationMap: map to add k,v pair to
     * @param valueSet: set of values to add
     * @return true if k,v pair could be added to map, else false
     */
    public static <K, V> boolean addEntriesToTargetMapValueSet(Map<K, Set<V>> destinationMap, K key, Set<V> valueSet){
        if (destinationMap == null) {
            throw new RuntimeException("Destination map must already be initialised");
        }

        if (key != null && valueSet != null && valueSet.size() > 0){
            if (!destinationMap.containsKey(key)){
                destinationMap.put(key, new HashSet<>());
            }
            destinationMap.get(key).addAll(valueSet);
            return true;
        }
        return false;
    }

    /**
     * Merge second map into first map
     * @return true if merge operation succeeded, else false
     */
    public static <K, V> boolean mergeMapWithValueSet(Map<K, Set<V>> firstMap, Map<K, Set<V>> secondMap){
        if (firstMap == null) {
            throw new RuntimeException("Destination map must already be initialised");
        }

        if (secondMap != null && secondMap.size() > 0){
            secondMap.entrySet().forEach(entry -> {
                addEntriesToTargetMapValueSet(firstMap, entry.getKey(), entry.getValue());
            });

            return true;
        }
        return false;
    }
}

