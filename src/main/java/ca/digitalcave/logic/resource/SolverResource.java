package ca.digitalcave.logic.resource;

import java.io.IOException;
import java.io.Writer;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import ca.digitalcave.logic.LogicApplication;
import ca.digitalcave.logic.domain.Puzzle;

public class SolverResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		if (entity.getMediaType() != MediaType.APPLICATION_JSON) {
			throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
		}
		
		final LogicApplication application = (LogicApplication) getApplication();
		final MappingJsonFactory jsonFactory = application.getJsonFactory();
		
		try {
			final JsonParser parser = jsonFactory.createJsonParser(entity.getReader());
			
			final Puzzle puzzle = new Puzzle(parser);
			puzzle.solve();
			
			return new WriterRepresentation(MediaType.APPLICATION_JSON) {
				@Override
				public void write(Writer w) throws IOException {
					final JsonGenerator g = jsonFactory.createJsonGenerator(w);
					//puzzle.write(g);  // TODO
				}
			};
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		} catch (ContradictionException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
		} catch (TimeoutException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
		}
	}
}
