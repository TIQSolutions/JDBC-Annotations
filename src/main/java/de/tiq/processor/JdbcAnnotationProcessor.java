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
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import de.tiq.jdbc.annotations.Connection;
import de.tiq.jdbc.annotations.ConnectionMetaData;
import de.tiq.jdbc.annotations.JdbcDriver;
import de.tiq.velocity.VelocityController;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JdbcAnnotationProcessor extends AbstractProcessor{

	private static final String DEFAULT_PACKAGE = "de.tiq.jdbc";
	
	private int laps;
	
	private Messager msgr;
	private Filer filer;
	private VelocityController vc;

	private int driverAnnotationCount;

	private String executorClassName;
	private String connectionHandlerClassName;
	private String connectionMetaDataProviderClassName;
	
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		// annoation processor
		filer = processingEnv.getFiler();
		msgr = processingEnv.getMessager();
		// initialize apache velocity
		vc = new VelocityController();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(roundEnv.getRootElements().size() != 0 && laps == 0) {
			Set<? extends Element> driverAnnotatedClass = roundEnv.getElementsAnnotatedWith(JdbcDriver.class);
			try {
				processElements(roundEnv);
				createTemplateClass("QueryExecutor", DEFAULT_PACKAGE, vc.getQueryExecTemp());
				createTemplateClass("ConnectionHandler", DEFAULT_PACKAGE, vc.getConnectionHandlerTemplate());
				createTemplateClass("ConnectionMetaDataProvider", DEFAULT_PACKAGE, vc.getConnectionMetaDataProviderTemplate());
				createTemplateClass("TIQConnection", DEFAULT_PACKAGE, vc.getConnectionTemp());
				createTemplateClass("TIQStatement", DEFAULT_PACKAGE, vc.getStatementTemp());
				warnForUsage(driverAnnotatedClass, driverAnnotationCount);
				msgr.printMessage(Kind.NOTE, "Source generation successfully finished!");
			} catch (Exception e) {
				msgr.printMessage(Kind.ERROR, "failed to generate classes, stacktrace was:\n" + e);
			}
		}
		laps++;
		// annotations consumed
		return true;
	}

	private void processElements(RoundEnvironment roundEnv) throws IOException {
		for (Element curElement : roundEnv.getRootElements()) {
			if (curElement.getAnnotation(javax.annotation.Generated.class) == null && curElement.getKind() == ElementKind.CLASS) {
				Connection con = curElement.getAnnotation(Connection.class);
				JdbcDriver driverAnnotation = curElement.getAnnotation(JdbcDriver.class);
				ConnectionMetaData conMetaData = curElement.getAnnotation(ConnectionMetaData.class);
				TypeElement classElement = (TypeElement)curElement;
				if (con != null) {
					if (checkExecutorClass(classElement.getSuperclass(), "ConnectionHandler")) {
						connectionHandlerClassName = curElement.toString();
					} else {
						connectionHandlerClassName = null;
						msgr.printMessage(Kind.WARNING, "The class " + curElement.toString() + " need to extend the abstract class \"ConnectionHandler\"!");
					}
				}
				if(conMetaData != null){
					List<? extends TypeMirror> containedInterfaces = classElement.getInterfaces();
					if (foreachToStringEndsWith(containedInterfaces.toArray(new TypeMirror[containedInterfaces.size()]), "ConnectionMetaDataProvider")) {
						connectionMetaDataProviderClassName = curElement.toString();
					} else {
						connectionMetaDataProviderClassName = null;
						msgr.printMessage(Kind.WARNING, "The class " + curElement.toString() + " need to implement the interface \"ConnectionMetaDataProvider\"!");
					}
				}
				if(driverAnnotation != null){
					if (checkExecutorClass(classElement.getSuperclass(), "QueryExecutor")) {
						executorClassName = curElement.toString();
					} else {
						executorClassName = null;
						msgr.printMessage(Kind.WARNING, "The class " + curElement.toString() + " need to extend the abstract class \"QueryExecutor\"!");
					}
					createDriverClass(evaluatePackage(driverAnnotation.packageDefinition(), curElement),
													  driverAnnotation.name(), 
													  driverAnnotation.prefix(),
													  driverAnnotation.scheme());
				}
			}
		}
	}

	private String evaluatePackage(String packageName, Element curElement) {
		return packageName.equals("") ? extractPackage(curElement.toString()) : packageName;
	}

	private boolean checkExecutorClass(TypeMirror superclass, String className) {
		return superclass.toString().endsWith(className);
	}

	private void warnForUsage(Set<? extends Element> annotations, int driverAnnotationCount) {
		driverAnnotationCount += annotations.size();
		if (driverAnnotationCount != 1)
			msgr.printMessage(Kind.WARNING, "Driver Annotation has not been used in the right way! Occurence was: " + this.driverAnnotationCount + " times" );
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
	
	private void createDriverClass(String packageName, String className, String prefix, String scheme) throws IOException {
		VelocityContext vcon = initializeVelocityContext();
		vcon.put("package", packageName);
		vcon.put("className", className);
		vcon.put("urlPrefix", prefix);
		vcon.put("urlScheme", scheme);
		Writer writer = createJavaSourceFile(className, packageName);
		vc.createFileFromTemplate(vcon, vc.getDriverTemp(), writer);
		writer.close();
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

	boolean foreachToStringEndsWith(Object[] testable, String searchable) {
		for(Object obj : testable){
			if(obj.toString().endsWith(searchable)){
				return true;
			}
		}
		return false;
	}
}
