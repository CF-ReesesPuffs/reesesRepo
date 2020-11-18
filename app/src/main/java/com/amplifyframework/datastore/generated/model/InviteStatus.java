package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the InviteStatus type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "InviteStatuses")
public final class InviteStatus implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField STATUS = field("status");
  public static final QueryField NAME = field("inviteStatusNameId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String") String status;
  private final @ModelField(targetType="User") @BelongsTo(targetName = "inviteStatusNameId", type = User.class) User name;
  public String getId() {
      return id;
  }
  
  public String getStatus() {
      return status;
  }
  
  public User getName() {
      return name;
  }
  
  private InviteStatus(String id, String status, User name) {
    this.id = id;
    this.status = status;
    this.name = name;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      InviteStatus inviteStatus = (InviteStatus) obj;
      return ObjectsCompat.equals(getId(), inviteStatus.getId()) &&
              ObjectsCompat.equals(getStatus(), inviteStatus.getStatus()) &&
              ObjectsCompat.equals(getName(), inviteStatus.getName());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getStatus())
      .append(getName())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("InviteStatus {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("status=" + String.valueOf(getStatus()) + ", ")
      .append("name=" + String.valueOf(getName()))
      .append("}")
      .toString();
  }
  
  public static BuildStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static InviteStatus justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new InviteStatus(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      status,
      name);
  }
  public interface BuildStep {
    InviteStatus build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep status(String status);
    BuildStep name(User name);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String status;
    private User name;
    @Override
     public InviteStatus build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new InviteStatus(
          id,
          status,
          name);
    }
    
    @Override
     public BuildStep status(String status) {
        this.status = status;
        return this;
    }
    
    @Override
     public BuildStep name(User name) {
        this.name = name;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String status, User name) {
      super.id(id);
      super.status(status)
        .name(name);
    }
    
    @Override
     public CopyOfBuilder status(String status) {
      return (CopyOfBuilder) super.status(status);
    }
    
    @Override
     public CopyOfBuilder name(User name) {
      return (CopyOfBuilder) super.name(name);
    }
  }
  
}