package aem.community.pageshare.core.transformer;


import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.swing.text.html.HTML;
import java.io.IOException;

@SuppressWarnings("squid:S1186")
public class DisableLinkCheckerTransformer extends AbstractTransformer {

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        attr = addNewAttribute(attr, localName);
        contentHandler.startElement(uri, localName, qName, attr);

    }

    private Attributes addNewAttribute(Attributes elAttrs, String localName) {
        AttributesImpl newAttrs = new AttributesImpl(elAttrs);
        if (HTML.Tag.A.toString().equalsIgnoreCase(localName)) {
            newAttrs.addAttribute(null, "x-cq-linkchecker", null, "CDATA", "skip");
        }
        return newAttrs;
    }

    @SuppressWarnings("squid:S1186")
    @Override
    public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
    }

}
