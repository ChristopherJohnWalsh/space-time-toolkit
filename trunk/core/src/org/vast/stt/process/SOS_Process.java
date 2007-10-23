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

package org.vast.stt.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import org.vast.cdm.common.DataBlock;
import org.vast.cdm.common.DataComponent;
import org.vast.cdm.common.DataHandler;
import org.vast.cdm.common.DataStreamParser;
import org.vast.cdm.common.DataType;
import org.vast.data.*;
import org.vast.math.Vector3d;
import org.vast.ows.OWSUtils;
import org.vast.ows.sos.GetObservationRequest;
import org.vast.ows.sos.SOSResponseReader;
import org.vast.physics.TimeExtent;
import org.vast.process.*;
import org.vast.unit.UnitConversion;
import org.vast.unit.UnitConverter;


/**
 * <p><b>Title:</b><br/>
 * SOS_Process
 * </p>
 *
 * <p><b>Description:</b><br/>
 * Issues a request to a SOS server using provided parameters
 * and input, and outputs the resulting data block per block.
 * </p>
 *
 * <p>Copyright (c) 2007</p>
 * @author Alexandre Robin
 * @date Jan 20, 2006
 * @version 1.0
 */
public class SOS_Process extends DataProcess implements DataHandler
{
    protected DataValue bboxLat1, bboxLon1, bboxLat2, bboxLon2;
    protected DataValue inputStartTime, inputStopTime, inputStepTime;
    protected DataComponent outputObsInfo, outputObsData;
    protected DataValue outputObsName, outputObsProcedure;
    protected DataGroup outputObsLocation;
    protected ConnectionList obsInfoConnections;
    protected InputStream dataStream;
    protected GetObservationRequest query;
    protected OWSUtils owsUtils;
    protected DataStreamParser dataParser;
    protected Thread workerThread;
    protected boolean hasTime, hasBbox; 
    protected boolean done, error, outputReady;
    protected Exception lastException;
    protected Hashtable<DataComponent, UnitConverter> converters;
    protected Vector3d obsLocation;
    protected String obsName, obsProcedure;
    

    public SOS_Process()
    {
        query = new GetObservationRequest();
        owsUtils = new OWSUtils();
        converters = new Hashtable<DataComponent, UnitConverter>();
        needSync = true;
    }


