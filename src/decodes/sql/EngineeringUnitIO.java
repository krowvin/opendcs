/*
 * $Id$
 *
 * $State$
 *
 * $Log$
 * Revision 1.2  2013/04/12 17:09:58  mmaloney
 * column mask on inserts required for VPD
 *
 * Revision 1.1  2008/04/04 18:21:04  cvs
 * Added legacy code to repository
 *
 * Revision 1.14  2007/08/30 21:04:45  mmaloney
 * dev
 *
 * Revision 1.13  2007/04/25 13:56:58  ddschwit
 * Changed SELECT * to SELECT columns
 *
 * Revision 1.12  2005/03/15 16:52:02  mjmaloney
 * Rename Enum to DbEnum for Java 5 compatibility
 *
 * Revision 1.11  2004/09/02 12:15:26  mjmaloney
 * javadoc
 *
 * Revision 1.10  2004/04/20 20:08:19  mjmaloney
 * Working reference list editor, required several mods to SQL code.
 *
 * Revision 1.9  2002/10/06 14:23:58  mjmaloney
 * SQL Development.
 *
 * Revision 1.8  2002/09/30 18:54:34  mjmaloney
 * SQL dev.
 *
 * Revision 1.7  2002/09/24 13:17:48  mjmaloney
 * SQL dev.
 *
 * Revision 1.6  2002/09/22 18:39:54  mjmaloney
 * SQL Dev.
 *
 * Revision 1.5  2002/08/29 05:48:49  chris
 * Added RCS keyword headers.
 *
 *
 */
package decodes.sql;

import java.util.Iterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ilex.util.Logger;

import decodes.db.DatabaseException;
import decodes.db.EngineeringUnit;
import decodes.db.EngineeringUnitList;
import decodes.db.DbEnum;
import decodes.db.EnumValue;

/**
This class handles SQL IO for Engineering Units
*/
public class EngineeringUnitIO extends SqlDbObjIo
{
	/** 
	  Constructor. 
	  @param dbio the SqlDatabaseIO to which this IO object belongs
	*/
	public EngineeringUnitIO(SqlDatabaseIO dbio)
	{
		super(dbio);
	}

	/** 
	  Read the list from the EngineeringUnit table.  
	  @param euList the EngineeringUnitList to populate
	*/
	public void read(EngineeringUnitList euList)
		throws DatabaseException
	{
		Logger.instance().log(Logger.E_DEBUG1,
			"Reading Engineering Units...");
		Statement stmt = null;
		try
		{
			String q = 
				"SELECT unitAbbr, name, family, measures " +
				"FROM EngineeringUnit";
			debug3("Executing query '" + q + "'");
			stmt = createStatement();
			ResultSet rs = stmt.executeQuery(q);

			while (rs != null && rs.next()) 
			{
				String abbr = rs.getString(1);
				String name = rs.getString(2);
				String family = rs.getString(3);
				String measures = rs.getString(4);
				EngineeringUnit eu = new EngineeringUnit(abbr, name, family, measures);
				euList.add(eu);
			}
		}
		catch (SQLException ex) 
		{
			String msg = "Error reading EU List: " + ex;
			System.err.println(msg);
			ex.printStackTrace(System.err);
			throw new DatabaseException(ex.toString());
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); }
				catch(Exception ex) {}
		}
	}

	/** 
	  Write the whole list out to the database. 
	  @param euList the EngineeringUnitList to write
	*/
	public void write(EngineeringUnitList euList)
		throws DatabaseException,
				 SQLException
	{
		info("EngineeringUnitIO.write()");
		
		// First read the current list so we know what needs to be
		// updated, inserted, or deleted.
		EngineeringUnitList dbList = new EngineeringUnitList();
		read(dbList);

		for(Iterator<EngineeringUnit> euit = euList.iterator(); euit.hasNext(); )
		{
			EngineeringUnit eu = euit.next();
			EngineeringUnit dbeu = dbList.getByAbbr(eu.getAbbr());
			if (dbeu != null)
			{
				if (!dbeu.equals(eu))
				{
					String q = "update EngineeringUnit "
						+ "set name=" + sqlReqString(eu.getName())
						+ ", family=" + sqlOptString(eu.getFamily())
						+ ", measures=" + sqlOptString(eu.getMeasures())
						+ " where lower(unitabbr) = " 
						+ sqlString(eu.getAbbr().toLowerCase());
					tryUpdate(q);
				}
				dbList.remove(dbeu);
			}
			else // it must be new
			{
				String q = "INSERT INTO engineeringunit(UNITABBR,NAME,FAMILY,MEASURES)" +
					" VALUES (" +
					sqlReqString(eu.abbr) + ", " +
					sqlReqString(eu.getName()) + ", " +
					sqlOptString(eu.family) + ", " +
					sqlOptString(eu.measures)
					+ ")";
				tryUpdate(q);
			}
		}
		
		// Now anything remaining in the dbList must have been deleted.
		for(Iterator<EngineeringUnit> euit = dbList.iterator(); euit.hasNext(); )
		{
			EngineeringUnit dbeu = euit.next();
			if (!dbeu.getAbbr().equalsIgnoreCase("raw"))
			{
				tryUpdate("delete from unitconverter where " +
					"lower(fromunitsabbr) = "
					+ sqlString(dbeu.getAbbr().toLowerCase()));
			}
			tryUpdate("delete from engineeringunit where lower(unitabbr) = "
				+ sqlString(dbeu.getAbbr().toLowerCase()));
		}
	}

	/**
	  Writes a single EU record to the database.
	  @param eu the EU
	*
	public void write(EngineeringUnit eu)
		throws DatabaseException, SQLException
	{
		write(eu, true);
	}

	/** 
	  Write a single EngineeringUnit object to the SQL database. 
	  @param eu the EU
	  @param deleteFirst true if old object needs to be deleted first
	*/
	public void write(EngineeringUnit eu, boolean deleteFirst)
		throws DatabaseException, SQLException
	{
		if (deleteFirst)
		{
			String q = "DELETE FROM engineeringunit WHERE unitabbr = "
				+ sqlReqString(eu.abbr);
			tryUpdate(q);
		}
		
		String q = "INSERT INTO engineeringunit(UNITABBR,NAME,FAMILY,MEASURES) VALUES (" +
				sqlReqString(eu.abbr) + ", " +
				sqlReqString(eu.getName()) + ", " +
				sqlOptString(eu.family) + ", " +
				sqlOptString(eu.measures)
				+ ")";

		executeUpdate(q);
	}
}


