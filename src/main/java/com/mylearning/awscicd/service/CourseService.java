package com.mylearning.awscicd.service;

import com.mylearning.awscicd.dto.Course;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    // Think it as RDS DB for CRUD operations
    private final List<Course> courses = new ArrayList<>();

    // For initial/pre-exiting data
    public CourseService() {

        Course c1 = new Course();
        c1.setId(1);
        c1.setName("Java");
        c1.setPrice(4999);

        Course c2 = new Course();
        c2.setId(2);
        c2.setName("Spring Boot");
        c2.setPrice(7999);

        Course c3 = new Course();
        c3.setId(3);
        c3.setName("AWS");
        c3.setPrice(9999);

        courses.add(c1);
        courses.add(c2);
        courses.add(c3);
    }

    // Create a new course
    public void addCourse(Course course) {
        courses.add(course);
    }

    // Retrieve all courses
    public List<Course> getAllCourses() {
        return courses;
    }

    // Retrieve a course by id
    public Optional<Course> getCourseById(int id) {
        return courses.stream()
                .filter(course -> course.getId() == id)
                .findFirst();
    }

    // Update a course
    public boolean updateCourse(int id, Course newCourse) {
        return getCourseById(id)
                .map(existingCourse -> {
                    existingCourse.setName(newCourse.getName());
                    existingCourse.setPrice(newCourse.getPrice());
                    return true;
                })
                .orElse(false);
    }

    // Delete a course by id
    public boolean deleteCourse(int id) {
        return courses.removeIf(course -> course.getId() == id);
    }
}
