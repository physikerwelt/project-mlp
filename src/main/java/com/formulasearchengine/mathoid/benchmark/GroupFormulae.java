package com.formulasearchengine.mathoid.benchmark;

import java.util.Iterator;

import eu.stratosphere.pact.common.stubs.Collector;
import eu.stratosphere.pact.common.stubs.ReduceStub;
import eu.stratosphere.pact.common.type.PactRecord;
import eu.stratosphere.pact.common.type.base.PactInteger;

	public class GroupFormulae extends ReduceStub {

		private final PactInteger count = new PactInteger();

		/**
		 * Counts the ones for each word and emits a (word, sum) record for
		 * each word.
		 */
		@Override
		public void reduce(Iterator<PactRecord> records, Collector<PactRecord> out) throws Exception {
			PactRecord current = null;

			int sum = 0;
			while (records.hasNext()) {
				current = records.next();
				sum += current.getField(2, PactInteger.class).getValue();
			}

			// output: (word, sum) record
			// note: the current record has the word already as first field (index 0).
			// therefore we only set the second field (index 1) to the sum.
			this.count.setValue(sum);
			current.setField(2, this.count);
			out.collect(current);
		}

	}
