package com.flipkart.sherlock.semantic.core.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SpellingParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anurag.laddha on 23/05/17.
 */
public class SearchRequest {

    public static enum Param {

        QT(CommonParams.QT),
        Q(CommonParams.Q),
        FQ(CommonParams.FQ),
        FL(CommonParams.FL),
        SORT(CommonParams.SORT),
        ROWS(CommonParams.ROWS),
        BF(DisMaxParams.BF),
        ALTQ(DisMaxParams.ALTQ),
        QF(DisMaxParams.QF),
        PF(DisMaxParams.PF),
        BQ(DisMaxParams.BQ),

        DEFTYPE("defType"),
        SPELLCHECK("spellcheck"),
        MM("mm"),
        SPELLCHECK_MAX_COLLATIONS(SpellingParams.SPELLCHECK_MAX_COLLATIONS),
        SPELLCHECK_COUNT(SpellingParams.SPELLCHECK_COUNT),
        SPELLCHECK_Q(SpellingParams.SPELLCHECK_Q),
        SPELLCHECK_COLLATE_EXTENDED_RESULTS(SpellingParams.SPELLCHECK_COLLATE_EXTENDED_RESULTS),
        SPELLCHECK_MAX_COLLATION_TRIES(SpellingParams.SPELLCHECK_MAX_COLLATION_TRIES),
        SPELLCHECK_ACCURACY(SpellingParams.SPELLCHECK_ACCURACY),
        SPELLCHECK_ONLY_MORE_POPULAR(SpellingParams.SPELLCHECK_ONLY_MORE_POPULAR),
        SPELLCHECK_COLLATE(SpellingParams.SPELLCHECK_COLLATE);

        private final String paramName;

        private Param(String s) {
            paramName = s;
        }

        public String getParamName() {
            return paramName;
        }
    }

    private Map<Param, ArrayList<String>> requestParams = new HashMap<>();

    public void addParam(Param param, String value) {
        if (StringUtils.isBlank(value)) {
            this.requestParams.remove(param);
        } else {
            this.requestParams.computeIfAbsent(param, k -> new ArrayList<>());
            this.requestParams.get(param).add(value);
        }
    }

    public void addParam(Param param, List<String> values) {
        if (values != null && values.size() > 0) {
            this.requestParams.computeIfAbsent(param, k -> new ArrayList<>());
            values.forEach(val -> {
                if (StringUtils.isNotBlank(val)) {
                    this.requestParams.get(param).add(val);
                }
            });
        }
    }

    Map<Param, ArrayList<String>> getRequestParams() {
        Map<Param, ArrayList<String>> allRequstParams = new HashMap<>();
        allRequstParams.putAll(this.requestParams);
        return allRequstParams;
    }
}
