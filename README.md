# JDBC Annotations

## Overview

This project provides utilities to create a jdbc driver.
Use this project, if you search for an easy way to create a jdbc driver. You do not need to write and implement all methods of the jdbc interface on your own.	
In fact, you simply mark some of your classes with a set of java annotations. 
The delivered annotation processor will create the structure of a jdbc driver for you.  

## Getting Started

All provided annotations are developed for class scope. 
Your annotated java-classes serve as a controller heading all functions of the jdbc driver. 
To ensure the presence of mandatory methods, your classes must extend a couple of predefinded types.
These types are included in a extern project, because they are needed at runtime. 
 
### Prerequisites

#### Build the project
This project is built with maven. You should install maven on your operating system to be able to build the project. 
Use 'mvn -install' in the root directory of the project (the project's POM file is located there). This will install a jar of this project in your local maven repository.
After a successful building process it will generate a jar file in the "target" folder, containing the compiled classes.

### Usage 

Include the Jdbc_Annotations.jar in your classpath. 
If you create a maven project, it is possible to include the installed annotation processor in the dependency section:

		 <dependency>
			<groupId>de.tiq</groupId>
			<artifactId>Jdbc_Annotations</artifactId>
			<version>0.0.4</version>
		</dependency>

These projects of TIQ Solutions are following the single responsibility principle. That means this project only defines a set of annotations and how they are processed. 
The predefined types, which your classes need, are part of a runtime library. So you don't have to include the annotation processor to your runtime 
dependencies. 

In order to obtain the JdbcUtils with this functionality, you can retrieve the source code from GitHub.   

Currently, you can use the following annotations:

#### JdbcDriver
```java
@JdbcDriver(name = "DriverClassName", packageDefinition = "org.example.pge", prefix = "jdbc", scheme = "file")
Class ArbitaryClasss extends QueryExecutor
{
	//... class body	
}
```
Note: The QueryExecutor superclass is a part of the JdbcUtils project also published on GitHub.

The JdbcDriver annotation provides meta information for your generated driver. Also, it marks the class which will execute SQL Queries.
Therefore, the annotated class should extend the QueryExecutor abstract class. 
You only need to process the incoming (most likely SQL) string and build a result set. 
If the annotation is not used, the annotation processor will not create a class implementing the jdbc-driver interface.

You can customize your driver with the following annotation arguments: 

name : The class name of your driver class

packageDefinition : The package of your driver class. Default value is the default package (no package name).

prefix : The Prefix of the jdbc URL, which you use to connect to a jdbc driver. The default is set to "jdbc".

scheme: This attribute determines the file scheme of your jdbc url. If a given connection url does not use this file scheme, 
		the driver class will raise an sql exception.
		
For example with the given parameters of the class above, you would get a driver class like:
```java
package org.example.page;

class DriverClassName implements Driver{
	//... class body 
}
```
You could connect to the driver with a jdbc url matching the pattern "jdbc:file///...filepath".

#### Connection

A connection annotation marks a connection handler class. You highlight the class which will be responsible for creating a connection and closing it:
```java
@Connection
Class YourConnectionHandlerClass extends ConnectionHandler
{
	//... class body	
}
```
Note: The ConnectionHandler superclass is a part of the JdbcUtils project also published on GitHub. 

The annotation has no attributes.  
Your class serves as a proxy class. All jdbc connection calls will be delegated to your class. 

#### ConnectionMetaData

This annotation enables you to specify connection meta data information: 
```java
@ConnectionMetaData
Class YourConnectionHandlerClass implements ConnectionMetaDataProvider
{
	//... class body	
}
```
Note: The ConnectionMetaDataProvider interface is a part of the JdbcUtils project also published on GitHub.
	
	
### Build your project

Check out the right way to process annotations with an annotation processor in your build environment. 
For maven, this project is tested with the (org.bsc.maven) maven processor plugin.  
		
## Developer info

Not all jdbc function are currently implemented. Checkout the source code for detailed information!

## Colophon		

### Company

This project is developed by TIQ Solutions GmbH, a german enterprise for data quality management.
You can contact us: info@tiq-solutions.de 

### License 

The project is licensed under terms of a dual treatment. For non-commercial projects, the source code is provided under terms of the GPL (http://www.gnu.org/licenses/gpl-2.0.html).
If you wish to include this project in a proprietary context, you must be granted a special vendor license.   
