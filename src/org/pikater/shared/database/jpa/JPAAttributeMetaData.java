package org.pikater.shared.database.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.pikater.shared.database.exceptions.NotUpdatableEntityException;

@Entity
@Table(name="AttributeMetaData")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class JPAAttributeMetaData extends JPAAbstractEntity implements Comparable<JPAAttributeMetaData>
{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
	
	private double ratioOfMissingValues;
	private boolean isTarget;
	private String name;
	private double classEntropy;
	private double entropy;
	private int attrOrder;

	public int getId() {
        return id;
    }
	
	public double getRatioOfMissingValues() {
		return ratioOfMissingValues;
	}

	public void setRatioOfMissingValues(double ratioOfMissingValues) {
		this.ratioOfMissingValues = ratioOfMissingValues;
	}

	public boolean isTarget() {
		return isTarget;
	}
	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public double getClassEntropy() {
		return classEntropy;
	}
	public void setClassEntropy(double classEntropy) {
		this.classEntropy = classEntropy;
	}

	public int getOrder() {
		return attrOrder;
	}

	public void setOrder(int order) {
		this.attrOrder = order;
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}

	@Override
	public void updateValues(JPAAbstractEntity newValues) throws NotUpdatableEntityException{
		throw new NotUpdatableEntityException();
	}
	
	/**
	 * Tests whether two attribute metadata entries are compatible for visualisation
	 * <p>
	 * Please note, that even this function returns true, doesn't mean that the result of
	 * visualisation should be attractive. 
	 * @param metadata Object of metadata for which we determine compatibility
	 * @return true if the objects can be visually compared
	 */
	public boolean isVisuallyCompatible(JPAAttributeMetaData metadata){
		if(metadata instanceof JPAAttributeNumericalMetaData){
			//pair of numerical metadata is always compatible
			//numerical is not compatible with categorical
			return this instanceof JPAAttributeNumericalMetaData;
		}else if (metadata instanceof JPAAttributeCategoricalMetaData){
			if(this instanceof JPAAttributeCategoricalMetaData){
				//two categoricals are compatible if having same category numbers
				return ((JPAAttributeCategoricalMetaData)this).getNumberOfCategories()
						==((JPAAttributeCategoricalMetaData)metadata).getNumberOfCategories();
			}else{
				//categorical is not compatible with other types (numerical)
				return false;
			}
		}else{
			return false;
		}
	}
	
	/*
	 * The code below is required in web:
	 */
	
	@Override
	public int compareTo(JPAAttributeMetaData o)
	{
		return getName().compareTo(o.getName());
	}
	
	//--------------------------------------------------------
	// INSTANCE COMPARING INTERFACE - generated with Eclipse
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JPAAttributeMetaData other = (JPAAttributeMetaData) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
