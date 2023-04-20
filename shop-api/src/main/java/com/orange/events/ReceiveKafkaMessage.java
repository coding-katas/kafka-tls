package com.orange.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.orange.dto.ShopDTO;
import com.orange.model.Shop;
import com.orange.repository.ShopRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveKafkaMessage {
	
	private final ShopRepository shopRepository;

	private static final String SHOP_TOPIC_EVENT_NAME = "SHOP_TOPIC_EVENT";
	
	@KafkaListener(topics = SHOP_TOPIC_EVENT_NAME, groupId = "group")
	public void listenShopEvents(ShopDTO shopDTO) {
		try {
		    log.info("Purchase received on topic: {}.", shopDTO.getIdentifier());
		    
		    Shop shop = shopRepository.findByIdentifier(shopDTO.getIdentifier());
		    shop.setStatus(shopDTO.getStatus());
		    shopRepository.save(shop);
		} catch(Exception e) {
			log.error("Error processing purchase {}", shopDTO.getIdentifier());
		}
	}

}
