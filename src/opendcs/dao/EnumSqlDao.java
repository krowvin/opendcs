/*
 * $Id$
 * 
 * $Log$
 */
package opendcs.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import opendcs.dai.EnumDAI;

import decodes.db.DbEnum;
import decodes.db.EnumList;
import decodes.db.EnumValue;
import decodes.sql.DbKey;
import decodes.sql.DecodesDatabaseVersion;
import decodes.tsdb.DbIoException;

/**
 * Data Access Object for writing/reading DbEnum objects to/from a SQL database
 * @author mmaloney Mike Maloney, Cove Software, LLC
 */
public class EnumSqlDao 
	extends DaoBase 
	implements EnumDAI
{
	private static DbObjectCache<DbEnum> cache = new DbObjectCache<DbEnum>(3600000, false);
	
	public EnumSqlDao(DatabaseConnectionOwner tsdb)
	{
		super(tsdb, "EnumSqlDao");
	}
	
	private String getEnumColumns(int dbVer)
	{
		return "id, name"
			+ (dbVer >= DecodesDatabaseVersion.DECODES_DB_10 ? ", defaultValue, description "
			: dbVer >= DecodesDatabaseVersion.DECODES_DB_6 ? ", defaultvalue " 
			: " ");
	}
	
	private DbEnum rs2Enum(ResultSet rs, int dbVer)
		throws DbIoException, SQLException
	{
		DbKey id = DbKey.createDbKey(rs, 1);
		DbEnum en = new DbEnum(id, rs.getString(2));

		if (dbVer >= DecodesDatabaseVersion.DECODES_DB_6)
		{
			String def = rs.getString(3);
			if (!rs.wasNull())
				en.setDefault(def.trim());
		}
		if (dbVer >= DecodesDatabaseVersion.DECODES_DB_10)
		{
			en.setDescription(rs.getString(4));
		}
		return en;
	}
	
	@Override
	public DbEnum getEnum(String enumName) 
		throws DbIoException
	{
		synchronized(cache)
		{
			DbEnum ret = cache.getByUniqueName(enumName);
			if (ret != null)
				return ret;
			
			int dbVer = db.getDecodesDatabaseVersion();
			String q = "SELECT " + getEnumColumns(dbVer) + " FROM Enum";
			q = q + " where lower(name) = " + sqlString(enumName.toLowerCase());
			ResultSet rs = doQuery(q);
	
			try
			{
				if (rs == null || !rs.next())
				{
					warning("No such enum '" + enumName + "'");
					return null;
				}
				DbEnum en = rs2Enum(rs, dbVer);
				readValues(en);
				cache.put(en);
				return en;
			}
			catch (SQLException ex)
			{
				String msg = "Error in query '" + q + "': " + ex;
				warning(msg);
				throw new DbIoException(msg);
			}
		}
	}

	@Override
	public void readEnumList(EnumList top) 
		throws DbIoException
	{
		int dbVer = db.getDecodesDatabaseVersion();
		String q = "SELECT " + getEnumColumns(dbVer) + " FROM Enum";
		ResultSet rs = doQuery(q);

		try
		{
			synchronized(cache)
			{
				while (rs != null && rs.next())
				{
					DbEnum en = rs2Enum(rs, dbVer);
					readValues(en);
					cache.put(en);
				}
			}
		}
		catch (SQLException ex)
		{
			String msg = "Error in query '" + q + "': " + ex;
			warning(msg);
			throw new DbIoException(msg);
		}
	}

	@Override
	public void writeEnumList(EnumList enumList) throws DbIoException
	{
		// Save off the values I want to be in the database
		ArrayList<DbEnum> newenums = new ArrayList<DbEnum>();
		for(Iterator<DbEnum> evit = enumList.iterator(); evit.hasNext(); )
			newenums.add(evit.next());

		// CLear the list and read whats currently in the database
		enumList.clear();
		readEnumList(enumList);
		
		// Write the new stuff & check it off from the old.
		for (DbEnum newenum : newenums)
		{
			DbEnum oldenum = enumList.getEnum(newenum.enumName);
			if (oldenum != null)
			{
				enumList.remove(oldenum);
				newenum.forceSetId(oldenum.getId());
			}
			writeEnum(newenum);
		}
		// Anything left in the list is an enumeration that needs to be completely removed.
		for(DbEnum oldenum : enumList.getEnumList())
		{
			info("writeEnumList Deleting enum '" + oldenum.enumName + "'");
			String q = "DELETE FROM EnumValue WHERE enumId = " + oldenum.getId();
			doModify(q);
			q = "delete from enum where id = " + oldenum.getId();
			doModify(q);
		}
		for(DbEnum newenum : newenums)
			enumList.addEnum(newenum);
	}

	@Override
	public void writeEnum(DbEnum dbenum)
		throws DbIoException
	{
		int dbVer = db.getDecodesDatabaseVersion();
		String q = "";
		if (dbenum.idIsSet())
		{
			q = "update enum set name = " + sqlString(dbenum.getUniqueName());
			if (dbVer >= DecodesDatabaseVersion.DECODES_DB_6)
			{
				q = q + ", defaultvalue = " + sqlString(dbenum.getDefault());
				if (dbVer >= DecodesDatabaseVersion.DECODES_DB_10)
					q = q + ", description = " + sqlString(dbenum.getDescription());
			}
			q = q + " where id = " + dbenum.getId();
		}
		else // New enum, allocate a key and insert
		{
			DbKey id = getKey("Enum");
			dbenum.forceSetId(id);
			q = "insert into enum";
			if (dbVer < DecodesDatabaseVersion.DECODES_DB_6)
				q = q + "(id, name) values (" 
					+ id + ", " + sqlString(dbenum.getUniqueName()) + ")";
			else if (dbVer < DecodesDatabaseVersion.DECODES_DB_10)
				q = q + "(id, name, defaultValue) values (" 
					+ id + ", " + sqlString(dbenum.getUniqueName())
					+ ", " + sqlString(dbenum.getDefault()) + ")";
			else
				q = q + "(id, name, defaultValue, description) values (" 
					+ id + ", " + sqlString(dbenum.getUniqueName())
					+ ", " + sqlString(dbenum.getDefault()) 
					+ ", " + sqlString(dbenum.getDescription()) + ")";
			cache.put(dbenum);
		}
		doModify(q);

		// Delete all enum values. They'll be re-added below.
		info("writeEnum deleting values from enum '" + dbenum.enumName + "'");
		q = "DELETE FROM EnumValue WHERE enumId = " + dbenum.getId();
		doModify(q);
		
		for (Iterator<EnumValue> it = dbenum.iterator(); it.hasNext(); )
			writeEnumValue(it.next());
	}
	
	private void readValues(DbEnum dbenum)
		throws SQLException, DbIoException
	{
		int dbVer = db.getDecodesDatabaseVersion();

		String q = 
			"SELECT enumId, enumValue, description, " +
			"execClass, editClass";
		if (dbVer >= DecodesDatabaseVersion.DECODES_DB_6)
			q = q + ", sortNumber";
		q = q + " FROM EnumValue WHERE EnumID = " + dbenum.getId();
		ResultSet rs = doQuery2(q);

		while (rs != null && rs.next()) 
		{
			String enumValue = rs.getString(2);
			String description = rs.getString(3);
			String execClass = rs.getString(4);
			String editClass = rs.getString(5);

			int sn = 0;
			boolean setSortNumber = false;
			if (dbVer >= DecodesDatabaseVersion.DECODES_DB_6)
			{
				sn = rs.getInt(6);
				if (!rs.wasNull())
					setSortNumber = true;
			}
			EnumValue ev = 
				dbenum.replaceValue(enumValue, description, execClass, editClass);
			if (setSortNumber)
				ev.sortNumber = sn;
		}
	}

	/**
	* Write a single EnumValue to the database.
	* Assume no conflict with EnumValues already in the database.
	* @param ev the EnumValue
	*/
	public void writeEnumValue(EnumValue ev)
		throws DbIoException
	{
		String q =
			"INSERT INTO EnumValue VALUES(" +
				ev.dbenum.getId() + ", " +
				sqlString(ev.value) + ", " +
				sqlString(ev.description) + ", " +
				sqlString(ev.execClassName) + ", " +
				sqlString(ev.editClassName);
		if (db.getDecodesDatabaseVersion() < DecodesDatabaseVersion.DECODES_DB_6)
			q += ")";
		else if (ev.sortNumber == EnumValue.UNDEFINED_SORT_NUMBER)
			q += ", NULL)";
		else
			q = q + ", " + ev.sortNumber + ")";

		doModify(q);
	}

}
