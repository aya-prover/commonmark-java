module org.commonmark {
    exports org.commonmark;
    exports org.commonmark.node;
    exports org.commonmark.parser;
    exports org.commonmark.parser.block;
    exports org.commonmark.parser.delimiter;
    exports org.commonmark.renderer;
    exports org.commonmark.renderer.html;
    exports org.commonmark.renderer.text;

    // accessed by front.matter and gfm.tables extensions
    exports org.commonmark.internal;
    exports org.commonmark.internal.util;
}
