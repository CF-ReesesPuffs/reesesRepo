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

/** This is an auto generated class representing the GuestList type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "GuestLists")
public final class GuestList implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField INVITE_STATUS = field("inviteStatus");
  public static final QueryField INVITEE = field("invitee");
  public static final QueryField INVITED_USER = field("invitedUser");
  public static final QueryField TAKEN_TURN = field("takenTurn");
  public static final QueryField TURN_ORDER = field("turnOrder");
  public static final QueryField USER = field("guestListUserId");
  public static final QueryField PARTY = field("guestListPartyId");
    private final @ModelField(targetType="ID", isRequired = true) String id;
    public @ModelField(targetType="String") String inviteStatus;
    private final @ModelField(targetType="String") String invitee;
    private final @ModelField(targetType="String") String invitedUser;
    public @ModelField(targetType="Boolean") Boolean takenTurn;
    public @ModelField(targetType="Int") Integer turnOrder;
    private final @ModelField(targetType="User") @BelongsTo(targetName = "guestListUserId", type = User.class) User user;
    private final @ModelField(targetType="Party") @BelongsTo(targetName = "guestListPartyId", type = Party.class) Party party;
    public String getId() {
        return id;
    }
  
  public String getInviteStatus() {
      return inviteStatus;
  }
  
  public String getInvitee() {
      return invitee;
  }
  
  public String getInvitedUser() {
      return invitedUser;
  }
  
  public Boolean getTakenTurn() {
      return takenTurn;
  }
  
  public Integer getTurnOrder() {
      return turnOrder;
  }
  
  public User getUser() {
      return user;
  }
  
  public Party getParty() {
      return party;
  }
  
  private GuestList(String id, String inviteStatus, String invitee, String invitedUser, Boolean takenTurn, Integer turnOrder, User user, Party party) {
    this.id = id;
    this.inviteStatus = inviteStatus;
    this.invitee = invitee;
    this.invitedUser = invitedUser;
    this.takenTurn = takenTurn;
    this.turnOrder = turnOrder;
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
      GuestList guestList = (GuestList) obj;
      return ObjectsCompat.equals(getId(), guestList.getId()) &&
              ObjectsCompat.equals(getInviteStatus(), guestList.getInviteStatus()) &&
              ObjectsCompat.equals(getInvitee(), guestList.getInvitee()) &&
              ObjectsCompat.equals(getInvitedUser(), guestList.getInvitedUser()) &&
              ObjectsCompat.equals(getTakenTurn(), guestList.getTakenTurn()) &&
              ObjectsCompat.equals(getTurnOrder(), guestList.getTurnOrder()) &&
              ObjectsCompat.equals(getUser(), guestList.getUser()) &&
              ObjectsCompat.equals(getParty(), guestList.getParty());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getInviteStatus())
      .append(getInvitee())
      .append(getInvitedUser())
      .append(getTakenTurn())
      .append(getTurnOrder())
      .append(getUser())
      .append(getParty())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("GuestList {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("inviteStatus=" + String.valueOf(getInviteStatus()) + ", ")
      .append("invitee=" + String.valueOf(getInvitee()) + ", ")
      .append("invitedUser=" + String.valueOf(getInvitedUser()) + ", ")
      .append("takenTurn=" + String.valueOf(getTakenTurn()) + ", ")
      .append("turnOrder=" + String.valueOf(getTurnOrder()) + ", ")
      .append("user=" + String.valueOf(getUser()) + ", ")
      .append("party=" + String.valueOf(getParty()))
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
  public static GuestList justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new GuestList(
      id,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      inviteStatus,
      invitee,
      invitedUser,
      takenTurn,
      turnOrder,
      user,
      party);
  }
  public interface BuildStep {
    GuestList build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep inviteStatus(String inviteStatus);
    BuildStep invitee(String invitee);
    BuildStep invitedUser(String invitedUser);
    BuildStep takenTurn(Boolean takenTurn);
    BuildStep turnOrder(Integer turnOrder);
    BuildStep user(User user);
    BuildStep party(Party party);
  }
  

  public static class Builder implements BuildStep {
    private String id;
    private String inviteStatus;
    private String invitee;
    private String invitedUser;
    private Boolean takenTurn;
    private Integer turnOrder;
    private User user;
    private Party party;
    @Override
     public GuestList build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new GuestList(
          id,
          inviteStatus,
          invitee,
          invitedUser,
          takenTurn,
          turnOrder,
          user,
          party);
    }
    
    @Override
     public BuildStep inviteStatus(String inviteStatus) {
        this.inviteStatus = inviteStatus;
        return this;
    }
    
    @Override
     public BuildStep invitee(String invitee) {
        this.invitee = invitee;
        return this;
    }
    
    @Override
     public BuildStep invitedUser(String invitedUser) {
        this.invitedUser = invitedUser;
        return this;
    }
    
    @Override
     public BuildStep takenTurn(Boolean takenTurn) {
        this.takenTurn = takenTurn;
        return this;
    }
    
    @Override
     public BuildStep turnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
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
    private CopyOfBuilder(String id, String inviteStatus, String invitee, String invitedUser, Boolean takenTurn, Integer turnOrder, User user, Party party) {
      super.id(id);
      super.inviteStatus(inviteStatus)
        .invitee(invitee)
        .invitedUser(invitedUser)
        .takenTurn(takenTurn)
        .turnOrder(turnOrder)
        .user(user)
        .party(party);
    }
    
    @Override
     public CopyOfBuilder inviteStatus(String inviteStatus) {
      return (CopyOfBuilder) super.inviteStatus(inviteStatus);
    }
    
    @Override
     public CopyOfBuilder invitee(String invitee) {
      return (CopyOfBuilder) super.invitee(invitee);
    }
    
    @Override
     public CopyOfBuilder invitedUser(String invitedUser) {
      return (CopyOfBuilder) super.invitedUser(invitedUser);
    }
    
    @Override
     public CopyOfBuilder takenTurn(Boolean takenTurn) {
      return (CopyOfBuilder) super.takenTurn(takenTurn);
    }
    
    @Override
     public CopyOfBuilder turnOrder(Integer turnOrder) {
      return (CopyOfBuilder) super.turnOrder(turnOrder);
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
