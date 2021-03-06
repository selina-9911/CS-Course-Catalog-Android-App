package edu.illinois.cs.cs125.spring2021.mp.network;

import android.util.Log;
import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private final int lengthOfPath = 4;
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();

  private MockResponse getSummary(@NonNull final String path) { //continue from below; now get summary of year/semester
    Log.i("NetworkExample", "Request for" + path);
    String[] parts = path.split("/");
    if (parts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    //summaries.put(year + "_" + semester, json)
    String summary = summaries.get(parts[0] + "_" + parts[1]); //pull from the json file 2021_spring_summary.json
    Log.i("NetworkExample", "getSummary");
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
    //returns the mock response json file -> client callback -> main activity lists all courses
  }

  private MockResponse getCourse(@NonNull final String path) {
    //courses.put(Summary course, node.toPrettyString())
    String[] parts = path.split("/");
    if (parts.length != lengthOfPath) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary theCourse = new Summary(parts[0], parts[1], parts[2], parts[3], null);
    String course = courses.get(theCourse);
    Log.i("NetworkExample", "getDescription");
    if (course == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(course);
  }

  private final int five = 5;
  private final int four = 4;

  private MockResponse handleRating(@NonNull final RecordedRequest request) throws JsonProcessingException {
    String path = request.getPath();
    path = path.replaceFirst("/rating/", "");
    String[] parts = path.split("[/?]");
    if (parts.length != five) {
      System.out.println("line 86");
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String uuid = parts[four].replaceFirst("client=", "");
    Summary theCourse = new Summary(parts[0], parts[1], parts[2], parts[3], null);
    Map<String, Rating> idRating = courseRatings.get(theCourse);
    // get the rating from ratings list[summary;[id;rating]]
    System.out.println(theCourse.getNumber());

    //the course is not valid
    if (idRating == null) {
      System.out.println("course not exist");
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }

    //Get request
    if (request.getMethod().equalsIgnoreCase("GET")) {
      Log.i("NetworkExample", "getRating");
      System.out.println("this is the uuid " + uuid);
      if (idRating.containsKey(uuid)) {
        Rating aRating = idRating.get(uuid);
        String ratingString = mapper.writeValueAsString(aRating);
        System.out.println("return rating " + ratingString);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(ratingString);
        //return the json file of rating
      } else {
        Rating aRating = new Rating(uuid, Rating.NOT_RATED);
        courseRatings.get(theCourse).put(uuid, aRating);
        String ratingString = mapper.writeValueAsString(aRating);
        System.out.println("return no rating");
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(ratingString);
      }

      //Post Request
    } else if (request.getMethod().equalsIgnoreCase("POST")) {
      System.out.println("start post");
      String ratingJson = request.getBody().readUtf8();
      System.out.println(ratingJson);
      try {
        System.out.println("invalid?");
        Rating theRating = mapper.readValue(ratingJson, Rating.class);
        if (!theRating.getId().equals(uuid)) {
          System.out.println(uuid);
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
        }
        idRating.put(uuid, theRating);
        System.out.println(theRating.getRating());
        //redirect
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP).setHeader(
                "Location", path.replaceFirst("2021/spring/CS/", "") //this should be url
        );
      } catch (Exception e) {
        System.out.println(e);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
      }
      //return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(""); //return in body
    }
    System.out.println("last line");
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }


  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>(); // create a map of summary object(a course) -> json file

  private final Map<Summary, Map<String, Rating>> courseRatings = new HashMap<>();
  //a map of summary -> new rating object




  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {  //server receives the client request here
    try {
      String path = request.getPath(); //get the path /summary/year/semester
      Log.i("NetworkExample", "Request for" + path);
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("GET")) {
        return new MockResponse().setBody("CS125").setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", "")); //now get summary of year/semester
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/")) {
        System.out.println("/rating/");
        return handleRating(request);
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      e.printStackTrace();
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  /**
   * Start the server if has not already been started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!isRunning(false)) {
      new Thread(Server::new).start();
    }
    if (!isRunning(true)) {
      throw new IllegalStateException("Server should be running");
    }
  }

  /** Number of times to check the server before failing. */
  private static final int RETRY_COUNT = 8;

  /** Delay between retries. */
  private static final int RETRY_DELAY = 512;

  /**
   * Determine if the server is currently running.
   *
   * @param wait whether to wait or not
   * @return whether the server is running or not
   * @throws IllegalStateException if something else is running on our port
   */
  public static boolean isRunning(final boolean wait) {
    for (int i = 0; i < RETRY_COUNT; i++) {
      OkHttpClient client = new OkHttpClient();
      Request request = new Request.Builder().url(CourseableApplication.SERVER_URL).get().build();
      try {
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
          if (Objects.requireNonNull(response.body()).string().equals("CS125")) {
            return true;
          } else {
            throw new IllegalStateException(
                "Another server is running on port " + CourseableApplication.DEFAULT_SERVER_PORT);
          }
        }
      } catch (IOException ignored) {
        if (!wait) {
          break;
        }
        try {
          Thread.sleep(RETRY_DELAY);
        } catch (InterruptedException ignored1) {
        }
      }
    }
    return false;
  }


  private Server() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2021", "spring");
    loadCourses("2021", "spring");
    loadRatings("2021", "spring");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.DEFAULT_SERVER_PORT);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    // line 163 will create courses
    String filename = "/" + year + "_" + semester + ".json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString()); // a dictionary of summary object : coursejson format
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadRatings(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
            new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        Map<String, Rating> idRatings = new HashMap<>();
        courseRatings.put(course, idRatings); // a dictionary of summary object : coursejson format
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
