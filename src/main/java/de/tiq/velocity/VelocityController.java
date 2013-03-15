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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;

public class VelocityController {

	private static final String DEFAULT_ENCODING = "utf-8";
	private VelocityEngine engine;

	public VelocityController() {
		initEngine();
	}

	private void initEngine() {
		engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.class", NullLogChute.class.getName());
		engine.setProperty("resource.loader", "classpath");
		engine.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
	}
	
	public void createFileFromTemplate(VelocityContext vcon, String templateName, Writer writer){
		engine.mergeTemplate(templateName, DEFAULT_ENCODING, vcon, writer);
	}
}
