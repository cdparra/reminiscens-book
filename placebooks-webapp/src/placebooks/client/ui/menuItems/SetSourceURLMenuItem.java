package placebooks.client.ui.menuItems;

import placebooks.client.Resources;
import placebooks.client.ui.UIMessages;
import placebooks.client.ui.items.frames.PlaceBookItemFrame;
import placebooks.client.ui.views.DragController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SetSourceURLMenuItem extends MenuItem
{
	private static final UIMessages uiMessages = GWT.create(UIMessages.class);

	private final DragController controller;
	private final PlaceBookItemFrame item;

	public SetSourceURLMenuItem(final DragController controller, final PlaceBookItemFrame item)
	{
		super(uiMessages.setURL());
		this.item = item;
		this.controller = controller;
	}

	@Override
	public void run()
	{
		final TextBox textBox = new TextBox();
		textBox.setWidth("300px");
		textBox.setValue(item.getItem().getSourceURL());

		final PopupPanel dialogBox = new PopupPanel(true, true);
		dialogBox.getElement().getStyle().setZIndex(2000);

		final Button closeButton = new Button(uiMessages.setURL(), new ClickHandler()
		{

			@Override
			public void onClick(final ClickEvent event)
			{
				dialogBox.hide();
				if (!textBox.getValue().equals(item.getItem().getSourceURL()))
				{
					item.getItem().setSourceURL(textBox.getValue());
					item.getItemWidget().refresh();
					controller.markChanged();
				}
			}
		});

		final Panel panel = new FlowPanel();
		panel.add(textBox);
		panel.add(closeButton);

		dialogBox.setGlassStyleName(Resources.STYLES.style().glassPanel());
		dialogBox.setStyleName(Resources.STYLES.style().popupPanel());
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);
		dialogBox.setWidget(panel);
		dialogBox.center();
		dialogBox.show();
	}
}
