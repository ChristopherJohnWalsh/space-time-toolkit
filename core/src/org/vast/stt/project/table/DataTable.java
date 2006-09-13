/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the
 University of Alabama in Huntsville (UAH).
 Portions created by the Initial Developer are Copyright (C) 2006
 the Initial Developer. All Rights Reserved.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.stt.project.table;

import org.vast.stt.event.STTEvent;
import org.vast.stt.project.tree.DataItem;


/**
 * <p><b>Title:</b>
 * Data Table
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Special Type of DataItem used to describe a table type display
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 12, 2006
 * @version 1.0
 */
public class DataTable extends DataItem
{
    protected int rowHeight;
    
    
    public DataTable()
    {
        super();
    }
    

    public int getRowHeight()
    {
        return rowHeight;
    }


    public void setRowHeight(int rowHeight)
    {
        this.rowHeight = rowHeight;
    }
    
    
    @Override
    public void handleEvent(STTEvent event)
    {
        switch(event.type)
        {
            case PROVIDER_DATA_CHANGED:
            case PROVIDER_DATA_CLEARED:
            case PROVIDER_DATA_REMOVED:
            case PROVIDER_TIME_EXTENT_CHANGED:
            case PROVIDER_SPATIAL_EXTENT_CHANGED:
                dispatchEvent(event.copy());
                break;
                
            case PROVIDER_ERROR:
                this.enabled = false;
                dispatchEvent(event.copy());
                break;
        }        
    }
}