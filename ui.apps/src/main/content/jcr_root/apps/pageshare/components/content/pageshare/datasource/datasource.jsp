<%--
  ADOBE CONFIDENTIAL

  Copyright 2012 Adobe Systems Incorporated
  All Rights Reserved.

  NOTICE:  All information contained herein is, and remains
  the property of Adobe Systems Incorporated and its suppliers,
  if any.  The intellectual and technical concepts contained
  herein are proprietary to Adobe Systems Incorporated and its
  suppliers and may be covered by U.S. and Foreign Patents,
  patents in process, and are protected by trade secret or copyright law.
  Dissemination of this information or reproduction of this material
  is strictly forbidden unless prior written permission is obtained
  from Adobe Systems Incorporated.
--%><%
%><%@page session="false" contentType="text/html; charset=utf-8"%><%
%><%@page import="java.util.Iterator,
                  java.util.ArrayList,
                  org.apache.sling.api.resource.Resource,
                  org.apache.sling.api.resource.ResourceUtil,
                  org.apache.sling.api.resource.ValueMap,
                  org.apache.sling.commons.json.JSONObject,
                  com.adobe.granite.xss.XSSAPI,
                  com.adobe.granite.ui.components.ds.AbstractDataSource,
                  com.adobe.granite.ui.components.ds.DataSource,
                  com.adobe.granite.ui.components.ds.EmptyDataSource,
                  com.adobe.granite.ui.components.ds.SimpleDataSource,
                  org.apache.commons.collections.iterators.TransformIterator,
                  org.apache.commons.collections.Transformer,
                  org.apache.sling.api.resource.ResourceWrapper,
                  com.adobe.granite.ui.components.Config"%><%

%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0"%><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0"%><%
%><%@taglib prefix="ui" uri="http://www.adobe.com/taglibs/granite/ui/1.0"%><%

%><cq:defineObjects /><%

String contentPath = "/var/sites/share";

Resource contentNode = slingRequest.getResourceResolver().resolve(contentPath);
Config cfg = new Config(resource.getChild(Config.DATASOURCE));
final String itemRT = cfg.get("itemResourceType", String.class);
DataSource ds;
if (!ResourceUtil.isNonExistingResource(contentNode)) {
    final Iterator<Resource> shareList = contentNode.listChildren();
     ds = new AbstractDataSource() {
        public Iterator<Resource> iterator() {
            return new TransformIterator(shareList, new Transformer() {
                public Object transform(Object input) {
                    ResourceWrapper wrapper = new ResourceWrapper((Resource) input) {
                        public String getResourceType() {
                            return itemRT;
                        }
                    };
                    return wrapper;
                }
            });
        }
    };


} else {
    ds = EmptyDataSource.instance();
}


request.setAttribute(DataSource.class.getName(), ds);

%>
