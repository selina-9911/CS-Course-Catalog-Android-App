package edu.illinois.cs.cs125.spring2021.mp.models;

public class Rating {
  public static final double NOT_RATED = -1.0;
  private String id;
  private double rating;

  public Rating() {};

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
    return rating;
  }
}
