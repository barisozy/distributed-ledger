package com.distributed.ledger.infrastructure.adapter.web;

import com.distributed.ledger.domain.model.Money;
import com.distributed.ledger.domain.port.in.SendMoneyCommand;
import com.distributed.ledger.domain.port.in.SendMoneyUseCase;
import com.distributed.ledger.infrastructure.adapter.web.dto.SendMoneyRequest;
import com.distributed.ledger.infrastructure.adapter.web.mapper.SendMoneyMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SendMoneyController.class)
@AutoConfigureMockMvc(addFilters = false)
class SendMoneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SendMoneyUseCase sendMoneyUseCase;

    @MockitoBean
    private SendMoneyMapper sendMoneyMapper;

    @Test
    void shouldReturnOkWhenTransferSucceeds() throws Exception {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        SendMoneyRequest request = new SendMoneyRequest(
                fromId,
                toId,
                new BigDecimal("100.00"),
                "TRY",
                "REF-TEST-001"
        );

        SendMoneyCommand validCommand = new SendMoneyCommand(
                fromId,
                toId,
                Money.of(new BigDecimal("100.00"), "TRY"),
                "REF-TEST-001"
        );

        given(sendMoneyMapper.toCommand(any(SendMoneyRequest.class)))
                .willReturn(validCommand);

        given(sendMoneyUseCase.sendMoney(any(SendMoneyCommand.class)))
                .willReturn(true);

        mockMvc.perform(post("/api/v1/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn500WhenTransferFails() throws Exception {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        SendMoneyRequest request = new SendMoneyRequest(
                fromId,
                toId,
                new BigDecimal("100.00"),
                "TRY",
                "REF-TEST-002"
        );

        SendMoneyCommand validCommand = new SendMoneyCommand(
                fromId,
                toId,
                Money.of(new BigDecimal("100.00"), "TRY"),
                "REF-TEST-002"
        );

        given(sendMoneyMapper.toCommand(any(SendMoneyRequest.class)))
                .willReturn(validCommand);

        given(sendMoneyUseCase.sendMoney(any(SendMoneyCommand.class)))
                .willReturn(false);

        mockMvc.perform(post("/api/v1/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}