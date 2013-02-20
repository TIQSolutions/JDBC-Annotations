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
package de.tiq.processor;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.*;

import org.junit.Test;

public class TestUtilsForAnnoProcessing {
	
	@Test
	public void testPackageExtraction(){
		JdbcAnnotationProcessor proc = new JdbcAnnotationProcessor();
		assertEquals("org.relique.jdbc.csv",proc.extractPackage("org.relique.jdbc.csv.ExecutorImplementationClass"));
		assertEquals("de.tiq",proc.extractPackage("de.tiq.ClassName"));
		assertEquals("de.tiq",proc.extractPackage("de.tiq.ClassName.java"));
		
	}
	
	@Test
	public void testArrayContainsStringRepresentationOfObject(){
		JdbcAnnotationProcessor proc = new JdbcAnnotationProcessor();
		Object[] fixture = new Object[]{"a", "b", "c", new Integer(4), "e"};
		assertTrue("array contains toString failed when it should be true", proc.elementOfArrayEndsWith(fixture, "e"));
		assertFalse("array contains toString failed when it should be false", proc.elementOfArrayEndsWith(fixture, "f"));
	}
	
	@Test
	public void testgetFirstElementOfSet() {
		JdbcAnnotationProcessor proc = new JdbcAnnotationProcessor();
		Set<String> fixture = new HashSet<String>();
		String fixtureContent = "test"; 
		fixture.add(fixtureContent);
		assertEquals(fixtureContent, proc.<String>getFirstElementOfSet(fixture));
	}
}
