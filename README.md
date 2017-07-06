Dead Code Detector (for Java applications)
==========================================

If you are a developer or a tech lead, Dead Code Detector (DCD) simply finds never used code in your Java/JEE applications.
Deleting dead code helps to reduce cost of maintenance and to improve quality of code (http://en.wikipedia.org/wiki/Unreachable_code),
and as a side effect it improves code coverage metrics of tests by deleting code that can't be tested.

Provide a directory of compiled classes, a jar or a war file in the UI and DCD lists suspects of dead code. Dead code found can be private, package-private and even protected or public.
Unread local variables, self assignments, toString on String and useless initializations are also detected.
Please remember that dead code found by DCD is just suspects.
DCD can not detect that reflection or other is used: ask to people who know your application.
You can read the usage manual (TODO) to know how to launch and use DCD. 


The search is very fast (2000 classes/s on warm files) and made by static analysis of compiled classes without running the application or loading classes.
DCD uses the library ASM (licence open source, BSD) to do the job.

DCD is pronounced "décédé" in French which means "deceased" in English. 

Author: Emeric Vernat

License: [ASL](http://www.apache.org/licenses/LICENSE-2.0)

[![Build Status](https://javamelody.ci.cloudbees.com/buildStatus/icon?job=DeadCodeDetector)](https://javamelody.ci.cloudbees.com/job/DeadCodeDetector/) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.bull.javamelody/dead-code-detector/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.bull.javamelody/dead-code-detector)
