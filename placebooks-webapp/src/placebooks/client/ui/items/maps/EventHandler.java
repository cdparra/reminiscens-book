package placebooks.client.ui.items.maps;

public abstract class EventHandler
{
	public native static EventHandlerFunction createHandler(EventHandler self)
	/*-{
		var controller = function(event)
		{
			self.@placebooks.client.ui.items.maps.EventHandler::handleEvent(Lplacebooks/client/ui/items/maps/Event;)(event);
		}
		return controller;
	}-*/;

	private EventHandlerFunction handler;

	public EventHandler()
	{
		handler = createHandler(this);
	}

	public EventHandlerFunction getFunction()
	{
		return handler;
	}

	protected abstract void handleEvent(Event event);
}
