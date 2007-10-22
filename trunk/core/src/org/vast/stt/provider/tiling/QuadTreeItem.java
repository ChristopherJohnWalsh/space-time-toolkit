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

package org.vast.stt.provider.tiling;

import org.vast.physics.SpatialExtent;


/**
 * <p><b>Title:</b><br/>
 * Quad Tree Item
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO QuadTreeItem type description
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Aug 10, 2006
 * @version 1.0
 */
public class QuadTreeItem extends SpatialExtent
{
    private final static int N = 1;
    private final static int E = 2;
    private final static int S = 4;
    private final static int W = 8;
    private final static int NE = N+E;
    private final static int NW = N+W;
    private final static int SE = S+E;
    private final static int SW = S+W;
    
    protected double tileSize;
    protected double distance;
    protected byte quadrant;
    protected Object data;
    protected QuadTreeItem parent;
    protected QuadTreeItem[] children;  // 0->3 from lower left, counter clockwise
    protected boolean needed;
    
    
    public QuadTreeItem()
    {
        children = new QuadTreeItem[4];
    }
    
    
    /**
     * Constructs a new item and add it as a child in the specified quadrant.
     * Quadrant is in the range 0-3, where 0 is lower left and going counter clockwise. 
     * @param parentBlock
     * @param quadrant
     */
    public QuadTreeItem(QuadTreeItem parentBlock, byte quadrant)
    {
        this();
        this.parent = parentBlock;
        this.quadrant = quadrant;
                
        double dX = (parentBlock.maxX - parentBlock.minX) / 2;
        double dY = (parentBlock.maxY - parentBlock.minY) / 2;
        tileSize = Math.abs(dX) * Math.abs(dY);
        
        switch (quadrant)
        {
            case 0:
                minX = parentBlock.minX;
                minY = parentBlock.minY;
                maxX = minX + dX;
                maxY = minY + dY;
                break;
                
            case 1:
                minX = parentBlock.minX + dX;
                minY = parentBlock.minY;
                maxX = minX + dX;
                maxY = minY + dY;
                break;
                
            case 2:
                minX = parentBlock.minX + dX;
                minY = parentBlock.minY + dY;
                maxX = minX + dX;
                maxY = minY + dY;
                break;
                
            case 3:
                minX = parentBlock.minX;
                minY = parentBlock.minY + dY;
                maxX = minX + dX;
                maxY = minY + dY;
                break;
        }
        
        parentBlock.setChild(quadrant, this);
    }
    
    
    /**
     * Constructs a new item and set it as the parent of the given child
     * expanding in the specified direction.
     * @param childBlock
     * @param directionOfExpansion
     */
    public QuadTreeItem(QuadTreeItem childBlock, int directionOfExpansion, boolean nothing)
    {
        this();
        
        double dX = (childBlock.maxX - childBlock.minX) * 2;
        double dY = (childBlock.maxY - childBlock.minY) * 2;
        tileSize = Math.abs(dX) * Math.abs(dY);
        
        switch (directionOfExpansion)
        {
            case N:
            case NE:
                children[0] = childBlock;
                childBlock.quadrant = 0;
                minX = childBlock.minX;
                minY = childBlock.minY;
                maxX = minX + dX;
                maxY = minY + dY;
                break;
            
            case E:    
            case SE:
                children[3] = childBlock;
                childBlock.quadrant = 3;
                minX = childBlock.minX;
                minY = childBlock.maxY - dY;
                maxX = minX + dX;
                maxY = childBlock.maxY;
                break;
            
            case S:
            case SW:
                children[2] = childBlock;
                childBlock.quadrant = 2;
                minX = childBlock.maxX - dX;
                minY = childBlock.maxY - dY;
                maxX = childBlock.maxX;
                maxY = childBlock.maxY;
                break;
                
            case W:
            case NW:
                children[1] = childBlock;
                childBlock.quadrant = 1;
                minX = childBlock.maxX - dX;
                minY = childBlock.minY;
                maxX = childBlock.maxX;
                maxY = minY + dY;
                break;
        }
    }
    
    
    /**
     * Constructs a QuadTreeItem with the given coordinates
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     */
    public QuadTreeItem(double minX, double minY, double maxX, double maxY)
    {
        this();
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        tileSize = Math.abs(maxX - minX) * Math.abs(maxY - minY);
    }
    
    
//    /**
//     * Expand tree until the root tile contains the bbox
//     * @param bbox
//     * @param bboxSize
//     * @return
//     */
//    public QuadTreeItem findTopItem(SpatialExtent bbox, double bboxSize)
//    {
//        double sizeRatio = bboxSize / tileSize;
//        
//        if (sizeRatio > 16 || !this.contains(bbox))
//        {
//            // create parent if null
//            if (parent == null)
//            {
//                int dir = whereIs(bbox);
//                parent = new QuadTreeItem(this, dir, false);
//            }
//            
//            return parent.findTopItem(bbox, bboxSize);
//        }
//        else        
//            return this;
//    }
   
    
    /**
     * Finds out where the bbox is relative to this tile
     * @param bbox
     * @return one of N,E,S,W,NE,SE,SW,NW
     */
    public int whereIs(SpatialExtent bbox)
    {
        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;
        
        double dX1 = bbox.getMinX() - centerX;
        double dX2 = bbox.getMaxX() - centerX;
        double vX = (dX2 + dX1) / 2;
        
        double dY1 = bbox.getMinY() - centerY;
        double dY2 = bbox.getMaxY() - centerY;
        double vY = (dY2 + dY1) / 2;
        
        int dir = 0;
        
        if (vX > 0)
            dir += E;
        else if (vX < 0)
            dir += W;
        
        if (vY > 0)
            dir += N;
        else if (vY < 0)
            dir += S;

        return dir;
    }
    

