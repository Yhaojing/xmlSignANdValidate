package xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Collections;

/**
 * Created by haoj on 2017/9/7.
 */
public class XmlSignAndValidate {
    // 签名
    public static String sign(InputStream in) throws Exception {
        // 1.创建XMLSignatureFactory实例
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
        // 转换方法使用的是ENVELOPED,这样对整个文档进行引用并生成摘要值的时候<Signature>元素不会被计算在内
        Transform envelopedTransform = signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec)null);
        DigestMethod sha1DigMethod = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
        // 创建reference时,将uri指定为""时,表示对整个XML文档进行引用
        Reference reference = signatureFactory.newReference("", sha1DigMethod,
                Collections.singletonList(envelopedTransform), null, null);
        // 创建SignedInfo元素 指定规范化方法以确定被最终处理的数据INCLUSIVE_WITH_COMMENTS,
        // 表示在规范化 XML 内容的时候会将 XML 注释也包含在内。
        CanonicalizationMethod canonicalizationMethod = signatureFactory.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec)null);
        SignatureMethod method = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
        SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalizationMethod, method,
                Collections.singletonList(reference));
        // 以上都是对XML进行规范化转换,进行摘要的过程,以下是使用私钥仅仅数字签名
        // 使用java.security 包生成 DSA 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // 以公钥创建<KeyValue>元素
        KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
        KeyValue keyValue = keyInfoFactory.newKeyValue(keyPair.getPublic());
        // 根据KeyValue 创建KeyInfo
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));
        // 使用私钥创建Signature元素
        XMLSignature signature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
        // 使用signture 中的sign(XMLSignContext signContext)方法进行签名
        //

        Document doc = XmlUtils.getDocument(in);
        DOMSignContext dsc = new DOMSignContext(keyPair.getPrivate(), doc.getDocumentElement());
        // 以上都是为了创建生成签名所需数据XML表示,并没有真正的签名
        // 生成签名
        signature.sign(dsc);

        // 签名后 使用JAXP 的xml转换接口将签名后的XML文档输出,查看签名结果
        return XmlUtils.documentToString(doc);
    }
    // 验证
    public static boolean validate(InputStream inputStream) throws Exception{
        Document document = XmlUtils.getDocument(inputStream);
        // 获取规范化的摘要数据
        NodeList n1 = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if(n1.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }
        Node signatureNode = n1.item(0);
        XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = factory.unmarshalXMLSignature(new DOMStructure(signatureNode));
        // 获取 publicKey
        KeyValue keyValue = (KeyValue) signature.getKeyInfo().getContent().get(0);
        PublicKey publicKey = keyValue.getPublicKey();
        // 指定上下文信息
        DOMValidateContext validateContext = new DOMValidateContext(publicKey, signatureNode);
        // 验证签名
        return signature.validate(validateContext);
    }

}
