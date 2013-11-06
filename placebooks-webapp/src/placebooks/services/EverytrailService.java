/**
 * 
 */
package placebooks.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import placebooks.controller.CommunicationHelper;
import placebooks.controller.ItemFactory;
import placebooks.controller.PropertiesSingleton;
import placebooks.model.GPSTraceItem;
import placebooks.model.ImageItem;
import placebooks.model.LoginDetails;
import placebooks.model.TextItem;
import placebooks.model.User;
import placebooks.services.model.EverytrailLoginResponse;
import placebooks.services.model.EverytrailPicturesResponse;
import placebooks.services.model.EverytrailResponseStatusData;
import placebooks.services.model.EverytrailTripsResponse;

/**
 * @author pszmp
 * 
 */
public class EverytrailService extends Service
{
	public final static ServiceInfo SERVICE_INFO = new ServiceInfo("Everytrail", "http://www.everytrail.com/", false);

	private final static String apiBaseUrl = "http://www.everytrail.com/api/";

	private static final Logger log = Logger.getLogger(EverytrailService.class.getName());

	@Override
	public boolean checkLogin(final String username, final String password)
	{
		final EverytrailLoginResponse response = userLogin(username, password);
		log.info(response.getStatus() + ":" + response.getValue());
		if (response.getStatus().equals("error")) { return false; }
		return true;
	}

	@Override
	public ServiceInfo getInfo()
	{
		return SERVICE_INFO;
	}

	/**
	 * Return a list of all pictures for a given user_id
	 * 
	 * @param userId
	 * @return EverytrailPicturesResponse
	 */
	public EverytrailPicturesResponse pictures(final String userId)
	{
		return pictures(userId, null, null);
	}

	/**
	 * Get a list of pictures for a given userid
	 * 
	 * @param userId
	 *            an everytrail userid - supply their username and password if private pictures are
	 *            needed
	 * @param String
	 *            A user's username
	 * @param String
	 *            A user's password
	 * @return EverytrailPicturesResponse
	 */
	public EverytrailPicturesResponse pictures(final String userId, final String username, final String password)
	{
		// The pictures API doesn't work well, so get pictures via trips...
		final EverytrailTripsResponse tripsData = trips(username, password, userId);

		final HashMap<String, Node> picturesToReturn = new HashMap<String, Node>();
		final HashMap<String, String> pictureTrips = new HashMap<String, String>();
		final HashMap<String, String> tripNames = new HashMap<String, String>();

		String status_to_return = "error";

		if (tripsData.getStatus().equals("error"))
		{
			log.warn("Get pictures failed when getting trips with error code: " + tripsData.getStatus());
			// picturesToReturn = tripsData.getTrips();
		}
		else
		{
			status_to_return = "success";
			final Vector<Node> trips = tripsData.getTrips();
			// log.debug("Got " + trips.size() + " trips, getting pictures...");
			for (int tripListIndex = 0; tripListIndex < trips.size(); tripListIndex++)
			{
				final Node tripNode = trips.elementAt(tripListIndex);
				final NamedNodeMap attributes = tripNode.getAttributes();
				final String tripId = attributes.getNamedItem("id").getNodeValue();
				// Then look at the properties in the child nodes to get url, title, description,
				// etc.
				final NodeList tripProperties = tripNode.getChildNodes();
				String tripName = "";
				for (int propertyIndex = 0; propertyIndex < tripProperties.getLength(); propertyIndex++)
				{
					final Node item = tripProperties.item(propertyIndex);
					final String itemName = item.getNodeName();
					// log.debug("Inspecting property: " + itemName + " which is " +
					// item.getTextContent());
					if (itemName.equals("name"))
					{
						tripName = item.getTextContent();
					}
				}

				log.debug("Getting pictures for trip: " + tripId + " " + tripName);
				tripNames.put(tripId, tripName);

				final EverytrailPicturesResponse tripPics = TripPictures(tripId, username, password, tripName);

				log.debug("Pictures in trip: " + tripPics.getPicturesMap().values().size());
				final HashMap<String, Node> tripPicList = tripPics.getPicturesMap();
				for (final String pic_id : tripPicList.keySet())
				{
					final Node picture_node = tripPicList.get(pic_id);
					if (picture_node.getNodeName().equals("picture"))
					{
						picturesToReturn.put(pic_id, picture_node);
						pictureTrips.put(pic_id, tripId);
					}
					// log.debug("Picture: " + pic_id + " " + picture_node.getTextContent());
				}
			}
		}

		final EverytrailPicturesResponse returnValue = new EverytrailPicturesResponse(status_to_return,
				picturesToReturn, pictureTrips, tripNames);
		return returnValue;
	}

