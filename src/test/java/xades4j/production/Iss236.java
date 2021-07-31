package xades4j.production;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.algorithms.XPathTransform;
import xades4j.properties.AllDataObjsCommitmentTypeProperty;
import xades4j.properties.CommitmentTypeProperty;
import xades4j.properties.DataObjectDesc;
import xades4j.properties.DataObjectFormatProperty;
import xades4j.properties.IndividualDataObjsTimeStampProperty;

public class Iss236 extends SignerTestBase
{
    @Test
    public void test() throws Exception
    {
        XadesSigner signer = new XadesBesSigningProfile(keyingProviderMy).newSigner();

        String ns = "http://test.xades4j/tracks";
        DataObjectDesc obj = new DataObjectReference("")
                .withTransform(new XPathTransform("not(ancestor-or-self::year)"))
                .withTransform(new XPathTransform("not(ancestor-or-self::t:tracks)").withNamespace("t", ns))
                .withDataObjectFormat(new DataObjectFormatProperty("text/xml"));

        // 1
        Document doc1 = getTestDocument();
        Node parent1 = doc1.getElementsByTagNameNS(ns, "tracks").item(0);

        signer.sign(new SignedDataObjects(obj), parent1);

        // 2
        Document doc2 = getTestDocument();
        Node parent2 = doc2.getElementsByTagNameNS(ns, "tracks").item(0);

        Node year = doc2.getElementsByTagName("year").item(0);
        year.setTextContent(String.valueOf(Math.random()));

        signer.sign(new SignedDataObjects(obj), parent2);

        // 3
        Document doc3 = getTestDocument();
        Node parent3 = doc3.getElementsByTagNameNS(ns, "tracks").item(0);

        Node artist = doc3.getElementsByTagName("artist").item(0);
        artist.setTextContent(String.valueOf(Math.random()));

        signer.sign(new SignedDataObjects(obj), parent3);

        // Asserts

        Element ref1 = (Element)doc1.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Reference").item(0);
        Assert.assertEquals("", ref1.getAttribute("URI"));
        Element ref2 = (Element)doc2.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Reference").item(0);
        Assert.assertEquals("", ref2.getAttribute("URI"));
        Element ref3 = (Element)doc3.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Reference").item(0);
        Assert.assertEquals("", ref3.getAttribute("URI"));

        Node digest1 = ref1.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "DigestValue").item(0);
        Node digest2 = ref2.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "DigestValue").item(0);
        Node digest3 = ref3.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "DigestValue").item(0);
        Assert.assertEquals(digest1.getTextContent(), digest2.getTextContent());
        Assert.assertNotEquals(digest1.getTextContent(), digest3.getTextContent());
    }
}

