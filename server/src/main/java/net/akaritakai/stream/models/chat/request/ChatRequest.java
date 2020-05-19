package net.akaritakai.stream.models.chat.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "requestType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatJoinRequest.class, name = "join"),
    @JsonSubTypes.Type(value = ChatSendRequest.class, name = "send"),
    @JsonSubTypes.Type(value = ChatPartRequest.class, name = "part")
})
public abstract class ChatRequest {
}
