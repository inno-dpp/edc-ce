package de.sovity.edc.extension.custommessages.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sovity.edc.extension.custommessages.api.SovityMessage;
import de.sovity.edc.extension.custommessages.api.SovityMessageException;
import de.sovity.edc.extension.custommessages.api.SovityMessenger;
import de.sovity.edc.utils.JsonUtils;
import jakarta.json.Json;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.response.StatusResult;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class SovityMessengerImpl implements SovityMessenger {

    private final RemoteMessageDispatcherRegistry registry;

    private final ObjectMapper serializer;

    @Override
    public <T extends SovityMessage, R extends SovityMessage>
    CompletableFuture<StatusResult<R>> send(Class<R> resultType, String counterPartyAddress, T payload) {
        try {
            val url = buildUrl(counterPartyAddress);
            val header = buildHeader(payload);
            val serialized = buildPayload(payload);
            val message = new SovityMessageRequest(url, header, serialized);
            val future = registry.dispatch(SovityMessageRequest.class, message);
            return future.thenApply(it -> it.map(content -> {
                try {
                    String resultHeaderStr = content.header();
                    val resultHeader = JsonUtils.parseJsonObj(resultHeaderStr);
                    if (resultHeader.getString("status").equals(SovityMessengerStatus.OK.getCode())) {
                        val resultBody = content.body();
                        return serializer.readValue(resultBody, resultType);
                    } else {
                        throw new SovityMessageException(resultHeader.getString("message"));
                    }
                } catch (JsonProcessingException e) {
                    throw new EdcException(e);
                }
            }));
        } catch (URISyntaxException | MalformedURLException | JsonProcessingException e) {
            throw new EdcException("Failed to build a custom sovity message", e);
        }
    }

    private <T extends SovityMessage> String buildPayload(T payload) throws JsonProcessingException {
        return serializer.writeValueAsString(payload);
    }

    private @NotNull URL buildUrl(String counterPartyAddress) throws MalformedURLException, URISyntaxException {
        return new URI(counterPartyAddress).toURL();
    }

    private <T extends SovityMessage> String buildHeader(T payload) {
        val header = Json.createObjectBuilder()
            .add("type", payload.getType())
            .build();
        return JsonUtils.toJson(header);
    }
}
