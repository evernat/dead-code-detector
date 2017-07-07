/*
 * Copyright 2008 by Emeric Vernat
 *
 *     This file is part of Dead Code Detector.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dcd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Affichage et export xml des résultats d'analyses.
 * @author evernat
 */
class Report {
	private static final String XML_INDENT = "  ";
	// retour à la ligne xml normalisé (http://www.w3.org/TR/xml11/#sec-line-ends)
	private static final String XML_END_OF_LINE = "\n";
	private final OutputStream output;
	private final XMLStreamWriter xmlStreamWriter;

	Report(File xmlReportFileIfNeeded) throws XMLStreamException, IOException {
		super();
		if (xmlReportFileIfNeeded == null) {
			output = null;
			xmlStreamWriter = null;
		} else {
			if (xmlReportFileIfNeeded.getParentFile() != null
					&& !xmlReportFileIfNeeded.getParentFile().mkdirs()
					&& !xmlReportFileIfNeeded.getParentFile().exists()) {
				throw new IOException("Parent directory of xmlReportFile can't be created: "
						+ xmlReportFileIfNeeded.getPath());
			}
			output = new BufferedOutputStream(new FileOutputStream(xmlReportFileIfNeeded),
					64 * 1024);
			final XMLOutputFactory factory = XMLOutputFactory.newInstance();
			// Output destination can be specified with an OutputStream or Writer.
			xmlStreamWriter = factory.createXMLStreamWriter(output);
			//		String url = "http://url/";
			//      xmlStreamWriter.setPrefix("date", url + "date");

			xmlStreamWriter.writeStartDocument(); // writes XML declaration
			xmlStreamWriter.writeCharacters(XML_END_OF_LINE);
			xmlStreamWriter
					.writeComment(' ' + DcdHelper.APPLICATION_NAME + ", http://dcd.dev.java.net ");
			xmlStreamWriter.writeCharacters(XML_END_OF_LINE);
			final String root = "dcd";
			//        String doctype =
			//            "<!DOCTYPE " + root + " SYSTEM \"" + url + "xml/cd.dtd\">";
			//        xsm.writeDTD(doctype);
			//        xsm.writeProcessingInstruction(
			//            "xml-stylesheet", "type=\"text/xsl\" href=\"cd.xslt\"");
			xmlStreamWriter.writeStartElement(root);
			xmlStreamWriter.writeCharacters(XML_END_OF_LINE);
			//        xsm.writeDefaultNamespace(url + "music");
			//        xsm.writeNamespace("date", url + "xml/date.xsd");
			//        xsm.writeNamespace("xsi", "http://www.w3.org/1999/XMLSchema-instance");
			//        xsm.writeAttribute("xsi:schemaLocation",
			//            url + "music " + url + "xml/cd.xsd " +
			//            url + "date " + url + "xml/date.xsd");
			// xmlStreamWriter.writeAttribute("year", "2008");
		}
	}

	void close(long durationMillis, int suspectCount, int analyzedClassCount, File xmlReportFile)
			throws IOException, XMLStreamException {
		log("Duration: " + durationMillis + "ms");
		log(suspectCount + " suspect methods (or fields or variables), " + analyzedClassCount
				+ " analyzed classes");
		if (xmlReportFile != null) {
			log("Xml report written to " + xmlReportFile.getPath());
		}
		log("");

		if (xmlStreamWriter != null) {
			try {
				try {
					xmlStreamWriter.writeCharacters(XML_INDENT);
					xmlStreamWriter.writeStartElement("summary");
					xmlStreamWriter.writeAttribute("durationMillis",
							String.valueOf(durationMillis));
					xmlStreamWriter.writeAttribute("suspectCount", String.valueOf(suspectCount));
					xmlStreamWriter.writeAttribute("analyzedClassCount",
							String.valueOf(analyzedClassCount));
					xmlStreamWriter.writeEndElement();
					xmlStreamWriter.writeCharacters(XML_END_OF_LINE);

					xmlStreamWriter.writeEndElement();
					xmlStreamWriter.writeCharacters(XML_END_OF_LINE);
				} finally {
					xmlStreamWriter.close();
				}
			} finally {
				output.close();
			}
		}
	}

	void reportDeadCodeSuspects(boolean publicDeadCode, String asmClassName, Set<String> descs)
			throws XMLStreamException {
		final String type = publicDeadCode ? "publicDeadCode" : "privateDeadCode";
		final String className = Type.getObjectType(asmClassName).getClassName();
		final String msg = (publicDeadCode ? "Public suspects in class "
				: "Private suspects in class ") + className + ':';
		final String msg2 = '\t' + descs.toString();
		reportWarning(type, className, msg, msg2);
	}

	void reportUselessInitializations(String className, Set<String> fields)
			throws XMLStreamException {
		final String msg = "Useless initializations in class " + className + ':';
		final String msg2 = '\t' + fields.toString();
		reportWarning("uselessInitialization", className, msg, msg2);
	}

	void reportDeadLocalVariables(String className, MethodNode methodNode,
			Set<LocalVariableNode> localVariables) throws XMLStreamException {
		final Set<String> names = new LinkedHashSet<>(localVariables.size());
		for (final LocalVariableNode localVariable : localVariables) {
			names.add(localVariable.name);
		}
		final String msg = "Local suspects in class " + className + " in method "
				+ getMethodDescription(methodNode) + ':';
		final String msg2 = '\t' + names.toString();
		reportWarning("deadLocalVariable", className, msg, msg2);
	}

	void reportSelfAssignments(String className, MethodNode methodNode, Set<String> selfAssignments)
			throws XMLStreamException {
		final String msg = "Self assignments in class " + className + " in method "
				+ getMethodDescription(methodNode) + ':';
		final String msg2 = '\t' + selfAssignments.toString();
		reportWarning("selfAssignment", className, msg, msg2);
	}

	void reportStringToString(String className, MethodNode methodNode) throws XMLStreamException {
		final String msg = "Call of toString() on String in class " + className + " in method "
				+ getMethodDescription(methodNode);
		reportWarning("stringToString", className, msg, null);
	}

	private void reportWarning(String type, String className, String msg, String msg2)
			throws XMLStreamException {
		if (xmlStreamWriter == null) {
			log(msg);
			if (msg2 != null) {
				log(msg2);
			}
		} else {
			xmlStreamWriter.writeCharacters(XML_INDENT);
			xmlStreamWriter.writeStartElement(type);
			xmlStreamWriter.writeAttribute("className", className);
			xmlStreamWriter.writeCharacters(msg);
			if (msg2 != null) {
				xmlStreamWriter.writeCharacters(msg2);
			}
			xmlStreamWriter.writeEndElement();
			xmlStreamWriter.writeCharacters(XML_END_OF_LINE); // retour à la ligne pour lisibilité
		}
	}

	private static String getMethodDescription(MethodNode methodNode) {
		return DcdHelper
				.getMethodDescription(DcdHelper.getMethodKey(methodNode.name, methodNode.desc));
	}

	private static void log(String msg) {
		DcdHelper.log(msg);
	}
}
