# Read Me
This project is an assignment to implement the following tasks:

* The mobile software testing team has 10 mobile phones that it needs to share for testing purposes.
  * Samsung Galaxy S9
  * 2 x Samsung Galaxy S8
  * Motorola Nexus 6
  * Oneplus 9
  * Apple iPhone 13
  * Apple iPhone 12
  * Apple iPhone 11
  * iPhone X
  * Nokia 3310
  

* Please create a service that allows a phone to be booked / returned. The following information should also be available for each phone
  * Availability (Yes / No)
  * When it was booked
  * Who booked the phone
    
 
* Use Fonoapi* to expose the following information for each phone:
  * Technology
  * 2g bands
  * 3g bands
  * 4g bands

*Note: Fonoapi is not working. Used GSMArena instead

# API Documentation

* GET http://localhost:8080/phone-booking
  * List all mobile phones (fields are id, model, isAvailable, lastBookedBy, lastBookedAt)
* GET http://localhost:8080/phone-booking?includeSpecs=true
  * List all mobile phones including fields technology, bands2g, bands3g and bands4g 
* GET http://localhost:8080/phone-booking/:id
  * Select a single mobile phone and all its booking history 
* POST http://localhost:8080/phone-booking
  * Book an available mobile phone
  * Example request: {"phoneId": 2, "bookedBy": "Lloyd", "bookedAt": "2023-05-28T13:56:00"}
  * Response will be 400 BadRequest if attempting to book a phone that is still booked
  * Response will be 404 NotFound if attempting to book a phone which does not exist
* POST http://localhost:8080/phone-booking/return
  * Return a mobile phone
  * Example request: {"phoneId": 2, "returnedAt": "2023-05-28T13:56:00"}
  * API is idempotent, will not care if current status of phone is already available
  * Response will be 404 NotFound if attempting to return a phone which does not exist

# How To Run
In your IDE, go to PhoneBookingApplication.java and execute it.