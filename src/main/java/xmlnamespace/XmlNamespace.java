package xmlnamespace;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlNamespace {

	public static void main(String[] args) {
		{
			Path xml1 = Paths.get("D:\\workspace\\eclipse_photon\\xmlnamespace\\test1.xml");
			Path xml2 = Paths.get("D:\\workspace\\eclipse_photon\\xmlnamespace\\test3.xml");
			reorderNSfile(xml1, xml2);
		}
		{
			Path xml1 = Paths.get("D:\\workspace\\eclipse_photon\\xmlnamespace\\test2.xml");
			Path xml2 = Paths.get("D:\\workspace\\eclipse_photon\\xmlnamespace\\test4.xml");
			reorderNSfile(xml1, xml2);
		}
	}

	private static void reorderNSfile(Path xml1, Path xml2) {
		final SAXBuilder builder = new SAXBuilder();
		try {
			final Document doc = builder.build(xml1.toFile());
			reorderNS(doc);

			XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(xml2.toFile()));

		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	private static void reorderNS(Document doc) {
		final Element root = doc.getRootElement();

		List<Namespace> currentNS = root.getNamespacesIntroduced().stream().collect(Collectors.toList());
		currentNS.sort((x, y) -> x.getURI().compareTo(y.getURI()));
//		currentNS.sort(new Comparator<Namespace>() {
//			@Override
//			public int compare(Namespace o1, Namespace o2) {
//				return o1.getURI().compareTo(o2.getURI());
//			}
//		});

		List<Namespace> newNS = new LinkedList<Namespace>();
		int i = 0;
		for (Namespace ns : currentNS) {
			if (i == 0) {
				newNS.add(Namespace.getNamespace(ns.getURI()));
			} else {
				newNS.add(Namespace.getNamespace(MessageFormat.format("ns{0}", i), ns.getURI()));
			}
			i++;
		}

		Map<String, Namespace> newNSmap = newNS.stream().collect(Collectors.toMap(x -> x.getURI(), x -> x));

		currentNS.forEach(x -> root.removeNamespaceDeclaration(x));

		recurseNS(newNSmap, root);

		for (Namespace ns : newNS) {
			if (!ns.getURI().equals(root.getNamespace().getURI())) {
				root.addNamespaceDeclaration(ns);
			}
		}
	}

	private static void recurseNS(Map<String, Namespace> newNSmap, Element el) {
		if (el.hasAttributes()) {
			for (Attribute attr : el.getAttributes()) {
				attr.setNamespace(newNSmap.get(attr.getNamespace().getURI()));
			}
		}
		el.setNamespace(newNSmap.get(el.getNamespace().getURI()));
		if (el.getChildren().size() > 0) {
			for (Element child : el.getChildren()) {
				recurseNS(newNSmap, child);
			}
		}
	}

}
