Usergrid Platform Stack for Mobile & Rich Client Apps

For more info:

Documentation:  http://usergrid.github.com/docs

Homepage:       http://www.usergrid.com

Blog:           http://blog.usergrid.com

Google Groups:  http://groups.google.com/group/usergrid-user
                http://groups.google.com/group/usergrid-dev

Twitter:        http://twitter.com/#!/usergrid


Getting Started

Note: The easiest way to run Usergrid is to download the double-clickable jar
at:

https://usergrid.s3.amazonaws.com/usergrid-launcher-0.0.1-SNAPSHOT.jar

This will let you try out the system without building or installing it.

Requirements

JDK 1.6 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
Maven (http://maven.apache.org/)

Building

Download and install Maven using the instructions on the Apache Maven website,
then, from the command line, go to the usergrid directory and type the
following:

mvn install

This will cause Maven to download and install all the necessary dependencies.
This also runs a small set of tests after compilation, however these tests do
start up an instance of Cassandra. To run the build without all the tests
being fired, use the following:

mvn install -DskipTests=true

Running

Usergrid-core contains the persistence layer and shared utilities for powering
the Usergrid service. The services layer is contained in usergrid-services and
exposes a higher-level API that's used by the usergrid-rest web services tier.

You can run Usergrid as either a webapp in tomcat, by deploying the ROOT.war
file generated in the usergrid/rest project, or from the command-line from the
jar in the usergrid/standalone project, or as a double-clickable app with the
usergrid/launcher project.

If you don't want to do a full build, you can download a pre-built version
of the launcher app from:

https://usergrid.s3.amazonaws.com/usergrid-launcher-0.0.1-SNAPSHOT.jar

Licenses

All source code files have copyright headers indicating their license under
either the GPL, AGPL, or LGPL.

All files under the core and config subdirectories are part of the "Usergrid
Core". These files are intended to be licensed under the GPL and should have
such indicated in their copyright headers.

All files under the other sub-directories are part of the "Usergrid Stack".
These files are intended to be licensed under the Affero GPL and should have
such indicated in their copyright headers.
