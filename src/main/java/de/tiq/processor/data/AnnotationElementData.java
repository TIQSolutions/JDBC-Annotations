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
package de.tiq.processor.data;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

import de.tiq.jdbc.annotations.Connection;
import de.tiq.jdbc.annotations.ConnectionMetaData;
import de.tiq.jdbc.annotations.JdbcDriver;


public class AnnotationElementData {
	
	Set<? extends Element> driverAnnotatedElements;
	Set<? extends Element> connectionAnnotatedElements;
	Set<? extends Element> conMetaDataAnnotatedElements;
	
	public AnnotationElementData(RoundEnvironment roundEnv) {
		driverAnnotatedElements = roundEnv.getElementsAnnotatedWith(JdbcDriver.class);
		connectionAnnotatedElements = roundEnv.getElementsAnnotatedWith(Connection.class);
		conMetaDataAnnotatedElements = roundEnv.getElementsAnnotatedWith(ConnectionMetaData.class);
	}
	
	public Set<? extends Element> getDriverAnnotatedElements() {
		return driverAnnotatedElements;
	}
	public void setDriverAnnotatedElements(Set<? extends Element> driverAnnotatedElements) {
		this.driverAnnotatedElements = driverAnnotatedElements;
	}
	public Set<? extends Element> getConnectionAnnotatedElements() {
		return connectionAnnotatedElements;
	}
	public void setConnectionAnnotatedElements(Set<? extends Element> connectionAnnotatedElements) {
		this.connectionAnnotatedElements = connectionAnnotatedElements;
	}
	public Set<? extends Element> getConMetaDataAnnotatedElements() {
		return conMetaDataAnnotatedElements;
	}
	public void setConMetaDataAnnotatedElements(Set<? extends Element> conMetaDataAnnotatedElements) {
		this.conMetaDataAnnotatedElements = conMetaDataAnnotatedElements;
	}
}
