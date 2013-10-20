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
package cc.clabs.stratosphere.mlp.types;

import eu.stratosphere.pact.common.type.Value;
import eu.stratosphere.pact.common.type.base.PactDouble;
import eu.stratosphere.pact.common.type.base.PactString;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

/**
 *
 * @author rob
 */
public class PactFormula implements Value {
	static HttpClient client = new DefaultHttpClient();
	static Runtime runtime = Runtime.getRuntime();
    /*
     * 
     */
    private PactString hash = new PactString();
    /*
     * 
     */
    private PactString src = new PactString();   
    private PactDouble time = new PactDouble();
    private PactString mml = new PactString();
    
    /**
     * default constructor
     * @see eu.stratosphere.nephele.io.IOReadableWritable
     */
    public PactFormula() { }
    
    
    /**
     * 
     * @param src 
     */
    public PactFormula( final String src ) {
    	String key = DigestUtils.md5Hex(src);
        this.hash.setValue( key );
        this.src.setValue( src );
        try {
			this.TeX2MML(src);
		} catch (XPathExpressionException | ParserConfigurationException
				| SAXException | IOException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
    }
    
    /**
     * Returns the string representation of the hash.
     * 
     * @return the string representation of the hash
     */
    public String getHash() {
        return hash.getValue();
    }
    
    /**
     * Sets the value of the hash.
     * 
     * @param string the string representation of the hash
     */
    public final void setHash( final String string ) {
        hash.setValue( string );
    }
    
    /**
     * Returns the source of the formula.
     * 
     * @return source of the formula (e.g. latex)
     */
    public String getSrc() {
        return src.getValue();
    }
    
    /**
     * Sets the source to a given string value.
     * 
     * @param string a given string value
     */
    public final void setSrc( final String string ) {
        src.setValue( string );
    }

    @Override
    public void write( final DataOutput out ) throws IOException {
        hash.write( out );
        src.write( out );
        time.write(out);
    }

    @Override
    public void read( final DataInput in ) throws IOException {
        hash.read( in );
        src.read( in );
        time.read(in);
    }
    
    @Override
    public String toString() {
        return this.getHash() + "," + this.getTime();
    }
	private static String tex2json2(String tex){
	    
	    HttpPost post = new HttpPost("http://localhost:8010");
	    try {
	      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	      nameValuePairs.add(new BasicNameValuePair("tex",
	          tex));
	      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	      HttpResponse response = client.execute(post);
	      post = null;
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
		result[0]=(Ojson.getString("sucess"));
		result[1]=(Ojson.getString("log"));
		result[2]=(Ojson.getString("mml"));
		} catch (Exception e) {
		      e.printStackTrace();
		    }
		return result;
		}
	public void TeX2MML(String tex) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException{
        double time_st =  System.nanoTime();
		this.mml.setValue( json2xml(tex2json2(tex))[2]);
		this.time.setValue( (System.nanoTime()- time_st)/1000000);
	}


	public String getTime() {
		// TODO Auto-generated method stub
		return this.time.toString();
	}
	private String CheckFormula(){
		try {
			Process p = runtime.exec("/home/dopa-user/ms/Math/texvccheck/texvccheck '" + this.getSrc() + "'" );
			p.waitFor();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while(line != null){
				line = reader.readLine();
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
