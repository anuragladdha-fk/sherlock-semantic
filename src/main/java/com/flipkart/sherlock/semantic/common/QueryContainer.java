package com.flipkart.sherlock.semantic.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.sherlock.semantic.common.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by anurag.laddha on 23/04/17.
 */

//From previous version
public class QueryContainer {    //todo:improvement add builder method, based on purpose rename class and remove unnecessary fields, methods
    @JsonProperty
    private String originalQuery;
    @JsonProperty
    private String identifiedBestQuery;
    // This keeps the query after modification from the augmentation layer.
    // where as identifiedBestQuery is the global best query at any point during the flow.
    @JsonProperty
    private String identifiedBestQueryPostAugmentation;
    @JsonProperty
    private final Map<String, Set<String>> augmentations = new LinkedHashMap<String, Set<String>>();   //todo:doubt why linked hashmap
    @JsonProperty
    private String luceneQuery;
    @JsonProperty
    private boolean modified;
    //should we show the augmentation to user (messaging), by default false.
    @JsonIgnore
    private boolean showAugmentation;  //todo:doubt why ignored?
    @JsonProperty
    private String type;
    @JsonProperty
    private boolean disableTextApprox = false;

    private QueryContainer() {}

    public QueryContainer(String originalQuery) {
        this(originalQuery,"augment");   //todo:doubt shouldnt type come as part of constructing this class?
    }

    public QueryContainer(String originalQuery, String type){   //not used elsewhere
        if (originalQuery != null) originalQuery = originalQuery.trim();
        this.originalQuery = originalQuery;
        this.type = type;
    }

    @JsonIgnore
    public boolean isShowAugmentation() {
        return showAugmentation;
    }

    @JsonIgnore
    public void setShowAugmentation(boolean showAugmentation) {
        this.showAugmentation = showAugmentation;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public String getLuceneQuery() {
        return StringUtils.isNotBlank(luceneQuery) ? luceneQuery : originalQuery;
    }

    public void setLuceneQuery(String luceneQuery){
        this.luceneQuery = luceneQuery;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addToType(String type) {
        if (this.type != null) {
            this.type += "," + type;
        }
        else {
            this.type = type;
        }
    }

    public String getIdentifiedBestQuery() {
        if(identifiedBestQuery == null || identifiedBestQuery.length() == 0){
            return getOriginalQuery();
        }
        return identifiedBestQuery;
    }

    public void setIdentifiedBestQuery(String identifiedBestQuery) {
        if (identifiedBestQuery != null) identifiedBestQuery = identifiedBestQuery.trim();
        this.identifiedBestQuery = identifiedBestQuery;
    }

    public String getIdentifiedBestQueryPostAugmentation() {
        if(identifiedBestQueryPostAugmentation == null || identifiedBestQueryPostAugmentation.length() == 0){
            return getOriginalQuery();
        }
        return identifiedBestQueryPostAugmentation;
    }

    public void setIdentifiedBestQueryPostAugmentation(String identifiedBestQueryPostAugmentation) {
        if (identifiedBestQueryPostAugmentation != null) identifiedBestQueryPostAugmentation = identifiedBestQueryPostAugmentation.trim();
        this.identifiedBestQueryPostAugmentation = identifiedBestQueryPostAugmentation;
    }

    public Map<String, Set<String>> getAugmentations() {
        return Collections.unmodifiableMap(augmentations);
    }

    public void addAugmentation(String term, Set<String> augmentations) {
        CollectionUtils.addEntriesToTargetMapValueSet(this.augmentations, term, augmentations);
    }

    public Map<String, Set<String>> setAugmentations(Map<String, Set<String>> augmentations) {
        if(augmentations == null)
            return augmentations;
        else {
            this.augmentations.putAll(augmentations);
            return augmentations;
        }
    }

    public boolean hasUserQueryChangedByAugment() {
        return (getOriginalQuery().equalsIgnoreCase(getIdentifiedBestQueryPostAugmentation()));
    }

    public boolean isDisableTextApprox() {
        return disableTextApprox;
    }

    public void setDisableTextApprox(boolean disableTextApprox) {
        this.disableTextApprox = disableTextApprox;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryContainer that = (QueryContainer) o;
        return Objects.equals(modified, that.modified) &&
            Objects.equals(originalQuery, that.originalQuery) &&
            Objects.equals(identifiedBestQuery, that.identifiedBestQuery) &&
            Objects.equals(augmentations, that.augmentations) &&
            Objects.equals(luceneQuery, that.luceneQuery) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalQuery, identifiedBestQuery, augmentations, luceneQuery, modified, type);
    }

    @Override
    public String toString() {
        return "type=" + type + ", origQuery=" + originalQuery + ", luceneQuery=" + luceneQuery
            + ", isModified=" + modified + ", augmentations=" +  augmentations + ", identifiedBestQuery=" + identifiedBestQuery;
    }
}
