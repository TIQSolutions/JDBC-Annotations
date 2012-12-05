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
import java.util.ArrayList;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.apache.velocity.VelocityContext;

import de.tiq.jdbc.annotations.Connection;
import de.tiq.jdbc.annotations.Driver;
import de.tiq.jdbc.annotations.Statement;
import de.tiq.velocity.VelocityController;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class JdbcAnnotationProcessor extends AbstractProcessor{

	private int laps;
	
	private Elements elementUtils;
	private TypeMirror driverAnnoType;
	private Messager msgr;
	private Filer filer;
	private VelocityController vc;

	private int driverAnnotationCount;
	private List<Element> resourceFields = new ArrayList<Element>();
	private List<Boolean> useParameterizedDefaultConstructor = new ArrayList<Boolean>();
	private String queryExecutorClassName;
	
	@Override
	public void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		// annoation processor

		filer = processingEnv.getFiler();
		elementUtils = processingEnv.getElementUtils();
		driverAnnoType = elementUtils.getTypeElement(Driver.class.getName()).asType();
		msgr = processingEnv.getMessager();

		// initialize apache velocity
		vc = new VelocityController();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(roundEnv.getRootElements().size() != 0 && laps == 0) {
			
			Set<? extends Element> driverAnnotatedClass = roundEnv.getElementsAnnotatedWith(Driver.class);
			try {
				if (isAnnoationsSingleton(driverAnnotatedClass, driverAnnoType.toString(), driverAnnotationCount)) {
					processElements(roundEnv);
					createConnectionClass();
					createStatementClass();
					createQueryExecutorInterface();
					msgr.printMessage(Kind.NOTE, "Source generation successfully finished!");
				}
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
				Driver drv = curElement.getAnnotation(Driver.class);
				Connection con = curElement.getAnnotation(Connection.class);
				Statement stmt = curElement.getAnnotation(Statement.class);
				if (drv != null) {
					createDriverClass(drv.packageDefinition(), drv.name(), drv.prefix(),drv.scheme());
				}
				if (con != null) {
					resourceFields.add(curElement);
					useParameterizedDefaultConstructor.add(curElement.getAnnotation(Connection.class).value());
				}
				if(stmt != null){
					if(queryExecutorClassName == null){
						queryExecutorClassName = curElement.toString();
					} else {
						msgr.printMessage(Kind.MANDATORY_WARNING, "Only one query executor (statement annotated class) is supported! Proceeding anyway using the first one...");
					}
				}
			}
		}
	}
	
	private boolean isAnnoationsSingleton(Set<? extends Element> annotations, String annotationName, int driverAnnotationCount) {
		driverAnnotationCount += annotations.size();
		if (driverAnnotationCount > 1 || driverAnnotationCount == 0){
			msgr.printMessage(Kind.ERROR, "Driver Annotation has not been used in the right way! Occurence was :" + this.driverAnnotationCount + " times" );
			return false;
		} else 
			return true;
	}
	
	private VelocityContext initializeVelocityContext() {
		VelocityContext vcon = new VelocityContext();
		vcon.put("creatingTimestamp",new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()));
		vcon.put("generatedAnnotationClass", getClass().getName());
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
	
	private void createConnectionClass() throws IOException {
		VelocityContext vcon = initializeVelocityContext();
		Writer writer = createJavaSourceFile("TIQConnection", "de.tiq.jdbc");
		vcon.put("resourceFields", resourceFields);
		vcon.put("parameterizedConstructor", useParameterizedDefaultConstructor);
		vc.createFileFromTemplate(vcon, vc.getConnectionTemp(), writer);
		writer.close();
	}
	
	private void createStatementClass() throws IOException {
		VelocityContext vcon = initializeVelocityContext();
		Writer writer = createJavaSourceFile("TIQStatement", "de.tiq.jdbc");
		vcon.put("resourceFields", resourceFields);
		vcon.put("executorClass", queryExecutorClassName);
		vc.createFileFromTemplate(vcon, vc.getStatementTemp(), writer);
		writer.close();
	}
	
	private void createQueryExecutorInterface() throws IOException{
		VelocityContext vcon = initializeVelocityContext();
		Writer writer = createJavaSourceFile("QueryExecutor", "de.tiq.jdbc");
		vc.createFileFromTemplate(vcon, vc.getQueryExecTemp(), writer);
		writer.close();
	}
	
	public List<Element> getResourceFields() {
		return resourceFields;
	}
}
