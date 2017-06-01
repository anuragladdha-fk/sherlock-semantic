package com.flipkart.sherlock.semantic.autosuggest.dao;

import com.flipkart.sherlock.semantic.autosuggest.models.AbParams;
import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao;
import com.flipkart.sherlock.semantic.common.dao.mysql.CompleteTableDao.SearchConfigs;
import com.google.inject.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by dhruv.pancholi on 31/05/17.
 */
public class AbConfigDoa extends AbstractReloadableCache<AbParams> {

    private static AbParams DEFAULT_AB_PARAMS = new AbParams();

    @Inject
    public AbConfigDoa(CompleteTableDao completeTableDao, JsonSeDe jsonSeDe) {
        super(completeTableDao, jsonSeDe, 1, TimeUnit.DAYS);
    }

    @Override
    protected AbParams getFromDB() {
        List<SearchConfigs> searchConfigs = completeTableDao.getSearchConfigs();
        for (SearchConfigs searchConfig : searchConfigs) {
            if (!"autosuggestExperimentConfig".equals(searchConfig.getName())) continue;
            return jsonSeDe.readValue(searchConfig.getValue(), AbParams.class);
        }
        return DEFAULT_AB_PARAMS;
    }
}
