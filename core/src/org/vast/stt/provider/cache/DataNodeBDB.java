/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "Space Time Toolkit Cache Engine".
  
 The Initial Developer of the Original Code is Sensia Software LLC.
 Portions created by the Initial Developer are Copyright (C) 2008
 the Initial Developer. All Rights Reserved.
 
 Contributor(s): 
    Alexandre Robin <alex.robin@sensiasoftware.com>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.stt.provider.cache;

import org.vast.cdm.common.DataComponent;
import org.vast.stt.data.BlockList;
import org.vast.stt.data.DataNode;


/**
 * <p><b>Title:</b>
 * DataNodeBDB
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO DataNodeBDB type description
 * </p>
 *
 * <p>Copyright (c) 2008</p>
 * @author Alexandre Robin
 * @date Jan 24, 2009
 */
public class DataNodeBDB extends DataNode
{
    
    @Override
    public BlockList createList(DataComponent component)
    {
        BlockList newList = new BlockListBDB();
        newList.setBlockStructure(component);
        listMap.put(component.getName(), newList);
        listArray.add(newList);
        rebuildMappings(component);
        component.clearData();
        return newList;
    }
}
