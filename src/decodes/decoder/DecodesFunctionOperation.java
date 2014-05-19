/*
*	$Id$
*/
package decodes.decoder;

/**
This is an abstract base class for all decodes operations.
*/
public abstract class DecodesFunctionOperation 
	extends DecodesOperation
{
	/**
	  Constructor is only called from subclass.
	*/
	protected DecodesFunctionOperation()
	{
		super(1);
	}

	/**
	 * Functions are usually constructed by registering a copy in the
	 * tokenizer. The tokenizer then uses copy() when needed.
	 */
	public abstract DecodesFunctionOperation copy();

	public void setRepetitions(int n)
	{
		repetitions = n;
	}

	/**
	  All function operations have a type 'M' (for method). Each one
	  has a unique function name.
	  @return 'M'
	*/
	public char getType() { return 'M'; }

	/**
	 * Each DECODES Function must have a unique name with 2 or more characters,
	 * with the first character being a letter and all subsequent characters
	 * being letter, number, or underscore.
	 * @return the unique name for this function.
	 */
	public abstract String getFunctionName();

	/**
	  Subclass overides this operation to execute the operation against
	  the raw data contained in the passed DataOperations object, and
	  place the resulting data samples in the passed DecodedMessage object.

	  @param dd the DataOperations holds raw data and context.
	  @param msg the DecodedMessage into which decoded data is placed.

	  @throws DecoderException subclasses on various error conditions.
	*/
	public abstract void execute(DataOperations dd, DecodedMessage msg) 
		throws DecoderException;

	/** 
	 * Each function must parse its own arguments. The value passed is
	 * everything inside the parens after the function name.
	 */
	public abstract void setArguments(String argString)
		throws ScriptFormatException;
}

