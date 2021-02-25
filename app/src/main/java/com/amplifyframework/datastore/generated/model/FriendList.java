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

/** This is an auto generated class representing the FriendList type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "FriendLists")
public final class FriendList implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField USER_NAME = field("userName");
  public static final QueryField ACCEPTED = field("accepted");
  public static final QueryField DECLINED = field("declined");
  public static final QueryField USER = field("friendListUserId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String userName;
  private final @ModelField(targetType="Boolean") Boolean accepted;
  private final @ModelField(targetType="Boolean") Boolean declined;
  private final @ModelField(targetType="User") @BelongsTo(targetName = "friendListUserId", type = User.class) User user;
  public String getId() {
      return id;
  }
  
  public String getUserName() {
      return userName;
  }
  
  public Boolean getAccepted() {
      return accepted;
  }
  
  public Boolean getDeclined() {
      return declined;
  }
  
  public User getUser() {
      return user;
  }
  
  private FriendList(String id, String userName, Boolean accepted, Boolean declined, User user) {
    this.id = id;
    this.userName = userName;
    this.accepted = accepted;
    this.declined = declined;
    this.user = user;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      FriendList friendList = (FriendList) obj;
      return ObjectsCompat.equals(getId(), friendList.getId()) &&
              ObjectsCompat.equals(getUserName(), friendList.getUserName()) &&
              ObjectsCompat.equals(getAccepted(), friendList.getAccepted()) &&
              ObjectsCompat.equals(getDeclined(), friendList.getDeclined()) &&
              ObjectsCompat.equals(getUser(), friendList.getUser());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserName())
      .append(getAccepted())
      .append(getDeclined())
      .append(getUser())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("FriendList {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userName=" + String.valueOf(getUserName()) + ", ")
      .append("accepted=" + String.valueOf(getAccepted()) + ", ")
      .append("declined=" + String.valueOf(getDeclined()) + ", ")
      .append("user=" + String.valueOf(getUser()))
      .append("}")
      .toString();
  }
  
  public static UserNameStep builder() {
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
  public static FriendList justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new FriendList(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userName,
      accepted,
      declined,
      user);
  }
  public interface UserNameStep {
    BuildStep userName(String userName);
  }
  

  public interface BuildStep {
    FriendList build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep accepted(Boolean accepted);
    BuildStep declined(Boolean declined);
    BuildStep user(User user);
  }
  

  public static class Builder implements UserNameStep, BuildStep {
    private String id;
    private String userName;
    private Boolean accepted;
    private Boolean declined;
    private User user;
    @Override
     public FriendList build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new FriendList(
          id,
          userName,
          accepted,
          declined,
          user);
    }
    
    @Override
     public BuildStep userName(String userName) {
        Objects.requireNonNull(userName);
        this.userName = userName;
        return this;
    }
    
    @Override
     public BuildStep accepted(Boolean accepted) {
        this.accepted = accepted;
        return this;
    }
    
    @Override
     public BuildStep declined(Boolean declined) {
        this.declined = declined;
        return this;
    }
    
    @Override
     public BuildStep user(User user) {
        this.user = user;
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
    private CopyOfBuilder(String id, String userName, Boolean accepted, Boolean declined, User user) {
      super.id(id);
      super.userName(userName)
        .accepted(accepted)
        .declined(declined)
        .user(user);
    }
    
    @Override
     public CopyOfBuilder userName(String userName) {
      return (CopyOfBuilder) super.userName(userName);
    }
    
    @Override
     public CopyOfBuilder accepted(Boolean accepted) {
      return (CopyOfBuilder) super.accepted(accepted);
    }
    
    @Override
     public CopyOfBuilder declined(Boolean declined) {
      return (CopyOfBuilder) super.declined(declined);
    }
    
    @Override
     public CopyOfBuilder user(User user) {
      return (CopyOfBuilder) super.user(user);
    }
  }
  
}
