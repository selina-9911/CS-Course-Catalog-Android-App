package edu.illinois.cs.cs125.spring2021.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import edu.illinois.cs.cs125.spring2021.mp.R;
import edu.illinois.cs.cs125.spring2021.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.spring2021.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.spring2021.mp.models.Course;
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
    private String coursetitle;
    private String coursedescription;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        // Bind to the layout in activity_course.xml
        binding = DataBindingUtil.setContentView(this, R.layout.activity_course);

        super.onCreate(savedInstanceState);
        Log.i(TAG, "course activity launched");
        Intent intent = getIntent();
        String course = intent.getStringExtra("COURSE");
        Log.i(TAG, course);
        ObjectMapper objectMapper = new ObjectMapper();
        Summary summary = null;
        try {
            summary = objectMapper.readValue(course, Summary.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CourseableApplication application = (CourseableApplication) getApplication(); //create instance
        application.getCourseClient().getCourse(summary,this);


    }
    @Override
    public void courseResponse(final Summary summary, final Course course) {
        binding.title.setText(course.getTitle());
        binding.description.setText(course.getDescription());
    }
}
