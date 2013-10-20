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
package cc.clabs.stratosphere.mlp.contracts;

import cc.clabs.stratosphere.mlp.types.PactFormula;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.type.PactRecord;

/**
 *
 * @author rob
 */
public class DocumentProcessor extends MapStub {
        
    private final PactRecord target = new PactRecord();
   
    @Override
    public void map( PactRecord record, Collector<PactRecord> collector ) {
        
        WikiDocument doc = (WikiDocument) record.getField( 0, WikiDocument.class );
        
        // populate the list of known identifiers
        for (PactFormula pactFormula : doc.getFormulas()) {
            target.clear();
            target.setField( 0, pactFormula);
        	collector.collect(target);
        }

    }
}








