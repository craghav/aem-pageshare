package aem.community.pageshare.core.transformer;

import aem.community.pageshare.core.service.PageShareService;
import com.day.cq.commons.Externalizer;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.mailer.MessageGatewayService;
import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.MockComponentContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.rewriter.ProcessingComponentConfiguration;
import org.apache.sling.rewriter.ProcessingContext;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.testing.mock.osgi.MockBundle;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.ContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.junit.runner.RunWith;

import java.io.OutputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class AssetLinkTransformerTest {

    @Mock
    private PageShareService pageShare;

    @Mock
    private ContentHandler contentHandler;

    @Mock
    private MockSlingHttpServletRequest request;

    @Mock
    private ProcessingContext context;

    @Captor
    private ArgumentCaptor<Attributes> attributesCaptor;

    @Before
    public void setUp() throws Exception{
        PowerMockito.mockStatic(FrameworkUtil.class);
        Bundle bundle = Mockito.mock(Bundle.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        PowerMockito.when(FrameworkUtil.getBundle(PageShareService.class)).thenReturn(bundle);
        Mockito.when(bundle.getBundleContext()).thenReturn(bundleContext);
        Mockito.when(bundleContext.getServiceReference(anyString())).thenReturn(Mockito.mock(ServiceReference.class));
        Mockito.when(bundleContext.getService(any(ServiceReference.class))).thenReturn(pageShare);


    }

    @Test
    public void testUpdateAssetLink() throws Exception {
        AssetLinkTransformerFactory factory = new AssetLinkTransformerFactory();

        Transformer transformer = factory.createTransformer();
        transformer.setContentHandler(contentHandler);
        ProcessingComponentConfiguration config = Mockito.mock(ProcessingComponentConfiguration.class);


        Mockito.when(context.getRequest()).thenReturn(request);
        Mockito.when(request.getAttribute(anyString())).thenReturn("1874840739n");

        Mockito.when(pageShare.createAsset(anyString(),anyString())).thenReturn("/bin/viewPage?type=asset&assetToken=12121212&pageToken=1231231213"  );


        AttributesImpl imageWithSrc = new AttributesImpl();
        imageWithSrc.addAttribute("", "src", "src", "CDATA", "/content/dam/demo.jpg");
        transformer.init(context, config);
        transformer.startElement(null, "img", null, imageWithSrc);

        Mockito.verify(contentHandler).startElement(isNull(), eq("img"), isNull(),
                attributesCaptor.capture());
        List<Attributes> values = attributesCaptor.getAllValues();
        assertEquals("/bin/viewPage?type=asset&assetToken=12121212&pageToken=1231231213", values.get(0).getValue(0));

    }

}
