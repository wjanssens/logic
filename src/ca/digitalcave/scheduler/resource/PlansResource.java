package ca.digitalcave.scheduler.resource;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.SqlSession;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.scheduler.SchedulerApplication;
import ca.digitalcave.scheduler.data.Id;
import ca.digitalcave.scheduler.data.RoomMapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

public class PlansResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants(Method.GET).add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final SchedulerApplication application = (SchedulerApplication) getApplication();
		return new WriterRepresentation(MediaType.APPLICATION_JSON) {
			@Override
			public void write(Writer w) throws IOException {
				final JsonGenerator g = application.getJsonFactory().createGenerator(w);
				final SqlSession session = application.getSqlSessionFactory().openSession();
				try {
					final RoomMapper mapper = session.getMapper(RoomMapper.class);
					g.writeStartObject();
					g.writeBooleanField("success", true);
					g.writeArrayFieldStart("data");
					mapper.select(new org.apache.ibatis.session.ResultHandler() {
						@Override
						public void handleResult(ResultContext ctx) {
							@SuppressWarnings("unchecked")
							final Map<String,Object> row = (Map<String,Object>) ctx.getResultObject();
							try {
								g.writeStartObject();
								g.writeNumberField("id", (Integer) row.get("id"));
								g.writeStringField("name", (String) row.get("name"));
								g.writeEndObject();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					});
					g.writeEndArray();
					g.writeEndObject();
					g.close();
				} finally {
					session.close();
				}
			}
		};
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final SchedulerApplication application = (SchedulerApplication) getApplication();
		final SqlSession session = application.getSqlSessionFactory().openSession();
		try {
			final JsonNode rootNode = application.getObjectMapper().readTree(entity.getReader());
			final Id<Integer> id = new Id<Integer>();
			final String name = rootNode.get("name").asText("Untitled");
			session.getMapper(RoomMapper.class).insert(id, name);
			return new StringRepresentation("{sucess:true}", MediaType.APPLICATION_JSON);
		} catch (IOException e) {
			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
		} finally {
			session.close();
		}
	}

}
