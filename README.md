# Getting Started

### Description
This is command-line client written in java to make payments through a simulated mobile-money payment service.


### How to Run
This can be run in any IDE by running the PaymentClientApplication.java class available in the path src/main/java/com/segovia/interview/paymentclient/PaymentClientApplication.java
On running the class file the console will prompt for csv Input with the message "Please Enter the Csv". This can be entered in the console in the following format


    ID,Recipient,Amount,Currency
    aaaaaaaa,254999999999,10,KES
    bbbbbbbb,12125551000,50,USD
    xyz12345,12125551005,150.35,USD
    29387431,12125550000,37,NGN


Press enter twice after enteringing your CSV. The system will then process your input and output your result csv in the console in some time
The output will be in the following format

    ID,Server-generated ID,Status,Fee,Details
    aaaaaaaa,XXXXXXXXXX,Succeeded,0.1,Succeeded with fee of 0.10
    bbbbbbbb,,Unknown,,Server was unable to process the request
    29387431,,Failed,,Recipient phone number invalid
    xyz12345,YYYYYYYYYY,Succeeded,5.43,Succeeded with fee of 5.43


### How does the Application work
Once the CSV is entered by the client, the application makes the REST call to the Server to submit the payment request.
It keeps track of all the ConversationIDs returned by the client.

The application will print the CSV only after it confirms the status of all requests submitted.

If callbacks is enabled (parmeter is callbackEnabled), the application waits for 5 minutes to see if callbacks are received.
If all callbacks are not received by this time, the app starts polling the Transaction API in the interval of 1 minute 
to check the status of Pending Transactions.

In case the callbacks are disabled from the beginning, it will keep polling the Transaction API until the response for are received.

All the timeout parameters and toggles are defined in application.yml so they can be changed in the Configuration

### Design Decisions
- I have chosen java and Springboot to complete this assignment
- Why I chose Springboot and structured my code this way?
Spring Boot provides a convention-over-configuration approach, which reduces the amount of boilerplate code required 
  and simplifies the application setup. It offers a set of default configurations and sensible defaults, allowing you to
  quickly get started with building applications. It follows "opinionated" approach, providing predefined conventions and best practices.
I have chosen Mockito to mock some of the behaviours while doing Unit Testing

- How did you prioritize parts of the problem?
I started off by 1st reading and writing the Csv files
Wrote methods to do the REST call for Session-token, payment, Check Status
Logic to check token expiry and re generate ot return same token
Wrote the polling logic to check the status if callback disabled
Logic to handle the Callbacks
Ensured the edge cases for various recipient numbers were covered 

### TODO Work
This is a Work In Progress. If I had more time to spend on this I would want to implement the following features
 1. Implement Code to generate callback url for Linux and other OS
 2. Handle malformed CSV more gracefully and give user the chance to enter again
 3. Add more logging
 4. Handle the scenario of HTTPS calls.- Currently the assumption is the calls are http only
