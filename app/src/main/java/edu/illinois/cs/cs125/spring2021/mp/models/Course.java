package edu.illinois.cs.cs125.spring2021.mp.models;

/**
 * Model holding the course summary information shown in the course list and the description of the course.
 */
public class Course extends Summary {
  private String description;

  /** get the description of a particular course.
   * @return the description for this Course
   */
  public String getDescription() {
    return description;
  }

}
