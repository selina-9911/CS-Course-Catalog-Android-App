package edu.illinois.cs.cs125.spring2021.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



import edu.illinois.cs.cs125.spring2021.mp.R;
import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.spring2021.mp.models.Course;
import edu.illinois.cs.cs125.spring2021.mp.models.Rating;
import edu.illinois.cs.cs125.spring2021.mp.models.Summary;
import edu.illinois.cs.cs125.spring2021.mp.network.Client;

/**
 * ...
 */

public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = CourseActivity.class.getSimpleName();
  // Binding to the layout in activity_main.xml
  private ActivityCourseBinding binding;
  private Summary summary;

  /**
   * ...
   */
  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    // Bind to the layout in activity_course.xml
    binding = DataBindingUtil.setContentView(this, R.layout.activity_course);

    super.onCreate(savedInstanceState);
    RatingBar rb = (RatingBar) findViewById(R.id.rating);

    Log.i(TAG, "course activity launched");
    Intent intent = getIntent();
    String course = intent.getStringExtra("COURSE");
    Log.i(TAG, course);
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      summary = objectMapper.readValue(course, Summary.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    CourseableApplication application = (CourseableApplication) getApplication(); //create instance
    application.getCourseClient().getCourse(summary, this);
    application.getCourseClient().getRating(summary, application.getClientID(), this);
    rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
      @Override
      public void onRatingChanged(final RatingBar ratingBar, final float rating, final boolean fromUser) {
        Log.i("rating", "click rating");
        Rating newRating = new Rating(application.getClientID(), rating);
        try {
          application.getCourseClient().postRating(summary, newRating, CourseActivity.this);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }
      }
    });
  }
  /**
   * ...
   */
  @Override
  public void courseResponse(final Summary theSummary, final Course course) {
    binding.title.setText(course.getTitle());
    binding.description.setText(course.getDescription());
  }
  /**
   * ...
   */
  @Override
  public void yourRating(
          final Summary theSummary, final Rating rating) { //summary is the course model
    Log.i("NetworkExample", "CourseActivity YourRating");
    System.out.println("yourrating");
    binding.rating.setRating((float) rating.getRating());

  }


}

