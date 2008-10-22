/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "Space Time Toolkit".
 
 The Initial Developer of the Original Code is the VAST team at the
 University of Alabama in Huntsville (UAH). <http://vast.uah.edu>
 Portions created by the Initial Developer are Copyright (C) 2007
 the Initial Developer. All Rights Reserved.
 
 Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.stt.data;

import org.vast.cdm.common.DataComponent;
import org.vast.data.AbstractDataBlock;


/**
 * <p><b>Title:</b><br/>
 * Block List
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO BlockList type description
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Apr 1, 2006
 * @version 1.0
 */
public class BlockList
{
    protected int size = 0;
    protected DataComponent blockStructure;
    protected BlockListItem firstItem;
    protected BlockListItem lastItem;
    protected BlockListItem currentItem;
    protected int currentIndex = -1;
    //protected BlockListItem[] fastAccessBlocks; // use if random access needed
    
    
    public BlockList()
    {
        this.clear();        
    }
    
    
    public BlockList copy()
    {
        BlockList newList = new BlockList();
        newList.blockStructure = this.blockStructure.copy();
        return newList;
    }
    
    
    public BlockListIterator getIterator()
    {
        return new BlockListIterator(this);
    }
    
    
    public void clear()
    {
        firstItem = null;
        lastItem = null;
        currentItem = null;
        currentIndex = -1;
        size = 0;
    }
    
    
    public void remove(BlockListItem item)
    {
        boolean endItem = false;
        
        if (item.nextItem == null && item.prevItem == null)
            return;
        
        // if item is first in list
        if (item == firstItem)
        {
            firstItem = item.nextItem;
            if (firstItem != null)
                firstItem.prevItem = null;
            endItem = true;
        }
        
        // if item is last in list
        if (item == lastItem)
        {
            lastItem = item.prevItem;
            if (lastItem != null)
                lastItem.nextItem = null;
            endItem = true;
        }
        
        // if item is in middle of list
        if (!endItem)
        {
            // connect previous to next
            item.prevItem.nextItem = item.nextItem;
            item.nextItem.prevItem = item.prevItem;
        }
        
        // set both prev and next to null
        item.nextItem = null;
        item.prevItem = null;
        
        // reduce list size
        size--;        
    }
    
    
    public boolean contains(BlockListItem item)
    {
    	if (item.nextItem != null || item.prevItem != null)
            return true;
    	
    	if (item == firstItem)
    		return true;
    	
    	return false;
    }
    
    
    public BlockListItem addBlock(AbstractDataBlock dataBlock)
    {
        BlockListItem newItem = new BlockListItem(dataBlock, null, null);
        add(newItem);        
        return newItem;
    }
    
    
    public void add(BlockListItem newItem)
    {
        this.remove(newItem);
        
        newItem.prevItem = lastItem;
        newItem.nextItem = null;
        
        if (lastItem != null)
            lastItem.nextItem = newItem;
        
        if (firstItem == null)
            firstItem = newItem;
        
        lastItem = newItem;
        
        size++;
    }
    
    
    public AbstractDataBlock get(int index)
    {
        BlockListItem item = null;
        
        int distCurrent = index - currentIndex;
        int distFirst = index;
        //int distLast = size - 1 - index;
            
        if (currentIndex != -1 && Math.abs(distCurrent) <= distFirst)
        {
            item = currentItem;
            
            if (distCurrent >= 0)
            {
                while (item.nextItem != null && currentIndex != index)
                {
                    item = item.nextItem;
                    currentIndex++;
                }
            }
            else
            {
                while (item.prevItem != null && currentIndex != index)
                {
                    item = item.prevItem;
                    currentIndex--;
                }
            }
        }
        else
        {
            item = firstItem;
            int i;
            for (i=0; i<index && item.nextItem != null; i++)
                item = item.nextItem;
            currentIndex = i;
        }
        
        currentItem = item;
        return item.data;
    }
    
    
    public BlockListItem getLastItem()
    {
        return lastItem;
    }
    
    public BlockListItem getCurrentItem()
    {
        return currentItem;
    }
    
    
    public BlockListItem getFirstItem()
    {
        return firstItem;
    }
    
    
    public int getSize()
    {
        return size;
    }


    public DataComponent getBlockStructure()
    {
        return blockStructure;
    }


    public void setBlockStructure(DataComponent blockStructure)
    {
        this.blockStructure = blockStructure;
    }
}
