package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.HasMany;

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

/** This is an auto generated class representing the Party type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Parties")
public final class Party implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField TITLE = field("title");
  public static final QueryField HOSTED_ON = field("hostedOn");
  public static final QueryField HOSTED_AT = field("hostedAt");
  public static final QueryField PRICE = field("price");

  public final @ModelField(targetType="ID", isRequired = true) String id;
  public final @ModelField(targetType="String", isRequired = true) String title;
  public final @ModelField(targetType="String") String hostedOn;
  public final @ModelField(targetType="String") String hostedAt;
  public final @ModelField(targetType="String") String price;
  public final @ModelField(targetType="InviteStatus") @HasMany(associatedWith = "name", type = InviteStatus.class) List<InviteStatus> status = null;
  public final @ModelField(targetType="GuestList") @HasMany(associatedWith = "party", type = GuestList.class) List<GuestList> users = null;
  public final @ModelField(targetType="Gift") @HasMany(associatedWith = "party", type = Gift.class) List<Gift> gifts = null;
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public String getHostedOn() {
      return hostedOn;
  }
  
  public String getHostedAt() {
      return hostedAt;
  }
  
  public String getPrice() {
      return price;
  }
  
  public List<InviteStatus> getStatus() {
      return status;
  }
  
  public List<GuestList> getUsers() {
      return users;
  }
  
  public List<Gift> getGifts() {
      return gifts;
  }
  
  public Party(String id, String title, String hostedOn, String hostedAt, String price) {
    this.id = id;
    this.title = title;
    this.hostedOn = hostedOn;
    this.hostedAt = hostedAt;
    this.price = price;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Party party = (Party) obj;
      return ObjectsCompat.equals(getId(), party.getId()) &&
              ObjectsCompat.equals(getTitle(), party.getTitle()) &&
              ObjectsCompat.equals(getHostedOn(), party.getHostedOn()) &&
              ObjectsCompat.equals(getHostedAt(), party.getHostedAt()) &&
              ObjectsCompat.equals(getPrice(), party.getPrice());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getHostedOn())
      .append(getHostedAt())
      .append(getPrice())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Party {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("hostedOn=" + String.valueOf(getHostedOn()) + ", ")
      .append("hostedAt=" + String.valueOf(getHostedAt()) + ", ")
      .append("price=" + String.valueOf(getPrice()))
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
  public static Party justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Party(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      hostedOn,
      hostedAt,
      price);
  }
  public interface TitleStep {
    BuildStep title(String title);
  }
  

  public interface BuildStep {
    Party build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep hostedOn(String hostedOn);
    BuildStep hostedAt(String hostedAt);
    BuildStep price(String price);
  }
  

  public static class Builder implements TitleStep, BuildStep {
    private String id;
    private String title;
    private String hostedOn;
    private String hostedAt;
    private String price;
    @Override
     public Party build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Party(
          id,
          title,
          hostedOn,
          hostedAt,
          price);
    }
    
    @Override
     public BuildStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep hostedOn(String hostedOn) {
        this.hostedOn = hostedOn;
        return this;
    }
    
    @Override
     public BuildStep hostedAt(String hostedAt) {
        this.hostedAt = hostedAt;
        return this;
    }
    
    @Override
     public BuildStep price(String price) {
        this.price = price;
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
    private CopyOfBuilder(String id, String title, String hostedOn, String hostedAt, String price) {
      super.id(id);
      super.title(title)
        .hostedOn(hostedOn)
        .hostedAt(hostedAt)
        .price(price);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder hostedOn(String hostedOn) {
      return (CopyOfBuilder) super.hostedOn(hostedOn);
    }
    
    @Override
     public CopyOfBuilder hostedAt(String hostedAt) {
      return (CopyOfBuilder) super.hostedAt(hostedAt);
    }
    
    @Override
     public CopyOfBuilder price(String price) {
      return (CopyOfBuilder) super.price(price);
    }
  }
  
}