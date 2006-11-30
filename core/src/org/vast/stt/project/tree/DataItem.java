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

package org.vast.stt.project.tree;

import java.util.*;

import org.vast.ows.sld.Symbolizer;
import org.vast.stt.event.STTEvent;
import org.vast.stt.event.STTEventListener;
import org.vast.stt.event.STTEventListeners;
import org.vast.stt.event.STTEventProducer;
import org.vast.stt.project.feedback.UserAction;
import org.vast.stt.provider.DataProvider;


/**
 * <p><b>Title:</b><br/>
 * DataItem
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Object representing a renderable item in a scene.
 * A DataItem is connected to a DataProvider and also stores
 * info about how the data should be represented/rendered though
 * the use of Symbolizers.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 3, 2005
 * @version 1.0
 */
public abstract class DataItem implements DataEntry, STTEventListener, STTEventProducer
{
	protected String name;
	protected boolean enabled = true;
	protected Hashtable<String, Object> options;
	protected DataProvider dataProvider;
    protected STTEventListeners listeners;    
    protected ArrayList<UserAction> actions;
    
    
    public DataItem()
	{
        listeners = new STTEventListeners(2);        
        actions = new ArrayList<UserAction>(1);
	}
    
    
    public abstract List<Symbolizer> getSymbolizers(); 
	

	public String getName()
	{
		return name;
	}


	public void setName(String name)
	{
		this.name = name;
	}


	public DataProvider getDataProvider()
	{
		return dataProvider;
	}


	public void setDataProvider(DataProvider dataProvider)
	{
        if (this.dataProvider != dataProvider)
        {
            if (this.dataProvider != null)
                this.dataProvider.removeListener(this);
            
            this.dataProvider = dataProvider;
            
            if (this.dataProvider != null)
                this.dataProvider.addListener(this);
        }
	}


	public boolean isEnabled()
	{
		return enabled;
	}


	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
        
        if (dataProvider != null)
            dataProvider.setEnabled(enabled);
	}
    
    
    public boolean hasEvent()
    {
        return !actions.isEmpty();
    }
    
    
    public List<UserAction> getActions()
    {
        return actions;
    }


	public Hashtable<String, Object> getOptions()
	{
		return options;
	}


	public void setOptions(Hashtable<String, Object> options)
	{
		this.options = options;
	}


    public void addListener(STTEventListener listener)
    {
        listeners.add(listener);        
    }


    public void removeListener(STTEventListener listener)
    {
        listeners.remove(listener);        
    }


    public void removeAllListeners()
    {
        listeners.clear();        
    }
    
    
    public boolean hasListeners()
    {
        return !listeners.isEmpty();
    }


    public void dispatchEvent(STTEvent event)
    {
        if (enabled)
        {
            event.producer = this;
            listeners.dispatchEvent(event);
        }
    }
    
    
    public void handleEvent(STTEvent event)
    {
        switch(event.type)
        {
            case PROVIDER_DATA_CHANGED:
            case PROVIDER_DATA_CLEARED:
            case PROVIDER_DATA_REMOVED:
                dispatchEvent(event.copy());
                break;
                
            case PROVIDER_ERROR:
                this.enabled = false;
                dispatchEvent(event.copy());
                break;
        }        
    }
    
    
    public String toString()
    {
        return this.name + " - " + super.toString();
    }
}
