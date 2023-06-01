package com.orange.controller;

import com.orange.dto.ShopDTO;
import com.orange.events.SendKafkaMessage;
import com.orange.model.Shop;
import com.orange.model.ShopItem;
import com.orange.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopRepository shopRepository;
    private final SendKafkaMessage sendKafkaMessage;

    @GetMapping
    public List<ShopDTO> getShop() {
        return shopRepository.findAll()
        		.stream()
                .map(shop -> ShopDTO.convert(shop))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ShopDTO saveShop(@RequestBody ShopDTO shopDTO) {    	
    	shopDTO.setIdentifier(UUID.randomUUID().toString());
    	shopDTO.setDateShop(LocalDate.now());
    	shopDTO.setTimeShop(System.currentTimeMillis());
    	shopDTO.setStatus("PENDING");
    	
    	Shop shop = Shop.convert(shopDTO);
    	for (ShopItem shopItem : shop.getItems()) {
    		shopItem.setShop(shop);
    	}
    	
        shopDTO = ShopDTO.convert(shopRepository.save(shop));
        sendKafkaMessage.sendMessage(shopDTO);
        return shopDTO;
        
    }

}
