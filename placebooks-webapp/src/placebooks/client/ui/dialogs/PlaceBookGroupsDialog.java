package placebooks.client.ui.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import placebooks.client.AbstractCallback;
import placebooks.client.PlaceBooks;
import placebooks.client.controllers.ServerInfoController;
import placebooks.client.controllers.UserController;
import placebooks.client.model.LoginDetails;
import placebooks.client.model.ServerInfo;
import placebooks.client.model.ServiceInfo;
import placebooks.client.model.User;
import placebooks.client.ui.UIMessages;
import placebooks.client.ui.views.DatePrinter;
import placebooks.client.ui.views.EnabledButtonCell;
import placebooks.client.ui.views.View;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;

public class PlaceBookGroupsDialog extends PlaceBookDialog implements View<User>
{
	interface PlaceBookAccountsDialogUiBinder extends UiBinder<Widget, PlaceBookGroupsDialog>
	{
	}

	private static final UIMessages uiConstants = GWT.create(UIMessages.class);

	private static PlaceBookAccountsDialogUiBinder uiBinder = GWT.create(PlaceBookAccountsDialogUiBinder.class);

	private static final ProvidesKey<LoginDetails> keyProvider = new ProvidesKey<LoginDetails>()
	{
		@Override
		public Object getKey(final LoginDetails details)
		{
			return details.getID();
		}
	};

	@UiField
	Panel tablePanel;

	private CellTable<LoginDetails> cellTable;

	public PlaceBookGroupsDialog()
	{
		setWidget(uiBinder.createAndBindUi(this));
		onInitialize();
		center();
		updateUser();
	}

	@Override
	public void itemChanged(final User user)
	{
		final Map<String, ServiceInfo> services = new HashMap<String, ServiceInfo>();
		if (ServerInfoController.getController().getItem() != null)
		{
			for (final ServiceInfo service : ServerInfoController.getController().getItem().getServices())
			{
				services.put(service.getName(), service);
			}
		}

		boolean syncing = false;
		final List<LoginDetails> list = new ArrayList<LoginDetails>();
		for (final LoginDetails details : user.getLoginDetails())
		{
			if (details.isSyncInProgress())
			{
				syncing = true;
			}
			services.remove(details.getService());
			list.add(details);
		}

		if (syncing)
		{
			new Timer()
			{
				@Override
				public void run()
				{
					updateUser();
				}
			}.schedule(1000);
		}

		center();
	}

	/**
	 * Add the columns to the table.
	 */
	private void initTableColumns(final SelectionModel<LoginDetails> selectionModel)
	{
		// Service
		final Column<LoginDetails, String> serviceColumn = new Column<LoginDetails, String>(new TextCell())
		{
			@Override
			public String getValue(final LoginDetails object)
			{
				return object.getService();
			}
		};
		cellTable.addColumn(serviceColumn, uiConstants.service());

		// Username.
		final Column<LoginDetails, String> userNameColumn = new Column<LoginDetails, String>(new TextCell())
		{
			@Override
			public String getValue(final LoginDetails object)
			{
				return object.getUsername();
			}
		};
		cellTable.addColumn(userNameColumn, uiConstants.username());

		// Last Update
		final Column<LoginDetails, String> lastUpdateColumn = new Column<LoginDetails, String>(new TextCell())
		{
			@Override
			public String getValue(final LoginDetails object)
			{
				if (object.isSyncInProgress())
				{
					return uiConstants.syncInProgress();
				}
				else if (object.getLastSync() == 0)
				{
					return uiConstants.neverSynced();
				}
				else
				{
					return uiConstants.lastSynced(DatePrinter.formatDate(new Date((long) object.getLastSync())));
				}
			}
		};
		cellTable.addColumn(lastUpdateColumn, uiConstants.status());

		final EnabledButtonCell<LoginDetails> syncCell = new EnabledButtonCell<LoginDetails>()
		{

			@Override
			public String getText(final LoginDetails object)
			{
				return uiConstants.syncNow();
			}

			@Override
			public boolean isEnabled(final LoginDetails object)
			{
				return !object.isSyncInProgress();
			}
		};

		final Column<LoginDetails, LoginDetails> updateColumn = new Column<LoginDetails, LoginDetails>(syncCell)
		{
			@Override
			public LoginDetails getValue(final LoginDetails details)
			{
				return details;
			}
		};
		updateColumn.setFieldUpdater(new FieldUpdater<LoginDetails, LoginDetails>()
		{
			@Override
			public void update(final int index, final LoginDetails object, final LoginDetails value)
			{
				PlaceBooks.getServer().sync(object.getService(), new AbstractCallback()
				{
					@Override
					public void success(final Request request, final Response response)
					{
					}
				});
				object.setSyncInProgress(true);
			}
		});
		cellTable.addColumn(updateColumn);
	}

	private void onInitialize()
	{
		cellTable = new CellTable<LoginDetails>(keyProvider);
		cellTable.setWidth("100%", false);

		final SelectionModel<LoginDetails> selectionModel = new NoSelectionModel<LoginDetails>(keyProvider);
		cellTable.setSelectionModel(selectionModel);

		initTableColumns(selectionModel);

		tablePanel.add(cellTable);

		UserController.getController().add(this);
		ServerInfoController.getController().add(new View<ServerInfo>()
		{

			@Override
			public void itemChanged(final ServerInfo value)
			{
				// TODO Auto-generated method stub

			}
		});
	}

	private void updateUser()
	{
		if (!isShowing()) { return; }
		UserController.getController().refresh();
		ServerInfoController.getController().refresh();
	}
}
