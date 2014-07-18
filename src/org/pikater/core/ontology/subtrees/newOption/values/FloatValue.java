package org.pikater.core.ontology.subtrees.newOption.values;

import org.pikater.core.ontology.subtrees.newOption.values.interfaces.IComparableValueData;
import org.pikater.core.ontology.subtrees.newOption.values.interfaces.IValueData;

public class FloatValue implements IComparableValueData
{
	private static final long serialVersionUID = -6154392916809467193L;
	
	private float value;
	
	/**
	 * Should only be used by JADE.
	 */
	@Deprecated
	public FloatValue() {}
	public FloatValue(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	
	@Override
	public Float hackValue()
	{
		return value;
	}
	
	@Override
	public IValueData clone()
	{
		return new FloatValue(value);
	}

	@Override
	public String exportToWeka() {
		
		return String.valueOf(value);
	}
	
	@Override
	public String toDisplayName()
	{
		return "Float";
	}
	@Override
	public int compareTo(IComparableValueData o)
	{
		return hackValue().compareTo((Float) o.hackValue());
	}
}