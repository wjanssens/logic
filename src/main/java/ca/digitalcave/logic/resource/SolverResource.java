package ca.digitalcave.logic.resource;

import ca.digitalcave.logic.LogicApplication;
import ca.digitalcave.logic.domain.Puzzle;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

import java.io.IOException;
import java.io.Writer;

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
		final JsonFactory jsonFactory = application.getJsonFactory();
		
		try {
			final JsonParser parser = jsonFactory.createParser(entity.getReader());
			
			final Puzzle puzzle = new Puzzle(parser);
			puzzle.solve();
			
			return new WriterRepresentation(MediaType.APPLICATION_JSON) {
				@Override
				public void write(Writer w) throws IOException {
					final JsonGenerator g = jsonFactory.createGenerator(w);
					//puzzle.write(g);  // TODO
				}
			};
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		} catch (ContradictionException | TimeoutException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e.getMessage());
		}
	}
}
