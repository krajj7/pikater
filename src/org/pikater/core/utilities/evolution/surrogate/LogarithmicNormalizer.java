/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pikater.core.utilities.evolution.surrogate;

import org.pikater.core.ontology.subtrees.newOption.typedValue.FloatValue;
import org.pikater.core.ontology.subtrees.newOption.typedValue.ITypedValue;
import org.pikater.core.ontology.subtrees.newOption.typedValue.IntegerValue;
import org.pikater.core.ontology.subtrees.search.searchItems.IntervalSearchItem;

/**
 *
 * @author Martin Pilat
 */
public class LogarithmicNormalizer extends ModelInputNormalizer{

    @Override
    public double normalizeFloat(ITypedValue dbl, IntervalSearchItem schema) {
        double val = ((FloatValue) dbl).getValue();
        val -= ((FloatValue) schema.getMin()).getValue();
        val += 1.0;
        return Math.log(val);
    }

    @Override
    public double normalizeInt(ITypedValue n, IntervalSearchItem schema) {
        double val = ((IntegerValue) n).getValue();
        val -= ((IntegerValue) schema.getMin()).getValue();
        val += 1.0;
        return Math.log(val);
    }
    
}
