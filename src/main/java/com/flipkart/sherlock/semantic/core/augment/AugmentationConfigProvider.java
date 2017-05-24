package com.flipkart.sherlock.semantic.core.augment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flipkart.sherlock.semantic.common.config.SearchConfigProvider;
import com.flipkart.sherlock.semantic.common.util.SerDeUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by anurag.laddha on 21/04/17.
 */
@Singleton
public class AugmentationConfigProvider {

    static final String augConfigDbKey = "augmentationExperimentConfig";
    static  final String CONTEXT_KEY_SEPARATOR = "--";
    private static final Logger log = LoggerFactory.getLogger(AugmentationConfigProvider.class);
    private static final TypeReference<Map<String, String>> stringMapTypeRef = new TypeReference<Map<String, String>>() {};

    private SearchConfigProvider searchConfigProvider;

    @Inject
    public AugmentationConfigProvider(SearchConfigProvider searchConfigProvider) {
        this.searchConfigProvider = searchConfigProvider;
    }

    //as-is from old semantic service
    public Set<String> getAllDisabledContext(String contextKey){

        Set<String> disabledExperiments = new HashSet<>();
        // handle null case. We still need to return db disabled names.
        if (contextKey == null) contextKey = "";
        // Read the experiment name list from the contextKey.
        String[] experiments = contextKey.split(CONTEXT_KEY_SEPARATOR);

        //Read the config from db.
        Map<String, String> configFromDB = null;
        try {
            configFromDB = SerDeUtils.getValueOrDefault(this.searchConfigProvider.getSearchConfig(augConfigDbKey,
                stringMapTypeRef), null);
        } catch (Exception e) {
            log.error("Error while reading Augmentation search config: ", e);
        }

        if (configFromDB != null) {
            Set<String> contextNamesSet = Sets.newHashSet(experiments);
            for (Map.Entry<String, String> entry : configFromDB.entrySet()) {
                boolean isEnabled = true; // by default everything is enabled.
                if ("false".equalsIgnoreCase(entry.getValue())) {
                    isEnabled = false;
                }

                // flip the status for ones in the context.
                if (contextNamesSet.contains(entry.getKey())) {
                    isEnabled = !isEnabled;
                }

                //just add the ones not enabled.
                if (!isEnabled) {
                    disabledExperiments.add(entry.getKey().trim().toLowerCase());
                }
            }
        }

        return disabledExperiments;
    }
}
