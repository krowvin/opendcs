package decodes.drgsinfogui;

import ilex.util.EnvExpander;
import ilex.util.Logger;
import ilex.xml.DomHelper;
import ilex.xml.XmlOutputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains the methods to read and write the DRGS Receivers 
 * information xml file.
 * This class creates an xml file in the DECODES_INSTALL_DIR/drgsident
 * directory. The file containing the DRGS Receiver identification information
 * is called drgsident.xml.
 * This file is converted to html so that it can be a link in the message
 * html page created by the HtmlFormatter. This is going to be used when
 * the HtmlFormatter is used by the Dcp Monitor. Otherwise this link will
 * not show up in the html output generated by HtmlFormatter.
 * 
 */
public class DrgsReceiverIo
{
	private static String module = "DrgsReceiverIo";
	private static String drgsDir = 
		EnvExpander.expand("$DECODES_INSTALL_DIR/drgsident");
	public static final String drgsRecvXmlFname = drgsDir + "/drgsident.xml";
	public static final String drgsRecvXsl = drgsDir + "/drgsident.xsl";
	public static final String drgsRecvHtml = drgsDir + "/drgsident.html";
	/** Store Drgs Ident List in memory */
	private static ArrayList<DrgsReceiverIdent> drgsList = null;
	
	/** Constructor. Empty */
	public DrgsReceiverIo()
	{	
	}
	
	/**
	 * Create the drgsident.xml file with the infomation provided in
	 * the drgsRecvList.
	 * 
	 * @param drgsRecvList
	 */
	public static void writeDrgsReceiverInfo(
		ArrayList<DrgsReceiverIdent> drgsRecvList)
		throws IOException
	{
		makeDir(drgsRecvXmlFname);
		
		//Open an output stream wrapped by an XmlOutputStream
		FileOutputStream fos;
		fos = new FileOutputStream(drgsRecvXmlFname);
		XmlOutputStream xos = 
			new XmlOutputStream(fos, DrgsIdentTags.drgsIdent);
		xos.writeXmlHeader();

		xos.startElement(DrgsIdentTags.drgsIdent);
		//Loop through the array list and create and ident entry in the xml
		//Sort by code
		Collections.sort(drgsRecvList, new CodeColumnComparator()); 
		for (DrgsReceiverIdent ident : drgsRecvList)
		{
			xos.startElement(DrgsIdentTags.ident);
			
			xos.writeElement(DrgsIdentTags.code, ident.getCode());
			xos.writeElement(DrgsIdentTags.description, 
												ident.getDescription());
			xos.writeElement(DrgsIdentTags.location, ident.getLocation());
			xos.writeElement(DrgsIdentTags.contact, ident.getContact());
			xos.writeElement(DrgsIdentTags.email, ident.getEmailAddr());
			xos.writeElement(DrgsIdentTags.phone, ident.getPhoneNum());
			
			xos.endElement(DrgsIdentTags.ident);
		}
		
		xos.endElement(DrgsIdentTags.drgsIdent);
		fos.close();
		
		//Need to create an HTML version of this xml file
		convertXmlToHtml();
	}
	
	/**
	 * Convert the XML file into HTML.
	 *
	 */
	private static void convertXmlToHtml()
	{
		
		try 
		{
			createXSL();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer
				(new javax.xml.transform.stream.StreamSource(drgsRecvXsl));
			
			transformer.transform(new javax.xml.transform.stream.StreamSource
							(drgsRecvXmlFname),
							new javax.xml.transform.stream.StreamResult
							( new FileOutputStream(drgsRecvHtml)));
		}
		catch (Exception ex) 
		{
			Logger.instance().failure(module + ":convertXmlToHtml " +
				" can not generate HTML version of the DRGS Recv XML file. " 
				+ ex.getMessage());
		}	
	}

