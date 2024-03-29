package com.x9.foodle.testservlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

@SuppressWarnings("serial")
public class Hasher extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		ServletOutputStream out = resp.getOutputStream();
		out
				.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
		out.println("<title>User Dumper</title></head><body>");

		String password = req.getParameter("pwd");
		password = password == null ? "password" : password;
		String hash = BCrypt.hashpw(password, BCrypt.gensalt());
		out.println("Password: " + password);
		out.println("<br/>");
		out.println("Hash: " + hash);

		out.println("<br/>");
		out.println("<br/>");

		out.println("<form action=\"hasher\" method=\"GET\">");
		out.println("<input name=\"pwd\" type=\"text\" value=\"" + password
				+ "\"/>");
		out.println("<input type=\"submit\" value=\"Hash\"/>");
		out.println("</form>");

		out.println("</body></html>");
	}

}
