package uk.gov.hmcts.cmc.ccd.sample.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;

import java.util.Map;

public class JsonHelper {

    public static Map<String, Object> convertCCDCaseToMap(CCDCase ccdCase) {
        return (Map<String, Object>) objectMapper().convertValue(ccdCase, Map.class);
    }

    private static ObjectMapper objectMapper() {
        return new ObjectMapper()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

}