	/**
	 * Create the XSL to be used when transforming the XML to HTML. This is 
	 * used to create an HTML version of the DRGS Receiver info XML file.
	 * This structure needs to match with the XML structure.
	 * 
	 * @return
	 */
	private static String createXSL()
	{
		StringBuilder st = new StringBuilder("");
		st.append("<?xml version=\"1.0\" " +
				"encoding=\"UTF-8\" standalone=\"yes\"?> \n");
		
		st.append("<xsl:stylesheet " +
				"xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" "
				+ "\nversion=\"1.0\"> ");
		
		st.append("\n<xsl:template match=\"/\">");
		
		st.append("\n<html>");
		st.append("\n<head><title>DRGS Receiver Identification" +
				"</title></head>");
		
		
		
		st.append("\n<body>");
		
		st.append("<h2 style=\"text-align: center;\">");
		st.append("DRGS Receiver Identification");
		st.append("</h2>");
		st.append("\n<br></br>");
		
		st.append("\n<table border=\"1\" align=\"CENTER\" >");
			
		st.append("\n<tr>");
		
		st.append("\n<th>Code</th>");
		st.append("\n<th>Description</th>");
		st.append("\n<th>Location</th>");
		st.append("\n<th>Contact</th>");
		st.append("\n<th>E-mail</th>");
		st.append("\n<th>Phone #</th>");
		
		st.append("\n</tr>");			
			
		st.append("\n<xsl:for-each select=\"drgsident/ident\">");
		
		st.append("\n<tr>");
			
		st.append("\n<td><xsl:value-of select=\"code\"/></td>");
		st.append("\n<td><xsl:value-of select=\"description\"/></td>");
		st.append("\n<td><xsl:value-of select=\"location\"/></td>");
		st.append("\n<td><xsl:value-of select=\"contact\"/></td>");
		st.append("\n<td><xsl:value-of select=\"email\"/></td>");
		st.append("\n<td><xsl:value-of select=\"phone\"/></td>");
		
		st.append("\n</tr>");
		
		st.append("\n</xsl:for-each>");
		
		st.append("\n</table>");
			
		st.append("\n</body></html>");
			
		st.append("\n</xsl:template>");
			
		st.append("\n</xsl:stylesheet>");

		writeXslToFile(st.toString());
		
		return st.toString();
	}
	
	/**
	 * Create the XSL file.
	 * 
	 * @param buffer
	 */
	private static void writeXslToFile(String buffer)
	{
		FileWriter fw;
		try
		{
			fw = new FileWriter(drgsRecvXsl);
			fw.write(buffer);
			fw.flush();
			fw.close();
		} catch (IOException e)
		{
			Logger.instance().failure(module
					+ e.getMessage());
			
		}
	}
	
	/**
	 * Reads the DRGS Receiver Identification information from the XML
	 * file located on DECODES INSTALL HOME DIR/drgsindent directory.
	 * Parse out the xml file and create an array list per each
	 * "ident" entry in the xml file.
	 *  
	 * @return drgsRecvList - array list with all information read from
	 * the DRGS xml file.
	 */
	public static ArrayList<DrgsReceiverIdent> readDrgsReceiverInfo()
	{
		ArrayList<DrgsReceiverIdent> drgsRecvList = 
										new ArrayList<DrgsReceiverIdent>();
		Document doc = readDrgsXMLFile(drgsRecvXmlFname);
		if (doc == null)
		{
			Logger.instance().debug1(module + 
					":readDrgsReceiverInfo document is null");
		}
		else
		{
			//Parse the xml file and add each identification entry to the 
			//array list
			parseXMLFile(doc, drgsRecvList);
		}
		return drgsRecvList;
	}
	
	/**
	 * Reads the Drgs identification xml file.
	 *  
	 * @param xmlFileName
	 * @return
	 */
	private static Document readDrgsXMLFile(String xmlFileName)
	{
		Document doc = null;
		try
		{
			//check to make sure that the file xmlFile  exists
			File temp = new File(xmlFileName);
			if (temp.canRead())
			{
				doc = DomHelper.readFile(module, xmlFileName);
			}
		}
		catch(ilex.util.ErrorException ex)
		{
			Logger.instance().failure(module + ":readDrgsXMLFile " 
					+ ex.getMessage());
		}
		return doc;
	}

