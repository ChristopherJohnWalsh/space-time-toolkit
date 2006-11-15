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

package org.vast.sttx.provider.worldwind;

import org.vast.stt.provider.tiling.QuadTreeItem;
import org.vast.stt.provider.tiling.QuadTreeVisitor;


/**
 * <p><b>Title:</b>
 * Virtual Earth Tile Number
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Generates a virtual earth tile number by visiting the QuadTree
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Sep 30, 2006
 * @version 1.0
 */
public class WorldwindMapTileXYZ implements QuadTreeVisitor
{
    protected int x, y, zoom;
    protected int xMul, yMul;
    
    
    public WorldwindMapTileXYZ()
    {
        zoom = 0;
        xMul = 1;
        yMul = 1;
    }
    
    
    public void visit(QuadTreeItem item)
    {
        // go further only if there is a parent
        QuadTreeItem parent = item.getParent();
        if (parent == null)
            return;
        
        switch (item.getQuadrant())
        {
            case 0:
                break;
                
            case 1:
                x += xMul;
                break;
                
            case 2:
                x += xMul;
                y += yMul;
                break;
                
            case 3:
                y += yMul;
                break;
        }
        
        zoom++;
        xMul *= 2;
        yMul *= 2;
        
        parent.accept(this);
    }


    public int getX()
    {
        return x;
    }


    public int getY()
    {
        return y;
    }


    public int getZoom()
    {
        return zoom - 3;
    }
}
