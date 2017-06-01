package com.flipkart.sherlock.semantic.common.config;

import com.google.common.collect.Sets;
import org.apache.lucene.util.Version;

import java.util.Set;

/**
 * Created by anurag.laddha on 13/04/17.
 */
public class Constants {

    public static final Version LUCENE_VERSION = Version.LUCENE_46;

    public static final String CONTEXT_DEFAULT = "default";
    public static final String DUMMY_KEY = "dummy";

     //TODO this can come from config
    public static final Set<String> stopWordsSet = Sets.newHashSet("&", "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
            "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with");
    //Named
    public static final String GUICE_LOCAL_CACHE_EXPIRY = "localCacheExpiry";
    public static final String GUICE_LOCAL_CACHE_LOADING_EXECUTOR_SERVICE = "localCacheLoadingExecutorService";
    public static final String CORE_TO_URL_MAPPING = "coreToUrlMapping";

}
