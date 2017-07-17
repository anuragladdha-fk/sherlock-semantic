package com.flipkart.sherlock.semantic.autosuggest.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dhruv.pancholi on 02/06/17.
 */
public class QuerySanitizer {

    @Getter
    @AllArgsConstructor
    public static class QueryPrefix {
        private String originalQuery;
        private String query;
        private String prefix;
    }

    private static Pattern pattern = Pattern.compile("^(.*?)\\s?([^\\s]*)$");

    public static QueryPrefix getQueryPrefix(String originalQuery) {

        originalQuery = originalQuery.toLowerCase();

        originalQuery = originalQuery.replaceAll("\"", "");

        Matcher matcher = pattern.matcher(originalQuery);
        if (matcher.matches()) return new QueryPrefix(matcher.group(0), matcher.group(1), matcher.group(2));
        else return new QueryPrefix(originalQuery, originalQuery, "");
    }
}
