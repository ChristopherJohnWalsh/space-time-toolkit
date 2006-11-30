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

package org.vast.stt.renderer.JFreeChart;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.vast.util.DateTimeFormat;

public class DateFormat extends NumberFormat
{
    private static final long serialVersionUID = 1L;


    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos)
    {
        toAppendTo.append(DateTimeFormat.formatIso(number, 0));
        return toAppendTo;
    }


    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos)
    {
        toAppendTo.append(DateTimeFormat.formatIso(number, 0));
        return toAppendTo;
    }


    @Override
    public Number parse(String source, ParsePosition parsePosition)
    {
        return null;
    }

}
