/*
 * Copyright (c) 2012, 2013 All Rights Reserved, www.tiq-solutions.com
 * 
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
 * EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * This code is product of:
 *
 * TIQ Solutions GmbH 
 * Weißenfelser Str. 84
 * 04229 Leipzig, Germany
 *
 * info@tiq-solutions.com
 *
 */
package de.tiq.jdbc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation, that will provide the annotated class in the connection element. </br>
 * You are able to grab an instance of the annotated class for process the query. </br> 
 * It should be used for global resources, which are heavy to build or require a remote connection. </br></br>
 * The annotated class can be instanced by using a parameterized constructor or the default constructor. </br>
 * The parameterized is the default. <b>You can change this behavior by adjusting the value of the Annotation --> @Connection(false)</b></br>
 * The parameters are always the given ones to the Connection class, <b>so your class need to adapt a constructor with the arguments:<br/> 
 * (String jdbcUrl2, Properties driverProperties)</b> 
 * 
 * 
 * @author D. Häberlein
 * @since 1.6
 *
 */
@Documented
@Retention(RetentionPolicy.SOURCE) 
@Target(ElementType.TYPE)
public @interface Connection {
	boolean value() default true;
}
