/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.retrieve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.opencds.cqf.cql.engine.retrieve.TerminologyAwareRetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.fhir.cql.engine.searchparam.DateParameter;
import com.ibm.fhir.cql.engine.searchparam.DateRangeParameter;
import com.ibm.fhir.cql.engine.searchparam.IQueryParameter;
import com.ibm.fhir.cql.engine.searchparam.IQueryParameterOr;
import com.ibm.fhir.cql.engine.searchparam.OrParameter;
import com.ibm.fhir.cql.engine.searchparam.ReferenceParameter;
import com.ibm.fhir.cql.engine.searchparam.SearchParameterResolver;
import com.ibm.fhir.cql.engine.searchparam.TokenParameter;
import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.type.code.SearchParamType;
import com.ibm.fhir.search.SearchConstants;
import com.ibm.fhir.search.SearchConstants.Prefix;

/**
 * Provide support for CQL Engine RetrieveProvider implementations that wish to build
 * retrieve support on top of query parameters defined in the FHIR REST API specification.
 */
public abstract class SearchParameterFHIRRetrieveProvider extends TerminologyAwareRetrieveProvider {

    private static final int DEFAULT_MAX_CODES_PER_QUERY = 64;

    private SearchParameterResolver searchParameterResolver;
    private Integer pageSize;
    private int maxCodesPerQuery;

    public SearchParameterFHIRRetrieveProvider(SearchParameterResolver searchParameterResolver) {
        this.searchParameterResolver = searchParameterResolver;
        this.maxCodesPerQuery = DEFAULT_MAX_CODES_PER_QUERY;
    }

