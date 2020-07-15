package aem.community.pageshare.core.service.impl.dto;

import java.io.InputStream;

public class ResponseData {

    InputStream responseContent;
    String contentType;
    String htmlResponse;

    public InputStream getResponseContent() {

        return responseContent;
    }

    public void setResponseContent(InputStream responseContent) {

        this.responseContent = responseContent;
    }

    public String getContentType() {

        return contentType;
    }

    public void setContentType(String contentType) {

        this.contentType = contentType;
    }

    public String getHtmlResponse() {
        return htmlResponse;
    }

    public void setHtmlResponse(String htmlResponse) {

        this.htmlResponse = htmlResponse;
    }
}
