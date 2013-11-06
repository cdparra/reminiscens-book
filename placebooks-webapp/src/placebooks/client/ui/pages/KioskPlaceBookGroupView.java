package placebooks.client.ui.pages;

import placebooks.client.PlaceBooks;
import placebooks.client.controllers.GroupController;
import placebooks.client.controllers.PlaceBookItemController;
import placebooks.client.model.Shelf;
import placebooks.client.ui.UIMessages;
import placebooks.client.ui.dialogs.PlaceBookDialog;
import placebooks.client.ui.items.ImageItem;
import placebooks.client.ui.pages.places.PlaceBook;
import placebooks.client.ui.views.PlaceBookShelf;
import placebooks.client.ui.views.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class KioskPlaceBookGroupView extends Page implements View<Shelf>
{
	interface PlaceBookLibraryUiBinder extends UiBinder<Widget, KioskPlaceBookGroupView>
	{
	}

	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private static PlaceBookLibraryUiBinder uiBinder = GWT.create(PlaceBookLibraryUiBinder.class);

	@UiField
	Label title;

	@UiField
	Label description;

	@UiField
	SimplePanel imagePanel;

	@UiField
	Image qrcode;
	
	@UiField
	PlaceBookShelf shelf;

	private final GroupController controller = new GroupController();

	public KioskPlaceBookGroupView(final String id)
	{
		controller.load(id);
	}

	@Override
	public Widget createView()
	{
		final Widget library = uiBinder.createAndBindUi(this);

		Window.setTitle(uiMessages.placebooksLibrary());

		controller.add(this);
		controller.add(shelf);

		return library;
	}
	
	@UiHandler("qrcode")
	void showQRCode(ClickEvent event)
	{
		PlaceBookDialog dialog = new PlaceBookDialog()
		{
			
		};
		dialog.setTitle("QR Code");
		dialog.setWidget(new Image(PlaceBooks.getServer().getHostURL() + "placebooks/a/qrcode/group/" + controller.getItem().getGroup().getId()));
		dialog.show();
	}

	@Override
	public void itemChanged(final Shelf shelf)
	{
		title.setText(shelf.getGroup().getTitle());
		description.setText(shelf.getGroup().getDescription());

		this.shelf.setType(PlaceBook.Type.kiosk);
		
		qrcode.setUrl(PlaceBooks.getServer().getHostURL() + "placebooks/a/qrcode/group/" + controller.getItem().getGroup().getId());
		
		if (imagePanel.getWidget() == null)
		{
			final PlaceBookItemController itemController = new PlaceBookItemController(shelf.getGroup().getItem(),
					controller, false);
			imagePanel.setWidget(new ImageItem(itemController));
		}
		else
		{
			((ImageItem) imagePanel.getWidget()).itemChanged(shelf.getGroup().getItem());
		}
	}
}