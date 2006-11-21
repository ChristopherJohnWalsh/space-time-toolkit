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

import java.util.Hashtable;
import org.vast.data.AbstractDataBlock;
import org.vast.data.DataIndexer;
import org.vast.ows.sld.Symbolizer;
import org.vast.stt.data.BlockList;
import org.vast.stt.data.BlockListItem;
import org.vast.stt.data.BlockListIterator;
import org.vast.stt.data.DataNode;
import org.vast.stt.project.tree.DataItem;
import org.vast.stt.project.world.Projection;
import org.vast.stt.project.world.Projection.Crs;
import org.vast.stt.provider.STTSpatialExtent;


/**
 * <p><b>Title:</b><br/>
 * Abstract Styler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Abstract base class for all stylers.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 15, 2005
 * @version 1.0
 */
public abstract class AbstractStyler implements DataStyler
{
    protected class ListInfo
    {
        protected BlockListIterator blockIterator;
        protected DataIndexer blockIndexer;
        protected int indexOffset = 0; // when a data index is along the list dimension itself
        
        public ListInfo(BlockList blockList, DataIndexer dataIndexer)
        {
            this.blockIndexer = dataIndexer;
            this.blockIterator = blockList.getIterator();
        }
    }
    
    
    protected DataItem dataItem;
    protected DataNode dataNode;
    protected Hashtable<String, IndexerTreeBuilder> treeBuilders;
    protected ListInfo[] dataLists;
    protected Projection projection;
    protected Crs geometryCrs;
    protected boolean computeExtent = true;
    protected int[] indexList = new int[3];
    protected STTSpatialExtent bbox;
    
    
    
    public abstract void setSymbolizer(Symbolizer symbolizer);
    public abstract void updateDataMappings();
    protected abstract void computeBoundingBox();
        
    
    public AbstractStyler()
    {
        treeBuilders = new Hashtable<String, IndexerTreeBuilder>();
        dataLists = new ListInfo[0];
        geometryCrs = Crs.EPSG4329;
        bbox = new STTSpatialExtent();
    }
    
    
    public DataItem getDataItem()
    {
        return dataItem;
    }
    
        
    public void setDataItem(DataItem dataItem)
	{
		this.dataItem = dataItem;
	}
    
    
    public Projection getProjection()
    {
        return this.projection;
    }
    
    
    public void setProjection(Projection projection)
    {
        this.projection = projection;
        computeExtent = true;
    }
    
    
    /**
     * Adds a property mapper at the right spot in the indexer tree
     * This method will create a new ListInfo if not created yet
     * @param componentPath
     * @param newMapper
     */
    public void addPropertyMapper(String componentPath, PropertyMapper newMapper)
    {
        // extract list name from component path
        String listName = componentPath;
        if (componentPath.indexOf('/') != -1)
            listName = componentPath.substring(0, componentPath.indexOf('/'));
        
        // retrieve previously created builder or create a new one
        IndexerTreeBuilder builder = treeBuilders.get(listName);
        if (builder == null)
        {
            BlockList list = dataNode.getList(listName);
            if (list == null) return;
            builder = new IndexerTreeBuilder(list.getBlockStructure());
            treeBuilders.put(listName, builder);
            builder.addVisitor(componentPath, newMapper);
                       
            // resize indexer array
            ListInfo[] oldDataLists = dataLists;
            dataLists = new ListInfo[oldDataLists.length + 1];
            System.arraycopy(oldDataLists, 0, dataLists, 0, oldDataLists.length);
            dataLists[oldDataLists.length] = new ListInfo(list, builder.getRootIndexer());
        }
        else
        {
            builder.addVisitor(componentPath, newMapper);
        }
    }
    
    
    /**
     * Reset the block counter used for the block iterator
     */
    public void resetIterators()
    {
        // reset all list iterators
        for (int i = 0; i < dataLists.length; i++)
        {
            ListInfo info = dataLists[i];
            info.blockIterator.reset();
        }
    }
    
    
    protected void clearAllMappers()
    {
        dataLists = new ListInfo[0];
        treeBuilders.clear();
    }
    
    
    protected boolean checkTime(double time)
    {
        if (Double.isNaN(time))
            return true;
        else
        {
            return false;
        }
    }
    
    
    protected void prepareBlock(BlockListItem block)
    {

    }
    
    
    protected void clearBlockData()
    {
        for (int i = 0; i < dataLists.length; i++)
            dataLists[i].blockIndexer.clearData();
    }
    
    
    /**
     * Load data for next block so that it is updated
     * for all array mappers. Also update block property mappers.
     */
    public BlockListItem nextBlock()
    {
        BlockListItem nextItem = null;
        
        for (int i = 0; i < dataLists.length; i++)
        {
            ListInfo info = dataLists[i]; 
            DataIndexer nextIndexer = info.blockIndexer;
                        
            if (!info.blockIterator.hasNext())
            {
                clearBlockData();
                return null;
            }
            
            nextItem = info.blockIterator.next();
            AbstractDataBlock nextBlock = nextItem.getData();
            nextIndexer.reset();
            nextIndexer.setData(nextBlock);

            prepareBlock(nextItem);
        }           
                        
        return nextItem;
    }
    
    
    /**
     * Skips the specified number of blocks from the lists
     */
    public void skipBlocks(int blockCount)
    {
        for (int i = 0; i < dataLists.length; i++)
        {
            ListInfo info = dataLists[i]; 
            
            int count = 0;
            while (info.blockIterator.hasNext() && count < blockCount)
            {
                info.blockIterator.next();
                count++;
            }
        }
    }
    
    
    public boolean hasMoreBlocks()
    {
        for (int i = 0; i < dataLists.length; i++)
        {
            if (!dataLists[i].blockIterator.hasNext())
                return false;
        }
        
        return true;
    }
    
    
    protected void addToExtent(PrimitiveGraphic point)
    {
        bbox.resizeToContain(point.x, point.y, point.z);
    }
    
    
    public void resetBoundingBox()
    {
        computeExtent = true;
    }
    
   
    public STTSpatialExtent getBoundingBox()
    {      
        if (computeExtent)
        {
            bbox.nullify();
            this.computeBoundingBox();
            computeExtent = false;
        }
            
        return bbox;
    }
}