package ca.digitalcave.logic.resource;

import java.util.Date;
import java.util.HashMap;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.logic.LogicApplication;

public class DefaultResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants(Method.GET).add(new Variant(MediaType.TEXT_HTML));
		getVariants(Method.GET).add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
		getVariants(Method.GET).add(new Variant(MediaType.IMAGE_PNG));
		getVariants(Method.GET).add(new Variant(MediaType.IMAGE_GIF));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final LogicApplication application = (LogicApplication) getApplication();
		final String path = new Reference(getRootRef(), getOriginalRef()).getRemainingPart(true, false);
				
		if (path.startsWith("WEB-INF")) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		
		boolean transform = variant.getMediaType().equals(MediaType.TEXT_HTML) 
					|| variant.getMediaType().equals(MediaType.APPLICATION_JAVASCRIPT); 
		
		if (transform) {
			final HashMap<String, Object> dataModel = new HashMap<String, Object>();
			dataModel.put("user", getClientInfo().getUser());
			final TemplateRepresentation entity = new TemplateRepresentation(path, application.getFmConfig(), dataModel, variant.getMediaType());
			if (entity.getTemplate() == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			entity.setModificationDate(new Date());
			return entity;
		} else {
			final Request request = new Request(Method.GET, new Reference("war://" + path));
			request.getConditions().setUnmodifiedSince(getRequest().getConditions().getUnmodifiedSince());
			getContext().getClientDispatcher().handle(request, getResponse());
			return getResponseEntity();
		}
	}
	
}
