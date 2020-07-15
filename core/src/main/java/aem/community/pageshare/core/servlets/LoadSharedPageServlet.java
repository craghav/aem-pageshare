package aem.community.pageshare.core.servlets;

import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.service.impl.dto.ResponseData;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import javax.servlet.Servlet;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;


@Component(service = {Servlet.class}, property = {
        Constants.SERVICE_DESCRIPTION + "=Page Share Servlet",
        "sling.servlet.paths=" + "/bin/viewPage",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET,
        "sling.servlet.extensions=" + "html",
        "sling.auth.requirements=" + "-/bin/viewPage"})
public class LoadSharedPageServlet extends SlingSafeMethodsServlet {

    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";

    private static final String JPG2 = "jpg";

    private static final String JPG = "JPG";

    private static final String JPEG = "JPEG";

    private static final String ASSET_TOKEN = "assetToken";

    private static final String PAGE_TOKEN = "pageToken";

    private static final Logger log = LoggerFactory.getLogger(LoadSharedPageServlet.class);

    @Reference
    private transient PageShareService pageShareService;

    /**
     * @param request  sling request object
     * @param response sling response object
     */
    @Override
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            String token = request.getParameter("token");
            ResponseData content = null;

            if (token != null) {
                getPage(response, token);
            } else {
                String pageToken = request.getParameter(PAGE_TOKEN);
                String assetToken = request.getParameter(ASSET_TOKEN);

                content = pageShareService.getAsset(pageToken, assetToken);
                if (content != null) {
                    updateResponse(response, content.getResponseContent(), content.getContentType());
                    response.setStatus(SlingHttpServletResponse.SC_OK);
                }

            }


        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_FORBIDDEN);
            log.error("Exception", e);
        }
    }

    private void updateResponse(SlingHttpServletResponse response, InputStream inputStream, String contentType)
            throws IOException {
        response.setContentType(contentType);
        try {
            IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void getPage(SlingHttpServletResponse response, String token) throws IOException {
        ResponseData content;
        try {
            content = pageShareService.getPage(token);
            if (content != null) {
                response.setContentType(TEXT_HTML_CHARSET_UTF_8);
                PrintWriter printWriter = response.getWriter();
                printWriter.write(content.getHtmlResponse());
                response.setStatus(SlingHttpServletResponse.SC_OK);
            }
        } catch (PageShareException e) {
            response.setStatus(SlingHttpServletResponse.SC_FORBIDDEN);
            PrintWriter printWriter = response.getWriter();
            printWriter.write(e.getMessage());
        }
    }


}
