package aem.community.pageshare.core.transformer;


import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.swing.text.html.HTML;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AssetLinkTransformer extends AbstractTransformer {

    private String token;
    private PageShareService pageShareService;

    private Map<String, String> assetMap = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(AssetLinkTransformer.class);

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        atts = updatePath(atts, localName);
        contentHandler.startElement(uri, localName, qName, atts);

    }

    private Attributes updatePath(Attributes elAttrs, String localName) {
        AttributesImpl newAttrs = new AttributesImpl(elAttrs);
        if (HTML.Tag.IMG.toString().equalsIgnoreCase(localName)) {
            String href = newAttrs.getValue("src");
            String path = getNewPath(href);
            newAttrs.setValue(newAttrs.getIndex("src"), path);
        } else if (HTML.Tag.DIV.toString().equalsIgnoreCase(localName) && newAttrs.getValue("data-asset-id") != null) {
            String dataAsset = newAttrs.getValue("data-asset");
            String path = getNewPath(dataAsset);
            newAttrs.setValue(newAttrs.getIndex("data-asset"), path);
            dataAsset = newAttrs.getValue("data-cmp-src");
            path = getNewPath(dataAsset);
            newAttrs.setValue(newAttrs.getIndex("data-cmp-src"), path);
        }
        return newAttrs;
    }

    private String getNewPath(String oldPath) {
        String newPath = null;
        newPath = assetMap.get(oldPath);
        if (newPath == null && token != null) {
            try {
                newPath = pageShareService.createAsset(token, oldPath);
            } catch (PageShareException e) {
                log.error("Exception", e);
            }
            if (newPath != null)
                assetMap.put(oldPath, newPath);
        }
        return newPath;
    }

    @Override
    public void init(ProcessingContext context, ProcessingComponentConfiguration config) throws IOException {
        SlingHttpServletRequest request = context.getRequest();
        token = (String) request.getAttribute("token");
        BundleContext bundleContext = FrameworkUtil.getBundle(PageShareService.class).getBundleContext();
        ServiceReference<?> pageShareServiceReference =
                bundleContext.getServiceReference(PageShareService.class.getName());
        pageShareService = (PageShareService) bundleContext.getService(pageShareServiceReference);
    }

}
