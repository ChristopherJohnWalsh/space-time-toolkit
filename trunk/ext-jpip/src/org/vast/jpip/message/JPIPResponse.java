/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License Version
 1.1 (the "License"); you may not use this file except in compliance with
 the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/MPL-1.1.html
 
 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.
 
 The Original Code is the "SensorML DataProcessing Engine".
 
 The Initial Developer of the Original Code is the VAST team at the
 University of Alabama in Huntsville (UAH). <http://vast.uah.edu>
 Portions created by the Initial Developer are Copyright (C) 2007
 the Initial Developer. All Rights Reserved.
 
 Please Contact Mike Botts <mike.botts@uah.edu> for more information.
 
 Contributor(s): 
    Alexandre Robin <robin@nsstc.uah.edu>
 
******************************* END LICENSE BLOCK ***************************/

package org.vast.jpip.message;

import java.util.LinkedList;
import java.util.List;


/**
 * <p><b>Title:</b>
 * JPIP Response
 * </p>
 *
 * <p><b>Description:</b><br/>
 *
 * </p>
 *
 * <p>Copyright (c) 2008</p>
 * @author Alexandre Robin
 * @date Feb 24, 2008
 * @version 1.0
 */
public class JPIPResponse
{
    protected JPIPResponseFields responseFields;
    protected List<JPIPMessage> messages;
    protected JPIPMessage mainHeaderMessage;
    
    
    public JPIPResponse(JPIPResponseFields jpipResponseFields)
    {
        this.responseFields = jpipResponseFields;
        this.messages = new LinkedList<JPIPMessage>();
    }
    
    
    public void addMessage(JPIPMessage message)
    {
        if (message.header.classIdentifier == JPIPMessageHeader.MAIN_HEADER)
            mainHeaderMessage = message;
        
        messages.add(message);            
    }
    
    
    public List<JPIPMessage> getAllMessages()
    {
        return messages;
    }
    
    
    public JPIPMessage getHeaderMessage()
    {
        return this.mainHeaderMessage;
    }
}
