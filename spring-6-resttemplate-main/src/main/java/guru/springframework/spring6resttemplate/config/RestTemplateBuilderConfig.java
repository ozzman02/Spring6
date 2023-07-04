package guru.springframework.spring6resttemplate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.DefaultUriBuilderFactory;

/* Rest Template needs to use an authenticated JWT token */
@Configuration
public class RestTemplateBuilderConfig {

    /*@Value("${rest.template.username}")
    String username;

    @Value("${rest.template.password}")
    String password;*/

    @Value("${rest.template.baseUrl}")
    String baseUrl;

    /*
        Authorized Client Manager:

            What this is going to do is handle the call to the authorization server for us so,
            we don't have to hand code that. This is a standard Spring security component from the
            client library that we are going to utilize and, we're going to set that up here to be a
            component that we can call upon to get that authorization token for us. Instead of creating
            a new class we create a bean since this is related to the RestTemplateBuilderConfig.

            The configuration properties that we previously set are going to get automatically bound to
            those components. And then also the framework is going to be providing the client framework
            will give us a client registration repository. Again, the configuration components that we added
            and the application.properties will help bind to that.
     */
    @Bean
    OAuth2AuthorizedClientManager auth2AuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                               OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        var authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials() // OAUTH2 flow that we are going to use
                .build();

        var authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager
                (clientRegistrationRepository, oAuth2AuthorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    /*
        Our REST app is now using Spring Security Basic so, all the test will fail unless we configure
        the RestTemplate with Authentication.
    */
    @Bean
    RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer restTemplateBuilderConfigurer,
                                            OAuthClientInterceptor interceptor) {
        /*
            Better refactoring

            RestTemplateBuilder builder = restTemplateBuilderConfigurer.configure(new RestTemplateBuilder());
            DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
            RestTemplateBuilder restTemplateBuilderWithAuth = builder.basicAuthentication(username, password);
            return restTemplateBuilderWithAuth.uriTemplateHandler(defaultUriBuilderFactory);

            Basic auth has been removed, we need to add the interceptor to the Rest Template.
        */
        assert baseUrl != null;

        return restTemplateBuilderConfigurer.configure(new RestTemplateBuilder())
                //.basicAuthentication(username, password)
                .additionalInterceptors(interceptor)
                .uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
    }
}
