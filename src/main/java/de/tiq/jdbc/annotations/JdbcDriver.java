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
 * Annotation to mark the class, which extends the AbstractQueryExecutor class for processing the SQL queries.</br> 
 * Also, it provides meta information for your jdbc driver. Must not be used more than one time.</br>
 * 
 * <p><b>name</b> : the name of your driver class, mandatory</p> 
 * <p><b>packageDefinition</b> : package of your driver class, default is "de.tiq.driver"</p>
 * <p><b>scheme</b> : ask the driver for checking some url scheme, mandatory</p>
 * <p><b>prefix</b> : jdbc prefix, default is "jdbc:"</p>
 * 
 * 
 * @author D. Häberlein
 * @since  1.6
 * @see de.tiq.jdbc.QueryExecutor
 */
@Documented
@Retention(RetentionPolicy.SOURCE) 
@Target(ElementType.TYPE)
public @interface JdbcDriver {
	String packageDefinition() default "";
	String name();
	String prefix() default "jdbc:";
	String scheme();
}

