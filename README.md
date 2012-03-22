# Vaadlets

Vaadlets is a template engine for vaadin. 

Imagine you can use a library like Facelets (Java Server Faces) in your Vaadin application. 

The declaration of a Vaadlets template is xsd based, so you have auto completion etc.

Please see the examples below.     

# Building from Source

## Prerequisites

* Java 7
* Maven 3
* Git
* Eclipse (m2eclipse, egit)
* A working internet connection

## Checking out the code

Clone the git repository using the URL on the Github home page:

    $ git clone git@github.com:ahoehma/vaadlets.git
    $ cd vaadlets

## Build on command line

    $ mvn install

## Editing the code in an IDE

I'm using eclipse with the power of m2eclipse (maven integration). Simply import the projects. That's it ;)   

# Trying out the demo

  $ cd vaadlets-demo
  $ mvn jetty:run
  open browser http://localhost:8080/vaadlets-demo/

# Using Vaadlets in your project

Simply add the following dependency to your Maven project:

    <dependency>
        <groupId>com.mymita</groupId>
        <artifactId>vaadlets-addon</artifactId>
        <version>0.0.1</version>
    </dependency>

# Release notes

## 0.1

* Initial release - in progress