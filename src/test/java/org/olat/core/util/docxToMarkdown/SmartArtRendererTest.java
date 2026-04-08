/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.docxToMarkdown;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

/**
 * @author gnaegi, https://www.frentix.com
 */
public class SmartArtRendererTest {

	@Test
	public void renderNullInputs() {
		assertNull(SmartArtRenderer.render(null, null, null, 0, 0));
	}

	@Test
	public void renderSimpleDiagram() throws Exception {
		// Create a minimal DOCX with a SmartArt drawing
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
			+ "<a:ln w=\"19050\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill></a:ln>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>Hello</a:t></a:r></a:p></dsp:txBody>"
			+ "</dsp:sp>"
			+ "<dsp:sp modelId=\"{2}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"600000\" y=\"0\"/><a:ext cx=\"400000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"chevron\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
			+ "<a:ln w=\"19050\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill></a:ln>"
			+ "</dsp:spPr>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_test", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_media_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400);

			assertNotNull("SVG file must be created", svgFile);
			assertTrue("Filename must end with .svg", svgFile.endsWith(".svg"));

			File svg = new File(mediaDir, svgFile);
			assertTrue("SVG file must exist on disk", svg.exists());

			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain <svg", content.contains("<svg"));
			assertTrue("SVG must contain rect", content.contains("<rect") || content.contains("<polygon"));
			assertTrue("SVG must contain text", content.contains("Hello"));
			assertTrue("SVG must contain chevron shape", content.contains("<polygon"));
		}
	}

	@Test
	public void renderMissingDrawingFile() throws Exception {
		File docx = File.createTempFile("smartart_missing", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_missing_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String result = SmartArtRenderer.render(zf, "diagrams/nonexistent.xml", mediaDir, 5000000, 3000000);
			assertNull("Missing drawing file must return null", result);
		}
	}
}
