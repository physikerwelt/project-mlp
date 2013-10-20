/*        __
 *        \ \
 *   _   _ \ \  ______
 *  | | | | > \(  __  )
 *  | |_| |/ ^ \| || |
 *  | ._,_/_/ \_\_||_|
 *  | |
 *  |_|
 * 
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <rob ∂ CLABS dot CC> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.
 * ----------------------------------------------------------------------------
 */
package cc.clabs.stratosphere.mlp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;

/**
 * 
 * @author rob
 */
public class TexIdentifierExtractor {
	private static final Log LOG = LogFactory.getLog( TexIdentifierExtractor.class );

	/**
	 * list of false positive identifiers
	 */
	private final static List<String> blacklist = Arrays.asList("sin", "cos",
			"tan", "min", "max", "inf", "lim", "log", "exp", "sup", "lim sup",
			"lim inf", "arg", "dim", "cosh", "arccos", "arcsin", "arctan",
			"rank", "ln", "det", "ker", "sec", "cot", "csc", "tanh", "sinh",
			"coth", "cot", "⋯", ":", "'", "′", "…", "∞", "Λ", "⋮", " ", " ",
			"~", ";", "#", "π", "e", "⋱", "{", "}", "%", "?",

			// ignore identifier that are also english (stop-)words
			"a", "A", "i", "I",

			// ignore special chars
			"$", "\\");

	/**
	 * Returns a list of all identifers within a given formula. The formula is
	 * coded in TeX.
	 * 
	 * @param formula
	 *            TeX representation of a formula
	 * @param augmention
	 * @return list of identifiers
	 */
	public static ArrayList<String> getAll(String formula, boolean augmention) {
		// create vanilla SnuggleEngine and new SnuggleSession
		String xml;
		if (!augmention) {

        try {
        	
        	double start= System.nanoTime();
			SnuggleEngine engine = new SnuggleEngine();
			SnuggleSession session = engine.createSession();
            SnuggleInput input = new SnuggleInput( "$$ "+ formula  +" $$" );
            session.parseInput( input );
            xml = session.buildXMLString();
            double time_st =  (System.nanoTime()- start)/1000000;

            start= System.nanoTime();
            String mml = TeX2MML(formula);
            System.out.println( "Snuggle rendering time: \n\t"+time_st+"ms vs LaTeXML rendering time \n\t" + (System.nanoTime()- start)/1000000 + "ms" );
            LOG.info( "Snuggle rendering time: \n\t"+time_st+"µs vs LaTeXML rendering time \n\t" + (System.nanoTime()- start)/1000 + "µs" );
        }
        catch ( Exception e ) {
            return new ArrayList<String>();
        }
		} else {
			xml = formula;
		}
		return getIdentifiersFrom(xml);
	}

	/**
	 * Returns a list of unique identifiers from a MathML string. This function
	 * searches for all <mi/> or <ci/> tags within the string.
	 * 
	 * @param mathml
	 * @return a list of unique identifiers. When no identifiers were found, an
	 *         empty list will be returned.
	 */
	private static ArrayList<String> getIdentifiersFrom(String mathml) {
		ArrayList<String> list = new ArrayList<>();
		Pattern p = Pattern.compile("<([mc]i)(.*?)>(.*?)</\\1>", Pattern.DOTALL);
		Matcher m = p.matcher(mathml);
		while (m.find()) {
			String identifier = m.group(3);
			if (blacklist.contains(identifier))
				continue;
			if (list.contains(identifier))
				continue;
			list.add(identifier);
		}
		return list;
	}

	/**
	 * Returns a cleaned version of the TeX string.
	 * 
	 * @param tex
	 *            the TeX string
	 * @return the cleaned TeX string
	 */
	private static String cleanupTexString(String tex) {
		// strip text blocks
		tex = tex.replaceAll(
				"\\\\(text|math(:?bb|bf|cal|frak|it|sf|tt))\\{.*?\\}", "");
		// strip arbitrary operators
		tex = tex.replaceAll("\\\\operatorname\\{.*?\\}", "");
		// strip dim/log
		tex = tex.replaceAll("\\\\(dim|log)_(\\w+)", "$1");
		// strip "is element of" definitions
		tex = tex.replaceAll("^(.*?)\\\\in", "$1");
		// strip indices
		tex = tex.replaceAll("^([^\\s\\\\\\{\\}])_[^\\s\\\\\\{\\}]$", "$1");
		return tex;
	}
	private static String tex2json(String tex){
	    HttpClient client = new DefaultHttpClient();
	    //HttpPost post = new HttpPost("http://localhost/convert");
	    HttpPost post = new HttpPost("http://latexml.mathweb.org/convert");
	    try {
	      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	      nameValuePairs.add(new BasicNameValuePair("tex",
	          "$"+tex+"$"));
	      //WARNING: This does not produce pmml, since there is a xstl trasformation that rewrites the output and removes pmml
//	      nameValuePairs.add(new BasicNameValuePair("profile",
//		          "mwsquery"));
	      nameValuePairs.add(new BasicNameValuePair("preload",
	              "mws.sty"));
	      nameValuePairs.add(new BasicNameValuePair("profile",
          "math"));
	      nameValuePairs.add(new BasicNameValuePair("noplane1",
	              ""));
	      nameValuePairs.add(new BasicNameValuePair("whatsout",
	              "fragment"));	      
	      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	      HttpResponse response = client.execute(post);
	      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	      String line = "";
	      String result="";
	      while ((line = rd.readLine()) != null) {
	        result+=line;
	      }
	      return result;
	    } catch (IOException e) {
		      e.printStackTrace();
		    }
	    return "";
	}
	private static String tex2json2(String tex){
	    HttpClient client = new DefaultHttpClient();
	    //HttpPost post = new HttpPost("http://localhost/convert");
	    HttpPost post = new HttpPost("http://dopa-2:8010");
	    try {
	      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	      nameValuePairs.add(new BasicNameValuePair("tex",
	          tex));
	      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	      HttpResponse response = client.execute(post);
	      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	      String line = "";
	      String result="";
	      while ((line = rd.readLine()) != null) {
	        result+=line;
	      }
	      return result;
	    } catch (IOException e) {
		      e.printStackTrace();
		    }
	    return "";
	}
	private static String[] json2xml(String json){
		String[] result = {"","",""};
		try {
		JSONObject Ojson = (JSONObject) JSONSerializer.toJSON(json);
		//result[0]=(Ojson.getString("status"));
		result[1]=(Ojson.getString("log"));
		result[2]=(Ojson.getString("mml"));
		} catch (Exception e) {
		      e.printStackTrace();
		    }
		return result;
		}
	public static String TeX2MML(String tex) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException{
		final String XPath="//math/semantics/mrow";
		String mml="";
		return  json2xml(tex2json2(tex))[2];
	}
}
