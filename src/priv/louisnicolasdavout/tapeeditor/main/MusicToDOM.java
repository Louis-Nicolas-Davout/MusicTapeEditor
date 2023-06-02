package priv.louisnicolasdavout.tapeeditor.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MusicToDOM {
	private TransformerFactory transformerFactory;
	private DocumentBuilderFactory documentBuilderFactory;

	public MusicToDOM() {
		this.transformerFactory = TransformerFactory.newInstance();
		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
	}

	public void writeXML(Document document, File saveFile)
			throws FileNotFoundException, IOException, TransformerException {
		Transformer transformer = this.transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		DOMSource source = new DOMSource();
		source.setNode(document);
		StreamResult result = new StreamResult();
		try (OutputStream os = new FileOutputStream(saveFile)) {
			result.setOutputStream(os);
			transformer.transform(source, result);
		}
	}

	public Document parseMusic(Music music, String toneMark, String barDuration) throws ParserConfigurationException {
		DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element root = document.createElement("lnd:musical_score");
		root.setAttribute("musical_tone", toneMark);
		root.setAttribute("bar_duration", barDuration);
		int barIndex = 1;
		for (int i = 0; i < music.getLinesCount(); i++) {
			MusicLine musicLine = music.getLine(i);
			for (int j = 0; j < musicLine.getBarsCount(); j++) {
				root.appendChild(this.parseMusicBar(document, musicLine.getBar(j), barIndex));
				barIndex++;
			}
		}
		document.appendChild(root);
		document.normalizeDocument();
		document.setXmlStandalone(true);
		return document;
	}

	private Element parseMusicBar(Document document, MusicBar musicBar, int barIndex) {
		Element elem = document.createElement("lnd:musical_bar");
		elem.setAttribute("description", Integer.toString(barIndex));
		for (int i = 0; i < musicBar.getPartsCount(); i++) {
			elem.appendChild(this.parseMusicPart(document, musicBar.getPart(i)));
		}
		return elem;
	}

	private Element parseMusicPart(Document document, MusicPart musicPart) {
		Element elem = document.createElement("lnd:musical_part");
		for (int i = 0; i < musicPart.getNotesCount(); i++) {
			elem.appendChild(this.parseMusicNote(document, musicPart.getNote(i)));
		}
		return elem;
	}

	private Element parseMusicNote(Document document, MusicNote musicNote) {
		Element elem = document.createElement("lnd:musical_note");
		elem.setAttribute("pitch", this.getFullNote(musicNote.getPitch(), musicNote.isSharp(), musicNote.isSilent()));
		elem.setAttribute("duration", this.parseDuration(musicNote.getUnderline(), musicNote.isDot()));
		return elem;
	}

	private String parseDuration(Underline underline, boolean dot) {
		String[][] durations = new String[][] {
				// NONE SINGLE DOUBLE
				new String[] { "1", "0.5", "0.25" }, // false
				new String[] { "1.5", "0.75", "0.375" } // true
		};
		return durations[dot ? 1 : 0][underline.ordinal()];
	}

	private String getFullNote(Pitch pitch, boolean sharp, boolean silent) {
		if (silent) {
			return "0";
		}
		if (!sharp) {
			return pitch.getTitle();
		}
		StringBuilder stringBuilder = new StringBuilder("#");
		stringBuilder.append(pitch.getTitle());
		return stringBuilder.toString();
	}
}
