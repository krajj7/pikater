package org.pikater.shared.experiment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pikater.core.ontology.subtrees.batchDescription.FileDataSaver;
import org.pikater.core.ontology.subtrees.batchDescription.examples.SearchOnly;
import org.pikater.core.ontology.subtrees.newOption.base.NewOption;
import org.pikater.shared.XStreamHelper;
import org.pikater.shared.logging.web.PikaterLogger;
import org.pikater.shared.util.SimpleIDGenerator;

public class UniversalComputationDescription
{
	/**
	 * Top-level options for this computation.
	 */
	private final Set<NewOption> globalOptions;
	
	/**
	 * Tree of ComputingDescription. Ontology elements wrapped in
	 * UniversalElement Set. Contains only FileDataSavers.
	 */
	private final Set<UniversalElement> rootElements;

	/**
	 * Contains all elements added to this computation.
	 */
	private final Set<UniversalElement> allElements;
	
	/**
	 * ID generator for {@link UniversalOntology} instances within {@link UniversalElement}.
	 */
	private final SimpleIDGenerator idGenerator;

	public UniversalComputationDescription()
	{
		this.globalOptions = new HashSet<NewOption>();
		this.rootElements = new HashSet<UniversalElement>();
		this.allElements = new HashSet<UniversalElement>();
		this.idGenerator = new SimpleIDGenerator();
	}
	
	// ----------------------------------------------------------
	// SOME BASIC INTERFACE
	
	public Set<NewOption> getGlobalOptions()
	{
		return globalOptions;
	}

	public void addGlobalOptions(NewOption... options)
	{
		this.globalOptions.addAll(Arrays.asList(options));
	}
	
	public void addGlobalOptions(Set<NewOption> options) {
		this.globalOptions.addAll(options);
	}
	
	public Set<UniversalElement> getRootElements()
	{
		return rootElements;
	}
	
	public Set<UniversalElement> getAllElements()
	{
		return allElements;
	}

	public void addElement(UniversalElement element)
	{
		if(!element.isOntologyDefined())
		{
			throw new IllegalArgumentException("The given element didn't have ontology defined.");
		}
		else
		{
			element.getOntologyInfo().setId(idGenerator.getAndIncrement());
			
			//if (BoxType.fromOntologyClass(element.getOntologyInfo().getOntologyClass()) == BoxType.OUTPUT)
			if (element.getOntologyInfo().getOntologyClass().equals(FileDataSaver.class))
			{
				rootElements.add(element);
			}
			allElements.add(element);
		}
	}
	
	/**
	 * Can this experiment be shown in the experiment editor? In other words,
	 * can it be converted to the web format?
	 */
	public boolean isGUICompatible()
	{
		for (UniversalElement elementI : this.allElements)
		{
			if (elementI.getGuiInfo() == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Determines whether this experiment is ready to be scheduled and should not
	 * end up with needless errors.
	 * @return
	 */
	public boolean isValid()
	{
		for(UniversalElement rootElem : rootElements)
		{
			if(!rootElem.getOntologyInfo().getOntologyClass().equals(FileDataSaver.class))
			{
				PikaterLogger.logThrowable("Invalid element processing", new IllegalStateException("Some root elements are not file savers."));
				return false;
			}
		}
		for(UniversalElement element : allElements)
		{
			for(UniversalConnector connector : element.getOntologyInfo().getInputDataSlots())
			{
				if(connector.getFromElement() == null)
				{
					PikaterLogger.logThrowable("Required information not defined", new IllegalStateException("Some connectors don't have the 'fromElement' field defined."));
					return false;
				}
				else if(!allElements.contains(connector.getFromElement()))
				{
					PikaterLogger.logThrowable("Invalid element binding", new IllegalStateException("Some connectors' 'fromElement' fields are not registered in the root class."));
					return false;
				}
				else
				{
					try
					{
						connector.validate();
					}
					catch (Throwable t)
					{
						PikaterLogger.logThrowable("Invalid connector", t);
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public String toXML()
	{
		return XStreamHelper.serializeToXML(this, 
        		XStreamHelper.getSerializerWithProcessedAnnotations(UniversalComputationDescription.class));
	}
	
	public static UniversalComputationDescription fromXML(String xml)
	{
		return XStreamHelper.deserializeFromXML(UniversalComputationDescription.class, xml, 
        		XStreamHelper.getSerializerWithProcessedAnnotations(UniversalComputationDescription.class));
	}
	
	public static void main(String[] args)
	{
		UniversalComputationDescription uDescription = SearchOnly.createDescription().exportUniversalComputationDescription();
		System.out.println(XStreamHelper.serializeToXML(uDescription, 
				XStreamHelper.getSerializerWithProcessedAnnotations(UniversalComputationDescription.class)));
	}
}