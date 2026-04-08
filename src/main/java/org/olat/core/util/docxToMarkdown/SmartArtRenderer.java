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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Renders SmartArt diagrams (from word/diagrams/drawing1.xml) to SVG files.
 * SmartArt in OOXML consists of shapes with preset geometries, positions,
 * and text. This renderer converts them to a single SVG image.
 *
 * @author gnaegi, https://www.frentix.com
 */
class SmartArtRenderer {

	private static final Logger log = Tracing.createLoggerFor(SmartArtRenderer.class);

	private SmartArtRenderer() {
		// utility
	}

	/**
	 * Render a SmartArt diagram to an SVG file.
	 *
	 * @param zipFile    the DOCX ZIP file (open)
	 * @param drawingRel the relationship target for the diagram drawing (e.g., "diagrams/drawing1.xml")
	 * @param mediaDir   directory to write the SVG file to
	 * @param widthEmu   display width from wp:extent cx (EMU)
	 * @param heightEmu  display height from wp:extent cy (EMU)
	 * @return the filename of the generated SVG (relative to media/), or null if rendering fails
	 */
	static String render(ZipFile zipFile, String drawingRel, File mediaDir,
			int widthEmu, int heightEmu) {
		if (zipFile == null || drawingRel == null || mediaDir == null) {
			return null;
		}

		String entryPath = drawingRel.startsWith("word/") ? drawingRel : "word/" + drawingRel;
		ZipEntry entry = zipFile.getEntry(entryPath);
		if (entry == null) {
			log.debug("SmartArt drawing not found: {}", entryPath);
			return null;
		}

		try {
			byte[] xml;
			try (var is = zipFile.getInputStream(entry)) {
				xml = is.readAllBytes();
			}

			SmartArtShapeCollector collector = new SmartArtShapeCollector();
			SAXParser parser = XMLFactories.newSAXParser();
			parser.getXMLReader().setFeature(
				"http://apache.org/xml/features/disallow-doctype-decl", true);
			parser.parse(new ByteArrayInputStream(xml), collector);

			List<ShapeInfo> shapes = collector.getShapes();
			if (shapes.isEmpty()) {
				return null;
			}

			String svg = buildSvg(shapes, widthEmu, heightEmu);
			String filename = "smartart_" + System.nanoTime() + ".svg";
			File svgFile = new File(mediaDir, filename);
			Files.writeString(svgFile.toPath(), svg, StandardCharsets.UTF_8);
			return filename;
		} catch (Exception e) {
			log.debug("SmartArt rendering failed: {}", e.getMessage());
			return null;
		}
	}

