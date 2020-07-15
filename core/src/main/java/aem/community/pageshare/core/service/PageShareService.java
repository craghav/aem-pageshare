package aem.community.pageshare.core.service;

import aem.community.pageshare.core.service.exception.PageShareException;
import aem.community.pageshare.core.service.impl.dto.ResponseData;
import aem.community.pageshare.core.servlets.dto.PageShareData;
import org.apache.sling.api.resource.ResourceResolver;


public interface PageShareService {

    ResponseData getPage(String tokenPath) throws PageShareException;

    void sharePage(PageShareData input, ResourceResolver resourceResolver) throws PageShareException;

    String createAsset(String token, String oldPath) throws PageShareException;

    ResponseData getAsset(String pageToken, String assetToken) throws PageShareException;
}
