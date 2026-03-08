package com.iso.hypo.admin.papi.cache;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.iso.hypo.services.clients.AzureGraphClientService;
import com.microsoft.graph.models.Group;

/**
 * Caffeine-backed thread-safe cache for brandUuid -> groupId.
 */
@Component
public class BrandGroupCache {

    private final Cache<String, Group> cache;
	private final AzureGraphClientService azureGraphClientService;
	
	private static final Logger logger = LoggerFactory.getLogger(BrandGroupCache.class);
	
    public BrandGroupCache(AzureGraphClientService azureGraphClientService) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterAccess(600, TimeUnit.MINUTES)
                .build();
		this.azureGraphClientService = azureGraphClientService;
    }

    public Group getGroup(String brandUuid) {
    	if (brandUuid == null) return null;
    	
    	Group group = cache.get(brandUuid, _ -> {
			try {
				return azureGraphClientService.getGroup(brandUuid);
			} catch (Exception e) {
				logger.error("Error fetching group for brandUuid {}: {}", brandUuid, e.getMessage());
			}
			return null;
		}); // Only set if absent
        return group;	
    }
}
