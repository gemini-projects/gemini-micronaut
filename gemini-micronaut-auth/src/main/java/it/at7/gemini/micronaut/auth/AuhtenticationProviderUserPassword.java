package it.at7.gemini.micronaut.auth;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.*;
import it.at7.gemini.micronaut.core.DataListRequest;
import it.at7.gemini.micronaut.core.DataListResult;
import it.at7.gemini.micronaut.core.EntityDataManager;
import it.at7.gemini.micronaut.core.EntityRecord;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class AuhtenticationProviderUserPassword implements AuthenticationProvider {

    @Value("${gemini.namespace}")
    String namespace;

    @Inject
    AuthDataManagerResolver authDataManagerResolver;

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequestGeneric) {
        AuthenticationRequest<String, String> authenticationRequest = (AuthenticationRequest<String, String>) authenticationRequestGeneric;
        return Flux.create(emitter -> {
            EntityDataManager nsBasicAuthDataManager = authDataManagerResolver.getNSBasicAuthDataManager();
            try {
                DataListResult<EntityRecord> users = nsBasicAuthDataManager.getRecords(DataListRequest.builder()
                        .addFilter("namespace", DataListRequest.OPE_TYPE.EQUALS, namespace)
                        .addFilter("user", DataListRequest.OPE_TYPE.EQUALS, authenticationRequest.getIdentity()).build());
                if (!users.getData().isEmpty()) {
                    EntityRecord entityRecord = users.getData().get(0);
                    String password = (String) entityRecord.get("password");;
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("profiles", entityRecord.get("profiles"));
                    if (password != null && password.equals(authenticationRequest.getSecret())) {
                        emitter.next(new UserDetails(authenticationRequest.getIdentity(), List.of(), attributes));
                        return;
                    }
                }
                emitter.next(new AuthenticationFailed());
            } catch (Exception ignored) {
            }
            emitter.next(new AuthenticationFailed());
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
