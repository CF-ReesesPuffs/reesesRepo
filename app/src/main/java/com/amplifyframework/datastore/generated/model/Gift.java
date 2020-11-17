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

/** This is an auto generated class representing the Gift type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Gifts")
public final class Gift implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField TITLE = field("title");
  public static final QueryField USER = field("giftUserId");
  public static final QueryField PARTY = field("giftPartyId");
    public final @ModelField(targetType="ID", isRequired = true) String id;
    public final @ModelField(targetType="String", isRequired = true) String title;
    public final @ModelField(targetType="User") @BelongsTo(targetName = "giftUserId", type = User.class) User user;
    public final @ModelField(targetType="Party") @BelongsTo(targetName = "giftPartyId", type = Party.class) Party party;
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public User getUser() {
      return user;
  }
  
  public Party getParty() {
      return party;
  }
  
  private Gift(String id, String title, User user, Party party) {
    this.id = id;
    this.title = title;
    this.user = user;
    this.party = party;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Gift gift = (Gift) obj;
      return ObjectsCompat.equals(getId(), gift.getId()) &&
              ObjectsCompat.equals(getTitle(), gift.getTitle()) &&
              ObjectsCompat.equals(getUser(), gift.getUser()) &&
              ObjectsCompat.equals(getParty(), gift.getParty());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getUser())
      .append(getParty())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Gift {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("user=" + String.valueOf(getUser()) + ", ")
      .append("party=" + String.valueOf(getParty()))
      .append("}")
      .toString();
  }
  
  public static TitleStep builder() {
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
  public static Gift justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Gift(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      user,
      party);
  }
  public interface TitleStep {
    BuildStep title(String title);
  }
  

  public interface BuildStep {
    Gift build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep user(User user);
    BuildStep party(Party party);
  }
  

  public static class Builder implements TitleStep, BuildStep {
    private String id;
    private String title;
    private User user;
    private Party party;
    @Override
     public Gift build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Gift(
          id,
          title,
          user,
          party);
    }
    
    @Override
     public BuildStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep user(User user) {
        this.user = user;
        return this;
    }
    
    @Override
     public BuildStep party(Party party) {
        this.party = party;
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
    private CopyOfBuilder(String id, String title, User user, Party party) {
      super.id(id);
      super.title(title)
        .user(user)
        .party(party);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder user(User user) {
      return (CopyOfBuilder) super.user(user);
    }
    
    @Override
     public CopyOfBuilder party(Party party) {
      return (CopyOfBuilder) super.party(party);
    }
  }
  
}
