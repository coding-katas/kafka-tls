package com.orange.dto;

import java.time.LocalDate;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopDTO {
    private String identifier;
    private String status;
    private String buyerIdentifier;
    private LocalDate dateShop;
    private Instant timeShop;
}
