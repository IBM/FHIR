/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.model.visitor;

import org.owasp.encoder.Encode;

import com.ibm.fhir.model.builder.Builder;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Xhtml;

/**
 * Encodes the string elements of the visited resource for use within a given context.
 * The XHTML narrative text is left unencoded as this content is validated elsewhere in the model.
 */
public class EncodingVisitor<T extends Resource> extends CopyingVisitor<T> {
    /**
     * The context for which the string elements should be encoded.
     * These roughly align with the OWASP Encoder types, but they are duplicated here because
     * the OWASP encoder library doesn't make it easy to pass the encoder type at runtime.
     */
    public enum EncodingContext {
        HTML,
        HTML_CONTENT,
        HTML_ATTRIBUTE,
        JAVA,
        JAVASCRIPT
    }

    private final EncodingContext context;

    /**
     * @param context the context for which to encode
     */
    public EncodingVisitor(EncodingContext context) {
        this.context = context;
    }

    @Override
    public boolean visit(java.lang.String elementName, int elementIndex, Xhtml xhtml) {
        // visit the id and extensions but not the value so that we avoid replacing the XHTML content
        visit("id", xhtml.getId());
        for (Extension extension : xhtml.getExtension()) {
            extension.accept(this);
        }
        return false;
    }

    @Override
    public void visit(java.lang.String elementName, java.lang.String value) {
        if(value == null) {
            return;
        }

        String newValue = encode(value);
        // we can compare the string identity because the OWASP encoder has an optimization
        // that returns the exact string that was passed when there is no encoding needed
        if (newValue == value) {
            return;
        }

        Builder<?> builder = getBuilder();
        if (builder instanceof com.ibm.fhir.model.type.String.Builder) {
            ((com.ibm.fhir.model.type.String.Builder) builder).value(newValue);
            markDirty();
        } else if (builder instanceof Extension.Builder && "url".equals(elementName)) {
            ((Extension.Builder) getBuilder()).url(newValue);
            markDirty();
        } else if (builder instanceof Element.Builder && "id".equals(elementName)) {
            ((Element.Builder) getBuilder()).id(newValue);
            markDirty();
        } else {
            throw new UnsupportedOperationException("Encoding is not yet support for element '" + elementName + "' of builder "+ builder.getClass().getName());
        }
    }

    private java.lang.String encode(java.lang.String string) {
        switch(context) {
        case JAVA:          return Encode.forJava(string);
        case JAVASCRIPT:    return Encode.forJavaScript(string);
        case HTML_CONTENT:  return Encode.forHtmlContent(string);
        case HTML_ATTRIBUTE:return Encode.forHtmlAttribute(string);
        case HTML:
        default:
            return Encode.forHtml(string);
        }
    }
}
