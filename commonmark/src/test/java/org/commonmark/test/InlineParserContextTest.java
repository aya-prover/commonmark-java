package org.commonmark.test;

import org.commonmark.internal.InlineParserImpl;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.InlineParserFactory;
import org.commonmark.parser.Parser;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.renderer.html.HtmlRenderer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class InlineParserContextTest {

    @Test
    public void labelShouldBeOriginalNotNormalized() {
        CapturingInlineParserFactory inlineParserFactory = new CapturingInlineParserFactory();

        Parser parser = Parser.builder().inlineParserFactory(inlineParserFactory).build();
        String input = "[link with special label][FooBarBaz]\n\n[foobarbaz]: /url";

        String rendered = HtmlRenderer.builder().build().render(parser.parse(input));

        // Lookup should pass original label to context
        assertEquals(Collections.singletonList("FooBarBaz"), inlineParserFactory.lookups);

        // Context should normalize label for finding reference
        assertEquals("<p><a href=\"/url\">link with special label</a></p>\n", rendered);
    }

    static class CapturingInlineParserFactory implements InlineParserFactory {

        private List<String> lookups = new ArrayList<>();

        @Override
        public InlineParser create(final InlineParserContext inlineParserContext) {
            InlineParserContext wrappedContext = new InlineParserContext() {
                @Override
                public List<DelimiterProcessor> getCustomDelimiterProcessors() {
                    return inlineParserContext.getCustomDelimiterProcessors();
                }

                @Override
                public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
                    lookups.add(label);
                    return inlineParserContext.getLinkReferenceDefinition(label);
                }
            };

            return new InlineParserImpl(wrappedContext);
        }
    }
}
