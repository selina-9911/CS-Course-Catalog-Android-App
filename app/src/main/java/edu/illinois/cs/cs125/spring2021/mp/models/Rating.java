package edu.illinois.cs.cs125.spring2021.mp.models;

/**
 * Rating for each course Rating.
 */
public class Rating {
  /**
   * ...
   */
  public static final double NOT_RATED = -1.0;
  /**
   * ...
   */
  private String id;
  /**
   * ...
   */
  private double rating;
  /**
   * ...
   */
  public Rating() {};
   /**
   *  ...
   *  @param setID id
   * @param setRating rating
   *
   */
  public Rating(final String setID, final double setRating) {
    id = setID;
    rating = setRating;
  }
  /**
   * ...
   * @return returnid
   */
  //return UUID
  public String getId() {
    return id;
  }
  /**
   * ...
   * @return rating
   */
  //return rating
  public double getRating() {
    return rating;
  }
}