    public void setPageSize(Integer value) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException("value must be a non-null integer > 0");
        }

        this.pageSize = value;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setMaxCodesPerQuery(int value) {

        if (value < 1) {
            throw new IllegalArgumentException("value must be > 0");
        }

        this.maxCodesPerQuery = value;
    }

    public int getMaxCodesPerQuery() {
        return this.maxCodesPerQuery;
    }

    protected abstract Iterable<Object> executeQueries(String dataType, List<SearchParameterMap> queries) throws Exception;

    @Override
    public Iterable<Object> retrieve(String context, String contextPath, Object contextValue, String dataType,
        String templateId, String codePath, Iterable<Code> codes, String valueSet, String datePath,
        String dateLowPath, String dateHighPath, Interval dateRange) {

        try {
            List<SearchParameterMap> queries =
                    this.setupQueries(context, contextPath, contextValue, dataType, templateId, codePath, codes, valueSet, datePath, dateLowPath, dateHighPath, dateRange);

            return this.executeQueries(dataType, queries);
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected List<SearchParameterMap> setupQueries(String context, String contextPath, Object contextValue,
        String dataType, String templateId, String codePath, Iterable<Code> codes, String valueSet, String datePath,
        String dateLowPath, String dateHighPath, Interval dateRange) throws Exception {

        List<SearchParameterMap> queries = null;

        Pair<String, IQueryParameter> templateParam = this.getTemplateParam(dataType, templateId);
        Pair<String, IQueryParameter> contextParam = this.getContextParam(dataType, context, contextPath, contextValue);
        Pair<String, DateRangeParameter> dateRangeParam = this.getDateRangeParam(dataType, datePath, dateLowPath, dateHighPath, dateRange);
        Pair<String, List<IQueryParameterOr<?>>> codeParams = this.getCodeParams(dataType, codePath, codes, valueSet);

        // In the case we filtered to a valueSet without codes, there are no possible
        // results.
        if (valueSet != null && (codeParams == null || codeParams.getValue().isEmpty())) {
            queries = Collections.emptyList();
        } else {
            queries = this.innerSetupQueries(dataType, templateParam, contextParam, dateRangeParam, codeParams);
        }

        return queries;
    }

    protected List<SearchParameterMap> innerSetupQueries(String dataType, Pair<String, IQueryParameter> templateParam,
        Pair<String, IQueryParameter> contextParam, Pair<String, DateRangeParameter> dateRangeParam,
        Pair<String, List<IQueryParameterOr<?>>> codeParams) throws Exception {

        List<SearchParameterMap> result = null;

        if (codeParams == null || codeParams.getValue().isEmpty()) {
            result = Collections.singletonList(this.getBaseMap(templateParam, contextParam, dateRangeParam));
        } else {

            result = new ArrayList<>();
            for (IQueryParameterOr<?> tolp : codeParams.getValue()) {
                SearchParameterMap base = this.getBaseMap(templateParam, contextParam, dateRangeParam);
                base.put(codeParams.getKey(), tolp);
                result.add(base);
            }
        }

        return result;
    }

    protected Pair<String, IQueryParameter> getTemplateParam(String dataType, String templateId) {
        // purposefully empty
        return null;
    }

    protected Pair<String, IQueryParameter> getContextParam(String dataType, String context, String contextPath,
        Object contextValue) throws Exception {
        Pair<String, IQueryParameter> result = null;

        if (context != null && "Patient".equals(context) && contextValue != null && contextPath != null) {
            result = searchParameterResolver.createSearchParameter(context, dataType, contextPath, (String) contextValue);
            if (result == null) {
                throw new IllegalArgumentException(String.format("Could not resolve search parameter for dataType '%s' and contextPath '%s'", dataType, contextPath));
            }
        }

        return result;
    }

    protected Pair<String, DateRangeParameter> getDateRangeParam(String dataType, String datePath, String dateLowPath,
        String dateHighPath, Interval dateRange) throws Exception {
        Pair<String, DateRangeParameter> result = null;
        if (datePath != null) {
            SearchParameter dateParam = this.searchParameterResolver.getSearchParameterDefinition(dataType, datePath, SearchParamType.DATE);
            if (dateParam != null) {
                String name = dateParam.getName().getValue();

                DateParameter low = null;
                DateParameter high = null;
                if (dateRange.getLow() != null) {
                    low = new DateParameter(Prefix.GE, Date.from(((DateTime) dateRange.getLow()).getDateTime().toInstant()));
                }

                if (dateRange.getHigh() != null) {
                    high = new DateParameter(Prefix.LE, Date.from(((DateTime) dateRange.getHigh()).getDateTime().toInstant()));
                }

                DateRangeParameter rangeParam;
                if (low == null && high != null) {
                    rangeParam = new DateRangeParameter(high);
                } else if (high == null && low != null) {
                    rangeParam = new DateRangeParameter(low);
                } else {
                    rangeParam = new DateRangeParameter(low, high);
                }

                result = Pair.of(name, rangeParam);
            } else {
                throw new UnsupportedOperationException(String.format("Could not resolve a search parameter with date type for %s.%s ", dataType, datePath));
            }
        }
        return result;
    }

    protected Pair<String, List<IQueryParameterOr<?>>> getCodeParams(String dataType, String codePath, Iterable<Code> codes,
        String valueSet) throws Exception {

        Pair<String, List<IQueryParameterOr<?>>> result = null;

        if (codePath != null) {
            SearchParameter searchParam = searchParameterResolver.getSearchParameterDefinition(dataType, codePath, SearchParamType.TOKEN);
            if (searchParam != null) {
                String name = searchParam.getName().getValue();
                result = Pair.of(name, getCodeParams(name, codes, valueSet));
            } else {
                throw new IllegalArgumentException(String.format("Could not resolve search parameter for dataType '%s' and codePath '%s'", dataType, codePath));
            }
        }

        return result;
    }

    // The code params will be either the literal set of codes in the event the data
    // server doesn't have the referenced ValueSet
    // (or doesn't support pulling and caching a ValueSet).
    // If the target server DOES support that then it's "dataType.codePath in
    // ValueSet"
    protected List<IQueryParameterOr<?>> getCodeParams(String name, Iterable<Code> codes, String valueSet) {
        List<IQueryParameterOr<?>> params = null;

        if (valueSet != null) {
            if (isExpandValueSets()) {
                if (this.terminologyProvider == null) {
                    throw new IllegalArgumentException("Expand value sets cannot be used without a terminology provider and no terminology provider is set.");
                }
                ValueSetInfo valueSetInfo = new ValueSetInfo().withId(valueSet);
                codes = this.terminologyProvider.expand(valueSetInfo);
            } else {
                TokenParameter p = new TokenParameter(SearchConstants.Modifier.IN, valueSet);
                IQueryParameterOr<TokenParameter> or = new OrParameter<TokenParameter>(p);
                params = Collections.singletonList(or);
            }
        }

        if (params == null) {
            if (codes == null) {
                params = Collections.emptyList();
            } else {

                params = new ArrayList<>();

                OrParameter<TokenParameter> codeParams = null;
                int codeCount = 0;
                for (Code code : codes) {
                    if (codeCount % this.maxCodesPerQuery == 0) {
                        if (codeParams != null) {
                            params.add(codeParams);
                        }

                        codeParams = new OrParameter<>();
                    }

                    codeCount++;
                    codeParams.addOr(new TokenParameter(code.getSystem(), code.getCode()));
                }

                if (codeParams != null) {
                    params.add(codeParams);
                }
            }
        }

        return params;
    }

    protected SearchParameterMap getBaseMap(Pair<String, IQueryParameter> templateParam,
        Pair<String, IQueryParameter> contextParam, Pair<String, DateRangeParameter> dateRangeParam) throws Exception {

        SearchParameterMap searchParameters = new SearchParameterMap();
        // baseMap.setLastUpdated(new DateRangeParam());

        if (this.pageSize != null) {
            searchParameters.count(this.pageSize);
        }

        if (templateParam != null) {
            searchParameters.put(templateParam.getKey(), templateParam.getValue());
        }

        if (dateRangeParam != null) {
            searchParameters.put(dateRangeParam.getKey(), dateRangeParam.getValue());
        }

        if (contextParam != null) {
            searchParameters.put(contextParam.getKey(), contextParam.getValue());
        }

        return searchParameters;
    }
    
    /**
     * Given a query parameter name and contents, transmute the name
     * into something that includes all the appropriate modifiers.
     * 
     * @param name query parameter name
     * @param param query parameter contents
     * @return query parameter name with appropriate modifiers appended
     */
    protected String getModifiedName(String name, IQueryParameter param) {
        StringBuilder paramName = new StringBuilder(name);
        if (param instanceof ReferenceParameter) {
            ReferenceParameter rp = (ReferenceParameter) param;
            if (rp.getResourceTypeModifier() != null) {
                paramName.append(":");
                paramName.append(rp.getResourceTypeModifier().getValue());
            }

            if (rp.getChainedProperty() != null) {
                paramName.append(".");
                paramName.append(rp.getChainedProperty());
            }

        } else {
            if (param.getMissing() != null) {
                paramName.append(":missing");
            } else if (param.getModifier() != null) {
                paramName.append(":");
                paramName.append(param.getModifier().value());
            }
        }

        return paramName.toString();
    }
}