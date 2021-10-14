package it.at7.gemini.micronaut.api;

import it.at7.gemini.micronaut.core.DataListRequest;

/**
 * Service responsible of REST Entity Management. Eg: default retrieve strategy, default pagination, default REST
 * parameters, default REST filters and so on..
 */
public interface RestEntityManager {


    EntityRestConfig getRestConfiguration(String entityName);

    String getConfigHash();

}

