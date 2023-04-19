package com.dynatrace.easytravel.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang3.time.DateUtils;

import com.dynatrace.easytravel.config.EasyTravelConfig;
import com.dynatrace.easytravel.constants.BaseConstants;
import com.dynatrace.easytravel.jpa.business.Journey;
import com.dynatrace.easytravel.jpa.business.User;
import com.dynatrace.easytravel.model.DataAccess;
import com.dynatrace.easytravel.model.GenericDataAccess;
import com.dynatrace.easytravel.persistence.SqlDatabase;

/**
 * fill data in the rating and guestbook tables
 *
 * @author cwat-cchen
 *
 */
public class CreateMysqlContent {
	private static final Object lock = new Object();

	private final int defaultCount = 1000;
	protected Thread randomJourneysThread;
	private final int count;

	private DataAccess dataAccess;
	
	private static final String[] CommentList = {
			" sucks. Don't waste your money on this journey.",
			" is too good to be true!!",
			" is a breathtaking place to be on holiday!! I love it!",
			" is awesome. You can't go wrong when you book trips with this company The staff in the booking office are "
					+ " very helpful,the coaches are very comfortable.",
			" not good. (sorry, don't mean to be negative, but....) Worst meal I had in 4 weeks in this area.",
			" is perfect. We arrived late in the evening and we checked in quickly. Exhausted; this was wonderful. Room service is available 24/7."
					+ " The beef noodle soup arrived within 10 - 15 minutes and was tasty.",
			" very modern,marvelous, trendy, and has a great location.",
			" is worth visiting. I definitely will come here again!",
			" is a great place to visit? Ugh, absolutely disgusting.",
			" is... well, the basis of my review is that everything is fine. No more, no less, just fine. "
					+ " It depends what you are looking for really." };

	// determine how much data is inserted into rating and guestbook tables
	// default amount is 1000
	public CreateMysqlContent(int count) {
		this.count = count;

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int count = 100;
		CreateMysqlContent main = new CreateMysqlContent(count);

		main.create();
		main.close();
	}

	public void create() throws SQLException {
		synchronized (lock) {
			SqlDatabase sqlDatabase = new SqlDatabase();
			dataAccess = new GenericDataAccess(sqlDatabase.createNewBusinessController());

			int jcount = dataAccess.getJourneyCount();
			Collection<Journey> journeys;
			if (count <= jcount && count > defaultCount) {
				journeys = dataAccess.getJourneys(count);
			} else {
				journeys = dataAccess.getJourneys(defaultCount);
				journeys.size();
			}

			createRatings(journeys);
			createComments(journeys);
		}

	}

	private void createRatings(Collection<Journey> journeys) throws SQLException {
		final EasyTravelConfig config = EasyTravelConfig.read();

		String ratingSql = "INSERT INTO " + BaseConstants.EASYTRAVEL.toLowerCase()
				+ ".rating VALUES(?,?,?,?,?,?)";

		try {
			Connection conn = DriverManager.getConnection(config.mysqlUrl,
					config.mysqlUser, config.mysqlPassword);
			try {
				Random r = new Random();
				int count = 1;

				PreparedStatement statement = conn.prepareStatement(ratingSql);
				try {
					for (Journey j : journeys) {

						for (Category c : Category.values()) {

							int rValue = r.nextInt();
							int number_votes = Math.abs(rValue) % 50 + 1;
							int dec_avg = Math.abs(rValue) % 5 + 1;

							statement.setString(1, j.getId() + c.name());
							statement.setInt(2, number_votes);
							statement.setInt(3, number_votes * dec_avg);
							statement.setFloat(4, dec_avg);
							statement.setFloat(5, dec_avg);
							statement.setFloat(6, dec_avg);

							statement.addBatch();
						}

						if (count % defaultCount == 0) {
							statement.executeBatch();
						}

						count++;
					}
				} finally {
					statement.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createComments(Collection<Journey> journeys) throws SQLException {
		String guestbookSql = "INSERT INTO " + BaseConstants.EASYTRAVEL.toLowerCase()
				+ ".guestbook VALUES(?,?,?,?,?)";

		final EasyTravelConfig config = EasyTravelConfig.read();
		try {
			Connection conn = DriverManager.getConnection(config.mysqlUrl,
					config.mysqlUser, config.mysqlPassword);
			try {
				int count = 1;

				List<User> ret = new ArrayList<User>(dataAccess.allUsers());
				int uSize = ret.size();
				int cSize = CommentList.length;
				Random r = new Random();

				PreparedStatement statement = conn.prepareStatement(guestbookSql);
				try {
					for (Journey j : journeys) {

						int rValue = r.nextInt();
						String user = ret.get(Math.abs(rValue) % uSize).getName();
						String comment = j.getName() + " "
								+ CommentList[Math.abs(rValue) % cSize];

						Date date = new Date(System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY
								* (Math.abs(rValue) % uSize));

						Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String commentDate = formatter.format(date);

						statement.setInt(1, count);
						statement.setString(2, user);
						statement.setInt(3, j.getId());
						statement.setString(4, comment);
						statement.setString(5, commentDate);

						statement.addBatch();

						if (count % defaultCount == 0) {

							statement.executeBatch();
						}

						count++;
					}
				} finally {
					statement.close();
				}
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public enum Category {

		_staff, _services, _cleanliness, _comfort, _value, _location;

	}

	public void close() throws IOException {
		synchronized (lock) {
			if (randomJourneysThread != null && randomJourneysThread.isAlive()) {
				try {
					randomJourneysThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (dataAccess != null) {
				dataAccess.close();
			}
		}
	}
}
