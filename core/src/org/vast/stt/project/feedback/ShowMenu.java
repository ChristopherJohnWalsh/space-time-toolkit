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

package org.vast.stt.project.feedback;

import java.util.ArrayList;


/**
 * <p><b>Title:</b>
 * Show Menu
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Used to popup a menu with further choices.
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Oct 19, 2006
 * @version 1.0
 */
public class ShowMenu extends ItemAction
{
    public class MenuEntry
    {
        public String name;
        public ItemAction action;
    }    
    
    protected ArrayList<MenuEntry> entries;

    
    public ShowMenu()
    {
        entries = new ArrayList<MenuEntry>();
    }
    
    
    public ArrayList<MenuEntry> getEntries()
    {
        return entries;
    }
}
