package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class StagingTransactionListenerTest {

  private final MockedStatic<PayloadEncoder> payloadEncoderFactoryFunction =
    mockStatic(PayloadEncoder.class);

  private StagingTransactionListener stagingTransactionListener;

  private PayloadEncoder payloadEncoder;

  @Before
  public void beforeTest() {
    stagingTransactionListener = new StagingTransactionListener();
    payloadEncoder = mock(PayloadEncoder.class);
    payloadEncoderFactoryFunction
      .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
      .thenReturn(Optional.of(payloadEncoder));
  }

  @After
  public void afterTest() {
    try {
      verifyNoMoreInteractions(payloadEncoder);
      payloadEncoderFactoryFunction.verifyNoMoreInteractions();
    } finally {
      payloadEncoderFactoryFunction.close();
    }
  }

  @Test
  public void onLoad() {

    byte[] payloadData = "PayloadData".getBytes();

    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED);
    stagingTransaction.setPayload(payloadData);

    stagingTransactionListener.onLoad(stagingTransaction);

    verify(payloadEncoder).decode(payloadData);

    payloadEncoderFactoryFunction.verify(
      () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void onLoadNoEncoderFound() {

    byte[] payloadData = "PayloadData".getBytes();

    payloadEncoderFactoryFunction.reset();
    payloadEncoderFactoryFunction
      .when(() -> PayloadEncoder.create(EncodedPayloadCodec.LEGACY))
      .thenReturn(Optional.empty());
    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.LEGACY);
    stagingTransaction.setPayload(payloadData);

    try {
      stagingTransactionListener.onLoad(stagingTransaction);
      failBecauseExceptionWasNotThrown(IllegalStateException.class);
    } catch (IllegalStateException illegalStateException) {
      assertThat(illegalStateException).hasMessage("No encoder found for LEGACY");
      payloadEncoderFactoryFunction.verify(() -> PayloadEncoder.create(EncodedPayloadCodec.LEGACY));
    }
  }

  @Test
  public void onSave() {
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setEncodedPayload(encodedPayload);

    byte[] payloadData = "PayloadData".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    stagingTransactionListener.onSave(stagingTransaction);

    verify(payloadEncoder).encode(encodedPayload);
    payloadEncoderFactoryFunction.verify(() -> PayloadEncoder.create(EncodedPayloadCodec.LEGACY));
    assertThat(stagingTransaction.getEncodedPayload()).isEqualTo(encodedPayload);
    assertThat(stagingTransaction.getPayload()).containsExactly(payloadData);
  }

  @Test
  public void onUpdate() {
    EncodedPayload encodedPayload = mock(EncodedPayload.class);
    StagingTransaction stagingTransaction = new StagingTransaction();
    stagingTransaction.setEncodedPayloadCodec(EncodedPayloadCodec.LEGACY);
    stagingTransaction.setEncodedPayload(encodedPayload);

    byte[] payloadData = "PayloadData".getBytes();
    when(payloadEncoder.encode(encodedPayload)).thenReturn(payloadData);

    stagingTransactionListener.onUpdate(stagingTransaction);

    verify(payloadEncoder).encode(encodedPayload);
    payloadEncoderFactoryFunction.verify(() -> PayloadEncoder.create(EncodedPayloadCodec.LEGACY));
    assertThat(stagingTransaction.getEncodedPayload()).isEqualTo(encodedPayload);
    assertThat(stagingTransaction.getPayload()).containsExactly(payloadData);

  }

}
