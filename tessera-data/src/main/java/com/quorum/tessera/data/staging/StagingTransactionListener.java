package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagingTransactionListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingTransactionListener.class);

  @PreUpdate
  public void onUpdate(StagingTransaction stagingTransaction) {

    final EncodedPayload encodedPayload = stagingTransaction.getEncodedPayload();
    final EncodedPayloadCodec encodedPayloadCodec = stagingTransaction.getEncodedPayloadCodec();

    PayloadEncoder payloadEncoder = lookup(encodedPayloadCodec);
    byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);
    stagingTransaction.setPayload(encodedPayloadData);
  }

  @PrePersist
  public void onSave(StagingTransaction stagingTransaction) {

    final EncodedPayload encodedPayload = stagingTransaction.getEncodedPayload();
    final EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.current();
    PayloadEncoder payloadEncoder = lookup(encodedPayloadCodec);
    byte[] encodedPayloadData = payloadEncoder.encode(encodedPayload);
    stagingTransaction.setEncodedPayloadCodec(encodedPayloadCodec);
    stagingTransaction.setPayload(encodedPayloadData);
  }

  @PostLoad
  public void onLoad(StagingTransaction stagingTransaction) {
    LOGGER.debug("onLoad[{}]", stagingTransaction);

    EncodedPayloadCodec encodedPayloadCodec = stagingTransaction.getEncodedPayloadCodec();
    byte[] encodedPayloadData = stagingTransaction.getPayload();
    PayloadEncoder payloadEncoder = lookup(encodedPayloadCodec);
    EncodedPayload encodedPayload = payloadEncoder.decode(encodedPayloadData);
    stagingTransaction.setEncodedPayload(encodedPayload);
  }

  private static PayloadEncoder lookup(EncodedPayloadCodec encodedPayloadCodec) {
    return PayloadEncoder.create(encodedPayloadCodec)
        .orElseThrow(
            () -> new IllegalStateException("No encoder found for " + encodedPayloadCodec));
  }
}
