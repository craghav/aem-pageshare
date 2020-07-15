package aem.community.pageshare.core.servlets;

import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.xss.XSSAPI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component(service = {Servlet.class}, property = {
        "sling.servlet.paths=" + "/bin/sharePage",
        "sling.servlet.methods=" + HttpConstants.METHOD_POST})
public class SharePageServlet extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(SharePageServlet.class);
    public static final String INVALID_INPUT_ERROR_MSG = "Invalid Input";

    @Reference
    private transient PageShareService pageService;

    @Reference
    private transient XSSAPI xssApi;

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    @SuppressWarnings("squid:S1075")
    public static final String PATH_REGEX = "/content/([^/]+)/?(.+)?";

    /**
     * @param request  sling request object
     * @param response sling response object
     */
    @Override
    public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            PageShareData input = validateAndProcessRequestParam(request);

            ResourceResolver resourceResolver = request.getResourceResolver();
            pageService.sharePage(input, resourceResolver);
            response.setStatus(SlingHttpServletResponse.SC_CREATED);
        } catch (PageShareException e) {
            processParseException(response, e);
        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private PageShareData validateAndProcessRequestParam(SlingHttpServletRequest request) throws PageShareException {
        String[] emailList = request.getParameterValues("email");
        String emailSubject = request.getParameter("emailSubject");
        String emailMessage = request.getParameter("emailContent");
        String expirationDate = request.getParameter("expirationDate");
        String pagePath = request.getParameter("path");

        PageShareData input = new PageShareData();
        input.setEmail(validateEmail(emailList));
        input.setEmailSubject(validateEmailSubject(emailSubject));
        input.setEmailContent(validateEmailContent(emailMessage));
        input.setExpirationDate(validateDate(expirationDate));
        input.setPagePath(validatePath(pagePath));
        return input;
    }

    private void processParseException(SlingHttpServletResponse response, PageShareException e) {
        response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write(e.getMessage());
        } catch (Exception ex) {
            log.error("Exception", ex);
        }
    }

    private String validateEmailSubject(String emailSubject) throws PageShareException {
        String validSubject = StringEscapeUtils.escapeHtml(emailSubject);
        if (StringUtils.isEmpty(validSubject)) {
            throw new PageShareException(INVALID_INPUT_ERROR_MSG);
        }
        return validSubject;
    }

    private String[] validateEmail(String[] emails) throws PageShareException {
        Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
        for (String email : emails) {
            Matcher emailMatcher = emailPattern.matcher(email);
            if (!emailMatcher.matches()) {
                throw new PageShareException(INVALID_INPUT_ERROR_MSG);
            }

        }
        return emails;
    }

    private String validateEmailContent(String enailContent) throws PageShareException {
        String validContent = xssApi.filterHTML(enailContent);
        if (StringUtils.isEmpty(validContent)) {
            throw new PageShareException(INVALID_INPUT_ERROR_MSG);
        }
        return validContent;
    }

    private String validatePath(String path) throws PageShareException {
        Pattern pathPattern = Pattern.compile(PATH_REGEX);
        Matcher pathMatcher = pathPattern.matcher(path);
        if (!pathMatcher.matches()) {
            throw new PageShareException(INVALID_INPUT_ERROR_MSG);
        }
        return path;
    }

    private Date validateDate(String expirationDate) throws PageShareException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date = null;
        try {
            date = formatter.parse(expirationDate);
        } catch (ParseException e) {
            throw new PageShareException(INVALID_INPUT_ERROR_MSG);
        }
        Calendar today = Calendar.getInstance();
        Calendar selectedDay = Calendar.getInstance();
        selectedDay.setTimeInMillis(date.getTime());
        boolean isInValid = selectedDay.before(today);

        if (isInValid) {
            throw new PageShareException(INVALID_INPUT_ERROR_MSG);
        }
        return date;
    }

}
