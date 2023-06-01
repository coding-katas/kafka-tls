package com.orange.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ShopDTO {
    private String identifier;
    private LocalDate dateShop;
    private long timeShop;
    private String status;
    private String buyerIdentifier;
    private List<ShopItemDTO> items = new  ArrayList<>();
    
}