	/**
	 * Return a list of all trips for a given user_id
	 * 
	 * @param userId
	 *            an everytrail userid obtained from a user - user may need to be logged in first,
	 *            and this will at least get the user id
	 * @return EverytrailTripsResponse
	 */
	public EverytrailTripsResponse trips(final String userId)
	{
		final EverytrailTripsResponse result = Trips(null, null, userId, null, null, null, null, null, null, null, null);
		return result;
	}

	/**
	 * Log in to the Everytrail API with a given username and password n.b. this appears to be
	 * submitted as HTTP not HTTPS so password is potentially insecure.
	 * 
	 * @param username
	 *            An everytrail username
	 * @param password
	 *            An everytrail password
	 * @return EverytrailLoginResponse with status success/error and value userid/error code
	 */
	public EverytrailLoginResponse userLogin(final String username, final String password)
	{
		final StringBuilder postResponse = new StringBuilder();
		String loginStatus = "";
		String loginStatusValue = "";

		try
		{
			// Construct data to post - username and password
			String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

			// Send data by setting up the api password http authentication and UoN proxy
			Authenticator.setDefault(new CommunicationHelper.HttpAuthenticator(PropertiesSingleton
					.get(EverytrailService.class.getClassLoader()).getProperty(PropertiesSingleton.EVERYTRAIL_API_USER,
																				""), PropertiesSingleton
					.get(EverytrailService.class.getClassLoader())
					.getProperty(PropertiesSingleton.EVERYTRAIL_API_PASSWORD, "")));
			final URL url = new URL(apiBaseUrl + "user/login");

			// Use getConnection to get the URLConnection with or without a proxy
			final URLConnection conn = CommunicationHelper.getConnection(url);
			conn.setDoOutput(true);
			final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null)
			{
				postResponse.append(line);
			}
			wr.close();
			rd.close();
		}
		catch (final Exception e)
		{
			log.debug(e.getMessage());
		}

		final Document doc = parseResponseToXml(postResponse);
		// Parse the XML response and construct the response data to return
		log.debug(postResponse);
		final EverytrailResponseStatusData responseStatus = parseResponseStatus("etUserLoginResponse", doc);
		log.info("UserLogin called parseResponseStatus, result=" + responseStatus.getStatus() + ","
				+ responseStatus.getValue());

		loginStatus = responseStatus.getStatus();
		loginStatusValue = responseStatus.getValue();
		log.debug(responseStatus.getStatus());
		log.debug(responseStatus.getValue());
		if (responseStatus.getStatus().equals("success"))
		{
			log.info("Log in succeeded with user id: " + responseStatus.getValue());
		}
		else
		{
			if (doc.getDocumentElement().getAttribute("status").equals("error"))
			{
				log.warn("Log in failed with error code: " + loginStatusValue);
			}
			else
			{
				log.error("Everytrail login status gave unexpected value: " + loginStatusValue);
			}
		}

