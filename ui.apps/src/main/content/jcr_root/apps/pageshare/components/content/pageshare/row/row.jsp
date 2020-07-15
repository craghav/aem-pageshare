<%--
  ADOBE CONFIDENTIAL

  Copyright 2016 Adobe Systems Incorporated
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
%><%@include file="/libs/granite/ui/global.jsp"%><%
%><%@page session="false"%><%
%><%@page import="com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.Tag,
                  org.apache.sling.api.resource.ValueMap,
                  com.day.cq.wcm.api.components.Component,
                  org.apache.commons.lang.StringUtils,
                  org.apache.sling.api.resource.Resource,
                  org.apache.sling.api.resource.ValueMap,
                  java.util.Date,
                  org.apache.jackrabbit.util.Text" %><%


    ValueMap properties = resource.getValueMap();
    String originalPath = properties.get("originalPath", String.class);
    if (originalPath != null) {
    String newPath = "/bin/viewPage?token="+resource.getName();
    String abbr = null;
    String translationContext = null;
    String expirationDate = String.valueOf(properties.get("expirationDate", Date.class));

    if (StringUtils.isNotEmpty(abbr) && translationContext != null) {
        abbr = i18n.getVar(abbr, translationContext);
    }

    if (StringUtils.isEmpty(abbr)) {
        // build abbreviation - either shorten (2 chars max) from given abbreviation or title: Image >> Im
        abbr = "PS";
        if (abbr.length() >= 2) {
            abbr = abbr.substring(0, 2);
        } else if (abbr.length() == 1) {
            abbr = String.valueOf(abbr.charAt(0));
        }
    }



    Tag tag = cmp.consumeTag();
    AttrBuilder attrs = tag.getAttrs();

    attrs.addClass("foundation-collection-navigator");
    attrs.add("is", "coral-table-row");




%><tr <%= attrs %>>
    <td is="coral-table-cell">
       <coral-tag size="M" color="grey"><%= xssAPI.encodeForHTML(abbr) %></coral-tag>
    </td>
    <td is="coral-table-cell"><span><%= originalPath %></span></td>
    <td is="coral-table-cell"><span><a href="<%= newPath %>" target="_blanks"><%= newPath %></a></span></td>
    <td is="coral-table-cell"><span><%= expirationDate %></span></td>
    <td is="coral-table-cell" alignment="right"></td>
</tr>
<% }%>