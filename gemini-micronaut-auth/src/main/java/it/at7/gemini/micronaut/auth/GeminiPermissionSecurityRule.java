package it.at7.gemini.micronaut.auth;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.rules.SecurityRuleResult;
import io.micronaut.security.token.jwt.generator.claims.JwtClaims;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.UriRouteMatch;
import it.at7.gemini.micronaut.core.*;
import it.at7.gemini.micronaut.exception.EntityFieldNotFoundException;
import it.at7.gemini.micronaut.exception.EntityRecordNotFoundException;
import it.at7.gemini.micronaut.exception.FieldConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class GeminiPermissionSecurityRule implements SecurityRule {
    private static final Logger LOG = LoggerFactory.getLogger(GeminiPermissionSecurityRule.class);

    private static final List<String> PUBLIC_ENTITIES = List.of("BASICSETTINGS");
    private static final String GOOGLE_ISS = "accounts.google.com";

    @Inject
    AuthDataManagerResolver authDataManagerResolver;

    @Value("${gemini.namespace:DEFAULT}")
    String namespace;

    @Override
    public int getOrder() {
        return -101;
    }

    @Override
    public SecurityRuleResult check(HttpRequest<?> request, @Nullable RouteMatch<?> routeMatch, @Nullable Map<String, Object> claims) {
        final HttpMethod httpMethod = request.getMethod();

        if (request.getPath().equals("/schema"))
            return SecurityRuleResult.UNKNOWN;

        Optional<UriRouteMatch> uriRouteMatch = request
                .getAttributes()
                .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);

        if (uriRouteMatch.isPresent()) {
            // access the path variables.
            Map<String, Object> variableValues = uriRouteMatch.get().getVariableValues();
            String entity = (String) variableValues.get("entity");
            String ID = (String) variableValues.get("ID");
            if (entity == null) {
                return SecurityRuleResult.UNKNOWN;
            }

            if (isPublic(entity, httpMethod, ID)) {
                return SecurityRuleResult.ALLOWED;
            }

            if (claims == null)
                return SecurityRuleResult.UNKNOWN;

            Object issuer = getIssuer(claims);
            if (issuer == null)
                return SecurityRuleResult.UNKNOWN;

            String account = getAccount(issuer, claims);
            if (account == null)
                return SecurityRuleResult.UNKNOWN;

            try {
                EntityRecord userRec = getUserEntityRecord(account);
                Object profilesObj = userRec.get("profiles");
                if (profilesObj != null) {
                    List<String> profiles = (List<String>) profilesObj;
                    for (String profile : profiles) {
                        EntityDataManager profileData = this.authDataManagerResolver.getProfileDataManager();
                        DataResult<EntityRecord> profileRecRes = profileData.getRecord(profile);


                        EntityRecord profileRec = profileRecRes.getData();
                        Object permissions = profileRec.get("permissions");
                        if (permissions != null) {
                            Map<String, Object> namespacesPerms = (Map<String, Object>) permissions;
                            Map<String, Object> namespacePerms = (Map<String, Object>) namespacesPerms.get(namespace);
                            if (namespacePerms != null) {
                                Map<String, Object> namespacePermissions = (Map<String, Object>) namespacePerms.get("namespacePermissions");
                                if (namespacePermissions != null && namespacePermissions.containsKey("entity")) {
                                    List<String> entityPerms = (List<String>) namespacePermissions.get("entity");
                                    String PERMISSION = getPermissionFromMethod(httpMethod, ID);
                                    return entityPerms.contains(PERMISSION) ? SecurityRuleResult.ALLOWED : SecurityRuleResult.REJECTED;
                                }
                            }
                            LOG.info(permissions.toString());
                        }
                        return SecurityRuleResult.REJECTED;
                    }
                }


            } catch (FieldConversionException | EntityFieldNotFoundException | EntityRecordNotFoundException e) {
                throw new RuntimeException("Unable to execute check", e);
            }
            return SecurityRuleResult.UNKNOWN;
        }


        return SecurityRuleResult.UNKNOWN;
    }

    private EntityRecord getUserEntityRecord(String account) throws FieldConversionException, EntityFieldNotFoundException, EntityRecordNotFoundException {
        EntityDataManager user = authDataManagerResolver.getUserDataManager();
        DataListResult<EntityRecord> matchingUsers = user.getRecords(
                DataListRequest.builder()
                        .addFilter("accounts", DataListRequest.OPE_TYPE.CONTAINS, account)
                        .build());
        List<EntityRecord> data = matchingUsers.getData();
        if (data.isEmpty())
            throw new EntityRecordNotFoundException(user.getEntity(), account);
        return data.get(0);
    }

/*    private EntityRecord getPermissionEntityRecord(String profile) throws FieldConversionException, EntityFieldNotFoundException, EntityRecordNotFoundException {
        EntityDataManager profileData = this.authDataManagerResolver.getProfileDataManager();
        DataResult<EntityRecord> profileRecRes = profileData.getRecord(profile);


    } */

    private String getPermissionFromMethod(HttpMethod httpMethod, String id) {
        boolean isRoot = StringUtils.isEmpty(id);
        switch (httpMethod) {

            case OPTIONS:
            case GET:
            case HEAD:
                return isRoot ? "LIST" : "GET";
            case POST:
                return "NEW";
            case PUT:
            case PATCH:
                return "EDIT";
            case DELETE:
                return "DELETE";
            case TRACE:
                break;
            case CONNECT:
                break;
            case CUSTOM:
                break;
        }
        return "GET";
    }

    private boolean isPublic(String entity, HttpMethod httpMethod, String ID) {
        return PUBLIC_ENTITIES.contains(entity.toUpperCase()) && List.of("GET", "LIST").contains(getPermissionFromMethod(httpMethod, ID));
    }


    private Object getIssuer(Map<String, Object> claims) {
        Object issuer = claims.get(JwtClaims.ISSUER);
        if (issuer == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("{} claim not present", JwtClaims.ISSUER);
            }
            return null;
        }
        return issuer;
    }

    private String getAccount(Object issuer, Map<String, Object> claims) {
        String iss = issuer.toString();
        if (iss.equals(GOOGLE_ISS)) {
            Object emailObk = claims.get("email");
            if (emailObk == null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("email claim not present");
                }
                return null;
            }
            return emailObk.toString();
        }
        return null;
    }

}
