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

import org.vast.data.AbstractDataBlock;
import org.vast.ows.sld.GridSymbolizer;
import org.vast.ows.sld.ScalarParameter;
import org.vast.stt.data.BlockListItem;


/**
 * <p><b>Title:</b><br/>
 * Grid Styler
 * </p>
 *
 * <p><b>Description:</b><br/>
 * 
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 15, 2005
 * @version 1.0
 */
public abstract class AbstractGridStyler extends AbstractStyler
{
    protected GridSymbolizer symbolizer;
    protected GridPatchGraphic patch;
    protected GridPointGraphic point;    
    
	
	public AbstractGridStyler()
	{
        patch = new GridPatchGraphic();
        point = new GridPointGraphic();
	}
    
    
    public GridPatchGraphic nextPatch()
    {
        ListInfo listInfo = dataLists[0];
        
        // if no more items in the list, just return null
        if (!listInfo.blockIterator.hasNext())
        {
            clearBlockData();
            return null;
        }
        
        // otherwise get the next item
        BlockListItem nextItem = listInfo.blockIterator.next();
        
        // TODO implement block filtering here
        
        // setup indexer with new data 
        AbstractDataBlock nextBlock = nextItem.getData();
        listInfo.blockIndexer.setData(nextBlock);
        listInfo.blockIndexer.reset();
        listInfo.blockIndexer.next(); // to make sure we visit array size data
        
        // see what's needed on this block
        prepareBlock(nextItem);
        
        // copy current item in the patch object
        BlockListItem prevItem = patch.block;
        patch.block = nextItem;
        
        // compute grid and tex sizes in case of continuous texture map
        if (dataLists[0].indexOffset >= 0)
        {
            // if previous block had a blockCount, increase blockOffset
            if (prevItem != null && prevItem.blockCount != 0)
                blockOffset += prevItem.blockCount - 1;
            
            // if this is the last data chunk that has not been processed yet
            // get all the data left (or max 256) and render it
            if (patch.block.blockCount == 0)
            {
                int newBlockCount = listInfo.blockIterator.getList().getSize() - blockOffset;
                if (newBlockCount < 2)
                    return null;
                patch.block.blockCount = Math.min(newBlockCount, 256);
            }
            
            // assign block count to patch length
            patch.length = patch.block.blockCount;
            
            // skip blocks to start of next tile
            this.skipBlocks(patch.length - 2);
        }
        
        return patch;
    }
    
    
    public GridPointGraphic getPoint(int u, int v)
    {
        point.x = point.y = point.z = 0.0;
        indexList[0] = u;
        indexList[1] = v;        

        dataLists[0].getData(indexList);
        
        // adjust geometry to fit projection
        projection.adjust(geometryCrs, point);
        
        return point;
    }
    
    
    public GridPointGraphic nextPoint()
    {
        if (dataLists[0].blockIndexer.hasNext())
        {
            point.x = point.y = point.z = 0.0;            
            dataLists[0].blockIndexer.next();
            
            // adjust geometry to fit projection
            projection.adjust(geometryCrs, point);
            
            return point;
        }
        
        return null;
    }
    
    
    @Override
    public void computeBoundingBox()
    {
        this.resetIterators();
        PrimitiveGraphic point;
        
        while (nextPatch() != null)
            while ((point = nextPoint()) != null)
                addToExtent(point);
    }


    @Override
	public void updateDataMappings()
	{
        ScalarParameter param;
        String propertyName = null;
        
        // reset all parameters
        patch = new GridPatchGraphic();
        point = new GridPointGraphic();
        this.clearAllMappers();
        
        // grid width array
        propertyName = this.symbolizer.getDimensions().get("width");
        if (propertyName != null)
        {
            addPropertyMapper(propertyName, new GridWidthMapper(0, patch, null));
        }
        
        // grid length array
        propertyName = this.symbolizer.getDimensions().get("length");
        if (propertyName != null)
        {
            if (propertyName.equals("/"))
                dataLists[0].indexOffset = 1;
            else    
                addPropertyMapper(propertyName, new GridLengthMapper(1, patch, null));
        }
        
        // grid depth array
        propertyName = this.symbolizer.getDimensions().get("depth");
        if (propertyName != null)
        {
            addPropertyMapper(propertyName, new GridDepthMapper(2, patch, null));
        }
        
        // geometry breaks
        param = this.symbolizer.getGeometry().getBreaks();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericBreakMapper(point, param.getMappingFunction()));               
            }
        }
        
        // geometry X
        param = this.symbolizer.getGeometry().getX();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericXMapper(point, param.getMappingFunction()));
            }
        }
        
        //geometry Y
        param = this.symbolizer.getGeometry().getY();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericYMapper(point, param.getMappingFunction()));
            }
        }
        
        // geometry Z
        param = this.symbolizer.getGeometry().getZ();
        if (param != null)
        {
            propertyName = param.getPropertyName();
            if (propertyName != null)
            {
                addPropertyMapper(propertyName, new GenericZMapper(point, param.getMappingFunction()));
            }
        }
	}


    @Override
    public void resetIterators()
    {
        super.resetIterators();
        this.blockOffset = 0;
        this.patch.block = null;
    }
}
