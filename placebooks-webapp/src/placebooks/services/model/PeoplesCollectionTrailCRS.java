package placebooks.services.model;

import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
public class PeoplesCollectionTrailCRS
{
	protected String type;
	protected HashMap<String, String> properties;

	public PeoplesCollectionTrailCRS(final String type, final HashMap<String, String> properies)
	{
		this.type = type;
		properties = properies;
	}

	PeoplesCollectionTrailCRS()
	{
	}

	public HashMap<String, String> GetProperties()
	{
		return properties;
	}

	public String GetType()
	{
		return type;
	}
}