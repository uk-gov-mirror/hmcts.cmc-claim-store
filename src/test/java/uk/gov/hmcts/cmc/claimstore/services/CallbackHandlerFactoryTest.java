package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CallbackHandlerFactory;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.GenerateOrderCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_PAPER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;

@RunWith(MockitoJUnitRunner.class)
public class CallbackHandlerFactoryTest {

    @Mock
    private MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler;
    @Mock
    private GenerateOrderCallbackHandler generateOrderCallbackHandler;
    @Mock
    private CallbackResponse callbackResponse;

    private CallbackHandlerFactory callbackHandlerFactory;

    @Before
    public void setUp() {
        doCallRealMethod().when(moreTimeRequestedCallbackHandler).handledEvents();
        doCallRealMethod().when(generateOrderCallbackHandler).handledEvents();
        doCallRealMethod().when(moreTimeRequestedCallbackHandler).register(anyMap());
        doCallRealMethod().when(generateOrderCallbackHandler).register(anyMap());
        callbackHandlerFactory = new CallbackHandlerFactory(
            ImmutableList.of(
                moreTimeRequestedCallbackHandler,
                generateOrderCallbackHandler)
        );
    }

    @Test
    public void shouldDispatchCallbackForMoreTimeRequested() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(MORE_TIME_REQUESTED_PAPER.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .build();
        when(moreTimeRequestedCallbackHandler.handle(params)).thenReturn(callbackResponse);
        callbackHandlerFactory
            .dispatch(params);
        verify(moreTimeRequestedCallbackHandler).handle(params);
    }

    @Test
    public void shouldDispatchCallbackForGenerateOrder() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(GENERATE_ORDER.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .build();
        when(generateOrderCallbackHandler.handle(params)).thenReturn(callbackResponse);
        callbackHandlerFactory
            .dispatch(params);
        verify(generateOrderCallbackHandler).handle(params);

    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnsupportedEventForCallback() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(SEALED_CLAIM_UPLOAD.getValue())
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .build();
        callbackHandlerFactory
            .dispatch(params);
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnknownEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId("nope")
            .build();
        CallbackParams params = CallbackParams.builder()
            .request(callbackRequest)
            .build();
        callbackHandlerFactory
            .dispatch(params);
    }
}
