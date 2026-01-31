package br.com.bipos.webapi.init

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class SpacesConfig {

    @Bean
    fun s3Client(
        @Value("\${DO_SPACES_KEY}") key: String,
        @Value("\${DO_SPACES_SECRET}") secret: String,
        @Value("\${DO_SPACES_REGION}") region: String
    ): S3Client =
        S3Client.builder()
            .endpointOverride(URI.create("https://$region.digitaloceanspaces.com"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(key, secret)
                )
            )
            .region(Region.of(region))
            .build()
}
