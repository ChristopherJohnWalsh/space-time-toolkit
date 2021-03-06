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

package org.vast.stt.gui.widgets.time;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.vast.stt.gui.widgets.SpinnerModel;
/**
 * <p><b>Title:</b><br/>
 * TimeSpinnerModel
 * </p>
 *
 * <p><b>Description:</b><br/>
 * The Model for controlling the behavior of the timeSpinner widget, mainly through 
 * the increment() and decrement() methods.  The model also highlights the currently 
 * selected field in the selectField() method.  It should also work for fraction of seconds
 * by passing in one or more 'F's in the format string, but that has not been tested.
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Tony Cook
 * @date Dec 20, 2005
 * @version 1.0
 */
public class TimeSpinnerModel implements SpinnerModel{
	
	protected String formatStr;
	// position of field in arrays start and len
    protected int YEAR = -1 , MONTH = -1, DAY = -1, HOUR = -1, MIN = -1, SEC = -1, FSEC = -1;
    Integer [] start, len; // position of fields in Str
    int yearDigits, dayDigits, fsecDigits;
    protected int years, months, days, hours, minutes, seconds, fseconds;
    int maxYears, maxDays, maxFseconds;  //  editor supplies these
    boolean hasFseconds = false;  //  is fraction of seconds present?
    boolean rollEnabled = true;
    boolean allowNegative = false;
    //int currentField = 0; moved to TimeSpinner class
    //TODO: sub these constants out for something like the old Units class
	static final int SECONDS_PER_MINUTE = 60;
	static final int SECONDS_PER_HOUR = 3600;
	static final int SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;
	static final int SECONDS_PER_YEAR = 365 * SECONDS_PER_DAY; 
    
	public TimeSpinnerModel(String formatStr){
		this.formatStr = formatStr;
		loadFieldPositionArrays(formatStr);
	}
	
