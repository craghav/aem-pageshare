package aem.community.pageshare.core.service.impl;

import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.service.impl.dto.ResponseData;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import com.day.cq.commons.Externalizer;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.mailer.MailingException;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.commons.mail.HtmlEmail;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith({AemContextExtension.class})
public class PageShareServiceTest {

    @Mock
    private RequestResponseFactory requestResponseFactory;

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private MessageGatewayService messageGatewayService;

    @Mock
    private SlingRequestProcessor slingRequestProcessor;

    @Mock
    private Externalizer externalizer;

    @Mock
    private MessageGateway<HtmlEmail> messageGatewayHtmlEmail;


    private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

    MockSlingHttpServletRequest request;
    MockSlingHttpServletResponse response;
    MockSlingHttpServletResponse assetResponse;
    ResourceResolver mockResourceResolver;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ResourceResolver resolver = Mockito.mock(ResourceResolver.class);
        Mockito.when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(
                resolver);
        context.registerService(MessageGatewayService.class, messageGatewayService);
        context.registerService(SlingRequestProcessor.class, slingRequestProcessor);
        context.registerService(RequestResponseFactory.class, requestResponseFactory);
        context.registerService(Externalizer.class, externalizer);
        mockResourceResolver = Mockito.mock(ResourceResolver.class);
        MockSlingHttpServletRequest assetRequest = new MockSlingHttpServletRequest(mockResourceResolver, context.bundleContext());
        Mockito.when(requestResponseFactory.createRequest(anyString(), anyString())).thenReturn(assetRequest);
        assetResponse = Mockito.mock(MockSlingHttpServletResponse.class);
        Mockito.when(requestResponseFactory.createResponse(any(OutputStream.class))).thenReturn(assetResponse);

        context.load().json("/sharepage/var-content.json", "/var/sites");
        context.load().json("/sharepage/page-content.json", "/content/core-components-examples/library/form/button");


        request = context.request();

        response = context.response();


    }

    @Test
    void testGetAsset() throws Exception {
        Mockito.when(assetResponse.getContentType()).thenReturn("JPG");
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        ResponseData responseData = underTest.getAsset("d2072cfe-639b-40dc-8118-18c08045ae01", "1874840739n");
        assertEquals("JPG", responseData.getContentType());
    }

    @Test
    void testGetAssetThrowsException() throws Exception {
        Mockito.doThrow(new IOException()).when(slingRequestProcessor).processRequest(any(MockSlingHttpServletRequest.class), any(MockSlingHttpServletResponse.class), any(ResourceResolver.class));
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        ResponseData responseData = underTest.getAsset("d2072cfe-639b-40dc-8118-18c08045ae01", "1874840739n");
        assertNull(responseData);
    }

    @Test
    void testGetPage() throws Exception {
        Mockito.when(assetResponse.getContentType()).thenReturn("text/html");
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        ResponseData responseData = underTest.getPage("d2072cfe-639b-40dc-8118-18c08045ae01");
        assertEquals("text/html", responseData.getContentType());
    }

    @Test
    void testGetPageThrowsException() throws Exception {
        Mockito.doThrow(new IOException()).when(slingRequestProcessor).processRequest(any(MockSlingHttpServletRequest.class), any(MockSlingHttpServletResponse.class), any(ResourceResolver.class));
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        assertThrows(PageShareException.class, () -> {
            underTest.getPage("d2072cfe-639b-40dc-8118-18c08045ae01");
        });
    }

    @Test
    void testGetPageExpiredToken() {
        Mockito.when(assetResponse.getContentType()).thenReturn("text/html");
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        assertThrows(PageShareException.class, () -> {
            underTest.getPage("e2072cfe-639b-40dc-8118-18c08045ae02");
        });
    }

    @Test
    void testSharePage() throws Exception {
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        PageShareData input = new PageShareData();
        input.setEmailContent("Please review");
        input.setEmail(new String[]{"test@test.com"});
        input.setEmailSubject("Summer Sale");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2040);
        input.setExpirationDate(cal.getTime());
        input.setPagePath("/content/core-components-examples/library/form/button");

        Mockito.when(externalizer.authorLink(any(ResourceResolver.class), anyString())).thenReturn("locaalhost:4502");

        Mockito.when(messageGatewayService.getGateway(HtmlEmail.class)).thenReturn(messageGatewayHtmlEmail);
        Mockito.doNothing().when(messageGatewayHtmlEmail).send(any(HtmlEmail.class));
        underTest.sharePage(input, context.resourceResolver());
        Resource resource = context.resourceResolver().getResource("/var/sites/share");
        assertEquals(3, StreamSupport.stream(resource.getChildren().spliterator(), false).count());
    }

    @Test
    void testSharePageEmailFailure() throws Exception {
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        PageShareData input = new PageShareData();
        input.setEmailContent("Please review");
        input.setEmail(new String[]{"test@test.com"});
        input.setEmailSubject("Summer Sale");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2040);
        input.setExpirationDate(cal.getTime());
        input.setPagePath("/content/core-components-examples/library/form/button");

        Mockito.when(externalizer.authorLink(any(ResourceResolver.class), anyString())).thenReturn("locaalhost:4502");

        Mockito.when(messageGatewayService.getGateway(HtmlEmail.class)).thenReturn(messageGatewayHtmlEmail);
        Mockito.doThrow(new MailingException("Exception")).when(messageGatewayHtmlEmail).send(any(HtmlEmail.class));
        assertThrows(PageShareException.class, () -> {
            underTest.sharePage(input, context.resourceResolver());
        });

    }

    @Test
    void testCreateAsset() throws Exception {
        PageShareServiceImpl underTest = context.registerInjectActivateService(new PageShareServiceImpl());
        String path = "/content/core-components-examples/library/form/button/_jcr_content/root/responsivegrid/teaser_556024830.coreimg.svg/1592607209952/adobe-logo.svg";
        String pageNodeName = "d2072cfe-639b-40dc-8118-18c08045ae01";
        underTest.createAsset(pageNodeName, path);
        int hash = path.hashCode();
        String generatedUUID = null;
        if (hash < 0) {
            generatedUUID = String.valueOf(Math.abs(hash) + "n");
        } else {
            generatedUUID = String.valueOf(hash);
        }
        Resource resource = context.resourceResolver().getResource("/var/sites/share/" + pageNodeName + "/assets/" + generatedUUID);
        assertNotNull(resource);
    }
}
