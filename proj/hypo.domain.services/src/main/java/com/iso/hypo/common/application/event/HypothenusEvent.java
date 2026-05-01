package com.iso.hypo.common.application.event;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

import org.springframework.context.ApplicationEvent;

import com.iso.hypo.common.application.event.enumeration.OperationEnum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import lombok.Getter;

@Getter
public abstract class HypothenusEvent<T> extends ApplicationEvent implements CloudEvent {

	private static final long serialVersionUID = 1L;
	private static final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private T entity;
	private OperationEnum operation;

	public HypothenusEvent(Object source, T entity, OperationEnum operation) {
		super(source);
		this.entity = entity;
		this.operation = operation;
	}

	// --- Abstract methods to be implemented by each specific event class ---

	protected abstract String resolveId();

	protected abstract String resolveType();

	protected abstract URI resolveSource();

	// --- CloudEvent implementation ---

	@Override
	public SpecVersion getSpecVersion() {
		return SpecVersion.V1;
	}

	@Override
	public String getId() {
		return resolveId();
	}

	@Override
	public String getType() {
		return resolveType();
	}

	@Override
	public URI getSource() {
		return resolveSource();
	}

	@Override
	public String getDataContentType() {
		return "application/json";
	}

	@Override
	public URI getDataSchema() {
		return null;
	}

	@Override
	public String getSubject() {
		return resolveId();
	}

	@Override
	public OffsetDateTime getTime() {
		return OffsetDateTime.now();
	}

	@Override
	public CloudEventData getData() {
		try {
			byte[] bytes = objectMapper.writeValueAsBytes(entity);
			return () -> bytes;
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize event entity to CloudEventData", e);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws IllegalArgumentException {
		return switch (attributeName) {
			case "id"              -> getId();
			case "source"          -> getSource();
			case "specversion"     -> getSpecVersion();
			case "type"            -> getType();
			case "datacontenttype" -> getDataContentType();
			case "dataschema"      -> getDataSchema();
			case "subject"         -> getSubject();
			case "time"            -> getTime();
			default -> throw new IllegalArgumentException("Unknown CloudEvent attribute: " + attributeName);
		};
	}

	@Override
	public Object getExtension(String extensionName) {
		return null;
	}

	@Override
	public Set<String> getExtensionNames() {
		return Set.of();
	}
}