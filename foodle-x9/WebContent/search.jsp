<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h"%>
<h:header title="Foodle X9 - The most awesome venue search"></h:header>
<h:headercontent />
<%@page import="com.x9.foodle.util.URLUtils"%>
<%@page import="com.x9.foodle.util.DateUtils"%>
<%@page import="com.x9.foodle.search.*"%>
<%@page import="com.x9.foodle.venue.*"%>
<%@page import="com.x9.foodle.review.*"%>
<%@page import="org.apache.solr.common.SolrDocumentList"%>
<%@page import="org.apache.solr.common.SolrDocument"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.regex.Pattern;"%>
<%
SolrDocumentList res = null;
	String query = request.getParameter("search_term");
	if (query != null && !query.isEmpty()) {
		query = URLUtils.encode(query);
		query = query.replaceAll("%C3%83%C2%A5", "&aring;");
		query = query.replaceAll("%C3%83%C2%A4", "&auml;");
		query = query.replaceAll("%C3%83%C2%B6", "&ouml;");
		query = query.replaceAll("%C3%83%C2%85", "&Aring;");
		query = query.replaceAll("%C3%83%C2%84", "&Auml;");
		query = query.replaceAll("%C3%83%C2%96", "&Ouml;");
		query = query.replaceAll("\\+", " ");

		res = SearchController.query(query, "all", null);
		TreeMap<String, Integer> tagmap = new TreeMap<String, Integer>();
		
		// Collect all tags from the search result
		if (res != null) {
			for (int i = 0; res.size() > i; i++) {
				try {

					VenueModel venue;
					//Determine type of the search result.
					SolrDocument doc = res.get(i);
					
					
					if(doc.get("type").equals("reviewmodel")){
						venue = VenueModel.getFromSolr((String) doc.get("reference"));
					}
					else if(doc.get("type").equals("commentmodel")) {
						venue = VenueModel.getFromSolr(ReviewModel.getFromSolr((String) doc.get("reference")).getVenueID());
					} 
					else {
						venue = VenueModel.venueFromSolrDocument(doc);
					}
					for(String tag : venue.getTags()){
						if(tagmap.containsKey(tag))
							tagmap.put(tag, tagmap.get(tag) + 1);
						else
							tagmap.put(tag, 1);
					}
				} catch (Exception e) {
					
				}
			}
		%>

<div id="tagcloud" class="msg_msg">
<h3>Tag cloud</h3>
<%
		// Print tag cloud with tagsize weighted according to tagfrequency (beta)
ArrayList<Integer> tagcount = new ArrayList<Integer>(tagmap.values());
if(tagcount.size() > 0){	
		Collections.sort(tagcount);
		int most_freq_tag = tagcount.get(tagcount.size()-1);
		
		for (String tag : tagmap.navigableKeySet()) { %>
			<a href="${pageContext.request.contextPath}/adv_search.jsp?search_term=<%=tag%>&adv_opt=tags" style="font-size: <%=6*tagmap.get(tag)/most_freq_tag+8%>pt"><%=tag%></a>&nbsp;
		<% 
		}
		%></div>
<%
	}
}
        
} // end if query != null
%>
<div id="resultarea">
<% if(res != null) { %>

<h3 style="text-align:center"> Search results for <%=query%> </h3>
<%		
	for (int i = 0; res.size() > i; i++) {
		try {
			SolrDocument doc = res.get(i);

			VenueModel venue = VenueModel.venueFromSolrDocument(doc);				
%>
<div id="search-result">
<h3><%=venue.getTitle()%></h3>
<h5><a href="${pageContext.request.contextPath}/venue/view.jsp?venueID=<%=venue.getID()%>">Show venue</a></h5>

<h5>Address: <%=venue.getAddress()%></h5>
</div>
<br />
<%
} catch (Exception e) {}
		}
%>
<% } else { %>
<div style="text-align:center">
<h5 >No matching results found or invalid input</h5>
</div>
<% } %>
<br/>
<a id="new_button" href="${pageContext.request.contextPath}/venue/edit.jsp">Add a new venue</a>
</div>
<h:footer />