    @Override
    public void init() throws ProcessException
    {
        // Read I/O mappings
        try
        {
            DataGroup input;
            
            // Optional bbox input mappings
            if (inputData.existComponent("bbox"))
            {
                input = (DataGroup)inputData.getComponent("bbox");
                bboxLat1 = (DataValue)input.getComponent("corner1").getComponent(0);
                bboxLon1 = (DataValue)input.getComponent("corner1").getComponent(1);
                bboxLat2 = (DataValue)input.getComponent("corner2").getComponent(0);
                bboxLon2 = (DataValue)input.getComponent("corner2").getComponent(1);
                input.assignNewDataBlock();
                hasBbox = true;
            }
            
            // optional time input mappings
            if (inputData.existComponent("time"))
            {
                input = (DataGroup)inputData.getComponent("time");
                inputStartTime = (DataValue)input.getComponent("start");
                inputStopTime = (DataValue)input.getComponent("stop");
                inputStepTime = (DataValue)input.getComponent("step");
                input.assignNewDataBlock();
                hasTime = true;
            }
            
            // Output Data mapping
            obsInfoConnections = outputConnections.get(0);
            outputObsInfo = outputData.getComponent("observationInfo");
            outputObsName = (DataValue) outputObsInfo.getComponent("name");
            outputObsProcedure = (DataValue) outputObsInfo.getComponent("procedure");
            outputObsLocation = (DataGroup) outputObsInfo.getComponent("location");
            outputObsData = outputData.getComponent("observationData");
            
            // Read parameters mappings + values
            DataGroup sosParams = (DataGroup)paramData.getComponent("sosOptions");
            
            // service end point url
            String url = sosParams.getComponent("endPoint").getData().getStringValue();
            String requestMethod = sosParams.getComponent("requestMethod").getData().getStringValue();
            if (requestMethod.equalsIgnoreCase("post"))
                query.setPostServer(url);
            else
                query.setGetServer(url);
            
            // version
            String version = sosParams.getComponent("version").getData().getStringValue();
            query.setVersion(version);
            
            // offering ID
            String offeringID = sosParams.getComponent("offering").getData().getStringValue();
            query.setOffering(offeringID);
            
            // procedure IDs
            if (sosParams.existComponent("procedures"))
            {
                String procedures = sosParams.getComponent("procedures").getData().getStringValue();            
                String[] procArray = procedures.split(" ");
                for (int i=0; i<procArray.length; i++)
                    query.getProcedures().add(procArray[i]);
            }
            
            // observable IDs
            String observables = sosParams.getComponent("observables").getData().getStringValue();
            String[] obsArray = observables.split(" ");
            for (int i=0; i<obsArray.length; i++)
                query.getObservables().add(obsArray[i]);
            
            // response format
            String format = sosParams.getComponent("format").getData().getStringValue();
            query.setFormat(format);

            // request name
            query.setOperation("GetObservation");
            
            checkData();
            reset();
        }
        catch (Exception e)
        {
            throw new ProcessException(ioError, e);
        }
    }
    
    
    @Override
    public void reset() throws ProcessException
    {
        endRequest();
        outputReady = false;
        done = true;        
        error = false;        
        lastException = null;
        for (int i=0; i<inputConnections.size(); i++)
            inputConnections.get(i).setNeeded(true);
    }
    
    
    protected void checkData() throws ProcessException
    {
        //TODO check that output is compatible with SOS data
    }
    
    
    @Override
    public void execute() throws ProcessException
    {
        // get input variables only if previous request is done
        if (done)
        {
            final DataHandler handler = this;
            Runnable initiateRequest = new Runnable()
            {
                public void run()
                {
                    try
                    {    
                        synchronized (handler)
                        {
                            // init request using spatial + time extent
                            initRequest();
                            
                            // create reader
                            SOSResponseReader reader = new SOSResponseReader();
                            
                            // select request type (post or get)
                            boolean usePost = (query.getPostServer() != null);
                            //System.out.println(owsUtils.buildURLQuery(query));
                            if(usePost)
                            	dataStream = owsUtils.sendPostRequest(query).getInputStream();
                            else 
                            	dataStream = owsUtils.sendGetRequest(query).getInputStream();
                            
                            // parse response
                            reader.parse(dataStream);
                            dataParser = reader.getDataParser();
                            dataParser.setDataHandler(handler);
                            
                            // get procedure, name and location
                            obsName = reader.getObservationName();
                            obsProcedure = reader.getProcedure();
                            obsLocation = reader.getFoiLocation();
                            
                            // start parsing
                            dataParser.parse(reader.getDataStream());
                            done = true;
                        }
                    }
                    catch (Exception e)
                    {
                        error = true;
                        done = true;
                        lastException = e;
                        synchronized (handler) {handler.notify();}
                    }
                    finally
                    {
                        endRequest();
                    }       
                }
            };
            
            workerThread = new Thread(initiateRequest, "SOS Process Parser");
            workerThread.start();
            done = false;
            
            // set inputs as not needed so that we can continue looping
            for (int i=0; i<inputConnections.size(); i++)
                inputConnections.get(i).setNeeded(false);
        }
        
        // now let the worker thread run one more time
        synchronized (this)
        {
            try
            {
                // notify parser thread that next packet is needed
                outputReady = false;
                this.notify();
                //System.out.println("next " + name);
                
                // give parser control and wait for next block to be parsed
                while (!done && !error && !outputReady)
                    this.wait();
                //System.out.println("ready " + name + ": " + outputObsData.getData().getDoubleValue(0));
            }
            catch (InterruptedException e)
            {
            }
            
            // if an error occured in the worker thread
            if (error)
            {
                String server = query.getPostServer();
                if (server == null)
                    server = query.getGetServer();                
                throw new ProcessException("Error while reading data from SOS server: " + server, lastException);
            }
        
            // if parsing is done (at end of stream)
            if (done)
            {
                // set inputs as needed since loop has ended
                for (int i=0; i<inputConnections.size(); i++)
                    inputConnections.get(i).setNeeded(true);
            }
        }
    }    
    
    
    /**
     * Reads all input parameters and set up query accordingly
     */
    protected void initRequest()
    {
        // make sure previous request is cancelled
        endRequest();
        
        // read lat/lon bbox
        if (hasBbox)
        {
            double lon1 = bboxLon1.getData().getDoubleValue();///Math.PI*180;
            double lat1 = bboxLat1.getData().getDoubleValue();///Math.PI*180;
            double lon2 = bboxLon2.getData().getDoubleValue();///Math.PI*180;
            double lat2 = bboxLat2.getData().getDoubleValue();///Math.PI*180;
            double minX = Math.min(lon1, lon2);
            double maxX = Math.max(lon1, lon2);
            double minY = Math.min(lat1, lat2);
            double maxY = Math.max(lat1, lat2);
            query.getBbox().setMinX(minX);
            query.getBbox().setMaxX(maxX);
            query.getBbox().setMinY(minY);
            query.getBbox().setMaxY(maxY);
        }
        
        // read time range
        if (hasTime)
        {
            double start = inputStartTime.getData().getDoubleValue();
            double stop = inputStopTime.getData().getDoubleValue();
            double step = inputStepTime.getData().getDoubleValue();
            
            if (start == TimeExtent.NOW && stop == TimeExtent.NOW)
            {
                query.getTime().setBaseAtNow(true);
            }
            else
            {
                if (start == TimeExtent.NOW)
                    query.getTime().setBeginNow(true);
                else
                    query.getTime().setStartTime(start);
                
                if (stop == TimeExtent.NOW)
                    query.getTime().setEndNow(true);
                else
                    query.getTime().setStopTime(stop);
            }
            
            query.getTime().setTimeStep(step);
        }
    }
    
    
    /**
     * Makes sure request is ended and stream is closed
     */
    protected void endRequest()
    {
        try
        {
            if (dataStream != null)
                dataStream.close();
            
            dataStream = null;
            
            if (dataParser != null)
                dataParser.stop();
            
            synchronized (this) {this.notify();}
        }
        catch (IOException e)
        {
        }
    }
    
    
    @Override
    public void dispose()
    {
        endRequest();
    }
    
    
    public void endData(DataComponent info, DataBlock data)
    {
        try
        {
            // give exec control and wait for the ok to continue
            while (outputReady)
                this.wait();
            
            // write observation data
            ///System.out.println("out " + name);
            outputObsData.setData(data);
            
            // also write observation info
            outputObsName.getData().setStringValue(obsName);
            outputObsProcedure.getData().setStringValue(obsProcedure);
            outputObsLocation.getData().setDoubleValue(0, obsLocation.y * Math.PI / 180);
            outputObsLocation.getData().setDoubleValue(1, obsLocation.x * Math.PI / 180);
            outputObsLocation.getData().setDoubleValue(2, obsLocation.z);
            
            // notify exec thread that next packet has been parsed
            outputReady = true;
            this.notify();
        }
        catch (InterruptedException e)
        {
        }
    }


    public void endDataAtom(DataComponent info, DataBlock data)
    {
        DataType dataType = data.getDataType();
        
        // TODO unit conversion WILL be handled by global chain unit converters
        if (dataType != DataType.UTF_STRING && dataType != DataType.ASCII_STRING)
        {
            UnitConverter converter = converters.get(info);
            
            if (converter == null)
            {
                String uom = (String)info.getProperty(DataComponent.UOM_CODE);
                converter = UnitConversion.createConverterToSI(uom);
                converters.put(info, converter);
            }
            
            // convert to SI
            double newVal = converter.convert(data.getDoubleValue());
            data.setDoubleValue(newVal);
        }
        
        //System.out.println(info.getName() + ": " + data.getStringValue());
    }


    public void endDataBlock(DataComponent info, DataBlock data)
    {
    }


    public void startData(DataComponent info)
    {
    }


    public void startDataBlock(DataComponent info)
    {
    }


    public void beginDataAtom(DataComponent info)
    {       
    }
}