package decodes.cwms.rating;

import java.util.List;

import lrgs.gui.DecodesInterface;

import decodes.cwms.CwmsTimeSeriesDb;
import decodes.tsdb.TsdbAppTemplate;
import decodes.util.DecodesException;

public class ListRatings extends TsdbAppTemplate
{

	public ListRatings(String logname)
	{
		super(logname);
	}

	@Override
	protected void runApp()
		throws Exception
	{
		CwmsRatingDao crd = new CwmsRatingDao((CwmsTimeSeriesDb)theDb);
		List<CwmsRatingRef> ratings = crd.listRatings(null);
		for(CwmsRatingRef crr : ratings)
			System.out.println(crr.toString());
	}

	public void initDecodes()
		throws DecodesException
	{
		DecodesInterface.silent = true;
		DecodesInterface.initDecodesMinimal(cmdLineArgs.getPropertiesFile());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
		throws Exception
	{
		ListRatings app = new ListRatings("util.log");
		app.execute(args);
	}

}
