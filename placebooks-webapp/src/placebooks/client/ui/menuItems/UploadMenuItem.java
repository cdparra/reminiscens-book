package placebooks.client.ui.menuItems;

import placebooks.client.controllers.PlaceBookController;
import placebooks.client.ui.UIMessages;
import placebooks.client.ui.dialogs.PlaceBookUploadDialog;
import placebooks.client.ui.items.frames.PlaceBookItemFrame;

import com.google.gwt.core.client.GWT;

public class UploadMenuItem extends MenuItem
{
	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private final PlaceBookController controller;
	private final PlaceBookItemFrame item;

	public UploadMenuItem(final PlaceBookController controller, final PlaceBookItemFrame item)
	{
		super(uiMessages.upload());

		this.controller = controller;
		this.item = item;
	}

	@Override
	public void run()
	{
		final PlaceBookUploadDialog dialog = new PlaceBookUploadDialog(controller, item.getItemWidget());
		dialog.show();
	}
}