	private static String buildSvg(List<ShapeInfo> shapes, int widthEmu, int heightEmu) {
		int pxW = Math.max(20, widthEmu / 9525);
		int pxH = Math.max(20, heightEmu / 9525);

		// Find bounding box from shapes
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = 0, maxY = 0;
		for (ShapeInfo s : shapes) {
			minX = Math.min(minX, s.x);
			minY = Math.min(minY, s.y);
			maxX = Math.max(maxX, s.x + s.cx);
			maxY = Math.max(maxY, s.y + s.cy);
		}
		int viewW = Math.max(1, maxX - minX);
		int viewH = Math.max(1, maxY - minY);

		StringBuilder svg = new StringBuilder();
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(pxW)
			.append("\" height=\"").append(pxH)
			.append("\" viewBox=\"").append(minX).append(' ').append(minY)
			.append(' ').append(viewW).append(' ').append(viewH).append("\">");

		// Default colors for scheme colors (approximate OpenOlat theme)
		String accentFill = "#4472C4";
		String accentStroke = "#2F528F";
		String lt1Fill = "#FFFFFF";

		for (ShapeInfo s : shapes) {
			// Skip invisible shapes (noFill AND noStroke) — these are text containers only
			boolean invisible = s.noFill && s.noStroke;

			String fill = s.noFill ? "none"
				: "lt1".equals(s.fillScheme) ? lt1Fill
				: "accent1".equals(s.fillScheme) ? accentFill
				: s.fillScheme != null ? accentFill : "none";
			String stroke = s.noStroke ? "none" : accentStroke;
			int sw = s.strokeWidth > 0 ? Math.max(1, s.strokeWidth / 12700) : 1;

			if (!invisible) {
				switch (s.presetGeom) {
					case "rect" -> svg.append("<rect x=\"").append(s.x).append("\" y=\"").append(s.y)
						.append("\" width=\"").append(s.cx).append("\" height=\"").append(s.cy)
						.append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke)
						.append("\" stroke-width=\"").append(sw).append("\"/>");
					case "chevron" -> {
						// OOXML chevron: adj controls the point depth as % of WIDTH
						// dx = cx * adj / 100000 (default adj=50000)
						int adjVal = s.adjValue > 0 ? s.adjValue : 50000;
						int dx = (int)((long) s.cx * adjVal / 100000);
						int midY = s.y + s.cy / 2;
						// Points: top-left, top-right-before-tip, right-tip, bottom-right-before-tip, bottom-left, left-indent
						svg.append("<polygon points=\"")
							.append(s.x).append(',').append(s.y).append(' ')
							.append(s.x + s.cx - dx).append(',').append(s.y).append(' ')
							.append(s.x + s.cx).append(',').append(midY).append(' ')
							.append(s.x + s.cx - dx).append(',').append(s.y + s.cy).append(' ')
							.append(s.x).append(',').append(s.y + s.cy).append(' ')
							.append(s.x + dx).append(',').append(midY)
							.append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke)
							.append("\" stroke-width=\"").append(sw).append("\"/>");
					}
					case "ellipse" -> svg.append("<ellipse cx=\"").append(s.x + s.cx / 2)
						.append("\" cy=\"").append(s.y + s.cy / 2)
						.append("\" rx=\"").append(s.cx / 2).append("\" ry=\"").append(s.cy / 2)
						.append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke)
						.append("\" stroke-width=\"").append(sw).append("\"/>");
					default -> svg.append("<rect x=\"").append(s.x).append("\" y=\"").append(s.y)
						.append("\" width=\"").append(s.cx).append("\" height=\"").append(s.cy)
						.append("\" fill=\"").append(fill).append("\" stroke=\"").append(stroke)
						.append("\" stroke-width=\"").append(sw).append("\"/>");
				}
			}

			// Render text — use txXfrm position if available, otherwise shape center
			if (s.text != null && !s.text.isBlank()) {
				int textX = s.txX > 0 ? s.txX + s.txCx / 2 : s.x + s.cx / 2;
				int textY = s.txY > 0 ? s.txY + s.txCy / 2 : s.y + s.cy / 2;
				int textW = s.txCx > 0 ? s.txCx : s.cx;
				int textH = s.txCy > 0 ? s.txCy : s.cy;
				int fontSize = Math.min(textH / 3, textW / Math.max(1, s.text.length()));
				fontSize = Math.max(fontSize, textH / 6);
				String textColor = invisible || "lt1".equals(s.fillScheme) ? "#333333" : "white";
				svg.append("<text x=\"").append(textX).append("\" y=\"").append(textY)
					.append("\" text-anchor=\"middle\" dominant-baseline=\"central\" fill=\"")
					.append(textColor).append("\" font-family=\"sans-serif\" font-size=\"")
					.append(fontSize).append("\">")
					.append(escapeXml(s.text)).append("</text>");
			}
		}

		svg.append("</svg>");
		return svg.toString();
	}

	private static String escapeXml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	// --- Shape data ---

	static class ShapeInfo {
		String presetGeom = "rect";
		int x, y, cx, cy;
		int adjValue;          // adjust value from a:avLst (e.g., 70610 for chevron point depth)
		String fillScheme;
		boolean noFill;
		boolean noStroke;
		int strokeWidth;
		String text;
		int txX, txY, txCx, txCy; // text transform from dsp:txXfrm
	}

	// --- SAX handler to collect shapes from dsp:drawing ---

	private static class SmartArtShapeCollector extends DefaultHandler {
		private final List<ShapeInfo> shapes = new ArrayList<>();
		private ShapeInfo current;
		private boolean inShape;
		private boolean inTxBody;
		private boolean inTxXfrm;
		private boolean inLn;
		private boolean inText;
		private final StringBuilder textBuf = new StringBuilder();

		List<ShapeInfo> getShapes() { return shapes; }

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs)
				throws SAXException {
			String name = stripPrefix(qName);
			switch (name) {
				case "sp" -> {
					current = new ShapeInfo();
					inShape = true;
				}
				case "off" -> {
					if (inShape && current != null) {
						if (inTxXfrm) {
							current.txX = intAttr(attrs, "x");
							current.txY = intAttr(attrs, "y");
						} else if (current.x == 0 && current.y == 0) {
							current.x = intAttr(attrs, "x");
							current.y = intAttr(attrs, "y");
						}
					}
				}
				case "ext" -> {
					if (inShape && current != null) {
						if (inTxXfrm) {
							current.txCx = intAttr(attrs, "cx");
							current.txCy = intAttr(attrs, "cy");
						} else if (current.cx == 0) {
							current.cx = intAttr(attrs, "cx");
							current.cy = intAttr(attrs, "cy");
						}
					}
				}
				case "prstGeom" -> {
					if (inShape && current != null) {
						String prst = attrs.getValue("prst");
						if (prst != null) current.presetGeom = prst;
					}
				}
				case "noFill" -> {
					if (inShape && current != null) {
						if (inLn) {
							current.noStroke = true;
						} else {
							current.noFill = true;
						}
					}
				}
				case "schemeClr" -> {
					if (inShape && current != null && current.fillScheme == null && !current.noFill) {
						String val = attrs.getValue("val");
						if (val != null) current.fillScheme = val;
					}
				}
				case "gd" -> {
					// Adjust value (e.g., chevron indent depth)
					if (inShape && current != null && "adj".equals(attrs.getValue("name"))) {
						String fmla = attrs.getValue("fmla");
						if (fmla != null && fmla.startsWith("val ")) {
							try { current.adjValue = Integer.parseInt(fmla.substring(4).trim()); }
							catch (NumberFormatException e) { /* */ }
						}
					}
				}
				case "ln" -> {
					inLn = true;
					if (inShape && current != null) {
						String w = attrs.getValue("w");
						if (w != null) try { current.strokeWidth = Integer.parseInt(w); } catch (NumberFormatException e) { /* */ }
					}
				}
				case "txXfrm" -> inTxXfrm = true;
				case "txBody" -> inTxBody = true;
				case "t" -> {
					if (inTxBody) {
						inText = true;
						textBuf.setLength(0);
					}
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inText) textBuf.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String name = stripPrefix(qName);
			switch (name) {
				case "sp" -> {
					if (current != null && current.cx > 0 && current.cy > 0) {
						shapes.add(current);
					}
					current = null;
					inShape = false;
				}
				case "txBody" -> inTxBody = false;
				case "txXfrm" -> inTxXfrm = false;
				case "ln" -> inLn = false;
				case "t" -> {
					if (inText && current != null) {
						String t = textBuf.toString().trim();
						if (!t.isEmpty()) {
							current.text = (current.text == null) ? t : current.text + " " + t;
						}
					}
					inText = false;
				}
			}
		}

		private static String stripPrefix(String qName) {
			int idx = qName.indexOf(':');
			return idx >= 0 ? qName.substring(idx + 1) : qName;
		}

		private static int intAttr(Attributes attrs, String name) {
			String v = attrs.getValue(name);
			if (v == null) return 0;
			try { return Integer.parseInt(v); } catch (NumberFormatException e) { return 0; }
		}
	}
}
