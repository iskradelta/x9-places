<%@ tag body-content="empty" %>
<%@ attribute name="venueID"%>
<%@ attribute name="reviewID" %>
<%@ tag import="com.x9.foodle.review.*" %>
<%@ tag import="com.x9.foodle.venue.*" %>
<% 
VenueModel venue = VenueModel.getFromSolr(venueID);
String title;
String text;
if (reviewID != null && !reviewID.isEmpty()) {
    ReviewModel review = ReviewModel.getFromSolr(reviewID);
    if (review == null) {
        throw new RuntimeException("no review with id: " + reviewID);   
    }
    venueID = review.getVenueID();
    title = review.getTitle();
    text = review.getText();
} else if (venueID != null && !venueID.isEmpty()) {
    reviewID = "";
    title = "";
    text = "";
} else {
    throw new RuntimeException("this page requires a review id or a venue id");   
}
%>

<div id="review_edit">
<center> <!-- sorry for the center tag --> 
<form action="${pageContext.request.contextPath}/review/edit" method="POST">
<input name="redirect" type="hidden" value="${pageContext.request.contextPath}/venue/view.jsp" />
<input name="reviewID" type="hidden" value="<%= reviewID %>" />
<input name="venueID" type="hidden" value="<%= venueID %>" />
<table class="content_block">
    <% 
    String doingWhat = "";
    if (reviewID.isEmpty()) { 
        doingWhat = "New review for venue: ";
    } else {
        doingWhat = "Editing review for venue: ";
    }
    %>
    <tr>
        <td><%= doingWhat %> <em><%= venue.getTitle() %></em> at <%= venue.getAddress() %></td>
    </tr>
    <tr>
        <td><label for="title">Title</label></td>
    </tr>
    <tr>
        <td><input class="editfield" name="title" id="title"
            type="text" value="<%= title %>" maxlength="<%= ReviewModel.Validator.REVIEW_TITLE_MAX_LENGTH %>"/></td>
    </tr>
    <tr>
        <td><label for="text">Text</label></td>
    </tr>
    <tr>
        <td>
        <textarea class="editfield" name="text" id="text" 
            onkeypress="return imposeMaxLength(this, <%= ReviewModel.Validator.REVIEW_TEXT_MAX_LENGTH %>);"><%= text %></textarea>
        </td>
    </tr>
    <tr>
        <td><input type="submit" value="Submit" /></td>
    </tr>

</table>
</form>
</center>
</div>