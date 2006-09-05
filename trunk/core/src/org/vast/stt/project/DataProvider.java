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

package org.vast.stt.project;

import org.vast.stt.data.DataException;
import org.vast.stt.data.DataNode;
import org.vast.stt.event.STTEventListener;
import org.vast.stt.event.STTEventProducer;


/**
 * <p><b>Title:</b><br/>
 * Data Provider
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Common interface for all data providers.
 * This allows updating and access to the DataNode, associating the
 * Provider to a given resource (file or service), as well as providing
 * feedback and control over the update operation.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 21, 2005
 * @version 1.0
 */
public interface DataProvider extends Resource, STTEventListener, STTEventProducer
{
    
    public void startUpdate(boolean force);
    
    
    public void cancelUpdate();
    
    
    public void init() throws DataException;
    
    
    public void updateData() throws DataException;
	
	
	public void clearData();
    
    
	public boolean isUpdating();
    
    
    public boolean hasError();
    
    
    public void setError(boolean error);
    
    
    public void setAutoUpdate(boolean autoUpdate);
    
    
    public boolean getAutoUpdate();


	public DataNode getDataNode();
	
	
	public STTSpatialExtent getSpatialExtent();
	
	
	public void setSpatialExtent(STTSpatialExtent spatialExtent);
	
  
	public STTTimeExtent getTimeExtent();
	
	
	public void setTimeExtent(STTTimeExtent timeExtent);
	
	
	public STTSpatialExtent getMaxSpatialExtent();
	
	
	public STTTimeExtent getMaxTimeExtent();
	
	
	public boolean isSpatialSubsetSupported();
	
	
	public boolean isTimeSubsetSupported();
}