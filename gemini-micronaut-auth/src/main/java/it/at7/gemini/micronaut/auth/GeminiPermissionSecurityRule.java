package it.at7.gemini.micronaut.auth;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Requires;
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
@Requires(property = "gemini.auth.enabled", value = "true", defaultValue = "true")
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

        if (request.getPath().equals("/entity"))
            return SecurityRuleResult.UNKNOWN;

        Optional<UriRouteMatch> uriRouteMatch = request
                .getAttributes()
                .get(HttpAttributes.ROUTE_MATCH.toString(), UriRouteMatch.class);


        String entity = request.getPath();
        String ID = null;
        if (uriRouteMatch.isPresent()) {
            // access the path variables.
            UriRouteMatch ruoteMatch = uriRouteMatch.get();
            Map<String, Object> variableValues = ruoteMatch.getVariableValues();
            if (variableValues.containsKey("entity"))
                entity = (String) variableValues.get("entity");
            if (variableValues.containsKey("id"))
                ID = (String) variableValues.get("id");
        }
        if (entity == null) {
            return SecurityRuleResult.UNKNOWN;
        }

        if (isPublic(entity, httpMethod, ID)) {
            return SecurityRuleResult.ALLOWED;
        }

        if (claims == null)
            return SecurityRuleResult.UNKNOWN;

        List<String> profiles = null;
        if (claims.get("username") != null) {
            // try to get the profiles directly
            profiles = (List<String>) claims.get("profiles");
        }

        try {
            if (profiles == null) {
                Object issuer = getIssuer(claims);
                if (issuer == null)
                    return SecurityRuleResult.UNKNOWN;

                String account = getAccount(issuer, claims);
                if (account == null)
                    return SecurityRuleResult.UNKNOWN;


                EntityRecord userRec = getUserEntityRecord(account);
                Object profilesObj = userRec.get("profiles");
                if (profilesObj != null) {
                    profiles = (List<String>) profilesObj;
                }

            }

            if (profiles != null) {
                for (String profile : profiles) {
                    EntityDataManager profileData = this.authDataManagerResolver.getProfileDataManager();

                    DataResult<EntityRecord> profileRecRes = profileData.getRecord(profile);


                    EntityRecord profileRec = profileRecRes.getData();
                    Object permissions = profileRec.get("permissions");
                    if (permissions != null) {
                        Map<String, Object> namespacesPerms = (Map<String, Object>) permissions;
                        Map<String, Object> namespacePerms = (Map<String, Object>) namespacesPerms.get(namespace);
                        if (namespacePerms != null) {
                            boolean allow = false;
                            Map<String, Object> namespacePermissions = (Map<String, Object>) namespacePerms.get("namespacePermissions");
                            if (namespacePermissions != null && namespacePermissions.containsKey("entity")) {
                                List<String> entityPerms = (List<String>) namespacePermissions.get("entity");
                                String PERMISSION = getPermissionFromMethod(httpMethod, ID);
                                if (entityPerms.contains(PERMISSION))
                                    allow = true;
                            }
                            Map<String, Object> entityPermissions = (Map<String, Object>) namespacePerms.get("entityPermissions");
                            if (!allow && entityPermissions != null) {
                                Map<String, Object> entityPermission = (Map<String, Object>) entityPermissions.get(entity.toUpperCase());
                                if (entityPermission != null) {
                                    List<String> entityPerms = (List<String>) entityPermission.get("permissions");
                                    String PERMISSION = getPermissionFromMethod(httpMethod, ID);
                                    if (entityPerms.contains(PERMISSION))
                                        allow = true;
                                }
                            }
                            Map<String, Object> routePermissions = (Map<String, Object>) namespacePerms.get("routePermissions");
                            if (!allow && routePermissions != null) {
                                Map<String, Object> routePermission = (Map<String, Object>) routePermissions.get(entity);
                                if (routePermission != null) {
                                    List<String> routePerm = (List<String>) routePermission.get("permissions");
                                    String methodName = httpMethod.name();
                                    if (routePerm.contains(methodName))
                                        allow = true;
                                }
                            }
                            if (allow)
                                return SecurityRuleResult.ALLOWED;
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
