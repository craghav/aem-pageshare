package aem.community.pageshare.core.servlets;

import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;

import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.apache.sling.xss.XSSAPI;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(AemContextExtension.class )
public class SharePageServletTest {
    @Mock
    private PageShareService pageService;

    @Mock
    private XSSAPI xss;

    @InjectMocks
    private SharePageServlet underTest;

    private final AemContext context = new AemContext();

    MockSlingHttpServletRequest request;
    MockSlingHttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test@test.com"});
        params.put("emailSubject", "Summer Sale");
        params.put("emailContent", "Please review the landing page");
        params.put("expirationDate", "2040-06-27T22:29:00.000+01:00");
        params.put("path", "/content/communitysite/landingpage");
        request.setParameterMap(params);
        response = context.response();
        Mockito.when(xss.filterHTML("Please review the landing page")).thenReturn("Please review the landing page");
    }

    @Test
    void doPostSharePage() throws Exception {
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_CREATED, response.getStatus());
    }

    @Test
    void doPostThrowsPageShareException() throws Exception {
        PageShareException exception = new PageShareException("Unable to process the request");
        Mockito.doThrow(exception).when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doPostThrowsException() throws Exception {
        Mockito.doThrow(new RuntimeException()).when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void doPostInvalidEmailInput() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test.com"});
        params.put("emailSubject", "Summer Sale");
        params.put("emailContent", "Please review the landing page");
        params.put("expirationDate", "2040-06-27T22:29:00.000+01:00");
        params.put("path", "/content/communitysite/landingpage");
        request.setParameterMap(params);
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doPostNoEmailSubject() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test.com"});
        params.put("emailSubject", "");
        params.put("emailContent", "Please review the landing page");
        params.put("expirationDate", "2040-06-27T22:29:00.000+01:00");
        params.put("path", "/content/communitysite/landingpage");
        request.setParameterMap(params);
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doPostNoEmailContent() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test.com"});
        params.put("emailSubject", "Summer Sale");
        params.put("emailContent", "");
        params.put("expirationDate", "2040-06-27T22:29:00.000+01:00");
        params.put("path", "/content/communitysite/landingpage");
        request.setParameterMap(params);
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doPostNoInValidDate() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test.com"});
        params.put("emailSubject", "Summer Sale");
        params.put("emailContent", "");
        params.put("expirationDate", "2010-06-27T22:29:00.000+01:00");
        params.put("path", "/content/communitysite/landingpage");
        request.setParameterMap(params);
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    void doPostNoInValidPath() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("email", new String[] {"test.com"});
        params.put("emailSubject", "Summer Sale");
        params.put("emailContent", "");
        params.put("expirationDate", "2010-06-27T22:29:00.000+01:00");
        params.put("path", "/apps/test");
        request.setParameterMap(params);
        Mockito.doNothing().when(pageService).sharePage(any(PageShareData.class), any(ResourceResolver.class));
        underTest.doPost(request, response);
        assertEquals(SlingHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }


}
