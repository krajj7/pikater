/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pikater.core.agents.system.metadata;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.GTest;
import org.pikater.core.ontology.subtrees.attribute.Attribute;
import org.pikater.core.ontology.subtrees.attribute.Instance;
import org.pikater.core.ontology.subtrees.datainstance.DataInstances;
import org.pikater.core.ontology.subtrees.metadata.Metadata;
import org.pikater.core.ontology.subtrees.metadata.attributes.*;

/**
 * Class that can read metadata from the list of instances and attributes
 * @author Kuba
 */
public class MetadataReader {
    private double defValue = 0;
    
    /**
     * Constructor
     */
	public Metadata computeMetadata(DataInstances data) {
		
		Metadata metadata = new Metadata();		
							
        // number of instances
		metadata.setNumberOfInstances(data.getInstances().size());
		
		// number of attributes
		// we do not count targer attribute
		metadata.setNumberOfAttributes(data.getAttributes().size());

		// missing values
		boolean missing = false; 
		for (Instance instanceI : data.getInstances()) {
			
			if (!instanceI.getMissing().isEmpty()) {
				missing = true;
			}			
		}		
		metadata.setMissingValues(missing);
		
		// data type
		String type = "";
		for (Attribute attributeI : data.getAttributes()) {
			if (type.isEmpty()) {
				type = attributeI.getType();
			}
			if (!attributeI.getType().equals(type)) {
				type = "Multivariate";
			}
		}		
		metadata.setAttributeType(type);
		
		// default task 
        setTaskType(data, metadata);

        //Attributes
        readAttributesMetadata(data,metadata);  
        
        //int c=metadata.getNumberOfCategorical();
        //int r=metadata.getNumberOfReal();
        //int i=metadata.getNumberOfInteger();
        return metadata;
    }
    
    private void setTaskType(DataInstances data, Metadata metadata) {
    	
    	int index;
    	if (data.getClassIndex() >= 0) {
    		index = data.getClassIndex();
    	} else {
    		index = data.getAttributes().size() - 1;
    	}
    	
        if (data.getAttributes().get(index).getType().equals("Numeric")) {
            metadata.setDefaultTask("Regression");
        } else {
            metadata.setDefaultTask("Classification");
        }
    }
    
    private void readAttributesMetadata(DataInstances data,
    		Metadata metadata) {
    	
    	List<AttributeMetadata> attributeList =
    			metadata.getAttributeMetadataList();
    	
        for (int i = 0; i < metadata.getNumberOfAttributes(); i++) {
            AttributeMetadata attMet = readAttributeMetadata(data, i);
            attributeList.add(attMet);
        }
    }
    
    private AttributeMetadata readAttributeMetadata(DataInstances data,
    		int attributeNumber) {
    	
        AttributeType type = getAttributeType(data, attributeNumber);
        AttributeMetadata attributeMetadata = getAttributeMetadataInstance(type);
        setBaseAttributeProperties(data, attributeMetadata, attributeNumber);
        
        if (type!=AttributeType.Categorical) {
        	setNumericalAttributeProperties(data, attributeMetadata,
        			attributeNumber);
        } else {
        	setCategoricalAttributeProperties(data, attributeMetadata,
        			attributeNumber);
        }
        if (type != AttributeType.Real) {
        	countEntropies(data, attributeMetadata, attributeNumber);
        }
        return attributeMetadata;
    }
    
    private void setBaseAttributeProperties(DataInstances data,
    		AttributeMetadata metadata, int attributeNumber) {
    	
        Attribute att = (Attribute)data.getAttributes().get(attributeNumber);
        metadata.setOrder(attributeNumber);  
        metadata.setName(att.getName());
        
        if (attributeNumber == data.getClassIndex()) {
        	metadata.setIsTarget(true);
        }
        setRatioMissingValues(data, attributeNumber, metadata);
    }
    
    /**
     * Counts entropy for the {@link DataInstances}
     */
    private void countEntropies(DataInstances data,
    		AttributeMetadata metadata, int attributeNumber){
    	
        List<Object> values = new ArrayList<Object>();     
        List<Object> classValues = new ArrayList<Object>();        

		for (Instance instanceI : data.getInstances()) {
		List<Boolean> missingList = instanceI.getMissing();
		
            if ((boolean)missingList.get(attributeNumber)) {
                continue;
            }
            values.add(instanceI.getValues().get(attributeNumber));
            classValues.add(instanceI.getValues().get(data.getClassIndex()));
        }
        double entropy = Entropy.countEntropy(values);
        metadata.setEntropy(entropy);        
        double classEntropy =
        		Entropy.countEntropyClassAttribute(values, classValues);
        metadata.setAttributeClassEntropy(classEntropy);
    }
    
    @SuppressWarnings("unchecked")
	private <TReturn> List<TReturn> getAttributeData(
    		DataInstances data,
    		int attributeNumber)
    		{
    	List<TReturn> values=new ArrayList<TReturn>();       
        
		for (Instance instanceI : data.getInstances()) {
		List<Boolean> missingList = instanceI.getMissing();
            if ((boolean)missingList.get(attributeNumber)) {
                continue;
            }
            values.add((TReturn)instanceI.getValues().get(attributeNumber));
        }
		
		return values;
    }
    
