package org.vast.stt.gui.widgets.symbolizer;

import org.vast.ows.sld.Color;
import org.vast.ows.sld.MappingFunction;
import org.vast.ows.sld.ScalarParameter;
import org.vast.ows.sld.Stroke;
import org.vast.ows.sld.VectorSymbolizer;


public class VectorOptionHelper //implements SelectionListener
{
    VectorSymbolizer symbolizer;
	//  Need a way to specify all options/types/args so 
	//  AdvancedOptions can use them also - just repeating
	//  code for now, and not using the labels/optTypes fields - TC
	//private String [] labels = {"Line Width:", "Line Color:"};
	//private int [] optTypes = { 0, 1}; 

	public VectorOptionHelper(VectorSymbolizer sym){
		symbolizer = sym;
	}
	
	public boolean getWidthConstant(){
		ScalarParameter widthSP = symbolizer.getStroke().getWidth();
		return widthSP.isConstant();
	}
	
	public void setWidthConstant(boolean b){
		ScalarParameter widthSP = symbolizer.getStroke().getWidth();
		widthSP.setConstant(b);
	}
	
	public String getWidthProperty(){
		ScalarParameter widthSP = symbolizer.getStroke().getWidth();
		return widthSP.getPropertyName();
	}
	
	public float getLineWidth(){
		ScalarParameter widthSP = symbolizer.getStroke().getWidth();
		if(widthSP == null)
			return 1.0f;
		Object widthCon = widthSP.getConstantValue();
		if(widthCon == null)
			return 1.0f;
		return ((Float)widthCon).floatValue();
	}
	
	public MappingFunction getWidthMappingFunction(){
		ScalarParameter widthSP = symbolizer.getStroke().getWidth();
		return widthSP.getMappingFunction();
	}
	
	public org.vast.ows.sld.Color getLineColor(){
		Color colorSP = symbolizer.getStroke().getColor();
		if(colorSP == null)
			return new Color(1.0f, 0.0f, 0.0f, 1.0f);
		return colorSP;
	}
	
	/**
	 * Convenience method to set line width
	 * @param w - width
	 */
	public void setLineWidth(float w){
		Stroke stroke = symbolizer.getStroke();
		ScalarParameter width = new ScalarParameter();
		width.setConstantValue(w);
		stroke.setWidth(width);
	}
	
	/**
	 * Convenience method to set line color
	 * @param Color
	 */
	public void setLineColor(Color color) {
		Stroke stroke = symbolizer.getStroke();
		stroke.setColor(color);
	}
}