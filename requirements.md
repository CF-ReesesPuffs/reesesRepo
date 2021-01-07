# Team: Claudio, Meghan, Paul, Matthew

## Software Requirements

### Vision
Our vision for the app is to provide our users with a meaningful and fun way to bring their family and friends together and give gifts. We are doing this by creating a 
white elephant game!
This gives our users a covid-friendly way to still stay in touch and celebrate any occasion. 
This also offers us a way to celebrate with friends/family who are too far away.

### Scope IN
- This **IS** and android app, downloadable from google play store.
- This app is meant for a group of players.
- This is meant to be a game to exchange virtual gifts with others.
- The app organizes a party based on users invited

### Scope OUT
- This is NOT a website
- We do not facilitate/order the items for users

### MVP
- Users login/signup
- Users can invite other users to a party
- Users create items and see them in their itemBox
- Users all enter a party with an item from their list. (one to many relationship, the item's owner will change)
- Users are notified when it's their turn, they can take whichever item. 
- After one user goes, the next user is selected.
- Once everyone has a gift, the round ends and users see an overview of who got which gift!

### Stretch Goals
- Party's have a chat where all users can talk ( recyclerview? )
- Themes 
- Utilize an api to get real items for the users to add
- Set a timer for each user, if they don't pick its auto selected?
- Wish lists
- Party host has controls they can set for each party
- Everyone is alerted of their party order, and who's turn it is
- Users can create their wrapping paper for their gift!

### Functional Requirements
- A user can sign up/login
- A user can create items
- A user can host a party / join a party if invited
- A user selects an item to bring to the party
- A user MUST select an item while in a party
- The user receives an item after the party

### Data Flow
A user is initially taken to our home page.
When a user is logged out, they don't have access to much so we suggest logging in.
Next up a user will need an item, so navigate to create an item.
Once a user navigates and creates said item, they can double check their profile to see the item is there.
Now they should either host or join a party, don't forget your item!
Once a user is in a party, the host can start the party whenever they want. The user is NOT dedicated to the party page, they can continue to navigate around.
Once the party has concluded, the user is suggested to leave the page and go back to the home page.

### Non-Functional Requirements

Security: User information should be encrypted other than their usernme. We'll use a password encoder to ensure that private data is not visible to the public.

Usability: Users must sign up in and login in order to play a game. We will make some conditions for users to meet before they can play!






