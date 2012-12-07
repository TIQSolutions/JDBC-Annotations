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
package de.tiq.velocity;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.NullLogChute;

public class VelocityController {

	private static final String DEFAULT_ENCODING = "utf-8";
	private Template driverTemp;
	private Template connectionTemp;
	private Template statementTemp;
	private Template queryexecTemplate;
	private Template connectionHandlerTemplate;

	public VelocityController() {
		Properties prop = new Properties();
		try {
			//for avoiding the occurrence of a velocity log exception, disable completely the log mechanism  
			System.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
			prop.load(new StringReader(generateProps()));
		} catch (IOException e) {
			// should not be raised
			throw new RuntimeException("failed to read properties");
		}
		Velocity.init(prop);
		initTemplates();
	}

	private String generateProps() {
		return 
		"runtime.log.logsystem.class = org.apache.velocity.runtime.log.SystemLogChute\n"
		+ "resource.loader = classpath\n"
		+ "classpath.resource.loader.class = org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
	}

	void initTemplates() {
		driverTemp = Velocity.getTemplate("driver.vm",DEFAULT_ENCODING);
		connectionTemp = Velocity.getTemplate("connection.vm",DEFAULT_ENCODING);
		statementTemp = Velocity.getTemplate("statement.vm",DEFAULT_ENCODING);
		queryexecTemplate = Velocity.getTemplate("query_executor.vm",DEFAULT_ENCODING);
		connectionHandlerTemplate = Velocity.getTemplate("connection_handler.vm",DEFAULT_ENCODING);
	}
	
	public Template getDriverTemp() {
		return driverTemp;
	}
	
	public Template getConnectionTemp() {
		return connectionTemp;
	}
	
	public Template getStatementTemp() {
		return statementTemp;
	}
	
	public Template getQueryExecTemp() {
		return queryexecTemplate;
	}

	public Template getConnectionHandlerTemplate() {
		return connectionHandlerTemplate;
	}
	
	public void createFileFromTemplate(VelocityContext vcon, Template temp, Writer w){
		temp.merge(vcon, w);
	}
}
