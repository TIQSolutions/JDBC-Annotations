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

import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;

public class VelocityController {

	private static final String DEFAULT_ENCODING = "utf-8";
	private Template driverTemp;
	private Template connectionTemp;
	private Template statementTemp;
	private VelocityEngine engine;

	public VelocityController() {
		engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
		engine.setProperty("resource.loader", "classpath");
		engine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		initTemplates();
	}

	void initTemplates() {
		driverTemp = engine.getTemplate("driver.vm",DEFAULT_ENCODING);
		connectionTemp = engine.getTemplate("connection.vm",DEFAULT_ENCODING);
		statementTemp = engine.getTemplate("statement.vm",DEFAULT_ENCODING);
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
	
	public void createFileFromTemplate(VelocityContext vcon, Template temp, Writer w){
		temp.merge(vcon, w);
	}
}
