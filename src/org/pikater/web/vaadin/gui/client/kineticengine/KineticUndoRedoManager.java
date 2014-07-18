package org.pikater.web.vaadin.gui.client.kineticengine;

import java.util.Stack;

import org.pikater.web.vaadin.gui.client.kineticcomponent.KineticComponentWidget;
import org.pikater.web.vaadin.gui.client.kineticengine.operations.base.BiDiOperation;

/**
 * TODO: options editing makes this incomplete... we have to merge all changes
 * to be registered either on the server or the client.
 */
public class KineticUndoRedoManager
{
	private final KineticComponentWidget widget;
	private final Stack<BiDiOperation> undoStack; 
	private final Stack<BiDiOperation> redoStack;
	
	public KineticUndoRedoManager(KineticComponentWidget widget)
	{
		this.widget = widget;
		this.undoStack = new Stack<BiDiOperation>();
		this.redoStack = new Stack<BiDiOperation>();
	}
	
	public void loadHistory(KineticUndoRedoManager history)
	{
		if(history != null)
		{
			if(!undoStack.isEmpty() || !redoStack.isEmpty())
			{
				throw new IllegalStateException("Can not load history if there is already an alternative newer version.");
			}
			else
			{
				this.undoStack.addAll(history.undoStack);
				this.redoStack.addAll(history.redoStack);
				
				alterStateIf(!undoStack.isEmpty());
			}
		}
	}
	
	public void clear()
	{
		undoStack.clear();
		redoStack.clear();
		
		alterStateIf(undoStack.isEmpty());
	}
	
	public void push(BiDiOperation operation)
	{
		alterStateIf(undoStack.isEmpty());
		
		undoStack.push(operation);
		redoStack.clear();
	}
	
	public void undo()
	{
		if(!undoStack.isEmpty())
		{
			BiDiOperation op = undoStack.pop();
			redoStack.push(op);
			op.undo();
			
			alterStateIf(undoStack.isEmpty());
		}
	}
	
	public void undoAndDiscard()
	{
		if(!undoStack.isEmpty())
		{
			undoStack.pop().undo();
			
			alterStateIf(undoStack.isEmpty());
		}
	}
	
	public void redo()
	{
		alterStateIf(undoStack.isEmpty());
		
		if(!redoStack.isEmpty())
		{
			BiDiOperation op = redoStack.pop();
			undoStack.push(op);
			op.redo();
		}
	}
	
	private void alterStateIf(boolean condition)
	{
		if(condition)
		{
			widget.command_setExperimentModified(!condition);
		}
	}
}