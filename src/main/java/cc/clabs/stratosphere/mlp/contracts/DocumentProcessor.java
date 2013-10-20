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

import com.formulasearchengine.mathoid.benchmark.FormulaRecordFields;

import cc.clabs.stratosphere.mlp.types.PactFormula;
import cc.clabs.stratosphere.mlp.types.WikiDocument;
import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.MapStub;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

/**
 *
 * @author rob
 */
public class DocumentProcessor extends MapStub {
        
    private final PactRecord target = new PactRecord();
    //TODO: Do we have PactHash or PactByteArray
    private final PactString hash = new PactString();
    private final PactInteger count = new PactInteger(1);

    @Override
    public void map( PactRecord record, Collector<PactRecord> collector ) {
        
        WikiDocument doc = (WikiDocument) record.getField( 0, WikiDocument.class );
        
        // populate the list of formulae
        for (PactFormula pactFormula : doc.getFormulas()) {
            target.clear();
            hash.setValue(pactFormula.getHash() );
            target.setField(FormulaRecordFields.HASH.ordinal(), hash);
            target.setField( FormulaRecordFields.FORMULAE.ordinal(), pactFormula);
            target.setField( FormulaRecordFields.COUNT.ordinal(), count);
        	collector.collect(target);
        }

    }
}








