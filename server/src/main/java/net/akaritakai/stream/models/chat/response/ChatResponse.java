package net.akaritakai.stream.models.chat.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "responseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessageResponse.class, name = "message"),
    @JsonSubTypes.Type(value = ChatStatusResponse.class, name = "status")
})
public class ChatResponse {
}
