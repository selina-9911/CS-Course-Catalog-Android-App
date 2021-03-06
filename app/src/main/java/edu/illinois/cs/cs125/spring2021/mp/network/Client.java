package edu.illinois.cs.cs125.spring2021.mp.network;

import android.util.Log;
import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.ExecutorDelivery;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.models.Course;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Course API client.
 *
 * <p>You will add functionality to the client as part of MP1 and MP2.
 */
public final class Client {
  private static final String TAG = Client.class.getSimpleName();
  private static final int INITIAL_CONNECTION_RETRY_DELAY = 1000;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Course API client callback interface.
   *
   * <p>Provides a way for the client to pass back information obtained from the course API server.
   */
  public interface CourseClientCallbacks {
    /**
     * Return course summaries for the given year and semester.
     *
     * @param year the year that was retrieved
     * @param semester the semester that was retrieved
     * @param summaries an array of course summaries
     */
    default void summaryResponse(String year, String semester, Summary[] summaries) {}

    /**
     * Return ...
     *
     * @param summary ...
     * @param course ...
     */
    default void courseResponse(Summary summary, Course course) {}
    /**
     * Return ...
     *
     * @param string ...
     */
    default void stringResponse(String string) {}
    /**
     * Return ...
     *
     * @param summary ...
     * @param rating ...
     */
    default void yourRating(Summary summary, Rating rating) {}
  }
  /**
   * Return ...
   *
   * @param summary ...
   * @param clientID ...
   * @param callbacks ...
   */
  public void getRating(
                         @NonNull final Summary summary,
                         @NonNull final String clientID,
                         @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "rating/" + summary.getYear() + "/" + summary.getSemester() + "/"
            + summary.getDepartment() + "/" + summary.getNumber() + "?client=" + clientID;  //server url
    StringRequest stringRequest =
            new StringRequest(
                    Request.Method.GET,
                    url, //create a request and below is the response received
                    response -> {
                      try {
                        Rating rating = objectMapper.readValue(response, Rating.class);
                        //the response from server is string version of rating object. now -> rating object
                        Log.i("NetworkExample", "getRating returned" + "with rating =" + rating.getRating());
                        callbacks.yourRating(summary, rating);
                        // take the deserialized rating and return the rating. this is called in courseactivity
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString()));
    requestQueue.add(stringRequest); // make the request
  }


  /**
   * Return ...
   *
   * @param summary ...
   * @param rating ...
   * @param callbacks ...
   */
  public void postRating(
          @NonNull final Summary summary,
          @NonNull final Rating rating,
          @NonNull final CourseClientCallbacks callbacks) throws JsonProcessingException {
    String url = CourseableApplication.SERVER_URL + "rating/" + summary.getYear() + "/" + summary.getSemester() + "/"
            + summary.getDepartment() + "/" + summary.getNumber() + "?client=" + rating.getId();  //server url
    String ratingString = mapper.writeValueAsString(rating);
    StringRequest stringRequest =
            new StringRequest(
                    Request.Method.POST,
                    url, //create a request and below is the response received
                    response -> {
                      try {
                        Rating postedRating = objectMapper.readValue(response, Rating.class);
                        //the response from server is string version of rating object. now -> rating object
                        Log.i("NetworkExample", "getRating returned" + "with rating =" + postedRating.getRating());
                        callbacks.yourRating(summary, postedRating);
                        // take the deserialized rating and return the rating. this is called in courseactivity
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString())) {
          @Override
          public byte[] getBody() throws AuthFailureError {
            return ratingString.getBytes();
          }
        };
    requestQueue.add(stringRequest); // make the request
  }


//  //not just string but an object
//  /**
//   * ...
//   * @param string ...
//   * @param callbacks ..
//   */
//  public void postString(@NonNull final String string, @NonNull final CourseClientCallbacks callbacks) {
//    String url = CourseableApplication.SERVER_URL + "string/";  //server url
//    Log.i("NetworkExample", "Request summary from " + url);
//    StringRequest stringRequest =
//            new StringRequest(
//                    Request.Method.POST,
//                    url, //create a request and below is the response received
//                    response -> {
//                      callbacks.stringResponse(response.getBytes().toString());
//                    },
//                    error -> Log.e(TAG, error.toString())) {
//              @Override
//              public byte[] getBody() throws AuthFailureError {
//                return string.getBytes();
//              }
//            };
//    requestQueue.add(stringRequest); // make the request
//  }
  /**
   * Retrieve course summaries for a given year and semester.
   *
   * @param year the year to retrieve
   * @param semester the semester to retrieve
   * @param callbacks the callback that will receive the result
   */
  public void getSummary(//mainactivity calls this function to get list of courses in home page via callback
      @NonNull final String year,
      @NonNull final String semester,
      @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "summary/" + year + "/" + semester;  //server url
    Log.i("NetworkExample", "Request summary from " + url);
    StringRequest summaryRequest =
        new StringRequest(
            Request.Method.GET,
            url, //create a request and below is the response received
            response -> {
              try {
                Summary[] courses = objectMapper.readValue(response, Summary[].class);
                //after getting response, json -> str[]
                Log.i("NetworkExample", "getSummary returned" + courses.length + "courses");
                callbacks.summaryResponse(year, semester, courses);
                // take the deserialized courses list and return the course list. this is called in main activity
              } catch (JsonProcessingException e) {
                e.printStackTrace();
              }
            },
            error -> Log.e(TAG, error.toString()));
    requestQueue.add(summaryRequest); // make the request
  }

  /**
   * Retrieve course description for a course(Summary object).
   *
   * @param summary the course that we are searching for the description
   * @param callbacks returns the description
   */
  public void getCourse(
          @NonNull final Summary summary,
          @NonNull final CourseClientCallbacks callbacks) {
    String url = CourseableApplication.SERVER_URL + "course/" + summary.getYear() + "/"
            + summary.getSemester() + "/" + summary.getDepartment() + "/" + summary.getNumber();
    //server url
    Log.i("NetworkExample", "Request summary from " + url);
    StringRequest courseRequest =
            new StringRequest(
                    Request.Method.GET,
                    url, //create a request and below is the response received
                    response -> {
                      try {
                        Course course = objectMapper.readValue(response, Course.class);
                        //after getting response, json -> str
                        Log.i("NetworkExample", "description returned");
                        callbacks.courseResponse(summary, course);
                        // take the deserialized courses list and return the course list.
                        // this is called in main activity
                      } catch (JsonProcessingException e) {
                        e.printStackTrace();
                      }
                    },
                    error -> Log.e(TAG, error.toString()));
    requestQueue.add(courseRequest); // make the request
  }




  private static Client instance;

  /**
   * Retrieve the course API client. Creates one if it does not already exist.
   *
   * @return the course API client
   */
  public static Client start() {
    if (instance == null) {
      instance = new Client(false);
    }
    return instance;
  }

  /**
   * Create and retrieve the course API client for testing.
   *
   * @return the course API client
   */
  public static Client startTesting() {
    return new Client(true);
  }

  private static final int MAX_STARTUP_RETRIES = 8;
  private static final int THREAD_POOL_SIZE = 4;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RequestQueue requestQueue;

  /*
   * Set up our client, create the Volley queue, and establish a backend connection.
   */
  private Client(final boolean testing) {
    // Configure the Volley queue used for our network requests
    Cache cache = new NoCache();
    Network network = new BasicNetwork(new HurlStack());
    HttpURLConnection.setFollowRedirects(true);
    if (testing) {
      requestQueue =
              new RequestQueue(
                      cache,
                      network,
                      THREAD_POOL_SIZE,
                      new ExecutorDelivery(Executors.newSingleThreadExecutor()));
    } else {
      requestQueue = new RequestQueue(cache, network);
    }

    // Configure the Jackson object mapper to ignore unknown properties
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Make sure the backend URL is valid
    URL serverURL;
    try {
      serverURL = new URL(CourseableApplication.SERVER_URL);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Bad server URL: " + CourseableApplication.SERVER_URL);
      return;
    }

    // Start a background thread to establish the server connection
    new Thread(
            () -> {
              for (int i = 0; i < MAX_STARTUP_RETRIES; i++) {
                try {
                  // Issue a HEAD request for the root URL
                  HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
                  connection.setRequestMethod("HEAD");
                  connection.connect();
                  connection.disconnect();
                  // Once this succeeds, we can start the Volley queue
                  requestQueue.start();
                  break;
                } catch (Exception e) {
                  Log.e(TAG, e.toString());
                }
                // If the connection fails, delay and then retry
                try {
                  Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY);
                } catch (InterruptedException ignored) {
                }
              }
            })
        .start();
  }
}
