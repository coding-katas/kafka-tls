package com.orange.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.orange.dto.ShopDTO;
import com.orange.dto.ShopItemDTO;
import com.orange.model.Product;
import com.orange.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveKafkaMessage {

	private static final String SHOP_TOPIC_NAME = "SHOP_TOPIC";
	private static final String SHOP_TOPIC_EVENT_NAME = "SHOP_TOPIC_EVENT";
	
	private final ProductRepository productRepository;

	private final KafkaTemplate<String, ShopDTO> kafkaTemplate;
	
	@KafkaListener(topics = SHOP_TOPIC_NAME, groupId = "group")
	public void listenShopTopic(ShopDTO shopDTO) {
	    log.info("Purchase received in topic: {}", 
	    		shopDTO.getIdentifier());
	    
	    boolean success = true;
	    for (ShopItemDTO item : shopDTO.getItems()) {
	    	Product product = productRepository
	    			.findByIdentifier(
	    					item.getProductIdentifier());
	    	if (!isValidShop(item, product)) {
			    shopError(shopDTO);
				success = false;
	    		break;
	    	}
	    }
	    if (success) {
		    shopSuccess(shopDTO);
	    }
	    
	}

	// valida se a compra possui algum erro
	private boolean isValidShop(ShopItemDTO item, Product product) {
		log.info("validity product vs item: {} --> {}.", product.getAmount(), item.getAmount());
		return product != null &&
			product.getAmount() >= item.getAmount();
	}


	private void shopError(ShopDTO shopDTO) {
		log.info("Error in the purchase process {}.", shopDTO.getIdentifier());
		shopDTO.setStatus("ERROR");
		kafkaTemplate.send(SHOP_TOPIC_EVENT_NAME, shopDTO);
	}

	private void shopSuccess(ShopDTO shopDTO) {
		log.info("Purchase {} succefully done.", shopDTO.getIdentifier());
		shopDTO.setStatus("SUCCESS");
		kafkaTemplate.send(SHOP_TOPIC_EVENT_NAME, shopDTO);
	}

}
