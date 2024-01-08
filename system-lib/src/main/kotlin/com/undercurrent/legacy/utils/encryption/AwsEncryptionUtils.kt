package com.undercurrent.legacy.utils.encryption

import com.undercurrent.shared.types.enums.Environment
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest


object AwsEncryptionUtils {
    //    val liveParameter = "/ORGNAME/live/SECRET_cipher"
    fun fetchMobMnemonicCipherText(environment: Environment) {
        val mobCipherParameter = "/ORGNAME/${environment.name.lowercase()}/SECRET_cipher"
        getCiphertextFromParameterStore(mobCipherParameter)
    }

    fun getCiphertextFromParameterStore(parameterName: String): String {
        val region = Region.US_EAST_2 // Replace with your desired AWS region
        val parameterName = parameterName // Replace with the actual parameter name representing the ciphertext

        val ssmClient = SsmClient.builder()
            .region(region)
            .build()


        val request = GetParameterRequest.builder()
            .name(parameterName)
            .withDecryption(true)
            .build()
        val response = ssmClient.getParameter(request)
        val ciphertext = response.parameter().value()

        return ciphertext
    }

}