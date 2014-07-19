package org.pikater.core.ontology.subtrees.search.searchItems;

import jade.content.Concept;
import org.pikater.core.ontology.subtrees.newOption.values.interfaces.IValueData;

import java.util.List;
import java.util.Random;


public abstract class SearchItem implements Concept {
	/**
	 * Item in solution-schema
	 */
	private static final long serialVersionUID = 3249399049389780447L;
	private Integer number_of_values_to_try;
	//Create random solution item
	public IValueData randomValue(Random rnd_gen)
    {
        List<IValueData>   possibleValues=possibleValues();
        return possibleValues.get(rnd_gen.nextInt(possibleValues.size()));
    }
	//Returns all possible values from this schema
	public abstract List<IValueData> possibleValues();
	public Integer getNumber_of_values_to_try() {
		return number_of_values_to_try;
	}
	public void setNumber_of_values_to_try(Integer numberOfValuesToTry) {
		number_of_values_to_try = numberOfValuesToTry;
	}
}
