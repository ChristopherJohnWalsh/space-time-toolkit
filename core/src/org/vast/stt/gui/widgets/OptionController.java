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

package org.vast.stt.gui.widgets;

import org.eclipse.swt.events.SelectionListener;
import org.vast.ows.sld.Symbolizer;
import org.vast.stt.project.tree.DataItem;


/**
 * <p><b>Title:</b>
 * Option Controller
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Base abstract class for all Option Controllers
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Tony Cook, Alexandre Robin
 * @date Jul 12, 2006
 * @version 1.0
 */
public abstract class OptionController implements SelectionListener
{
    protected OptionControl[] optionControls;
    protected Symbolizer symbolizer;
    protected DataItem dataItem;

    abstract public void loadFields();
    
	//  TODO  REMOVE THIS METHOD
    public OptionControl[] getControls()
    {
        return optionControls;
    }

    public DataItem getDataItem()
    {
        return dataItem;
    }
    
    public void setDataItem(DataItem dataItem)
    {
        this.dataItem = dataItem;
    }
    
    
    public Symbolizer getSymbolizer()
    {
        return symbolizer;
    }


    public void addSelectionListener(SelectionListener sl)
    {
        for (int i = 0; i < optionControls.length; i++)
            optionControls[i].addSelectionListener(sl);
    }

    public void removeSelectionListener(SelectionListener sl)
    {
        for (int i = 0; i < optionControls.length; i++)
        {
            optionControls[i].removeSelectionListener(sl);
        }
    }
}
