package com.tuiop.markethub;

import com.tuiop.markethub.admin.config.AdminProperties;
import com.tuiop.markethub.products.images.config.ProductImagesProperties;
import com.tuiop.markethub.ratelimiter.RateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableConfigurationProperties({AdminProperties.class, RateLimitProperties.class, ProductImagesProperties.class})
@SpringBootApplication
@EnableCaching
public class MarketHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketHubApplication.class, args);
    }

}