		final EverytrailLoginResponse output = new EverytrailLoginResponse(loginStatus, loginStatusValue);
		return output;
	}

	@Override
	protected void search(final EntityManager em, final User user, final double lon, final double lat,
			final double radius)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void sync(final EntityManager manager, final User user, final LoginDetails details, final double lon,
			final double lat, final double radius)
	{
		try
		{
			final EverytrailLoginResponse loginResponse = userLogin(details.getUsername(), details.getPassword());

			if (loginResponse.getStatus().equals("error"))
			{
				log.error("Everytrail login failed");
				return;
			}

			// Save user id if needed
			if (loginResponse.getValue() != details.getUserID())
			{
				manager.getTransaction().begin();
				details.setUserID(loginResponse.getValue());
				manager.merge(details);
				manager.getTransaction().commit();
			}

			final ArrayList<String> imported_ids = new ArrayList<String>();
			final ArrayList<String> available_ids = new ArrayList<String>();

			final EverytrailTripsResponse trips = trips(details.getUsername(), details.getPassword(),
														loginResponse.getValue());

			for (final Node trip : trips.getTrips())
			{
				// Get trip ID
				final NamedNodeMap tripAttr = trip.getAttributes();
				final String tripId = tripAttr.getNamedItem("id").getNodeValue();
				log.debug("IMPORT: Trip ID is " + tripId + " **************");

				String tripName = "Unknown trip";
				String tripDescription = "";
				// Then look at the properties in the child nodes to get url, title, description,
				// etc.
				final NodeList tripProperties = trip.getChildNodes();
				for (int propertyIndex = 0; propertyIndex < tripProperties.getLength(); propertyIndex++)
				{
					final Node item = tripProperties.item(propertyIndex);
					final String itemName = item.getNodeName();
					// log.debug("Inspecting property: " + itemName + " which is " +
					// item.getTextContent());
					if (itemName.equals("name"))
					{
						log.debug("Trip name is: " + item.getTextContent());
						tripName = item.getTextContent();
					}
					if (itemName.equals("description"))
					{
						log.debug("Trip description is: " + item.getTextContent());
						tripDescription = item.getTextContent();
					}
				}
				available_ids.add("everytrail-" + tripId);

				if (tripDescription.length() > 0)
				{
					final TextItem descriptionItem = new TextItem();
					descriptionItem.setOwner(user);
					final String externalId = "everytrail-" + tripId + "-textItem";
					descriptionItem.setExternalID(externalId);
					descriptionItem.setText(tripDescription);
					descriptionItem.addMetadataEntry("source", SERVICE_INFO.getName());
					descriptionItem.addMetadataEntryIndexed("trip_name", tripName);
					descriptionItem.addMetadataEntryIndexed("title", tripName);
					descriptionItem.addMetadataEntry("trip", tripId);
					descriptionItem.saveUpdatedItem();
					available_ids.add(externalId);
					imported_ids.add(externalId);
				}

				GPSTraceItem gpsItem = new GPSTraceItem(user);
				try
				{
					ItemFactory.toGPSTraceItem(user, trip, gpsItem, tripId, tripName);
					gpsItem = (GPSTraceItem) gpsItem.saveUpdatedItem();
					imported_ids.add(gpsItem.getExternalID());
				}
				catch (final Exception e)
				{
					log.error("Problem importing Trip " + tripId, e);
				}

				final EverytrailPicturesResponse picturesResponse = TripPictures(	tripId, details.getUsername(),
																					details.getPassword(), tripName);

				if (picturesResponse.getStatus().equals("success"))
				{
					final HashMap<String, Node> pictures = picturesResponse.getPicturesMap();
					int i = 0;
					for (final Node picture : pictures.values())
					{
						log.info("Processing picture " + i++);
						ImageItem imageItem = new ImageItem(user, null, null, null);
						ItemFactory.toImageItem(user, picture, imageItem, tripId, tripName);
						imageItem = (ImageItem) imageItem.saveUpdatedItem();
						imported_ids.add(imageItem.getExternalID());
						available_ids.add(imageItem.getExternalID());
					}
				}
				else
				{
					log.error("Can't get trip pictures: " + picturesResponse.getStatus());
				}
			}

			final int itemsDeleted = cleanupItems(manager, imported_ids, user);
			log.info("Everytrail cleanup, " + imported_ids.size() + " items added/updated, " + itemsDeleted
					+ " removed");
			// Now add search results
			// log.info("Searching Everytrail for items near " + lat + " " + lon + " radius:" +
			// radius);
			// search(manager, user, lon, lat, radius);
			details.setLastSync();
			log.info("Finished Everytrail import, " + imported_ids.size() + " items added/updated, " + itemsDeleted
					+ " removed");
		}
		finally
		{
			if (manager.getTransaction().isActive())
			{
				manager.getTransaction().rollback();
			}
		}
	}

	/**
	 * Perform a post to the given Everytrail api destination with the parameters specified
	 * 
	 * @param postDestination
	 *            API destination after http://www.everytrail.com/api/ - e.g. user/trips
	 * @param Hashtable
	 *            <String, String> params a hastable of the parameters to post to the api with name
	 *            / values as strings
	 * @return String A string containing the post response from Everytrail
	 */
	private String getPostResponseWithParams(final String postDestination, final Hashtable<String, String> params)
	{
		final StringBuilder postResponse = new StringBuilder();

		// Add version 3 param to all requests
		params.put("version", "3");
		// Construct data to post by iterating through parameter Hashtable keys Enumeration
		final StringBuilder data = new StringBuilder();
		final Enumeration<String> paramNames = params.keys();
		try
		{
			while (paramNames.hasMoreElements())
			{
				final String paramName = paramNames.nextElement();
				data.append(URLEncoder.encode(paramName, "UTF-8") + "="
						+ URLEncoder.encode(params.get(paramName), "UTF-8"));
				if (paramNames.hasMoreElements())
				{
					data.append("&");
				}
			}

			// Send data by setting up the api password http authentication and UoN proxy

			Authenticator.setDefault(new CommunicationHelper.HttpAuthenticator(PropertiesSingleton
					.get(EverytrailService.class.getClassLoader()).getProperty(PropertiesSingleton.EVERYTRAIL_API_USER,
																				""), PropertiesSingleton
					.get(EverytrailService.class.getClassLoader())
					.getProperty(PropertiesSingleton.EVERYTRAIL_API_PASSWORD, "")));

			final URL url = new URL(apiBaseUrl + postDestination);
			final URLConnection conn = CommunicationHelper.getConnection(url);

			conn.setDoOutput(true);
			final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data.toString());
			wr.flush();

			// Get the response
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null)
			{
				postResponse.append(line);
			}
			wr.close();
			rd.close();
		}
		catch (final Exception ex)
		{
			log.debug(ex.getMessage());
		}

		return postResponse.toString();
	}

	/**
	 * Return the success/error status of an Everytrail API call
	 * 
	 * @param targetElementId
	 * @return
	 */
	private EverytrailResponseStatusData parseResponseStatus(final String targetElementId, final Document doc)
	{
		String status = "";
		String value = "";
		try
		{
			if (doc.getDocumentElement().getNodeName() == targetElementId)
			{
				status = doc.getDocumentElement().getAttribute("status");
				if (doc.getDocumentElement().hasChildNodes())
				{
					if (status.equals("success"))
					{
						value = doc.getDocumentElement().getChildNodes().item(0).getTextContent();
					}
					else
					{
						log.debug("Everytrail call returned status: " + status);
						value = doc.getDocumentElement().getChildNodes().item(0).getTextContent();
					}
				}
			}
		}
		catch (final Exception e)
		{
			log.error("Problem checking Everytrail response: " + e.toString());
			log.debug(e.toString(), e);
		}
		return new EverytrailResponseStatusData(status, value);
	}

	/**
	 * Parse and Everytrail API response into an XML document from HTTP string response.
	 * 
	 * @param Strung
	 *            Post response from everytrail http post
	 * @return Document XML structured document
	 */
	private Document parseResponseToXml(final String postString)
	{
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		Document doc = null;
		try
		{
			db = dbf.newDocumentBuilder();
			final InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(postString));
			doc = db.parse(is);
			doc.getDocumentElement().normalize();
		}
		catch (final Exception ex)
		{
			log.equals("Problem parsing Everytrail XML response: " + ex.getMessage());
			log.debug(ex.getStackTrace());
		}
		return doc;
	}

	/**
	 * Parse and Everytrail API response into an XML document from HTTP string response.
	 * 
	 * @param StrungBuilder
	 *            Post response from everytrail http post
	 * @return Document XML structured document
	 */
	private Document parseResponseToXml(final StringBuilder postResponse)
	{
		return parseResponseToXml(postResponse.toString());
	}

	/**
	 * Get a list of pictures for a particular trip including private pictures
	 * 
	 * @param tripId
	 * @param username
	 * @param password
	 * @return Vector<Node>
	 */
	private EverytrailPicturesResponse TripPictures(final String tripId, final String username, final String password,
			final String tripNameParam)
	{

		final Hashtable<String, String> params = new Hashtable<String, String>();
		params.put("trip_id", tripId);
		if (username != null)
		{
			params.put("username", username);
		}
		if (password != null)
		{
			params.put("password", password);
		}

		String tripName = "";
		final HashMap<String, Node> picturesList = new HashMap<String, Node>();
		EverytrailResponseStatusData resultStatus = null;
		if (tripNameParam == null)
		{
			final String tripResponse = getPostResponseWithParams("trip/", params);
			final Document tripDoc = parseResponseToXml(tripResponse);
			final EverytrailResponseStatusData tripResultStatus = parseResponseStatus("etTripPicturesResponse", tripDoc);
			if (tripResultStatus.getStatus().equals("success"))
			{
				final NodeList nameNodes = tripDoc.getElementsByTagName("name");;
				// log.debug("Child nodes: " + pictureNodes.getLength());
				for (int nameNodesIndex = 0; nameNodesIndex < nameNodes.getLength(); nameNodesIndex++)
				{
					final Node nameNode = nameNodes.item(nameNodesIndex);
					tripName = nameNode.getTextContent();
				}
			}
			else
			{
				log.error("Can't get trip name");
			}
		}
		else
		{
			tripName = tripNameParam;
		}
		final HashMap<String, String> tripData = new HashMap<String, String>();
		tripData.put(tripId, tripName);
		final HashMap<String, String> pictureTrips = new HashMap<String, String>();

		log.debug("Pictures username: " + username + " " + password);
		final String postResponse = getPostResponseWithParams("trip/pictures", params);
		final Document doc = parseResponseToXml(postResponse);
		resultStatus = parseResponseStatus("etTripPicturesResponse", doc);

		if (resultStatus.getStatus().equals("success"))
		{
			final NodeList pictureNodes = doc.getElementsByTagName("picture");;
			// log.debug("Child nodes: " + pictureNodes.getLength());
			for (int pictureNodesIndex = 0; pictureNodesIndex < pictureNodes.getLength(); pictureNodesIndex++)
			{
				final Node pictureNode = pictureNodes.item(pictureNodesIndex);
				// Get pic ID
				final NamedNodeMap attr = pictureNode.getAttributes();
				final String id = attr.getNamedItem("id").getNodeValue();
				pictureTrips.put(id, tripId);
				picturesList.put(id, pictureNode);
			}
		}
		else
		{
			if (resultStatus.getStatus().equals("error"))
			{
				log.warn("Get pictures failed with error code: " + resultStatus.getValue());
				log.debug(postResponse);
			}
			else
			{
				log.error("Get pictures error status gave unexpected value: " + resultStatus.getStatus());
			}
			final NodeList response_list_data = doc.getElementsByTagName("error");
			{
				for (int i = 0; i < response_list_data.getLength(); i++)
				{
					final Node element = response_list_data.item(i);
					final NodeList error_children = element.getChildNodes();
					for (int child_counter = 0; child_counter < error_children.getLength(); child_counter++)
					{
						try
						{
							final Node error_element = error_children.item(child_counter);
							picturesList.put("0", error_element);
						}
						catch (final NullPointerException npx)
						{
							log.error("Can't interpret error data for item " + child_counter + " from response: "
									+ resultStatus.getValue());
						}
					}
				}
			}
		}
		return new EverytrailPicturesResponse(resultStatus.getStatus(), picturesList, pictureTrips, tripData);
	}

	/**
	 * Return a list of all trips for a given user_id - return private pictures and trips if
	 * username.password is given
	 * 
	 * @param username
	 * @param password
	 * @param userId
	 *            an everytrail userid obtained from a user - user may need to be logged in first,
	 *            and this will at least get the user id
	 * @return
	 * @return EverytrailTripsResponse
	 */
	private EverytrailTripsResponse trips(final String username, final String password, final String userId)
	{
		final EverytrailTripsResponse result = Trips(	username, password, userId, null, null, null, null, null, null,
														null, null);
		return result;
	}

	/**
	 * Get a list of trips for a given userid
	 * 
	 * @param userId
	 *            an everytrail userid obtained from a user - user may need to be logged in first,
	 *            and this will at least get the user id
	 * @param limit
	 *            Number of photos to get, limit is 20 as default, set by everytrail API.
	 * @param start
	 *            Starting point for photos, default 0 set by everytrail api
	 * @return EverytrailTripsResponse
	 */
	private EverytrailTripsResponse Trips(final String username, final String password, final String userId,
			final Double lat, final Double lon, final Date modifiedAfter, final String sort, final String order,
			final String limit, final String start, final Boolean minimal)
	{
		final Hashtable<String, String> params = new Hashtable<String, String>();
		params.put("user_id", userId);
		/*
		 * MCP - Ignore username and password since you can't get media for private trips from the
		 * API if (username != null) { params.put("username", username); } if (password != null) {
		 * params.put("password", password); }
		 */
		if (lat != null)
		{
			params.put("lat", lat.toString());
		}
		if (lon != null)
		{
			params.put("lon", lon.toString());
		}

		if (modifiedAfter != null)
		{
			final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			params.put("modified_after", formatter.format(modifiedAfter));
		}

		if (sort != null)
		{
			params.put("sort", sort);
		}
		if (order != null)
		{
			params.put("order", order);
		}
		if (limit != null)
		{
			params.put("limit", limit);
		}
		if (limit != null)
		{
			params.put("start", start);
		}
		if (minimal != null)
		{
			params.put("minimal", minimal.toString());
		}

		final String postResponse = getPostResponseWithParams("user/trips", params);
		log.info(postResponse);
		final Document doc = parseResponseToXml(postResponse);
		final EverytrailResponseStatusData responseStatus = parseResponseStatus("etUserTripsResponse", doc);
		log.info("Trips called parseResponseStatus, result=" + responseStatus.getStatus());

		final Vector<Node> tripsList = new Vector<Node>();
		if (responseStatus.getStatus().equals("success"))
		{
			final NodeList response_list_data = doc.getElementsByTagName("trip");
			for (int i = 0; i < response_list_data.getLength(); i++)
			{
				final Node element = response_list_data.item(i);
				tripsList.add(element);
			}
		}
		else
		{
			final NodeList response_list_data = doc.getElementsByTagName("error");
			{
				for (int i = 0; i < response_list_data.getLength(); i++)
				{
					final Node element = response_list_data.item(i);
					final NodeList error_children = element.getChildNodes();
					for (int child_counter = 0; child_counter < error_children.getLength(); child_counter++)
					{
						try
						{
							final Node error_element = error_children.item(child_counter);
							tripsList.add(error_element);
						}
						catch (final NullPointerException npx)
						{
							log.error("Can't interpret error data for item " + child_counter + " from response: "
									+ responseStatus.getValue());
						}
					}
				}
			}
		}

		final EverytrailTripsResponse returnValue = new EverytrailTripsResponse(responseStatus.getStatus(), tripsList);
		return returnValue;
	}

}