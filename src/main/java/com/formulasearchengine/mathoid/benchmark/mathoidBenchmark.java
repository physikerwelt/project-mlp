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
package com.formulasearchengine.mathoid.benchmark;

import cc.clabs.stratosphere.mlp.contracts.*;
import cc.clabs.stratosphere.mlp.io.*;
import cc.clabs.stratosphere.mlp.types.*;
import eu.stratosphere.pact.common.contract.*;
import eu.stratosphere.pact.common.io.RecordOutputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.type.base.PactInteger;

public class mathoidBenchmark implements PlanAssembler, PlanAssemblerDescription {

    /**
    * {@inheritDoc}
    */
    @Override
    public Plan getPlan( String... args ) {
        // parse job parameters
        String dataset = args[0];
        String output = args[1];
        
        FileDataSource source = new FileDataSource( WikiDocumentEmitter.class, dataset, "Dumps" );
        
        MapContract doc = MapContract
                .builder( DocumentProcessor.class )
                .name( "Processing Documents" )
                .input( source )
                .build();
        ReduceContract formulae = ReduceContract
        		.builder(GroupFormulae.class)
        		.name( "Removing duplicates" )
        		.input(doc)
        		.build();
        FileDataSink out = new FileDataSink( RecordOutputFormat.class, output, doc, "Output" );
        RecordOutputFormat.configureRecordFormat( out )
                .recordDelimiter( '\n' )
                .fieldDelimiter( '\t' )
                .field( PactFormula.class, 1 )
                .field( PactInteger.class, 2);
                
        Plan plan = new Plan( out, "Relation Finder" );
        
        plan.setDefaultParallelism(32);
        return plan;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getDescription() {
        return "Parameters: [DATASET] [OUTPUT]";
    }
    
}
