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
    Alexandre Robin <robin@nsstc.uah.edu>    Tony Cook <tcook@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.stt.gui.views;

import org.vast.math.Vector3d;
import org.vast.physics.MapProjection;
import org.vast.stt.event.EventType;
import org.vast.stt.event.STTEvent;
import org.vast.stt.project.feedback.FeedbackEvent;
import org.vast.stt.project.feedback.FeedbackEventListener;
import org.vast.stt.project.feedback.FeedbackEvent.FeedbackType;
import org.vast.stt.project.tree.DataItem;
import org.vast.stt.project.world.Projection;
import org.vast.stt.project.world.Projection_ECEF;
import org.vast.stt.project.world.WorldScene;
import org.vast.stt.provider.DataProvider;
import org.vast.stt.provider.STTSpatialExtent;
import org.vast.stt.renderer.PickFilter;
import org.vast.stt.renderer.PickedObject;
import org.vast.stt.renderer.SceneRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.*;


/**
 * <p><b>Title:</b><br/>
 * Scene View Controller
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Gives user 3D control on the view (rotation, translation, zoom)
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Nov 9, 2005
 * @version 1.0
 */
public class WorldViewController implements MouseListener, MouseMoveListener, Listener
{
	protected WorldScene scene;
    protected FeedbackEventListener pickListener;
    private Vector3d P0 = new Vector3d();
	private int xOld;
	private int yOld;
    private int corner;
	private boolean rotating;
	private boolean translating;
	private boolean zooming;
    private boolean resizing;
    private boolean dragged;
    private final static double RTD = 180/Math.PI;
    

	public WorldViewController()
	{
        pickListener = new FeedbackEventListener();
	}
    
    
    protected void doChangeROI(int x0, int y0, int x1, int y1)
    {
        DataItem selectedItem = scene.getSelectedItems().get(0).getDataItem();
        DataProvider provider = selectedItem.getDataProvider();
        STTSpatialExtent bbox = provider.getSpatialExtent();
        
        Projection projection = scene.getViewSettings().getProjection();
        boolean found = projection.pointOnMap(x1, y1, scene, P0);
        
        if (!found)
            return;
        
        // hack to convert from ECEF back to LLA
        if (projection instanceof Projection_ECEF)
        {
            double[] lla = MapProjection.ECFtoLLA(P0.x, P0.y, P0.z, null);
            
            if (lla[1] > Math.PI)
                lla[1] -= 2*Math.PI;
            
            else if (lla[1] < -Math.PI)
                lla[1] += 2*Math.PI;
            
            P0.y = lla[0];
            P0.x = lla[1];
            P0.z = lla[2];
        }
        
        P0.x *= RTD;
        P0.y *= RTD;
        
        switch (corner)
        {
            case 1:
                bbox.setMinX(P0.x);
                bbox.setMinY(P0.y);
                break;
                
            case 2:
                bbox.setMinX(P0.x);
                bbox.setMaxY(P0.y);
                break;
                
            case 3:
                bbox.setMaxX(P0.x);
                bbox.setMaxY(P0.y);
                break;
                
            case 4:
                bbox.setMaxX(P0.x);
                bbox.setMinY(P0.y);
                break;
                
            case 5:
                double dX = (bbox.getMaxX() - bbox.getMinX()) / 2;
                double dY = (bbox.getMaxY() - bbox.getMinY()) / 2;
                P0.x = Math.max(-179.99 + dX, P0.x);
                P0.x = Math.min(+179.99 - dX, P0.x);
                P0.y = Math.max(-89.99 + dY, P0.y);
                P0.y = Math.min(+89.99 - dY, P0.y);
                bbox.setMinX(P0.x - dX);
                bbox.setMaxX(P0.x + dX);
                bbox.setMinY(P0.y - dY);
                bbox.setMaxY(P0.y + dY);
                break;
        }
        
        // send event to update spatial extent listeners
        //provider.setEnabled(false);
        //bbox.dispatchEvent(new STTEvent(this, EventType.PROVIDER_SPATIAL_EXTENT_CHANGED));
        // commented out because it would cause other providers subscribed to this bbox to redraw
    }
	