    public Object getData()
    {
        return data;
    }


    public void setData(Object data)
    {
        this.data = data;
    }


    public double getMinX()
    {
        return minX;
    }


    public void setMinX(double minX)
    {
        this.minX = minX;
    }


    public double getMinY()
    {
        return minY;
    }


    public void setMinY(double minY)
    {
        this.minY = minY;
    }
    
    
    public double getMaxX()
    {
        return maxX;
    }


    public void setMaxX(double maxX)
    {
        this.maxX = maxX;
    }


    public double getMaxY()
    {
        return maxY;
    }


    public void setMaxY(double maxY)
    {
        this.maxY = maxY;
    }
    
    
    public QuadTreeItem getChild(int i)
    {
        return children[i];
    }


    public void setChild(int i, QuadTreeItem item)
    {
        this.children[i] = item;
    }


    public QuadTreeItem getParent()
    {
        return parent;
    }


    public void setParent(QuadTreeItem parentBlock)
    {
        this.parent = parentBlock;
    }


    public byte getQuadrant()
    {
        return quadrant;
    }


    public double getTileSize()
    {
        return tileSize;
    }


    public double getDistance()
    {
        return distance;
    }


    public void setDistance(double distance)
    {
        this.distance = distance;
    }
    
    
    public void accept(QuadTreeVisitor visitor)
    {
        visitor.visit(this);
    }
    
    
    public boolean isNeeded()
    {
        return needed;
    }


    public void setNeeded(boolean needed)
    {
        this.needed = needed;
    }
    
    
    protected void appendId(StringBuffer buf)
    {
        if (parent != null)
        {
            parent.appendId(buf);
            buf.append(quadrant);
        }
        else
            buf.append('q');
    }
    
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        appendId(buf);
        buf.append(": ");
        buf.append(maxX-minX);
        buf.append(" x ");
        buf.append(maxY-minY);
        buf.append(" (");
        buf.append(minX);
        buf.append(",");
        buf.append(minY);
        buf.append(" - ");
        buf.append(maxX);
        buf.append(",");
        buf.append(maxY);
        buf.append(")");
        return buf.toString();
    }
}
