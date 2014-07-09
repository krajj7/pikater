package org.pikater.core.ontology.subtrees.newOption.typedValue;

import jade.content.Concept;

public interface ITypedValue extends Concept {

	public String exportToWeka();
	public ITypedValue cloneValue();

    //TODO: Stepan - override to string for each derived type - needed by weka
}
