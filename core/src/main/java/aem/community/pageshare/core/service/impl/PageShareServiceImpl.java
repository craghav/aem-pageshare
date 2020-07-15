package aem.community.pageshare.core.service.impl;

import aem.community.pageshare.core.service.PageShareService;
import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.service.impl.dto.ResponseData;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import com.day.cq.commons.Externalizer;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.mailer.MessageGateway;
import com.day.cq.mailer.MessageGatewayService;
import com.day.cq.wcm.api.WCMMode;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.api.resource.*;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(service = PageShareService.class, property = {Constants.SERVICE_DESCRIPTION
        + "=A service that is used to create share link and view shared page "}, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class PageShareServiceImpl implements PageShareService {

    private static final String TOKEN = "token";

    private static final String PAGE_PATH = "pagePath";

    private static final String ASSET_PATH = "assetPath";

    private static final String PREVIEW_LINK = "#previewlink";

    private static final String PAGE_URL = "/bin/viewPage?token=";

    @SuppressWarnings("squid:S1075")
    private static final String STORAGE_PATH = "/var/sites/share/";

    public static final String EXCEPTION_MSG = "Error Occured ";
    public static final String ASSETS = "assets";
    public static final String EXPIRATION_DATE_PROPERTY = "expirationDate";
    public static final String INCORRECT_TOKEN_MSG = "Incorrect token";
    public static final String LINK_EXPIRED_MSG = "Link Expired";
    public static final String UNABLE_TO_RETRIEVE_CONTENT_MSG = "Unable to retrieve content";
    public static final String UNABLE_TO_PROCESS_THE_REQUEST_MSG = "Unable to process the request";
    public static final String UNABLE_TO_SEND_THE_EMAIL_MSG = "Unable to send the email";
    public static final String EMAIL_NOT_SENT_MSG = "Email not Sent. Please configure email service.";
    public static final String ORIGINAL_PATH_PROPERTY = "originalPath";
    public static final String EMAIL_SUBJECT_PROPERTY = "emailSubject";
    public static final String EMAIL_MESSAGE_PROPERTY = "emailMessage";
    public static final String EMAIL_LIST_PROPERTY = "emailList";
    public static final String INTERNAL_ERROR_MSG = "Internal Error";

    @Reference
    private RequestResponseFactory requestResponseFactory;

    @Reference
    private SlingRequestProcessor requestProcessor;

    @Reference
    private MessageGatewayService messageGatewayService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private Externalizer externalizer;

    private static final String SERVICE_ACCOUNT_IDENTIFIER = "page-share";

    private final Logger log = LoggerFactory.getLogger(PageShareServiceImpl.class);


    public ResponseData getAsset(String pageToken, String assetToken)
            throws PageShareException {
        ResponseData responsedata = null;
        ResourceResolver resolver = getResourceResolver();
        String pagePath = null;

        try {
            Resource resource = resolver.getResource(STORAGE_PATH + pageToken + "/assets/" + assetToken);
            pagePath = getProperty(resource, ASSET_PATH);
            responsedata = retrieveContent(resolver, pagePath, null);
        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
            return null;
        }

        return responsedata;

    }

    private String getProperty(Resource resource, String propertyName) {
        ValueMap properties = resource.adaptTo(ValueMap.class);
        String pagePath = properties.get(propertyName, String.class);
        return pagePath;
    }

    @Override
    public ResponseData getPage(String tokenPath)
            throws PageShareException {
        ResourceResolver resolver = getResourceResolver();
        Resource resource = null;
        boolean isValid = false;
        ResponseData responseData = null;
        try {
           resource = resolver.getResource(STORAGE_PATH + tokenPath);
           isValid = isTokenValid(tokenPath, resource);
            if (isValid) {
                String pagePath = getProperty(resource, PAGE_PATH);
                responseData = retrieveContent(resolver, pagePath, tokenPath);
            }
        } catch (Exception e) {
            log.error("Error while accessing the repository", e);
            throw new PageShareException(INCORRECT_TOKEN_MSG);
        }

        if (!isValid) {
            throw new PageShareException(LINK_EXPIRED_MSG);
        }

        return responseData;

    }

    private boolean isTokenValid(String tokenPath, Resource resource) throws ParseException {
        boolean isValid ;
        String expiry = getProperty(resource, EXPIRATION_DATE_PROPERTY);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date expiryDate = formatter.parse(expiry);

        Calendar cal = Calendar.getInstance();
        isValid = expiryDate.after(cal.getTime());
        return isValid;
    }

    private ResponseData retrieveContent(ResourceResolver resolver, String path, String token)
            throws PageShareException {
        HttpServletRequest request = this.requestResponseFactory.createRequest("GET", path);
        request.setAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME, WCMMode.DISABLED);
        if (token != null) {
            request.setAttribute(TOKEN, token);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse response = this.requestResponseFactory.createResponse(out);
        try {
            this.requestProcessor.processRequest(request, response, resolver);
        } catch (ServletException | IOException e) {
            log.error(EXCEPTION_MSG, e);
            throw new PageShareException(UNABLE_TO_RETRIEVE_CONTENT_MSG);
        }

        ResponseData data = new ResponseData();
        data.setContentType(response.getContentType());
        if (token == null) {
            InputStream in = new ByteArrayInputStream(out.toByteArray());
            data.setResponseContent(in);
        } else {
            data.setHtmlResponse(out.toString());
        }
        return data;
    }

    @Override
    public void sharePage(PageShareData input, ResourceResolver resourceResolver) throws PageShareException {
        java.util.UUID obj = java.util.UUID.randomUUID();
        String generatedUUID = obj.toString();
        String[] emailList = input.getEmail();
        String emailSubject = input.getEmailSubject();
        String emailMessage = input.getEmailContent();

        try {

            createNodeProperties(input, generatedUUID,
                    resourceResolver);

            String pageUrl = generateLink(resourceResolver, generatedUUID);
            emailMessage = emailMessage.replaceAll(PREVIEW_LINK,
                    pageUrl);
            sendMail(emailList, emailSubject, emailMessage);

        } catch (Exception e) {
            log.error(EXCEPTION_MSG, e);
            throw new PageShareException(UNABLE_TO_PROCESS_THE_REQUEST_MSG);
        }
    }

    private String generateLink(ResourceResolver resourceResolver, String generatedUUID) {
        String pageUrl = PAGE_URL + generatedUUID;
        if (externalizer != null) {
            pageUrl = externalizer.authorLink(resourceResolver, pageUrl);
        }
        return pageUrl;
    }

    private void sendMail(String[] emailList, String emailSubject, String emailMessage) throws PageShareException {
        try {
            MessageGateway<HtmlEmail> messageGateway = messageGatewayService.getGateway(HtmlEmail.class);
            if (messageGateway != null) {
                HtmlEmail email = new HtmlEmail();
                email.setSubject(emailSubject);
                email.setMsg(emailMessage);
                List<InternetAddress> emailRecipients = new ArrayList<>();
                for (String emailItem : emailList) {
                    emailRecipients.add(new InternetAddress(emailItem));
                }
                email.setTo(emailRecipients);
                messageGateway.send(email);
            } else {
                log.error(EMAIL_NOT_SENT_MSG);
            }
        } catch (EmailException | AddressException e) {
            log.error(EXCEPTION_MSG, e);
            throw new PageShareException(UNABLE_TO_SEND_THE_EMAIL_MSG);
        }
    }

    private void createNodeProperties(PageShareData input,
                                      String generatedUUID, ResourceResolver resourceResolver) throws PersistenceException {
        Resource parentResource = resourceResolver.getResource(STORAGE_PATH);
        String pagePath = input.getPagePath();
        String[] emailList = input.getEmail();
        String emailSubject = input.getEmailSubject();
        String emailMessage = input.getEmailContent();
        Date expirationDate = input.getExpirationDate();

        Map<String, Object> properties = new HashMap<>();
        properties.put(ORIGINAL_PATH_PROPERTY, pagePath + ".html");
        properties.put(PAGE_PATH, pagePath + ".share.html");
        properties.put(EMAIL_SUBJECT_PROPERTY, emailSubject);
        properties.put(EMAIL_MESSAGE_PROPERTY, emailMessage);
        properties.put(EMAIL_LIST_PROPERTY, emailList);
        properties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(expirationDate.getTime());
        properties.put(EXPIRATION_DATE_PROPERTY, cal);
        resourceResolver.create(parentResource, generatedUUID, properties);
        resourceResolver.commit();
    }

    public String createAsset(String token, String oldPath) throws PageShareException {
        String assetPath = STORAGE_PATH + token;
        ResourceResolver resolver = getResourceResolver();
        int hash = oldPath.hashCode();
        String generatedUUID = null;
        Map<String, Object> properties = new HashMap<>();
        properties.put(JcrConstants.JCR_PRIMARYTYPE, JcrConstants.NT_UNSTRUCTURED);
        if (hash < 0) {
            generatedUUID = String.valueOf(Math.abs(hash) + "n");
        } else {
            generatedUUID = String.valueOf(hash);
        }

        try {
            Resource pageNode = resolver.getResource(assetPath);
            Resource assetRootNode = pageNode.getChild(ASSETS);
            if (assetRootNode == null) {
                assetRootNode = resolver.create(pageNode, ASSETS, properties);
            }
            Resource assetNode = assetRootNode.getChild(generatedUUID);

            if (assetNode == null) {
                properties.put(ASSET_PATH, oldPath);
                resolver.create(assetRootNode, generatedUUID, properties);

            }
            resolver.commit();

            return "/bin/viewPage?type=asset&assetToken=" + generatedUUID + "&pageToken=" + token;
        } catch (PersistenceException e) {
            log.error(EXCEPTION_MSG, e);
        }

        return null;
    }

    private final ResourceResolver getResourceResolver() throws PageShareException {
        final Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE,
                (Object) SERVICE_ACCOUNT_IDENTIFIER);
        try {
            return resourceResolverFactory.getServiceResourceResolver(authInfo);
        } catch (LoginException e) {
            log.error("Login Exception when obtaining a User for the Bundle Service - ", e);
            throw new PageShareException(INTERNAL_ERROR_MSG);
        }
    }
}
