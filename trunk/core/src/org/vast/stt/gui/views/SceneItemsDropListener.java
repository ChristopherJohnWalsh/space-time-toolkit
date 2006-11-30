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

package org.vast.stt.gui.views;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.vast.stt.event.EventType;
import org.vast.stt.event.STTEvent;
import org.vast.stt.project.world.WorldScene;
import org.vast.stt.project.world.WorldSceneItem;


/**
 * <p><b>Title:</b>
 * Scene Items Tree Drop Listener
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Drop listener used in the scene items view to implement
 * reordering capabilities.
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 29, 2006
 * @version 1.0
 */
public class SceneItemsDropListener extends ViewerDropAdapter
{
    private StructuredViewer viewer;
    private Object target;
    
    
    public SceneItemsDropListener(StructuredViewer viewer)
    {
        super(viewer);
        this.viewer = viewer;        
    }
    
    
    @Override
    public boolean performDrop(Object data)
    {
        WorldScene scene = (WorldScene)viewer.getInput();
        int targetPos = scene.getSceneItems().indexOf(target);
        scene.getSceneItems().remove(data);
        scene.getSceneItems().add(targetPos, (WorldSceneItem)data);
        viewer.refresh();
        scene.dispatchEvent(new STTEvent(this, EventType.SCENE_ITEM_CHANGED));
        return true;
    }


    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType)
    {
        if (target == null)
            return false;
        
        if (!(target instanceof WorldSceneItem))
            return false;
        
        this.target = target;
        return true;
    }
}