    public void loadFieldPositionArrays(String formatStr){
        String token;
        StringTokenizer parser = new StringTokenizer(formatStr," :./,", true);
        //int numFields = parser.countTokens();
        List<Integer> startList = new ArrayList<Integer>();
        List<Integer> lenList = new ArrayList<Integer>();
        int index = 0;
        int where = 0;

        try {
            while (parser.hasMoreTokens()){
                token = parser.nextToken();
                switch(token.charAt(0)) {
                    case 'Y': case 'y':
                        YEAR = index;
                        yearDigits = token.length();
                        maxYears = (int)(Math.pow(10.0, (double)yearDigits) - 1);                        
                        break;
                    case 'M':
                        MONTH = index;
                        break;
                    case 'D': case 'd':
                        DAY = index;
                        dayDigits = token.length();
                        maxDays = (int)(Math.pow(10.0, (double)dayDigits) - 1);
                        break;
                    case 'H': case 'h':
                        HOUR = index;
                        break;
                    case 'm':
                        MIN = index;
                        break;
                    case 'S': case 's':
                        SEC = index;
                        break;
                    case 'F': case 'f':
                        FSEC = index;  // fratction of seconds
                        fsecDigits = token.length();
                        maxFseconds = (int)(Math.pow(10.0, (double)fsecDigits) - 1);
                        break;
                    case ' ':
                    case ':':
                    case '.':
                    case '/':
                    case ',':
                        where++;  //numFields--;
                        continue;
                    default:
                        System.err.println("TimeStepSpinnerEditor:  Format invalid: " + formatStr);
                        return;
                }

                startList.add(where);
                lenList.add(token.length());
                //len[index] = token.length();
                //start[index] = where;
                //where = start[index]+len[index];
                where = where + token.length();

                index++;
            }

        }catch (NoSuchElementException e) {
        	//  These are really fatal exceptions for the Model 
        	e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
      
        start = (Integer [])startList.toArray(new Integer[]{});
        len = (Integer [])lenList.toArray(new Integer[]{});
    }
	
	private void stepCurrentField(int currentField, int step){
		if(currentField == YEAR){
            years += step;
            if(years<0)  years = maxYears;
            if(years>maxYears)  years = 0;
		} else if(currentField == MONTH){
            ;//changeDay(step);
		} else if(currentField == DAY){
            changeDay(step);
		} else if(currentField == HOUR){
            changeHour(step);
        } else if(currentField == MIN) {
        	changeMinute(step);
		} else if(currentField == SEC) {
			changeSecond(step);
		} else if(currentField == FSEC) {
			changeFsecond(step);
		} else 
			System.err.println(" *** currentField unknown.  Blimey!!!!");
			
	}

	public void increment(int field){
		stepCurrentField(field, 1);
		//return toString();
	}
    
	public void decrement(int field){
		stepCurrentField(field, -1);
		//return toString();
	}
	
	/** 
	 * TODO:  modify to support different formatStrings
	 */
    public String toString(){
    	StringBuffer buff = new StringBuffer(formatStr.length());
    	buff.append(zeroPad("" + years, yearDigits) + " ");
    	buff.append(zeroPad("" + days, dayDigits) + " ");
    	buff.append(zeroPad("" + hours,2) + ":");
    	buff.append(zeroPad("" + minutes,2) + ":");
    	buff.append(zeroPad("" + seconds,2));
    			
    	return buff.toString();
    }
	
    private String zeroPad(String str, int desiredLength){
        int count = desiredLength - str.length();
        if(count <= 0)  // if < 0, we have a problem actually
            return str;
        char [] zeros = new char[count];
        for(int i=0;i<zeros.length;i++){
            zeros[i] = '0';
        }
        String zeroStr = new String(zeros);
        return zeroStr + str;
    }


    //  NOTE:  step should be either 1 or -1
    protected void changeDay(int step){
        days+=step;
        if(days > maxDays)  {
            days = 0;
            //  No year rolling by design for now.  TC 1/25/05
        } else if (days < 0)
            days = maxDays;
    }

    //  NOTE:  step should be either 1 or -1
    protected void changeHour(int step){
        hours+=step;
        if(hours > 23)  {
            hours = 0;
            if(rollEnabled)
                changeDay(step);
        } else if (hours < 0) {
            hours = 23;
            if(rollEnabled)
                changeDay(step);
        }
    }

    //  NOTE:  step should be either 1 or -1
    protected void changeMinute(int step){
        minutes+=step;
        if(minutes > 59)  {
            minutes = 0;
            if(rollEnabled)
                changeHour(step);
        } else if (minutes < 0) {
            minutes = 59;
            if(rollEnabled)
                changeHour(step);
        }
    }

    //  NOTE:  step should be either 1 or -1
    protected void changeSecond(int step){
        seconds+=step;
        if(seconds > 59)  {
            seconds = 0;
            if(rollEnabled)
                changeMinute(step);
        } else if (seconds < 0) {
            seconds = 59;
            if(rollEnabled)
                changeMinute(step);
        }
    }

    //  NOTE:  step should be either 1 or -1
    protected void changeFsecond(int step){
        fseconds+=step;
        if(fseconds > maxFseconds)  {
            fseconds = 0;
            if(rollEnabled)
                changeSecond(step);
        } else if (fseconds < 0) {
            fseconds = maxFseconds;
            if(rollEnabled)
                changeSecond(step);
        }
    }

    public Object getValue(){
        double timeStep = years * SECONDS_PER_YEAR;  // no leap year here.
        timeStep += days * SECONDS_PER_DAY;
        timeStep += hours * SECONDS_PER_HOUR;
        timeStep += minutes * SECONDS_PER_MINUTE;
        timeStep += seconds;
        if(hasFseconds)
            timeStep += fseconds/maxFseconds;

        return new Double(timeStep);
    }
    
    /**
     * Sets the spinner value based on number of seconds
     * @param sec - seconds since 1Jan1970
     */
    public void setValue(Object secondsObj){
    	double sec = ((Double)secondsObj).doubleValue();
        //  clear all fields
        years = days = hours = minutes = seconds = fseconds = 0;
        //  BUG here when sec > MAX INT
        int isec = (int)sec;
        if (hasFseconds) {
            double frac = sec - (double)isec;
            String fsStr = "" + maxFseconds;
            int numDigits = fsStr.length();
            fseconds = (int)(Math.pow(frac, numDigits));
        }
        if(isec >= SECONDS_PER_YEAR) {
            years = isec / (int)SECONDS_PER_YEAR;
            isec = isec % (int)SECONDS_PER_YEAR;
        }
        if(isec >= SECONDS_PER_DAY) {
            days = isec / (int)SECONDS_PER_DAY;
            isec = isec % (int)SECONDS_PER_DAY;
        }
        if(isec >= SECONDS_PER_HOUR) {
            hours = isec / (int)SECONDS_PER_HOUR;
            isec = isec % (int)SECONDS_PER_HOUR;
        }
        if(isec >= SECONDS_PER_MINUTE) {
            minutes = isec / 60;
            isec = isec % 60;
        }
        seconds = isec;
    }

    public Integer [] getStart() {
    	return start;
    }
    
    public Integer [] getLength() {
    	return len;
    }
    
}
