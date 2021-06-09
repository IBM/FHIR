/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence;

import com.ibm.fhir.model.annotation.Required;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.util.ValidationSupport;

/**
 * A Result wrapper for FHIR interactions that return a single resource.
 * Instances are immutable and can be constructed via {@code new SingleResourceResult.Builder<T>()}.
 *
 * @author lmsurpre
 */
public class SingleResourceResult<T extends Resource> {
    @Required
    final boolean success;
    final T resource;
    final OperationOutcome outcome;
    final boolean deleted;

    private SingleResourceResult(Builder<T> builder) {
        success = ValidationSupport.requireNonNull(builder.success, "success");
        resource = builder.resource;
        outcome = builder.outcome;
        deleted = builder.deleted;
        if (!success && (outcome == null || outcome.getIssue().isEmpty())) {
            throw new IllegalStateException("Failed interaction results must include an OperationOutcome with one or more issue.");
        }
    }

    /**
     * Whether or not the interaction was successful
     *
     * @return
     *     whether the interaction was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Whether or not the resource is deleted
     * @return whether the resource is deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * The resource resulting from the interaction
     *
     * @return
     *     An immutable object of type {@link Resource}.
     */
    public T getResource() {
        return resource;
    }
    /**
     * An OperationOutcome that represents the outcome of the interaction
     *
     * @return
     *     An immutable object of type {@link OperationOutcome}
     */
    public OperationOutcome getOutcome() {
        return outcome;
    }

    // result builder
    public static class Builder<T extends Resource> {
        boolean success;
        T resource;
        OperationOutcome outcome;
        boolean deleted;

        /**
         * Whether or not the interaction was successful
         *
         * <p>This field is required.
         *
         * @param success
         *     whether the interaction was successful
         *
         * @return
         *     A reference to this Builder instance
         */
        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Whether or not the resource is deleted
         * @param flag
         * @return A reference to this Builder instance
         */
        public Builder<T> deleted(boolean flag) {
            this.deleted = flag;
            return this;
        }

        /**
         * The resulting resource from the interaction
         *
         * @param resource
         *     the resulting resource from the interaction; this may be null if the interaction was not successful
         *
         * @return
         *     A reference to this Builder instance
         */
        public Builder<T> resource(T resource) {
            this.resource = resource;
            return this;
        }

        /**
         * An OperationOutcome that represents the outcome of the interaction
         *
         * <p>This field is required when the interaction is not successful
         *
         * @param outcome
         *     the outcome of the interaction
         *
         * @return
         *     A reference to this Builder instance
         */
        public Builder<T> outcome(OperationOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        /**
         * Build the {@link SingleResourceResult}
         *
         * <p>Required fields:
         * <ul>
         * <li>success</li>
         * </ul>
         *
         * @return
         *     An immutable object of type {@link SingleResourceResult}
         */
        public SingleResourceResult<T> build() {
            return new SingleResourceResult<T>(this);
        }
    }
}