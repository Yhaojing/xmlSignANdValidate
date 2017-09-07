package xml;

import com.sun.javadoc.Doc;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * xml工具类
 * Created by haoj on 2017/9/7.
 */
public class XmlUtils {
    public static Document getDocument(InputStream file) throws Exception{
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true); // 没有这句会出问题
        return documentBuilderFactory.newDocumentBuilder().parse(file);
    }

    /**
     * 根据输入了String转换为Document
     * @param string
     * @return
     * @throws Exception
     */
    public static Document getDocument(String string) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        StringReader stringReader = new StringReader(string);
        // InputSource xml的单一输入源
        InputSource inputSource = new InputSource(stringReader);
        return  documentBuilderFactory.newDocumentBuilder().parse(inputSource);
    }

    public static String documentToString(Document document) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty("encoding", "UTF-8");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(outputStream));
        return outputStream.toString();
    }

    public static void documentToFile(Document document, String filePath) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(filePath)));
    }
}
