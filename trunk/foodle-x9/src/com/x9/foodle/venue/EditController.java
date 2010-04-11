package com.x9.foodle.venue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.x9.foodle.datastore.SolrRuntimeException;
import com.x9.foodle.user.UserModel;
import com.x9.foodle.venue.exceptions.InvalidAddressException;
import com.x9.foodle.venue.exceptions.InvalidAverageRatingException;
import com.x9.foodle.venue.exceptions.InvalidCreatorIDException;
import com.x9.foodle.venue.exceptions.InvalidDescriptionException;
import com.x9.foodle.venue.exceptions.InvalidIDException;
import com.x9.foodle.venue.exceptions.InvalidNumberOfRatingsException;
import com.x9.foodle.venue.exceptions.InvalidTitleException;

@SuppressWarnings("serial")
public class EditController extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = req.getParameter("id");
		String title = req.getParameter("title");
		String address = req.getParameter("address");
		String description = req.getParameter("description");
		String creatorID = req.getParameter("creator_id");

		String redirect = req.getParameter("redirect");

		try {
			VenueModel.Builder builder = new VenueModel.Builder();

			builder.setId(id);
			builder.setTitle(title);
			builder.setAddress(address);
			builder.setDescription(description);
			builder.setCreator(UserModel.getFromDbByID(1));
			builder.apply();

			resp.sendRedirect(redirect);
		} catch (InvalidIDException e) {
			throw new RuntimeException(e);
		} catch (InvalidTitleException e) {
			throw new RuntimeException(e);
		} catch (InvalidAddressException e) {
			throw new RuntimeException(e);
		} catch (InvalidDescriptionException e) {
			throw new RuntimeException(e);
		} catch (InvalidNumberOfRatingsException e) {
			throw new RuntimeException(e);
		} catch (InvalidAverageRatingException e) {
			throw new RuntimeException(e);
		} catch (InvalidCreatorIDException e) {
			throw new RuntimeException(e);
		} catch (SolrRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

}
