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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import de.tiq.jdbc.annotations.JdbcDriver;
import de.tiq.processor.data.AnnotationElementData;
import de.tiq.velocity.VelocityController;

@SupportedAnnotationTypes("de.tiq.jdbc.annotations.*")
public class JdbcAnnotationProcessor extends AbstractProcessor{

	private static final String VERSION_NUMBER = "0.0.4";
	private static final String DEFAULT_PACKAGE = "de.tiq.jdbc";
	
	private Messager msgr;
	private Filer filer;
	private VelocityController vc;

	private String executorClassName;
	private String connectionHandlerClassName;
	private String connectionMetaDataProviderClassName;
	
	private boolean createdDriverClass;
	private boolean createdSources;
	
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		filer = processingEnv.getFiler();
		msgr = processingEnv.getMessager();
		vc = new VelocityController();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		msgr.printMessage(Kind.NOTE, "Started Annotation Processing, Using JDBC_Annotation Version " + VERSION_NUMBER);
		boolean classesConsumed = true;
		AnnotationElementData elementData = new AnnotationElementData(roundEnv);
		boolean processClassesInThatRound = willProcessingClasses(roundEnv, elementData);
		if(processClassesInThatRound){
			processAnnotatedElements(elementData);
		} else {
			classesConsumed = false;
		}
		return classesConsumed;
	}
	
	private boolean willProcessingClasses(RoundEnvironment roundEnv, AnnotationElementData elementData){
		boolean classesAvailable = roundEnv.getRootElements().size() != 0;
		int sumOfAnnotationOccurence = elementData.getDriverAnnotatedElements().size() 
									 + elementData.getConnectionAnnotatedElements().size()
									 + elementData.getConMetaDataAnnotatedElements().size();
		boolean annotatedClassesForThisProcessorArePresent = sumOfAnnotationOccurence != 0;
		return classesAvailable && annotatedClassesForThisProcessorArePresent;
	}

	private void processAnnotatedElements(AnnotationElementData elementData) {
		try {
			evalDriverAnnotatedElements(elementData.getDriverAnnotatedElements());
			getTypeNameOfConnectionAnnotatedElement(elementData.getConnectionAnnotatedElements());
			getTypeNameOfConnectionMetaDataAnnotatedElement(elementData.getConMetaDataAnnotatedElements());
			createDefaultTemplateSources();
		} catch (Exception e) {
			msgr.printMessage(Kind.ERROR, "failed to generate classes, stacktrace was:\n");
			e.printStackTrace();
		}
	}

	private void createDefaultTemplateSources() throws IOException {
		if (!createdSources) {
			createTemplateClass("TIQConnection", DEFAULT_PACKAGE, vc.getConnectionTemp());
			createTemplateClass("TIQStatement", DEFAULT_PACKAGE, vc.getStatementTemp());
			createdSources = true;
			msgr.printMessage(Kind.NOTE, "Source generation successfully finished!");
		}
	}
	
	private void evalDriverAnnotatedElements(Set<? extends Element> driverAnnotatedElements) throws IOException {
		int countOfAnnotatedClasses = driverAnnotatedElements.size();
		if(countOfAnnotatedClasses == 1 && !createdDriverClass){
			Element annotatedType = this.<Element>getFirstElementOfSet(driverAnnotatedElements);
			JdbcDriver annotation = annotatedType.getAnnotation(JdbcDriver.class);
			buildDriverClass(annotatedType);
			buildServicesFile(annotation);
		} else {
			printWarning(countOfAnnotatedClasses);
		}
	}
	
	private void buildDriverClass(Element annotatedType) throws IOException {
		JdbcDriver driverMetaInfo = annotatedType.getAnnotation(JdbcDriver.class);
		String packageName = evaluatePackage(driverMetaInfo.packageDefinition(), annotatedType);
		createDriverClass(packageName, driverMetaInfo);
		executorClassName = annotatedType.toString();
		createdDriverClass = true;
	}
	
	private void createDriverClass(String packageName, JdbcDriver driverMetaInfo) throws IOException {
		VelocityContext vcon = initializeVelocityContext();
		vcon.put("package", packageName);
		vcon.put("className", driverMetaInfo.name());
		vcon.put("urlPrefix", driverMetaInfo.prefix());
		vcon.put("urlScheme", driverMetaInfo.scheme());
		Writer writer = createJavaSourceFile(driverMetaInfo.name(), packageName);
		vc.createFileFromTemplate(vcon, vc.getDriverTemp(), writer);
		writer.close();
	}

	private void buildServicesFile(JdbcDriver annotation) {
		try {
			String driverName = annotation.packageDefinition() + "." + annotation.name();
			//Output path is designed for maven! 
			FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/java.sql.Driver");
			writeFile(driverName, file);
		} catch (Exception e) {
			System.err.println("Wasn't able to create the services file! Stacktrace was: ");
			e.printStackTrace();
		}
	}

	private void writeFile(String content, FileObject file) throws Exception{
		OutputStream fileOutputStream = null;
		try {
			fileOutputStream = file.openOutputStream();
			fileOutputStream.write(content.getBytes("utf-8"));
		} catch(Exception e){
			throw e;
		} finally{
			if (fileOutputStream != null)
				fileOutputStream.close();
		}
	}

	private void printWarning(int countOfAnnotatedClasses) {
		if(countOfAnnotatedClasses != 1){
			msgr.printMessage(Kind.WARNING, "Driver Annotation has not been used in the right way! Occurence was: " + countOfAnnotatedClasses + " times" );
		}
	}
	
	private void getTypeNameOfConnectionAnnotatedElement(Set<? extends Element> connectionAnnotatedElements) {
		if (!connectionAnnotatedElements.isEmpty()) {
			Element conTypeName = connectionAnnotatedElements.iterator().next();
			connectionHandlerClassName = conTypeName.toString();
		} else {
			msgr.printMessage(Kind.WARNING, "No ConnectionHandler class was found! Do you have set the @Connection annotation?");
		}
	}

	private void getTypeNameOfConnectionMetaDataAnnotatedElement(Set<? extends Element> conMetaDataAnnotatedElements) {
		if (!conMetaDataAnnotatedElements.isEmpty()) {
			Element conMetaDataTypeName = conMetaDataAnnotatedElements.iterator().next();
			connectionMetaDataProviderClassName = conMetaDataTypeName.toString();
		} else {
			msgr.printMessage(Kind.WARNING, "No ConnectionMetaData class was found!");
		}
	}

	private String evaluatePackage(String packageName, Element curElement) {
		return packageName.equals("") ? extractPackage(curElement.toString()) : packageName;
	}

	private VelocityContext initializeVelocityContext() {
		VelocityContext vcon = new VelocityContext();
		vcon.put("creatingTimestamp",new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
		vcon.put("generatedAnnotationClass", getClass().getName());
		vcon.put("connectionHandler", connectionHandlerClassName);
		vcon.put("executorClass", executorClassName);
		vcon.put("connectionMetaDataProviderClass", connectionMetaDataProviderClassName);
		return vcon;
	}

	private Writer createJavaSourceFile(String className, String packageName) throws IOException {
		JavaFileObject file = filer.createSourceFile(packageName + "." + className);
		return file.openWriter();
	}
	
	private void createTemplateClass(String className, String packageName, Template temp) throws IOException{
		VelocityContext vcon = initializeVelocityContext();
		Writer writer = createJavaSourceFile(className, packageName);
		vc.createFileFromTemplate(vcon, temp, writer);
		writer.close();
	}
	
	String extractPackage(String qualifiedClassName) {
		int cuttingDepth = 1;
		if(qualifiedClassName.endsWith(".java"))
			cuttingDepth = 2;
		String[] parts = qualifiedClassName.split("\\.");
		return StringUtils.join(Arrays.copyOfRange(parts,0,parts.length-cuttingDepth),".");
	}

	boolean elementOfArrayEndsWith(Object[] testable, String searchable) {
		for(Object obj : testable){
			if(obj.toString().endsWith(searchable)){
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	<T> T getFirstElementOfSet(Set<?> set){
		return (T)set.iterator().next();
	}
}
