/***************************************************************
 (c) Copyright 2005, University of Alabama in Huntsville (UAH)
 ALL RIGHTS RESERVED

 This software is the property of UAH.
 It cannot be duplicated, used, or distributed without the
 express written consent of UAH.

 This software developed by the Vis Analysis Systems Technology
 (VAST) within the Earth System Science Lab under the direction
 of Mike Botts (mike.botts@atmos.uah.edu)
 ***************************************************************/

package org.vast.stt.data;

import java.io.IOException;

import org.ogc.cdm.common.CDMException;
import org.ogc.cdm.common.DataComponent;
import org.ogc.cdm.common.DataEncoding;
import org.ogc.cdm.reader.DataStreamParser;
import org.vast.cdm.reader.URIStreamHandler;
import org.vast.stt.project.StaticDataSet;
import org.vast.stt.readers.SWEResourceReader;


/**
 * <p><b>Title:</b><br/>
 * SWE Data Provider
 * </p>
 *
 * <p><b>Description:</b><br/>
 * TODO SWEDataProvider type description
 * </p>
 *
 * <p>Copyright (c) 2005</p>
 * @author Alexandre Robin
 * @date Nov 22, 2005
 * @version 1.0
 */
public class SWEProvider extends AbstractProvider
{
	protected DataStreamParser dataParser;
	protected SWEDataHandler dataHandler;
	
	
	public SWEProvider()
	{
		dataHandler = new SWEDataHandler();
	}
	
	
	@Override
	public void updateData() throws DataException
	{
		String uri = ((StaticDataSet)resource).getUrl();
		
		try
		{
			dataStream = URIStreamHandler.openStream(uri);
		}
		catch (CDMException e)
		{
			throw new DataException(e.getMessage());
		}
		
		// check that format is 'SWE'
		String format = ((StaticDataSet)resource).getFormat();
		if (!format.equalsIgnoreCase("SWE"))
			throw new DataException("Invalid format: " + format);
		
		// parse response
		try
		{
			SWEResourceReader reader = new SWEResourceReader();
			reader.parse(dataStream);
						
			// display data structure and encoding
			dataParser = reader.getDataParser();
			DataComponent dataInfo = dataParser.getDataComponents();
			DataEncoding dataEnc = dataParser.getDataEncoding();
			System.out.println(dataInfo);
			System.out.println(dataEnc);			
			
			// create data node if needed
			if (cachedData == null)
				cachedData = new DataNode();
			
			// clean up old data			
			clearData();
			cachedData.addComponent(dataInfo);
			
			// register the CDM data handler
			dataHandler.setDataNode(cachedData);
			dataParser.setDataHandler(dataHandler);
			
			// start parsing if not cancelled
			if (!canceled)
				dataParser.parse(reader.getDataStream());
		}
		catch (CDMException e)
		{
			throw new DataException("Error while parsing resource stream: " + uri);
		}
		finally
		{
			try
			{
				dataStream.close();
			}
			catch (IOException e)
			{
				throw new DataException("Error while closing resource stream: " + uri);
			}
		}
	}
	
	
	@Override
	public void cancelUpdate()
	{
		dataParser.stop();
		super.cancelUpdate();
	}
}
