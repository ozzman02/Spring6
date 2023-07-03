package guru.springframework.spring6resttemplate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateBuilderConfig {

    @Value("${rest.template.username}")
    String username;

    @Value("${rest.template.password}")
    String password;

    @Value("${rest.template.baseUrl}")
    String baseUrl;

    /*
        Our REST app is now using Spring Security Basic so, all the test will fail unless we configure
        the RestTemplate with Authentication.
    */
    @Bean
    RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer restTemplateBuilderConfigurer) {
        /*
            Better refactoring

            RestTemplateBuilder builder = restTemplateBuilderConfigurer.configure(new RestTemplateBuilder());
            DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
            RestTemplateBuilder restTemplateBuilderWithAuth = builder.basicAuthentication(username, password);
            return restTemplateBuilderWithAuth.uriTemplateHandler(defaultUriBuilderFactory);
        */
        assert baseUrl != null;

        return restTemplateBuilderConfigurer.configure(new RestTemplateBuilder())
                .basicAuthentication(username, password)
                .uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }
}
