package net.akaritakai.stream.models.quartz.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.akaritakai.stream.models.quartz.TriggerEntry;

import java.util.List;

@Value
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(builder = ListTriggersResponse.ListTriggersResponseBuilder.class)
public class ListTriggersResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<TriggerEntry> triggers;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ListTriggersResponseBuilder {
    }
}
