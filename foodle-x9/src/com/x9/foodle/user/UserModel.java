package com.x9.foodle.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import com.x9.foodle.datastore.DBUtils;
import com.x9.foodle.datastore.SQLRuntimeException;
import com.x9.foodle.model.exceptions.BadEmailException;
import com.x9.foodle.model.exceptions.BadLocationException;
import com.x9.foodle.model.exceptions.BadNameException;
import com.x9.foodle.model.exceptions.BadPasswordException;
import com.x9.foodle.model.exceptions.BadUsernameException;
import com.x9.foodle.model.exceptions.InvalidUserException;

public class UserModel {

	public static final int NO_RATING = -1;
	public static final int NO_RANKING = 0;

	private int userID;
	private String username;
	private String passwordHash;
	private String email;
	private String name;
	private String location;
	private int reputationLevel;
	private String sessionToken;
	private boolean isConnectedToFacebook;
	private String deleteToken;

	/**
	 * Returns a UserModel representing a user with id {@code userID}. blabla.
	 * 
	 * @param userID
	 * @return
	 */
	public static UserModel getFromDbByID(int userID) {
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			conn = DBUtils.openConnection();
			stm = conn.prepareStatement("select * from users where userID = ?");
			stm.setInt(1, userID);
			boolean success = stm.execute();
			if (!success) {
				return null;
			}

			result = stm.getResultSet();

			if (!result.next()) {
				return null;
			}

			UserModel user = userFromResultSet(result);

			return user;

		} catch (SQLException e) {
			throw new SQLRuntimeException("Bad SQL syntax getting user by id",
					e);
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}
	}

	public static UserModel getFromDbByUsername(String username) {
		if (username == null)
			throw new IllegalArgumentException("username was null!");
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			conn = DBUtils.openConnection();

			stm = conn
					.prepareStatement("select * from users where username = ?");
			stm.setString(1, username);
			boolean success = stm.execute();
			if (!success) {
				return null;
			}

			result = stm.getResultSet();

			if (!result.next()) {
				return null;
			}

			UserModel user = userFromResultSet(result);

			return user;

		} catch (SQLException e) {
			throw new SQLRuntimeException(
					"Bad SQL syntax getting user by username", e);
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}
	}

	public int getID() {
		return userID;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public int getReputationLevel() {
		return reputationLevel;
	}

	public String getSessionToken() {
		return sessionToken;
	}

	public boolean isConnectedToFacebook() {
		return isConnectedToFacebook;
	}

	public String getDeleteToken() {
		return deleteToken;
	}

	public int getRatingForVenue(String venueID) {
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			conn = DBUtils.openConnection();
			stm = conn
					.prepareStatement("select rating from ratings where userID = ? and venueID = ?");
			stm.setInt(1, getID());
			stm.setString(2, venueID);
			boolean succ = stm.execute();

			if (!succ) {
				return NO_RATING;
			}

			result = stm.getResultSet();

			if (result.next()) {
				return result.getInt(result.findColumn("rating"));
			}
			return NO_RATING;
		} catch (SQLException e) {
			throw new SQLRuntimeException("error getting rating for venue: "
					+ venueID + " for user: " + userID);
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}
	}

	public int getRankingForReview(String reviewID) {
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			conn = DBUtils.openConnection();
			stm = conn
					.prepareStatement("select ranking from rankings where userID = ? and reviewID = ?");
			stm.setInt(1, getID());
			stm.setString(2, reviewID);
			boolean succ = stm.execute();

			if (!succ) {
				return NO_RANKING;
			}

			result = stm.getResultSet();

			if (result.next()) {
				return result.getInt(result.findColumn("ranking"));
			}
			return NO_RANKING;
		} catch (SQLException e) {
			throw new SQLRuntimeException("error getting ranking for review: "
					+ reviewID + " for user: " + userID);
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}
	}

	public void applyReplevel(int repLevel) {
		Connection conn = null;
		PreparedStatement stm = null;
		ResultSet result = null;
		try {
			conn = DBUtils.openConnection();
			stm = conn
					.prepareStatement("update users set repLevel = ? where userID = ?");
			stm.setInt(1, repLevel);
			stm.setInt(2, userID);
			stm.execute();

		} catch (SQLException e) {
			throw new SQLRuntimeException("error setting reputation level "
					+ repLevel + " for user: " + userID);
		} finally {
			DBUtils.closeResultSet(result);
			DBUtils.closeStatement(stm);
			DBUtils.closeConnection(conn);
		}

	}

	/**
	 * Gets a {@link Builder} for updating this UserModel. Changes made to this
	 * builder will not affect the UserModel until {@link Builder#apply()} as
	 * been successfully called.
	 * 
	 * @return a mutable builder.
	 */
	public Builder getEditable() {
		return new Builder(this);
	}

	/**
	 * Class for creating a new user and inserting it into the db.
	 * 
	 * @author tgwizard
	 * 
	 */
	public static class Builder {

		private UserModel user;
		private UserModel editMe;

		public Builder() {
			this.user = new UserModel();
		}

		private Builder(UserModel editMe) {
			this.user = new UserModel(editMe);
			this.editMe = editMe;
		}

		public Builder setUsername(String username) {
			user.username = username;
			return this;
		}

		public Builder setPasswordHash(String passwordHash) {
			user.passwordHash = passwordHash;
			return this;
		}

		public Builder setEmail(String email) {
			user.email = email;
			return this;
		}

		public Builder setName(String name) {
			user.name = name;
			return this;
		}

		public Builder setLocation(String location) {
			user.location = location;
			return this;
		}

		public Builder setReputationLevel(int reputationLevel) {
			user.reputationLevel = reputationLevel;
			return this;
		}

		public Builder setSessionToken(String sessionToken) {
			user.sessionToken = sessionToken;
			return this;
		}

		public Builder setConnectedToFacebook(boolean isConnectedToFacebook) {
			user.isConnectedToFacebook = isConnectedToFacebook;
			return this;
		}

		public Builder setDeleteToken(String deleteToken) {
			user.deleteToken = deleteToken;
			return this;
		}

/**
		 * Will throw a subclass of {@link InvalidUserException} if any of the
		 * parameters for this user is invalid.
		 * 
		 * @throws BadUsernameException
		 *             see {@link Validator#validateUsername(UserModel)
		 * @throws BadPasswordException
		 *             see {@link Validator#validatePasswordHash(UserModel)
		 * @throws BadEmailException
		 *             see {@link Validator#validateEmail(UserModel)
		 * @see Validator#validate(UserModel)
		 */
		public void validate() throws BadUsernameException, BadNameException,
				BadPasswordException, BadEmailException, BadLocationException {
			Validator.validate(user);
		}

		/**
		 * Calls {@code validate()} to check the parameters, and upon success
		 * inserts them as a user in the database. If the builder was created
		 * using the default constructor, a new user is inserted. If it was
		 * created using the {@link UserModel#getEditable()} method, that user
		 * will be updated, and all references to that object will be updated.
		 * 
		 * Will throw subclasses of {@link InvalidUserException} if any
		 * parameter is invalid.
		 * 
		 * A user model (with a correct user id) will be returned if no
		 * exceptions are thrown.
		 * 
		 * @return The inserted or edited user model, never null
		 * @throws BadUsernameException
		 *             see {@link #validate()}
		 * @throws BadPasswordException
		 *             see {@link #validate()}
		 * @throws BadEmailException
		 *             see {@link #validate()}
		 * @throws SQLRuntimeException
		 *             if an sql error occurs.
		 */
		public UserModel apply() throws BadUsernameException,
				BadLocationException, BadNameException, BadPasswordException,
				BadEmailException, SQLRuntimeException {
			// throws on bad data
			validate();

			Connection conn = null;
			PreparedStatement stm = null;
			ResultSet result = null;
			try {

				conn = DBUtils.openConnection();

				if (editMe != null) {
					// edit an existing user
					stm = conn
							.prepareStatement("update users set username = ?, passwordHash = ?, "
									+ "email = ?, name = ?, repLevel = ?, isFBConnected = ?, location = ? where userID = ?");
					stm.setInt(8, user.userID);
				} else {
					// insert a new user
					stm = conn
							.prepareStatement(
									"insert IGNORE into users (username, passwordHash, email, name, repLevel, isFBConnected, location, sessionToken) "
											+ "values (?, ?, ?, ?, ?, ?, ?, ?)",
									Statement.RETURN_GENERATED_KEYS);
					stm.setString(8, user.sessionToken);
				}

				stm.setString(1, user.username);
				stm.setString(2, user.passwordHash);
				stm.setString(3, user.email);
				stm.setString(4, user.name);
				stm.setInt(5, user.reputationLevel);
				stm.setBoolean(6, user.isConnectedToFacebook);
				stm.setString(7, user.location);

				if (stm.executeUpdate() == 0) {
					throw new BadEmailException(
							"Email or username already taken.");
				}

				if (editMe != null) {
					copy(editMe, user);
					return editMe;
				} else {
					// retrieve the value of the auto_increment 'userID' column
					result = stm.getGeneratedKeys();
					if (result.next()) {
						user.userID = result.getInt(1);
					} else {
						throw new SQLRuntimeException(
								"Got no user id for new user");
					}
					return user;
				}
			} catch (SQLException e) {
				throw new SQLRuntimeException(
						"Bad SQL syntax inserting/updating user", e);
			} finally {
				DBUtils.closeResultSet(result);
				DBUtils.closeStatement(stm);
				DBUtils.closeConnection(conn);
			}
		}
	}

	public static class Validator {

		public static final int USER_USERNAME_MAX_LENGTH = 20;
		public static final int USER_USERNAME_MIN_LENGTH = 4;
		public static final int USER_PASSWORD_MAX_LENGTH = 40;
		public static final int USER_PASSWORD_MIN_LENGTH = 4;

		public static void validate(UserModel user)
				throws BadUsernameException, BadPasswordException,
				BadEmailException, BadNameException, BadLocationException {
			validateUsername(user);
			validatePasswordHash(user);
			validateEmail(user);
			validateName(user);
			validateLocation(user);

			// TODO: if account is connected to facebook, what more do we need?
		}

		public static void validateUsername(UserModel user)
				throws BadUsernameException {
			if (user.username == null) {
				throw new BadUsernameException("Empty username");
			}
			if (user.username.length() < USER_USERNAME_MIN_LENGTH) {
				throw new BadUsernameException("Username too short ("
						+ user.username.length()
						+ " characters), needs to be at least "
						+ USER_USERNAME_MIN_LENGTH);
			}
			if (user.username.length() > USER_USERNAME_MAX_LENGTH) {
				throw new BadUsernameException("Username too long ("
						+ user.username.length()
						+ " characters ), needs to be at least "
						+ USER_USERNAME_MAX_LENGTH);
			}

			Pattern p = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");
			if (!p.matcher(user.username).matches()) {
				throw new BadUsernameException(
						"Username contained invalid characters: "
								+ user.username);
			}

		}

		public static void validateName(UserModel user) throws BadNameException {
			Pattern p = Pattern.compile("[a-zA-Z][a-zA-Z0-9_ -]*");
			if (!p.matcher(user.name).matches()) {
				throw new BadNameException(
						"Name contained invalid characters: " + user.name);
			}
		}

		public static void validateLocation(UserModel user)
				throws BadLocationException {
			// TODO: Improve this pattern, spaces , . : is ok
			Pattern p = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");
			if (!p.matcher(user.location).matches()) {
				throw new BadLocationException(
						"Location contained invalid characters: "
								+ user.location);
			}
		}

		public static void validatePassword(String password, String password2)
				throws BadPasswordException {
			if (password == null || password.isEmpty()) {
				throw new BadPasswordException("Empty password");
			}

			if (!password.equals(password2)) {
				throw new BadPasswordException("The passwords don't match");
			}

			if (password.length() < USER_PASSWORD_MIN_LENGTH) {
				throw new BadPasswordException(
						"Password too short, it has to be at least "
								+ USER_PASSWORD_MIN_LENGTH + " characters");
			}

			if (password.length() > USER_PASSWORD_MAX_LENGTH) {
				throw new BadPasswordException(
						"Password too long, it has to be at least "
								+ USER_PASSWORD_MAX_LENGTH + " characters");
			}

			// TODO: check password strength?
		}

		public static void validatePasswordHash(UserModel user)
				throws BadPasswordException {
			if (user.passwordHash == null) {
				throw new BadPasswordException("Internal error (password hash)");
			}

		}

		public static void validateEmail(UserModel user)
				throws BadEmailException {
			if (user.email == null) {
				throw new BadEmailException("Empty email");
			}
			// regex copied from: http://www.regular-expressions.info/email.html
			// with some modifications to allow for new TLDs
			// http://en.wikipedia.org/wiki/List_of_Internet_top-level_domains

			String ccTLD = "[A-Z]{2}";
			String gTLD = "aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel";

			Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.(?:"
					+ ccTLD + "|" + gTLD + ")$", Pattern.CASE_INSENSITIVE);
			if (!p.matcher(user.email).matches()) {
				throw new BadEmailException("Invalid email (" + user.email
						+ ")");
			}
		}
	}

	/**
	 * Private constructor, use {@link Builder} create new users.
	 */
	private UserModel() {
		this.userID = -1;
		this.username = null;
		this.passwordHash = null;
		this.email = null;
		this.name = null;
		this.location = null;
		this.reputationLevel = 0;
		this.sessionToken = null;
		this.isConnectedToFacebook = false;
		this.deleteToken = null;
	}

	/**
	 * Private copy constructor.
	 * 
	 * @param copyMe
	 *            the user to be copied.
	 */
	private UserModel(UserModel copyMe) {
		this();
		copy(this, copyMe);
	}

	/**
	 * Copies all the fields of src into dest.
	 * 
	 * @param dest
	 *            the destination of the copy operation.
	 * @param src
	 *            the source of the copy operation.
	 */
	private static void copy(UserModel dest, UserModel src) {
		dest.userID = src.userID;
		dest.username = src.username;
		dest.passwordHash = src.passwordHash;
		dest.email = src.email;
		dest.name = src.name;
		dest.location = src.location;
		dest.reputationLevel = src.reputationLevel;
		dest.sessionToken = src.sessionToken;
		dest.isConnectedToFacebook = src.isConnectedToFacebook;
		dest.deleteToken = src.deleteToken;
	}

	/**
	 * Creates a new user, initializing it with the data from {@code result}.
	 * {@code result} must therefore be a valid row from the users table.
	 * 
	 * @param result
	 *            the data to be put into the new user.
	 * @return a user with data taken from {@code result}.
	 * @throws SQLException
	 *             if any SQL error occurs.
	 */
	private static UserModel userFromResultSet(ResultSet result)
			throws SQLException {
		UserModel user = new UserModel();
		user.userID = result.getInt(result.findColumn("userID"));
		user.username = result.getString(result.findColumn("username"));
		user.passwordHash = result.getString(result.findColumn("passwordHash"));
		user.email = result.getString(result.findColumn("email"));
		user.name = result.getString(result.findColumn("name"));
		user.location = result.getString(result.findColumn("location"));
		user.reputationLevel = result.getInt(result.findColumn("repLevel"));
		user.sessionToken = result.getString(result.findColumn("sessionToken"));
		user.isConnectedToFacebook = result.getBoolean(result
				.findColumn("isFBConnected"));
		user.deleteToken = result.getString(result.findColumn("deleteToken"));
		return user;
	}

	/**
	 * For debug purposes only.
	 */
	@Override
	public String toString() {
		return "UserModel [userID=" + userID + ", username=" + username
				+ ", passwordHash=" + passwordHash + ", email=" + email
				+ ", name=" + name + ", reputationLevel=" + reputationLevel
				+ ", sessionToken=" + sessionToken + ", isConnectedToFacebook="
				+ isConnectedToFacebook + "location, " + location
				+ "deleteToken, " + deleteToken + "]";
	}

}
