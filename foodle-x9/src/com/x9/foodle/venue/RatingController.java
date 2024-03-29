package com.x9.foodle.venue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.x9.foodle.datastore.DBUtils;
import com.x9.foodle.datastore.SolrRuntimeException;
import com.x9.foodle.model.exceptions.InvalidAddressException;
import com.x9.foodle.model.exceptions.InvalidAverageRatingException;
import com.x9.foodle.model.exceptions.InvalidCreatorIDException;
import com.x9.foodle.model.exceptions.InvalidDescriptionException;
import com.x9.foodle.model.exceptions.InvalidIDException;
import com.x9.foodle.model.exceptions.InvalidNumberOfRatingsException;
import com.x9.foodle.model.exceptions.InvalidTitleException;
import com.x9.foodle.user.UserModel;
import com.x9.foodle.user.UserUtils;
import com.x9.foodle.util.StringUtils;
import com.x9.foodle.util.MessageDispatcher.ErrorMessage;
import com.x9.foodle.util.MessageDispatcher.OkMessage;

@SuppressWarnings("serial")
public class RatingController extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletOutputStream out = resp.getOutputStream();

		String venueID = req.getParameter("venueID");
		String rating = req.getParameter("rating");

		if (venueID == null || venueID.isEmpty()) {
			throw new RuntimeException("null or empty venue id");
		}
		if (rating == null || rating.isEmpty()) {
			throw new RuntimeException("rating is null or empty");
		}

		int intRating = Integer.parseInt(rating);
		if (intRating < 0 || intRating > 5) {
			throw new RuntimeException("ranting is out-of-range: " + intRating);
		}
		try {
			Rating r = setVenueRating(UserUtils.getCurrentUser(req, resp),
					venueID, intRating);

			JSONObject json = new JSONObject(new OkMessage("Venue rated.")
					.toJSON());

			json.put("rating", StringUtils.formatRating(r.rating));
			json.put("ratings", r.ratings);
			json.put("userRating", intRating);

			out.print(json.toString());
		} catch (Exception e) {
			// TODO: log exception
			out.print(new ErrorMessage("Rating failed").toJSON());
		}
	}

	public static class Rating {
		public double rating;
		public int ratings;

		public Rating(double rating, int ratings) {
			super();
			this.rating = rating;
			this.ratings = ratings;
		}
	}

	public static Rating setVenueRating(UserModel user, String venueID,
			int rating) throws SQLException, InvalidIDException {
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			VenueModel venue = VenueModel.getFromSolr(venueID);
			if (venue == null) {
				throw new InvalidIDException("no venue with id: " + venueID);
			}

			// set the user specific rating in the db
			conn = DBUtils.openConnection();
			// delete any previous rating
			stm = conn
					.prepareStatement("delete from ratings where userID = ? and venueID = ?");
			stm.setInt(1, user.getID());
			stm.setString(2, venueID);
			stm.execute();
			DBUtils.closeStatement(stm);
			// set new rating
			stm = conn
					.prepareStatement("insert into ratings (userID, venueID, rating) values (?, ?, ?)");
			stm.setInt(1, user.getID());
			stm.setString(2, venueID);
			stm.setInt(3, rating);
			stm.execute();
			DBUtils.closeStatement(stm);

			//increase repLevel of creator if ranking positive otherwise decrease
			if (rating > 1 ) {
				UserModel repupcreator = UserModel.getFromDbByID(venue.getCreatorID());
				if (repupcreator != null && repupcreator.getID() != user.getID() && repupcreator.getUsername() != null)
					repupcreator.applyReplevel(repupcreator.getReputationLevel() + 5);
				if (user != null && repupcreator.getID() != user.getID())
					user.applyReplevel(user.getReputationLevel() + 1);
				repupcreator = null;
			}
			
			// get average rating and number of raters
			stm = conn
					.prepareStatement("select count(*), avg(rating) from ratings where venueID = ?");
			stm.setString(1, venueID);
			stm.execute();

			result = stm.getResultSet();

			if (result == null) {
				throw new SQLException(
						"error getting average rating for a venue");
			}

			result.next();
			try {
				VenueModel.Builder builder = venue.getEditable();

				Rating r = new Rating(result.getDouble(2), result.getInt(1));

				builder.setNumberOfRatings(r.ratings);
				builder.setAverageRating(r.rating);
				builder.apply();

				return r;

				// TODO: don't throw runtime exceptions
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
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}
	}

}
