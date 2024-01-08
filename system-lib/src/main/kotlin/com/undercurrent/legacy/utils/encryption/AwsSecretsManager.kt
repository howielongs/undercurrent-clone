package com.undercurrent.legacy.utils.encryption

import com.undercurrent.shared.utils.Log
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException
import kotlin.system.exitProcess


//Remember, you must have the following permissions to the key:
// kms:Encrypt,
// kms:Decrypt, and
// kms:GenerateDataKey
object AwsSecretsManager {
    val secretName = "live/ORGNAME/SECRET_NAME"
    val secretARN = "arn:aws:secretsmanager:us-east-2:199999999945:secret:live/ORGNAME/SECRET_ID"

    fun fetchSecret() {
        Log.debug("Fetching secret from AWS...")
        credsProvider()?.let {
            println("SECRET from AWS: " + getValue(it, secretName))
            return
        }
        println("SECRET NOT FOUND")
        Log.fatal("SECRET NOT FOUND")

    }

    private fun credsProvider(): SecretsManagerClient? {
        val profileName = "SecretsManagerAdmin"
        val credentialsProvider = ProfileCredentialsProvider.builder()
            .profileName(profileName)
            .build()

        return SecretsManagerClient.builder()
            .credentialsProvider(credentialsProvider)
            .region(Region.US_EAST_2) // replace with your desired region
            .build()

    }

    fun getValue(secretsClient: SecretsManagerClient, secretName: String) {
        try {
            val valueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build()

            val valueResponse = secretsClient.getSecretValue(valueRequest)
            val secret = valueResponse.secretString()
            println(secret)

        } catch (e: SecretsManagerException) {
            System.err.println(e.awsErrorDetails().errorMessage())
            exitProcess(1)
        }
    }


    fun getSecret() {
        val secretName = "live/ORGNAME/SECRET_NAME"
        val region = Region.of("us-east-2")

        // Create a Secrets Manager client
        val client: SecretsManagerClient = SecretsManagerClient.builder()
            .region(region)
            .build()
        val getSecretValueRequest: GetSecretValueRequest = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build()
        val getSecretValueResponse: GetSecretValueResponse = try {
            client.getSecretValue(getSecretValueRequest)
        } catch (e: Exception) {
            // For a list of exceptions thrown, see
            // https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
            throw e
        }
        val secret: String = getSecretValueResponse.secretString()

        // Your code goes here.
    }


}