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
package cc.clabs.stratosphere.mlp.io;

import cc.clabs.stratosphere.mlp.types.WikiDocument;
import cc.clabs.stratosphere.mlp.utils.StringUtils;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.common.io.TextInputFormat;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactString;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author rob
 */
public class WikiDocumentEmitter extends TextInputFormat  {
     private static final Log LOG = LogFactory.getLog( WikiDocumentEmitter.class );

    @Override
    public void configure (Configuration parameter) {
        parameter.setString(DEFAULT_CHARSET_NAME, "UTF-8");
        super.configure( parameter );
        this.delimiter =  "</page>".getBytes();
    }
    
    /* (non-Javadoc)
     * @see eu.stratosphere.pact.common.io.DelimitedInputFormat#readRecord(eu.stratosphere.pact.common.type.PactRecord, byte[], int)
     */
    @Override
    public boolean readRecord(PactRecord target, byte[] bytes, int offset, int numBytes) {
        LOG.info("Offset:"+offset+"numBytes:"+numBytes);
     
        super.readRecord( target, bytes, offset, numBytes );
        String content = target.getField( 0, PactString.class ).getValue();
        
        // search for a page-xml entity
        Pattern pageRegex = Pattern.compile( "(?:<page>\\s+)(?:<title>)(.*?)(?:</title>)\\s+(?:<ns>)(.*?)(?:</ns>)\\s+(?:<id>)(.*?)(?:</id>)(?:.*?)(?:<text.*?>)(.*?)(?:</text>)", Pattern.DOTALL );
        Matcher m = pageRegex.matcher( content );
        // if the record does not contain parsable page-xml
        if ( !m.find() ) return false;
        
        // otherwise create a WikiDocument object from the xml
        WikiDocument doc = new WikiDocument();
        doc.setId( Integer.parseInt( m.group( 3 ) ) );
        doc.setTitle( m.group( 1 ) );
        doc.setNS( Integer.parseInt( m.group( 2 ) ) );
        doc.setText( StringUtils.unescapeEntities( m.group( 4 ) ) );
        
        // skip docs from namespaces other than
        if ( doc.getNS() != 0 ) return false;
        
        target.setField( 0, doc );
        return true;
    }

}