	public void mouseDown(MouseEvent e)
	{
        dragged = false;
        int viewHeight = scene.getRenderer().getViewHeight();
        e.y = viewHeight - e.y;
        
        // check if resizing ROI (need to call renderer pick method)
        if (scene.getViewSettings().isShowItemROI() && !scene.getSelectedItems().isEmpty())
        {
            SceneRenderer<WorldScene> renderer = scene.getRenderer();
            PickFilter pickFilter = new PickFilter();
            pickFilter.x = e.x;
            pickFilter.y = e.y;
            pickFilter.dX = 5;
            pickFilter.dY = 5;
            pickFilter.onlyBoundingBox = true;
            PickedObject obj = renderer.pick(scene, pickFilter);
            
            // case of corner selected
            if (obj != null && obj.indices.length > 0)
            {
                if (obj.indices[0] < 0)
                {
                    corner = -obj.indices[0];
                    resizing = true;
                    xOld = e.x;
                    yOld = e.y;
                    return;
                }
            }
        }
        
        // case of left button pressed
        if (e.button == 1)
		{
			if (e.stateMask == SWT.CTRL)
				zooming = true;
			else if (e.stateMask == SWT.SHIFT)
				translating = true;
			else
				rotating = true;
		}
		
        // case of middle button pressed
        else if (e.button == 2)
            zooming = true;
        
        // case of right button pressed
        else if (e.button == 3)
			translating = true;
		
		xOld = e.x;
		yOld = e.y;
	}


	public void mouseUp(MouseEvent e)
	{
		if (!dragged)
		{
            FeedbackType feedbackType = null;
            
            if (e.button == 1)
                feedbackType = FeedbackType.LEFT_CLICK;
            else if (e.button == 2)
                feedbackType = FeedbackType.MID_CLICK;
            else if (e.button == 3)
                feedbackType = FeedbackType.RIGHT_CLICK;
            
            FeedbackEvent event = new FeedbackEvent(feedbackType);
            event.setSourceScene(scene);
            pickListener.handleEvent(event);
        }
        else if (resizing)
        {
            // trigger provider refresh when button is released
            DataProvider provider = scene.getSelectedItems().get(0).getDataItem().getDataProvider();
            //provider.setEnabled(true);
            provider.getSpatialExtent().dispatchEvent(new STTEvent(this, EventType.SPATIAL_EXTENT_CHANGED));
        }
        
        rotating = false;
		translating = false;
		zooming = false;
        resizing = false;
        
		((Control) e.widget).setCursor(e.widget.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
	}


	public void mouseMove(MouseEvent e)
	{
        dragged = true;
        int viewHeight = scene.getRenderer().getViewHeight();
        e.y = viewHeight - e.y;
        
        if (rotating)
        {
            ((Control) e.widget).setCursor(e.widget.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
            scene.getCameraController().doRotation(xOld, yOld, e.x, e.y);
            xOld = e.x;
            yOld = e.y;
            updateView();
        }
        
        else if (translating)
        {
            ((Control) e.widget).setCursor(e.widget.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
            scene.getCameraController().doTranslation(xOld, yOld, e.x, e.y);
            xOld = e.x;
            yOld = e.y;
            updateView();
        }
        
        else if (zooming)
        {
            ((Control) e.widget).setCursor(e.widget.getDisplay().getSystemCursor(SWT.CURSOR_SIZEN));
            scene.getCameraController().doZoom(xOld, yOld, e.x, e.y);
            xOld = e.x;
            yOld = e.y;
            updateView();
        }
        
        else if (resizing)
        {
            ((Control) e.widget).setCursor(e.widget.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
            doChangeROI(xOld, yOld, e.x, e.y);
            updateView();
        }
	}


	public void handleEvent(Event event)
	{
		double amount = event.count/20.0;
        scene.getCameraController().doZoom(amount);
		updateView();
	}
	
	
	public void mouseDoubleClick(MouseEvent e)
	{
	}
	
	
	protected void updateView()
	{
        scene.getViewSettings().dispatchEvent(new STTEvent(this, EventType.SCENE_VIEW_CHANGED));
	}


	public WorldScene getScene()
    {
        return scene;
    }


    public void setScene(WorldScene scene)
    {
        this.scene = scene;
    }
}