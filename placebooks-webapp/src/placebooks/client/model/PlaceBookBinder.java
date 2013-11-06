package placebooks.client.model;

import java.util.Iterator;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;

public class PlaceBookBinder extends JavaScriptObject
{
	protected PlaceBookBinder()
	{

	}

	public final native void add(int index, PlaceBook page)
	/*-{
		this.pages.splice(index, 0, page);
	}-*/;

	public final native void add(PlaceBook page)
	/*-{
		this.pages.push(page);
	}-*/;

	public final native void clearPages()
	/*-{
		this.pages = new Array();
	}-*/;

	public final Iterable<PlaceBookGroup> getGroups()
	{
		return new Iterable<PlaceBookGroup>()
		{
			@Override
			public Iterator<PlaceBookGroup> iterator()
			{
				return new JSIterator<PlaceBookGroup>(getGroupsImpl());
			}
		};
	}

	public final native String getId()
	/*-{
		return this.id;
	}-*/;

	public final native String getMetadata(String name)
	/*-{
		return this.metadata[name];
	}-*/;

	public final native String getMetadata(String name, final String defaultValue)
	/*-{
		if ('metadata' in this && name in this.metadata) {
			return this.metadata[name];
		}
		return defaultValue;
	}-*/;

	public final native User getOwner() /*-{
										return this.owner;
										}-*/;

	public final Iterable<PlaceBook> getPages()
	{
		return new Iterable<PlaceBook>()
		{
			@Override
			public Iterator<PlaceBook> iterator()
			{
				return new JSIterator<PlaceBook>(getPagesInternal());
			}
		};
	}

	public final native int getParameter(String name) /*-{
														return this.parameters[name];
														}-*/;

	public final native int getParameter(String name, final int defaultValue)
	/*-{
		if ('parameters' in this && name in this.parameters) {
			return this.parameters[name];
		}
		return defaultValue;
	}-*/;

	public final JSONObject getPermissions()
	{
		return new JSONObject(getPermissionsImpl());
	}

	public final native String getState() /*-{
											return this.state;
											}-*/;

	public final native boolean hasMetadata(String name) /*-{
															return 'metadata' in this && name in this.metadata;
															}-*/;

	public final native boolean hasParameter(String name) /*-{
															return 'parameters' in this && name in this.parameters;
															}-*/;

	public final native void remove(final PlaceBook page)
	/*-{
		var idx = this.pages.indexOf(page);
		if (idx != -1) {
			this.pages.splice(idx, 1);
		}
	}-*/;

	public final native void removeParameter(String name)
	/*-{
		if (('parameters' in this)) {
			delete this.parameters[name];
		}
	}-*/;

	public final native void setId(String text) /*-{
												this.id = text;
												}-*/;

	public final native void setMetadata(String name, String value)
	/*-{
		if (!('metadata' in this)) {
			this.metadata = new Object();
		}
		this.metadata[name] = value;
	}-*/;

	public final native void setPages(final JsArray<PlaceBook> pages)
	/*-{
		this.pages = pages;
	}-*/;

	public final native void setParameter(String name, int value)
	/*-{
		if (!('parameters' in this)) {
			this.parameters = new Object();
		}
		this.parameters[name] = value;
	}-*/;

	private final native JsArray<PlaceBookGroup> getGroupsImpl()
	/*-{
		if(!('groups' in this))
		{
			this.groups = new Array();
		}
		return this.groups;
	}-*/;

	private final native JsArray<PlaceBook> getPagesInternal()
	/*-{
		if (!('pages' in this)) {
			this.pages = new Array();
		}
		return this.pages;
	}-*/;

	private final native JavaScriptObject getPermissionsImpl()
	/*-{
		return this.perms;
	}-*/;
}
