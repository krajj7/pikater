package org.pikater.shared.experiment.webformat;

public interface IBoxInfoCommon<I extends Object>
{
	I getID();
	void setID(I id);
	int getPosX();
	void setPosX(int posX);
	int getPosY();
	void setPosY(int posY);
}