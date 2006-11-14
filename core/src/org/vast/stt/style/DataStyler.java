/***************************************************************
 (c) Copyright 2005, University of Alabama in Huntsville (UAH)
 ALL RIGHTS RESERVED

 This software is the property of UAH.
 It cannot be duplicated, used, or distributed without the
 express written consent of UAH.

 This software developed by the Vis Analysis Systems Technology
 (VAST) within the Earth System Science Lab under the direction
 of Mike Botts (mike.botts@atmos.uah.edu)
 ***************************************************************/

package org.vast.stt.style;

import org.vast.ows.sld.Symbolizer;
import org.vast.stt.project.scene.Projection;
import org.vast.stt.project.tree.DataItem;
import org.vast.stt.provider.STTSpatialExtent;


/**
 * <p><b>Title:</b><br/>
 * Data Styler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * A styler transforms data into graphic objects (point, line, polygon, etc...).
 * This is done using the style info provided by the corresponding symbolizer.
 * Stylers are renderer independent and contains helper logic to find the data
 * within the data component structure and process data with mapping functions
 * when indicated by the symbolizer.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 21, 2005
 * @version 1.0
 */
public interface DataStyler
{

	public void setSymbolizer(Symbolizer sym);


	public Symbolizer getSymbolizer();


    public DataItem getDataItem();
    
    
	public void setDataItem(DataItem dataItem);
    
    
    public void setProjection(Projection projection);
    
    
    public Projection getProjection();

	
	public void updateDataMappings();
    
    
    public void skipBlocks(int blockCount);
    
    
    public boolean hasMoreBlocks();
    
    
    public void resetBoundingBox();
	
	
	public STTSpatialExtent getBoundingBox();
    
    
    public void accept(StylerVisitor visitor);
    
}