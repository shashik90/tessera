package com.quorum.tessera.messaging;

import com.quorum.tessera.serviceloader.ServiceLoaderUtil;
import org.junit.Test;

import java.util.ServiceLoader;
import static org.mockito.Mockito.*;
public class CourierTest  {

  @Test
  public void testCreate() {
    Courier courier = mock(Courier.class);
    ServiceLoader<Courier> serviceLoader = mock(ServiceLoader.class);
    ServiceLoaderUtil serviceLoaderUtil = mock(ServiceLoaderUtil.class);

    Courier result;
    try (var serviceLoaderUtilMockedStatic = mockStatic(ServiceLoaderUtil.class)) {

      var serviceLoaderMockedStatic = mockStatic(ServiceLoader.class);

      serviceLoaderMockedStatic
        .when(() -> ServiceLoader.load(Courier.class))
        .thenReturn(serviceLoader);

      serviceLoaderUtilMockedStatic
        .when(() -> ServiceLoaderUtil.loadSingle(ServiceLoader.load(Courier.class))).thenReturn(courier);

      result = Courier.create();
    }
  }
}
