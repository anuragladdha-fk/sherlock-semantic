package com.flipkart.sherlock.semantic.common.lucene;

import com.flipkart.sherlock.semantic.common.config.Constants;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by anurag.laddha on 24/04/17.
 */

//as-is from previous semantic version
public class FkTokenizerFactory {

    public static StandardTokenizer standardTokenizer(String text) {
        return standardTokenizer(new StringReader(text));
    }

    public static StandardTokenizer standardTokenizer(Reader reader) {
        return new StandardTokenizer(Constants.LUCENE_VERSION, reader);
    }
}