    private void setCategoricalAttributeProperties(DataInstances data,
    		AttributeMetadata metadata, int attributeNumber) {
    	
        Attribute att = (Attribute)data.getAttributes().get(attributeNumber);
        CategoricalAttributeMetadata met = (CategoricalAttributeMetadata)metadata;
        
        int categoryCount = att.getValues().size();
        
        met.setNumberOfCategories(categoryCount);
        
        
        List<Double> values=getAttributeData(data, attributeNumber);
        
        
        Map<Integer,Integer> groupMap =new HashMap<Integer,Integer>();
        
        for(Double value : values){
        	
        	int key = value.intValue();
        	
        	if(groupMap.containsKey(key)){
        		int count = groupMap.get(key);
        		groupMap.put(key, count+1);
        	}else{
        		groupMap.put(key, 1);
        	}
        }
        
        Object[] observeda = groupMap.values().toArray();
        
        
        long[] observed =  new long[observeda.length];
        for(int i=0;i<observed.length;i++){
        	observed[i] = ((Integer)observeda[i]).longValue();
        }
        
        
        double[] expected = new double[categoryCount];
        for(int i=0;i<expected.length;i++){
        	expected[i] = values.size() / (double)categoryCount;
        }
        
        ChiSquareTest cst = new ChiSquareTest();
        try{
        	double chiSquareValue = cst.chiSquare(expected, observed);
        	met.setChiSquare(chiSquareValue);
        }catch(
        		NotPositiveException|
        		NotStrictlyPositiveException|
        		DimensionMismatchException ex){
        	met.setChiSquare(Double.NaN);
        }
        
        try{
        	double chiSquareTestValue = cst.chiSquareTest(expected, observed);
        	met.setChiSquareTest(chiSquareTestValue);
        }catch(
        		NotPositiveException|
        		NotStrictlyPositiveException|
        		DimensionMismatchException|
        		MaxCountExceededException ex)
        {
        	met.setChiSquareTest(Double.NaN);
        }
        
        GTest gt=new GTest();
        
        try{
        	double gTestValue = gt.gTest(expected, observed);
        	met.setGTest(gTestValue);
        }catch(
        		NotPositiveException|
        		NotStrictlyPositiveException|
        		DimensionMismatchException|
        		MaxCountExceededException ex)
        {
        	met.setGTest(Double.NaN);
        }
        
    }
    
    private void setNumericalAttributeProperties(DataInstances data,
    		AttributeMetadata metadata, int attributeNumber) {
        List<Double> values = getAttributeData(data, attributeNumber);      
        NumericalAttributeMetadata met=(NumericalAttributeMetadata)metadata;
		
        Collections.sort(values);
        if (!values.isEmpty()) {
        	met.setMin(values.get(0));
        } else {
        	met.setMin(defValue);
        }
        
        if (!values.isEmpty()) {
        	met.setMax(values.get(values.size()-1));
        } else {
        	met.setMax(defValue);
        }
        double average = 0;
        double squareaverage = 0;
        double n = values.size();
        
        for (int i=0;i<values.size();i++) {
            double currentvalue=values.get(i);
            average+=(currentvalue/n);
            squareaverage+=((currentvalue*currentvalue)/n);
        }
        double variation=squareaverage-(average*average);
        met.setAvg(average);
        met.setStandardDeviation(Math.sqrt(variation));
        met.setVariation(variation);
        
        double mean = average;
        met.setMean(mean);
        
        int iq1 = values.size()/4;//index of first quartil
        double q1 = values.get(iq1);
        met.setQ1(q1);
        int iq2 = values.size()/2;//index of second quartil
        double q2 = values.get(iq2);
        met.setQ2(q2);
        int iq3 =3*values.size()/4;//index of third quartil
        double q3=values.get(iq3);
        met.setQ3(q3);
        
        int half=(int)Math.floor(values.size()/2.0);
        
        if (!values.isEmpty()) {
        	met.setMedian(values.get(half));
        } else {
        	met.setMedian(defValue);
        }
    }
    
    private void setRatioMissingValues(DataInstances data,
    		int attributeNumber, AttributeMetadata attributeMetadata) {
    	
        double numberOfValues = data.getInstances().size();
        double numberOfMissingValues=0;

		for (Instance instanceI : data.getInstances() ) {
			List<Boolean> missingList = instanceI.getMissing();

			if ((boolean)missingList.get(attributeNumber)) {
                numberOfMissingValues++;
            }
        }
		if (numberOfValues > 0) {
	      attributeMetadata.setRatioOfMissingValues(
	    		  numberOfMissingValues/numberOfValues);
	    }
    }  
    
    private AttributeMetadata getAttributeMetadataInstance(
    		AttributeType type) {
    	
        switch (type) {
            case Categorical: 
                return new CategoricalAttributeMetadata();
            case Integer: 
                return new IntegerAttributeMetadata();
            default: return new RealAttributeMetadata();
        }
    }
    
    private AttributeType getAttributeType(DataInstances data,
    		int attributeNumber) {
    	
    	Attribute attribute = (Attribute)
        		data.getAttributes().get(attributeNumber);
        
        if (attribute.getType().equals("NOMINAL")) {
            return AttributeType.Categorical;
        }
        boolean canBeInt = true;
		for (Instance instanceI : data.getInstances()) {
			List<Boolean> missingList = instanceI.getMissing();
			
            if ((boolean)missingList.get(attributeNumber)) {
                continue;
            } else {
            	List<Double> values = instanceI.getValues();
                Object value = values.get(attributeNumber);                            
                if (value instanceof Double) {
                    double doub=(double)value;
                    if (!((doub == Math.floor(doub)) &&
                    		!Double.isInfinite(doub))) {
                        canBeInt= false;
                    }
                } else {
                    return AttributeType.Categorical;
                }
            }
        }
		
		if (canBeInt) {
			return AttributeType.Integer;
		} else {
			return AttributeType.Real;
		}
		
    }
}
