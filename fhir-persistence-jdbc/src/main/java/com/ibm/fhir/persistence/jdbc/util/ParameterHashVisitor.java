/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.jdbc.dto.CompositeParmVal;
import com.ibm.fhir.persistence.jdbc.dto.DateParmVal;
import com.ibm.fhir.persistence.jdbc.dto.ExtractedParameterValue;
import com.ibm.fhir.persistence.jdbc.dto.ExtractedParameterValueVisitor;
import com.ibm.fhir.persistence.jdbc.dto.LocationParmVal;
import com.ibm.fhir.persistence.jdbc.dto.NumberParmVal;
import com.ibm.fhir.persistence.jdbc.dto.QuantityParmVal;
import com.ibm.fhir.persistence.jdbc.dto.ReferenceParmVal;
import com.ibm.fhir.persistence.jdbc.dto.StringParmVal;
import com.ibm.fhir.persistence.jdbc.dto.TokenParmVal;
import com.ibm.fhir.schema.control.FhirSchemaVersion;
import com.ibm.fhir.search.util.ReferenceValue;

/**
 * Compute a cryptographic hash of the visited parameters.
 */
public class ParameterHashVisitor implements ExtractedParameterValueVisitor {
    private static final String SHA_256 = "SHA-256";
    // Since the digest is not updated when values are null, use a delimiter to ensure that
    // different combinations subfields being null do not end up generating the same hash
    private static final byte[] DELIMITER = "|".getBytes(StandardCharsets.UTF_8);

    // The digest being accumulated as the parameters are visited
    private final MessageDigest digest;
    private final Encoder encoder;

    /**
     * Public constructor.
     */
    public ParameterHashVisitor() {
        this(false);
    }

    /**
     * Public constructor.
     * @param legacyWholeSystemSearchParamsEnabled if true, then update digest to ensure hash changes
     * from when it is false; this can be removed when the legacyWholeSystemSearchParamsEnabled config
     * setting is removed.
     */
    public ParameterHashVisitor(boolean legacyWholeSystemSearchParamsEnabled) {
        try {
            digest = MessageDigest.getInstance(SHA_256);
            // Start digest with latest FHIR schema version (with parameter storage update)
            digest.update(FhirSchemaVersion.getLatestParameterStorageUpdate().toString().getBytes(StandardCharsets.UTF_8));
            // If legacyWholeSystemSearchParamsEnabled is true, then update digest to ensure hash changes from when
            // it is false; this can be removed when the legacyWholeSystemSearchParamsEnabled config setting is removed
            if (legacyWholeSystemSearchParamsEnabled) {
                updateDigestWithString("legacyWholeSystemSearchParamsEnabled");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MessageDigest not found: " + SHA_256, e);
        }
        encoder = Base64.getEncoder();
    }

    @Override
    public void visit(StringParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithString(param.getValueString());
    }

    @Override
    public void visit(NumberParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithBigDecimal(param.getValueNumber());
        updateDigestWithBigDecimal(param.getValueNumberLow());
        updateDigestWithBigDecimal(param.getValueNumberHigh());
    }

    @Override
    public void visit(DateParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithTimestamp(param.getValueDateStart());
        updateDigestWithTimestamp(param.getValueDateEnd());
    }

    @Override
    public void visit(TokenParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithString(param.getValueSystem());
        updateDigestWithString(param.getValueCode());
    }

    @Override
    public void visit(QuantityParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithBigDecimal(param.getValueNumber());
        updateDigestWithBigDecimal(param.getValueNumberLow());
        updateDigestWithBigDecimal(param.getValueNumberHigh());
        updateDigestWithString(param.getValueSystem());
        updateDigestWithString(param.getValueCode());
    }

    @Override
    public void visit(LocationParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithDouble(param.getValueLongitude());
        updateDigestWithDouble(param.getValueLatitude());
    }

    @Override
    public void visit(ReferenceParmVal param) throws FHIRPersistenceException {
        updateDigestWithParmValKey(param);
        updateDigestWithReferenceValue(param.getRefValue());
    }

    @Override
    public void visit(CompositeParmVal compositeParameter) throws FHIRPersistenceException {
        updateDigestWithParmValKey(compositeParameter);
        // This composite is a collection of multiple parameters
        List<ExtractedParameterValue> component = compositeParameter.getComponent();
        for (ExtractedParameterValue val : component) {
            val.accept(this);
        }
    }

    /**
     * Compute and return the hash.
     * @return the hash string as Base64
     */
    public String getBase64Hash() {
        byte[] hash = digest.digest();
        return encoder.encodeToString(hash);
    }

    /**
     * Updates the digest with the key of the ExtractedParameterValue.
     * @param epv the ExtractedParameterValue
     */
    private void updateDigestWithParmValKey(ExtractedParameterValue epv) {
        updateDigestWithString(epv.getClass().getName());
        updateDigestWithString(epv.getName());
        updateDigestWithString(epv.getUrl());
        updateDigestWithString(epv.getVersion());
    }

    /**
     * Updates the digest with the String.
     * @param string the String
     */
    private void updateDigestWithString(String value) {
        if (value != null) {
            digest.update(value.getBytes(StandardCharsets.UTF_8));
        }
        digest.update(DELIMITER);
    }

    /**
     * Updates the digest with the BigDecimal.
     * @param value the BigDecimal
     */
    private void updateDigestWithBigDecimal(BigDecimal value) {
        if (value != null) {
            ByteBuffer bb = ByteBuffer.allocate(Double.SIZE);
            bb.putDouble(value.doubleValue());
            digest.update(bb);
        }
        digest.update(DELIMITER);
    }

    /**
     * Updates the digest with the Integer.
     * @param value the Integer
     */
    private void updateDigestWithInteger(Integer value) {
        if (value != null) {
            ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE);
            bb.putInt(value.intValue());
            digest.update(bb);
        }
        digest.update(DELIMITER);
    }

    /**
     * Updates the digest with the Double.
     * @param value the Double
     */
    private void updateDigestWithDouble(Double value) {
        if (value != null) {
            ByteBuffer bb = ByteBuffer.allocate(Double.SIZE);
            bb.putDouble(value.doubleValue());
            digest.update(bb);
        }
        digest.update(DELIMITER);
    }

    /**
     * Updates the digest with the Timestamp.
     * @param value the Timestamp
     */
    private void updateDigestWithTimestamp(Timestamp value) {
        if (value != null) {
            ByteBuffer bb = ByteBuffer.allocate(Long.SIZE);
            bb.putLong(value.getTime());
            digest.update(bb);
        }
        digest.update(DELIMITER);
    }

    /**
     * Updates the digest with the ReferenceValue.
     * @param value the ReferenceValue
     */
    private void updateDigestWithReferenceValue(ReferenceValue value) {
        if (value != null) {
            updateDigestWithString(value.getTargetResourceType());
            updateDigestWithString(value.getValue());
            updateDigestWithString(value.getType() != null ? value.getType().toString() : null);
            updateDigestWithInteger(value.getVersion());
        } else {
            digest.update(DELIMITER);
        }
    }
}
