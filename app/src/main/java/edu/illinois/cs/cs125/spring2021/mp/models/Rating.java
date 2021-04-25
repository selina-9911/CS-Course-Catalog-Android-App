package edu.illinois.cs.cs125.spring2021.mp.models;

public class Rating {
  public static final double NOT_RATED = -1.0;
  private String id;
  private double rating;

  public Rating(final String setID) {
    id = setID;
    rating = NOT_RATED;
  }
  public Rating(String setID, double setRating) {
    id = setID;
    rating = setRating;
  }

  //return UUID
  public String getId() {
    return id;
  }

  //return rating
  public double getRating() {
    if (rating == 0) {
      return NOT_RATED;
    }
    return rating;
  }
}
