package com.demo.devstandards.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Domain model representing a user in the WebUser collection.
 * Maps to the "WebUser" collection in DocumentDB.
 *
 * INDEX DOCUMENTATION:
 * - _id        : Default primary index (MongoDB auto-created)
 * - email      : Indexed for fast lookup by email address
 * - last_name  : Indexed for fast lookup/search by last name
 *
 * When adding new fields, ensure:
 * 1. Necessary indexes are added using @Indexed annotation
 * 2. Index documentation above is updated
 * 3. Index creation is included in the deployment pipeline (see docs/INDEX_DOCUMENTATION.md)
 */
@Document(collection = "WebUser")
public class WebUser {

    @Id
    private String id;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    @Indexed
    private String lastName;

    @Field("email")
    @Indexed(unique = true)
    private String email;

    // Default constructor required by Spring Data
    public WebUser() {
    }

    public WebUser(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "WebUser{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
