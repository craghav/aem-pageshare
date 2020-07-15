package aem.community.pageshare.core.servlets;

import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.service.impl.dto.ResponseData;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import com.day.image.Layer;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletRequest;
import org.apache.sling.testing.mock.sling.servlet.MockSlingHttpServletResponse;
import org.apache.sling.xss.XSSAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


@ExtendWith({AemContextExtension.class} )
public class LoadSharesPageServletTest {
    @Mock
    private PageShareService pageService;

    @InjectMocks
    private LoadSharedPageServlet underTest;

    private static final String ASSET_TOKEN = "assetToken";

    private static final String PAGE_TOKEN = "pageToken";

    private final AemContext context = new AemContext();

    MockSlingHttpServletRequest request;
    MockSlingHttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        request = new MockSlingHttpServletRequest(context.resourceResolver(), context.bundleContext());
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put("token", "dummytoken");
        request.setParameterMap(params);
        response = context.response();
    }

    @Test
    void doGetPage() throws Exception {
        ResponseData responseData = new ResponseData();
        responseData.setContentType("text/html");
        responseData.setHtmlResponse("HTML Page Content");
        Mockito.when(pageService.getPage(anyString())).thenReturn(responseData);
        underTest.doGet(request, response);
        assertEquals(SlingHttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doGetThrowsPageShareException() throws Exception {
        PageShareException exception = new PageShareException("Invalid Token");
        Mockito.when(pageService.getPage(anyString())).thenThrow(exception);
         underTest.doGet(request, response);
        assertEquals(SlingHttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void doGetAsset() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put(PAGE_TOKEN, "dummyPageToken");
        params.put(ASSET_TOKEN, "dummyAssetToken");
        request.setParameterMap(params);

        BufferedImage simpleImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        Layer layer = new Layer(simpleImage);
        layer.write("image/png", 1, byteOut);

        ResponseData resData = new ResponseData();
        resData.setContentType("png");
        resData.setResponseContent(new ByteArrayInputStream(byteOut.toByteArray()));

        Mockito.when(pageService.getAsset(anyString(), anyString())).thenReturn(resData);
        underTest.doGet(request, response);
        assertEquals(SlingHttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doGetAssetThrowsPageShareException() throws Exception {
        Map<String, Object> params = new HashMap<String, Object> ();
        params.put(PAGE_TOKEN, "dummyPageToken");
        params.put(ASSET_TOKEN, "dummyAssetToken");
        request.setParameterMap(params);

        PageShareException exception = new PageShareException("Invalid Token");
        Mockito.when(pageService.getAsset(anyString(), anyString())).thenThrow(exception);
        underTest.doGet(request, response);
        assertEquals(SlingHttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }


}
