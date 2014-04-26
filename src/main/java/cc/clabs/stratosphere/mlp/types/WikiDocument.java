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

import cc.clabs.stratosphere.mlp.utils.PlaintextDocumentBuilder;
import cc.clabs.stratosphere.mlp.utils.StringUtils;
import cc.clabs.stratosphere.mlp.utils.TexIdentifierExtractor;

import eu.stratosphere.pact.common.type.Value;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.mediawiki.core.MediaWikiLanguage;

/**
 * @author rob
 */
public class WikiDocument implements Value {
    
    private static final Log LOG = LogFactory.getLog( WikiDocument.class );

    
    /*
     * Raw raw of the document
     */
    private PactString raw = new PactString();
    
    /*
     * Plaintext version of the document
     */
    private PactString plaintext = new PactString();
    
    /*
     * Title of the document
     */
    private PactString title = new PactString();
    
    /*
     * Wikipedia id of the document
     */
    private PactInteger id = new PactInteger();
    
    /**
     * Wikipedia pages belong to different namespaces. Below
     * is a list that describes a commonly used namespaces.
     * 
     *  -2	Media
     *  -1	Special
     *  0	Default
     *  1	Talk
     *  2	User
     *  3	User talk
     *  4	Wikipedia
     *  5	Wikipedia talk
     *  6	File
     *  7	File talk
     *  8	MediaWiki
     *  9	MediaWiki talk
     *  10	Template
     *  11	Template talk
     *  12	Help
     *  13	Help talk
     *  14	Category
     *  15	Category talk
     *  100	Portal
     *  101	Portal talk
     *  108	Book
     *  109	Book talk
     */
    private PactInteger ns = new PactInteger();
    
    /*
     * Holds all formulas found within the document. The key of
     * the HashMap is the replacement string in the document and
     * the value contains the TeX String
     */
    private PactFormulaList formulas = new PactFormulaList();
    
    /*
     * Stores all unique identifiers found in this document
     */
    private PactIdentifiers knownIdentifiers = new PactIdentifiers();
    
    /**
     * Returns a plaintext version of this document.
     * 
     * @return a plaintext string
     */
    public String getPlainText() {
        StringWriter writer = new StringWriter();
        MarkupParser parser = new MarkupParser();
        MarkupLanguage wiki = new MediaWikiLanguage();
        parser.setMarkupLanguage( wiki );
        parser.setBuilder( new PlaintextDocumentBuilder( writer ) );
        parser.parse( raw.getValue() );
        plaintext.setValue( writer.toString() );
        return plaintext.getValue();
    }

    @Override
    public void write( DataOutput out ) throws IOException {
        id.write( out );
        ns.write( out );
        title.write( out );
        raw.write( out );
        plaintext.write( out );
        formulas.write( out );
        knownIdentifiers.write( out );
    }

    @Override
    public void read( DataInput in ) throws IOException {
        id.read( in );
        ns.read( in );
        title.read( in );
        raw.read( in );
        plaintext.read( in );
        formulas.read( in );
        knownIdentifiers.read( in );
    }
    
    /**
     * Returns the document id.
     * 
     * @return id of the document
     */
    public int getId() {
        return id.getValue();
    }
    
    /**
     * Returns the document title.
     * 
     * @return title of the document
     */
    public String getTitle() {
        return title.getValue();
    }
    
    
    /**
     * Sets the id of the document
     * @param id
     */
    public void setId( Integer id ) {
        this.id.setValue( id );
    }

    /**
     * Sets the title of the document
     * @param title 
     */
    public void setTitle( String title ) {
        this.title.setValue( title );
    }

    /**
     * Returns the namespace id of the document.
     * 
     * @return namespace id
     */
    public int getNS() {
        return ns.getValue();
    }

    /**
     * Sets the namespace of the document.
     * 
     * @param ns 
     */
    public void setNS( int ns ) {
        this.ns.setValue( ns );
    }

    /**
     * Returns the raw raw body of the document.
     * 
     * @return the raw body
     */
    public String getText() {
        return raw.getValue();
    }
    
    /**
     * Sets the raw body of the document.
     * 
     * @param raw 
     */
    public void setText( String text ) {
        this.raw.setValue( StringUtils.unescapeEntities( text ) );
        this.replaceMathTags();
    }
    
    
    /**
     * Helper that replaces all math tags from the document
     * and stores them in a list. Math tags that contain exactly
     * on identifier will be replaced in line with the identifier.
     */
    private void replaceMathTags() {
        Pattern p = Pattern.compile( "<math(.*?)>(.*?)</math>", Pattern.DOTALL );
        Matcher m;
        String  formula, text = raw.getValue();
        try{
        while ( (m = p.matcher( text )).find() ) {
            formula = m.group( 2 ).trim();
            PactFormula pFormula = new PactFormula( formula ); 
            formulas.add( pFormula);
            text = m.replaceFirst( pFormula.getHash());                
        }}catch(Exception e){
        	
        }
        raw.setValue( text );
    }
    
    /**
     * Returns the formula list of all found and replaced formulas.
     * 
     * @return a list of all formulas
     */
    public PactFormulaList getFormulas() {
        return formulas;
    }

    /**
     * Returns a list of all found unique identifiers within
     * this document.
     * 
     * @return a list of unique identifiers
     */
    public PactIdentifiers getKnownIdentifiers() {
        return knownIdentifiers;
    }
    
}
