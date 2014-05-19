/*
*  $Id$
*
*  This is open-source software written by ILEX Engineering, Inc., under
*  contract to the federal government. You are free to copy and use this
*  source code for your own purposes, except that no part of this source
*  code may be claimed to be proprietary.
*
*  Except for specific contractual terms between ILEX and the federal 
*  government, this source code is provided completely without warranty.
*  For more information contact: info@ilexeng.com
*
*  $Log$
*  Revision 1.5  2013/03/21 18:27:39  mmaloney
*  DbKey Implementation
*
*  Revision 1.4  2011/09/09 21:58:52  gchen
*  Fix a bug for Oracle sequence key generator with adding a trim function to remove the tailing spaces.
*
*  Revision 1.3  2009/01/03 20:31:14  mjmaloney
*  Added Routing Spec thread-specific database connections for synchronization.
*
*  Revision 1.2  2008/12/23 22:26:42  mbogner
*  create new sequence generator specifically for HDB only  M Bogner
*
*  Revision 1.1  2008/04/04 18:21:04  cvs
*  Added legacy code to repository
*
*  Revision 1.4  2007/12/11 01:05:19  mmaloney
*  javadoc cleanup
*
*  Revision 1.3  2007/11/20 14:27:35  mmaloney
*  dev
*
*  Revision 1.2  2006/05/03 21:01:01  mmaloney
*  dev
*
*  Revision 1.1  2005/06/09 20:45:44  mjmaloney
*  Modifications for Oracle compatibility.
*
*/
package decodes.sql;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import decodes.db.DatabaseException;

import ilex.util.Logger;

/**
Implements the KeyGenerator interface by using SQL sequences using the
ORACLE syntax.
This class is the same as the vanilla SequenceKeyGenerator, except
that it uses the oracle attribute syntax, rather than the postgres
function call syntax.
This implementation will work with Version 5 or Version 6 database.
*/
public class OracleSequenceKeyGenerator
	implements KeyGenerator
{

	/** Default constructor. */
	public OracleSequenceKeyGenerator()
	{
	}

	/**
	  Generates a database-unique key suitable for adding a new entry
	  to the table.
	  The 'tableName' argument should be the name of the table to which
	  a new record will be added.
	  @param tableName the tableName for which a key is needed
	  @return numeric key for a new row in this table
	*/
	public DbKey getKey(String tableName, Connection conn)
		throws DatabaseException
	{
		String seqname;
		if (tableName.equalsIgnoreCase("EquipmentModel"))
			seqname = "EquipmentIdSeq";
		else
			seqname = tableName.trim() + "IdSeq";
		//String q = "SELECT nextval('" + seqname + "');";
		String q = "SELECT " + seqname.trim() + ".nextval from dual";

		try
		{
			Statement stmt = conn.createStatement();
	
			ResultSet rs = stmt.executeQuery(q);
			if (rs == null || !rs.next())
			{
				String err = "Cannot read sequence value from '" + seqname 
					+ "': " + (rs == null ? "Null Return" : "Empty Return");
				Logger.instance().log(Logger.E_FAILURE, err);
				throw new DatabaseException(err);
			}
	
			DbKey ret = DbKey.createDbKey(rs, 1);
			stmt.close();
			Logger.instance().debug3("OracleSequenceGenerator.getKey: " + q + " returning " + ret);
			return ret;
		}
		catch(SQLException ex)
		{
			String err = "SQL Error executing '" + q + "': " + ex;
			Logger.instance().log(Logger.E_FAILURE, err);
			throw new DatabaseException(err);
		}
	}
}

