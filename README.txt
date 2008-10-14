About
=====
The Echo3 File Transfer Library is a collection of Echo3 components to facilitate uploading and downloading of files between the client and server. 

At the present time this library should be considered API-unstable, i.e., breaking changes may be made to subsequent versions of the API.

Building
========
To just build the JARs, you only need to have the Echo3 library JARs available, and the folder location set as environment variable
ECHO3_LIB_HOME.

In order to build the test web application, you must have the echo3 source code checked out and built, and you must set up the location
of that checkout as an environment variable named ECHO3_HOME.  You still need to specify the LIB variable above.  
Look at the included ant.properties to understand what you need to do.

To check out echo3, follow the instructions at http://echo.nextapp.com/site/echo3/download