	/**
	 * Parse XML file and add each identification entry in the 
	 * given array list.
	 *   
	 * @param doc
	 * @param drgsRecvList
	 */
	private static void parseXMLFile(Document doc, 
								ArrayList<DrgsReceiverIdent> drgsRecvList)
	{
		Node drgsIdentElement = doc.getDocumentElement();
		if (!drgsIdentElement.getNodeName().equalsIgnoreCase(
													DrgsIdentTags.drgsIdent))
		{
			String s = module 
				+ ": Wrong type of DRGS Receiver Identification XML file " +
				"-- Cannot Read Info. Root element is not 'drgsident'.";
			Logger.instance().failure(s);
		}
		else
		{
			NodeList children = drgsIdentElement.getChildNodes();
			if (children != null)
			{
				for(int i=0; i<children.getLength(); i++)
				{
					Node node = children.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE)
					{	//Find the ident tag
						if (node.getNodeName().equalsIgnoreCase(
													DrgsIdentTags.ident))
						{	//Get all info for this entry
							NodeList identChildren = node.getChildNodes();
							if (identChildren != null)
							{
								addEntryToList(drgsRecvList, identChildren);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Add this entry info to the given array list.
	 * @param drgsRecvList
	 * @param identChildren
	 */
	private static void addEntryToList(
			ArrayList<DrgsReceiverIdent> drgsRecvList, NodeList identChildren)
	{
		String code = "";
		String description = "";
		String location = "";
		String contact = "";
		String email = "";
		String phone = "";
		for(int x=0; x<identChildren.getLength(); x++)
		{
			Node child = identChildren.item(x);
			if (child.getNodeType() == 
									Node.ELEMENT_NODE)
			{
				if (child.getNodeName().
				equalsIgnoreCase(DrgsIdentTags.code))
				{	
					code = 
					DomHelper.getTextContent(child);
				}
				else if (child.getNodeName().
						equalsIgnoreCase(
							DrgsIdentTags.description))
				{	
					description = 
					DomHelper.getTextContent(child);
				}
				else if (child.getNodeName().
					equalsIgnoreCase(
							DrgsIdentTags.location))
				{	
					location = 
					DomHelper.getTextContent(child);
				}
				else if (child.getNodeName().
					equalsIgnoreCase(
							DrgsIdentTags.contact))
				{	
					contact = 
					DomHelper.getTextContent(child);
				}
				else if (child.getNodeName().
					equalsIgnoreCase(
							DrgsIdentTags.email))
				{	
					email = 
					DomHelper.getTextContent(child);
				}
				else if (child.getNodeName().
					equalsIgnoreCase(
							DrgsIdentTags.phone))
				{	
					phone = 
					DomHelper.getTextContent(child);
				}
				
			}
		} //end for
		//Add entry to ArrayList
		drgsRecvList.add(
				new DrgsReceiverIdent(code,
				description, location, contact,
				email, phone));
	}

	/**
	 * Creates a directory if it does not exist.
	 * @param dirStr
	 */
	private static void makeDir(String dirStr)
	{
		String dir = dirStr;
		int lastIndx = dirStr.lastIndexOf(File.separator);
		if (lastIndx != -1)
			dir = dirStr.substring(0, lastIndx);
		File directory = new File(dir);
		if (!directory.isDirectory())
			directory.mkdirs();
	}
	
	/**
	 * Find the description for the given code.
	 * 
	 * @param code
	 * @return
	 */
	public static String findDescforCode(String code)
	{
		if (code == null || code.equals(""))
			return "";
		
		String description = "";
		if (drgsList != null && drgsList.size() > 0)
		{
			for (DrgsReceiverIdent dr : drgsList)
			{
				if (dr != null)
				{
					if (dr.getCode().equalsIgnoreCase(code))
					{
						//Found code - get description
						description = dr.getDescription();
						break;
					}
				}
			}
		}
		return description;
	}
	
	/**
	 * Read the Drgs Receiver Identification List and
	 * store it in memory.
	 *
	 */
	public static void readDrgsReceiverFile()
	{
		drgsList = readDrgsReceiverInfo();
	}

	/**
	 * Return the file path of the DRGS XML file.
	 * 
	 * @return
	 */
	public static String getDrgsRecvXmlFname()
	{
		return drgsRecvXmlFname;
	}
}
