Use Case:

Our custom-build server logs different events to a file named logfile.txt. Every event has 2 entries in the file  - one entry when the event was started and another when the event was finished. The entries in the file  have no specific order (a finish event could occur before a start event for a given id) 

Every line in the file is a JSON object containing the following event data: 
∙ id - the unique event identifier 
∙ state - whether the event was started or finished (can have values "STARTED" or "FINISHED"
∙ timestamp - the timestamp of the event in milliseconds 

Application Server logs also have the following additional attributes: 
∙ type - type of log 
∙ host - hostname

The program should: 
∙ Take the path to logfile.txt as an input argument 
∙ Parse the contents of logfile.txt 
∙ Flag any long events that take longer than 4ms 
∙ Write the found event details to file-based HSQLDB (http://hsqldb.org/) in the working folder 
∙ The application should create a new table if necessary and store the following values: 
	∙ Event id 
	∙ Event duration 
	∙ Type and Host if applicable 
	∙ Alert (true if the event took longer than 4ms, otherwise false) 
	
===================================================

Prerequisites -
Application needs gradle to be installed on the system before running the code (preferably the latest version)

===================================================

Java package structure -
com.loghandler.apps - Contains the application launch Main method
com.loghandler.utils - Contains the ulility classes
com.loghandler.model - contains the POJOs and the DB entities
com.loghandler.component - has the classes with actual business logic
com.loghandler.dao - has database access connectivity sniplet

====================================================

Assumptions -
Business:
1) If for any of the event ids - events are received in subsequent runs, the first one would be retained.
2) For events where either starting or finishing time is not logged, no DB entry has to be made.
3) For subsequent runs, no explicit table truncation has been done, it is expected for the program to have fresh ids for all runs done within one session.
Technical:
4) For simplicity, code is developed on java, and no frameworks are used.
5) DB files/tables would be ok if created at root folder
6) The input file shall not end with "/" and shall contain only the folder path, and not the file name (example: C:\Users\Palak)
	
