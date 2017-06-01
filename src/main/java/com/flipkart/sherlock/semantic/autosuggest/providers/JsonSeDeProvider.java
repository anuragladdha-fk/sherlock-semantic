package com.flipkart.sherlock.semantic.autosuggest.providers;

import com.flipkart.sherlock.semantic.autosuggest.utils.JsonSeDe;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Created by dhruv.pancholi on 30/05/17.
 */
public class JsonSeDeProvider extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    JsonSeDe jsonSeDeProvider() {
        return JsonSeDe.getInstance();
    }
}
