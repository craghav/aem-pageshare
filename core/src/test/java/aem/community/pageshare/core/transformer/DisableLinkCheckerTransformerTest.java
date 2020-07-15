package aem.community.pageshare.core.transformer;

import aem.community.pageshare.core.service.PageShareService;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DisableLinkCheckerTransformerTest {

    @Captor
    private ArgumentCaptor<Attributes> attributesCaptor;

    @Mock
    private ContentHandler contentHandler;

    @Test
    public void testAddSkipLinkCheck() throws Exception {
        MockitoAnnotations.initMocks(this);
        DisableLinkCheckerTransformerFactory factory = new DisableLinkCheckerTransformerFactory();

        Transformer transformer = factory.createTransformer();
        transformer.setContentHandler(contentHandler);


        AttributesImpl attr = new AttributesImpl();
        transformer.startElement(null, "a", null, attr);

        Mockito.verify(contentHandler, Mockito.only()).startElement(isNull(), eq("a"), isNull(),
                attributesCaptor.capture());
        Attributes out = attributesCaptor.getValue();
        assertEquals("skip", out.getValue(0));

    }

}
