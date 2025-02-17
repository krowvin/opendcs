/*
* $Id$
*
* $Log$
* Revision 1.2  2014/05/28 13:09:26  mmaloney
* dev
*
* Revision 1.1.1.1  2014/05/19 15:28:59  mmaloney
* OPENDCS 6.0 Initial Checkin
*
* Revision 1.3  2011/09/27 01:24:48  mmaloney
* Enhancements for SHEF and NOS Decoding.
*
* Revision 1.2  2011/09/21 18:27:08  mmaloney
* Updates to NOS decoding.
*
* Revision 1.1  2011/08/26 19:49:34  mmaloney
* Implemented the NOS decoders.
*
*/
package decodes.decoder;

import java.util.Calendar;
import java.util.Date;

import ilex.util.Logger;
import ilex.var.Variable;
import decodes.db.DecodesScript;
import decodes.db.Platform;
import decodes.db.PlatformConfig;
import decodes.decoder.DataOperations;
import decodes.decoder.DecodedMessage;
import decodes.decoder.DecodesFunction;

/** Handles NOS 6-minute message format. */
public class Nos6Min
	extends NosDecoder 
{
	public static final String module = "Nos6Min";

	public Nos6Min()
	{
		super();
	}
	
	public DecodesFunction makeCopy()
	{
		return new Nos6Min();
	}

	public String getFuncName() { return module; }

	/**
	 * No arguments expected for NOS 6 Min
	 */
	public void setArguments(String argString, DecodesScript script)
	{
	}
	
	public void execute(DataOperations dd, DecodedMessage msg)
		throws DecoderException
	{
		dataOps = dd;
		
		Platform platform = msg.getPlatform();
		if (platform == null)
			throw new DecoderException(module + " function cannot be called with null platform.");
		PlatformConfig config = platform.getConfig();
		if (config == null)
			throw new DecoderException(module + " function cannot be called with null config.");

		char msgType = (char)dd.curByte();
		if (msgType != 'P')
			throw new DecoderException(module + " requires 'P' in first char.");
		dd.forwardspace();

		byte field[] = dd.getField(7, null);
		String stationId = new String(field);
		msg.getRawMessage().setPM(PM_STATION_ID, new Variable(stationId));
		
		primaryDcpNum = dcpNum = (int)((char)dd.curByte()) - (int)'0';
		dd.forwardspace();
		msg.getRawMessage().setPM(PM_DCP_NUM, new Variable(dcpNum));

		Logger.instance().info(module + ": stationId=" + stationId + ", dcpNum=" + dcpNum);
		
		
		double datumOffset = getDouble(3, false);
		msg.getRawMessage().setPM(PM_DATUM_OFFSET, new Variable (datumOffset));
		double sensorOffset = getDouble(2, true);
		msg.getRawMessage().setPM(PM_SENSOR_OFFSET, new Variable (sensorOffset));
		int systemStatus = getInt(2, false);
		msg.getRawMessage().setPM(PM_SYSTEM_STATUS, new Variable(systemStatus));
		int timeOffset = getInt(1, false);
		Logger.instance().info(module + ": dataumOffset=" + datumOffset + 
			", sensorOffset=" + sensorOffset + ", timeOffset=" + timeOffset);
		boolean haveBV = false;

		initSensorIndeces();
		
		int x, y, z, wl, sigma, outlier;
		Date dataTime = null;
		Date redundantDataTime = null;
		Variable v;
		int sensorNum = -1; // current WL sensor num
		int ancSensor = -1; // current ancillary sensor num
		char c2;
		boolean firstTime = true;
		
		while(dd.moreChars())
		{
			char flag = (char)dd.curByte();
			Logger.instance().debug3(module + " flag=" + flag);
			dd.forwardspace();
			switch(flag)
			{
			case '0': // time tag
				x = getInt(2, false);
				setDayNumber(x);
				y = getInt(1, false);
				cal.set(Calendar.HOUR_OF_DAY, y);
				cal.set(Calendar.MINUTE, timeOffset);
				cal.set(Calendar.SECOND, 0);
				dataTime = cal.getTime();
				if (firstTime)
				{
					msg.getRawMessage().setPM(PM_STATION_TIME, new Variable(dataTime));
					Logger.instance().info(module + " Set station time to " + cal.getTime());
					firstTime = false;
				}
				cal.add(Calendar.MINUTE, -6);
				redundantDataTime = cal.getTime();
				Logger.instance().info(module + " daynum=" + x + ", hr=" + y 
					+ ", min=" + timeOffset + ", dataTime=" + dataTime 
					+ ", redundantTime=" + redundantDataTime);
				break;
			case '1': // PWL Aqua Track = Acousitic Water Level
			case '(': // Air Gap
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				x = getInt(2, false); // AQT1
				y = getInt(2, false); // AQT2
				sensorNum = getSensorNumber(
					flag == '1' ? 'A' : 
					flag == '(' ? 'Q' : 'A', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier
					+ "," + x + "," + y);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				break;
			case '2': // BWL
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				sensorNum = getSensorNumber('B', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				break;
			
			// The redundant blocks are all handled the same. We have
			// a 3-byte WL sample for 6 minutes ago for the sensor we
			// just parsed.
			case '>': // RWL (redundant for Aqua Track)
			case '"': // RBWL (redundant Backup
			case '.': // Redundant SAE = value from 6 min ago.
			case '#': // Redundant MWWL
			case ')': // Redundant Air Gap
			case '\'': // Redundant Paroscientific #1
			case '*': // Redundant Paroscientific #2
				v = getNumber(3, false);
				v.setFlags(v.getFlags() | NosDecoder.FLAG_REDUNDANT);
				if (sensorNum != -1) // sensor number already set from previous flag
				{
					msg.addSampleWithTime(sensorNum, v, redundantDataTime, 1);
					Logger.instance().info(module + ": redundant WL sensor " 
						+ sensorNum + " =" + v);
				}
				break;

			case '3': // Wind speed, dir, gust
				x = getInt(2, false);
				y = getInt(2, false);
				z = getInt(2, false);
				ancSensor = getSensorNumber('C', config);
				if (ancSensor == -1)
					continue;
				v = new Variable("" + x + "," + y + "," + z);
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '4': // Air temp
				v = getNumber(2, true);
				ancSensor = getSensorNumber('D', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '5': // Water Temp
				v = getNumber(2, true);
				ancSensor = getSensorNumber('E', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '6': // Barometric Pressure
				x = getInt(2, false) + 800;  // add 800 millibars to decoded value.
				ancSensor = getSensorNumber('F', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, new Variable(x), dataTime, 1);
				break;
			case '+': // Frequency #1 -- Ignore as per comment from Sudha
				break;
			case '!': // Shaft Angle Encoder (SAE)
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				sensorNum = getSensorNumber('V', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				Logger.instance().info(module + ": SAE sensor " + sensorNum 
					+ " ='" + v + "'");
				break;
			case '/': // unused flag
				break;
			case '7': // conductivity
				v = getNumber(2, true);
				ancSensor = getSensorNumber('G', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '8': // Microwave Water Level (MWWL)
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				sensorNum = getSensorNumber('Y', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				break;
			case '9': // Rel Hum
				v = getNumber(2, true);
				ancSensor = getSensorNumber('R', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case ':': // Rainfall
				v = getNumber(2, true);
				ancSensor = getSensorNumber('J', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case ';': // Solar Rad
				v = getNumber(2, true);
				ancSensor = getSensorNumber('K', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '<': // Analog #1 universally used for VB
				v = getNumber(2, true);
				ancSensor = getSensorNumber('L', config);
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				// Phil says that after VB, increment DCP number and reset sensor indeces
				dcpNum++;
				initSensorIndeces();
				break;
			case '=': // Analog #2
				v = getNumber(2, true);
				ancSensor = getSensorNumber('M', config);
				if (ancSensor == -1)
					continue;
				msg.addSampleWithTime(ancSensor, v, dataTime, 1);
				break;
			case '%': // Paroscientific #1 - Pressure WL (N1,N2)
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				sensorNum = getSensorNumber('N', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				break;
			case '&': // Paroscientific #2 - Pressure WL (T1, T2, T3)
				wl = getInt(3, false);
				sigma = getInt(2, false);
				outlier = getInt(1, false);
				sensorNum = getSensorNumber('T', config);
				if (sensorNum == -1)
					continue;
				v = new Variable("" + wl + "," + sigma + "," + outlier);
				msg.addSampleWithTime(sensorNum, v, dataTime, 1);
				break;
			case ',': // Unused flag
				break;
			case ' ': // Delimiter for single-char VB at end of message
				// Only try to get BV once to avoid reading past end of data.
				if (!haveBV)
				{
					v = new Variable(getDouble(1, true)+9.5);
					msg.getRawMessage().setPM(PM_NOS_BATTERY, v);
					haveBV = true;
				}
				break;
			case '-': // Start of 2-char flag
				c2 = (char)dd.curByte();
				dd.forwardspace();
				if (c2 == 'O')
				{
					x = getInt(3, false);
					y = getInt(1, false);
					ancSensor = getSensorNumber('O', config);
					if (ancSensor == -1)
						continue;
					msg.addSampleWithTime(ancSensor, 
						new Variable("" + x + "," + y), dataTime, 1);
				}
				break;
			case 'T': // start of Tsunami add-on block
				z = dcpNum;
				dcpNum = primaryDcpNum;
				sensorNum = getSensorNumber('U', config);
				cal.set(Calendar.HOUR_OF_DAY, getInt(1, false));
				cal.set(Calendar.MINUTE, getInt(1, false));
				x = getInt(1, false); // WL offset in units of 250mm (1/4m)
				for(int i=0; i<6; i++)
				{
					y = getInt(2, false);
					dataTime = cal.getTime();
					// Variable will have units of mm
					msg.addSampleWithTime(sensorNum,
						new Variable(x*250 + y), dataTime, 1);
					cal.add(Calendar.MINUTE, -1);
				}
				dcpNum = z;
				break;
			default:
				if (!Character.isWhitespace(flag))
					Logger.instance().warning(module + " Unrecognized flag char '" + flag + "'");
			}
		}
		Logger.instance().debug3(module + " end of message");
	}
}
