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

import org.vast.data.AbstractDataBlock;

/**
 * <p><b>Title:</b><br/>
 * Block List Item
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO BlockListItem type description
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Apr 1, 2006
 * @version 1.0
 */
public class BlockListItem
{
    //protected BlockInfo info;
    protected AbstractDataBlock data;    
    protected BlockListItem nextItem;
    protected BlockListItem prevItem;
    protected boolean hidden;
    
    
    public BlockListItem(AbstractDataBlock data, BlockListItem prevBlock, BlockListItem nextBlock)
    {
        this.data = data;
        this.prevItem = prevBlock;
        this.nextItem = nextBlock;
        
        if (prevBlock != null)
            prevBlock.nextItem = this;
        
        if (nextBlock != null)
            nextBlock.prevItem = this;
    }
    

    public AbstractDataBlock getData()
    {
        return data;
    }


    public void setData(AbstractDataBlock data)
    {
        this.data = data;
    }


//    public BlockInfo getInfo()
//    {
//        if (info == null)
//            info = new BlockInfo();
//        
//        return info;
//    }
//
//
//    public void setInfo(BlockInfo info)
//    {
//        this.info = info;
//    }
    
    
    public boolean isHidden()
    {
        return hidden;
    }


    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }
    
    
    public boolean isLinked()
    {
        return (nextItem != null || prevItem != null);
    }
}