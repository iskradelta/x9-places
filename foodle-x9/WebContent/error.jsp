<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page isErrorPage="true" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="com.x9.foodle.venue.*" %>
<%@ page import="com.x9.foodle.util.*" %>
<%@ page import="com.x9.foodle.util.MessageDispatcher.*" %>

<%

/* TODO: log error */

/*
    To get to this page when an exception is thrown, add the following line to the jsp
    <%@ page errorPage="../error.jsp" % >
or something similar


*/

%>
<h:header title="Venue edit - Foodle X9"></h:header>
<h:headercontent />

<h3 class="my_header">A server error occured</h3>
<br/>
${pageContext.errorData.throwable.cause}
// TODO: show error somehow, explained here: http://java.sun.com/javaee/5/docs/tutorial/doc/bnahe.html#bnahi

<h:footer />