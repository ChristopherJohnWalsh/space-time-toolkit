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

package org.vast.stt.data;

import org.vast.stt.project.Resource;
import org.vast.stt.util.SpatialExtent;
import org.vast.stt.util.TimeExtent;


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
public interface DataProvider
{

	public void updateData() throws DataException;
	
	
	public boolean isUpdating();
	
	
	public void cancelUpdate();
	
	
	public void clearData();


	public DataNode getDataNode();
	
	
	public void setDataNode(DataNode dataNode);


	public Resource getResource();


	public void setResource(Resource resource);
	
	
	public SpatialExtent getSpatialExtent();
	
	
	public void setSpatialExtent(SpatialExtent spatialExtent);
	
  
	public TimeExtent getTimeExtent();
	
	
	public void setTimeExtent(TimeExtent timeExtent);
}