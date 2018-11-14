package uk.gov.hmcts.cmc.claimstore.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.InvalidApplicationException;

import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;

@Service
public class JsonMapper {

    private static final String SERIALISATION_ERROR_MESSAGE = "Failed to serialize '%s' to JSON";
    private static final String DESERIALIZATION_ERROR_MESSAGE = "Failed to deserialize '%s' from JSON";

    private final ObjectMapper objectMapper;

    public JsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new InvalidApplicationException(
                format(SERIALISATION_ERROR_MESSAGE, input.getClass().getSimpleName()), e
            );
        }
    }

    public <T> T fromMap(Map<String, Object> input, Class<T> clazz) {
        return objectMapper.convertValue(input, clazz);
    }

    public <T> T fromJson(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new InvalidApplicationException(
                format(DESERIALIZATION_ERROR_MESSAGE, clazz.getSimpleName()), sanitize(e)
            );
        }
    }

    public <T> T fromJson(String value, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (IOException e) {
            throw new InvalidApplicationException(
                format(DESERIALIZATION_ERROR_MESSAGE, typeReference.getType()), sanitize(e)
            );
        }
    }

    public <T> T convertValue(Object value, Class<T> clazz) {
        return objectMapper.convertValue(value, clazz);
    }

    private MaskedException sanitize(IOException ioe) {
        return MaskedException.from(ioe);
    }

    private static class MaskedException extends RuntimeException {
        MaskedException(Throwable ex) {
            super(format("Original %s message masked", ex.getClass().getCanonicalName()), from(ex.getCause()));
            setStackTrace(ex.getStackTrace());
        }

        static MaskedException from(Throwable ex) {
            return ex == null
                ? null
                : new MaskedException(ex);
        }
    }
}
