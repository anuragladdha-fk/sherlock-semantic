package com.flipkart.sherlock.semantic.common.lucene;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by anurag.laddha on 24/04/17.
 */

//as-is from previous semantic version
public class FKTokenFilter extends TokenFilter {
	CharTermAttribute termAttr;

	public FKTokenFilter(TokenStream input) {
		super(input);
		termAttr = input.addAttribute(CharTermAttribute.class);
	}

	public List<String> getTokens() throws IOException {
		List<String> tokens = new LinkedList<String>();
		reset();
		while (incrementToken()) {
			tokens.add(termAttr.toString());
		}
		return tokens;
	}

	@Override
	public final boolean incrementToken() throws IOException {
		return input.incrementToken();
	}
}
