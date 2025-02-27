package id.walt.rest.custodian

import com.beust.klaxon.Klaxon
import id.walt.crypto.Key
import id.walt.crypto.KeyAlgorithm
import id.walt.custodian.Custodian
import id.walt.services.key.KeyFormat
import id.walt.services.keystore.KeyType
import id.walt.vclib.credentials.VerifiablePresentation
import id.walt.vclib.model.VerifiableCredential
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import kotlinx.serialization.Serializable

@Serializable
data class ExportKeyRequest(
    val keyAlias: String,
    val format: KeyFormat = KeyFormat.JWK,
    val exportPrivate: Boolean = false
)

object CustodianController {

    private val custodian = Custodian.getService()

    /* Keys */

    data class GenerateKeyRequest(val keyAlgorithm: KeyAlgorithm)
    data class StoreCredentialRequest(val alias: String, val vc: VerifiableCredential)
    data class ListKeyResponse(val list: List<Key>)

    //    @OpenApi(
//        summary = "Generates a key with a specific key algorithm", operationId = "generateKey", tags = ["Keys"],
//        requestBody = OpenApiRequestBody([OpenApiContent(GenerateKeyRequest::class)], true, "Generate Key Request"),
//        responses = [OpenApiResponse("200", [OpenApiContent(Key::class)], "Created Key")]
//    )
    fun generateKeyDocs() = document()
        .operation {
            it.summary("Generates a key with a specific key algorithm").operationId("generateKey").addTagsItem("Keys")
        }
        .body<GenerateKeyRequest> { it.description("Generate Key Request") }
        .json<String>("200") { it.description("Created key") }

    fun generateKey(ctx: Context) {
        ctx.json(custodian.generateKey(ctx.bodyAsClass<GenerateKeyRequest>().keyAlgorithm))
    }

    //    @OpenApi(
//        summary = "Gets a key specified by its alias", operationId = "getKey", tags = ["Keys"],
//        responses = [OpenApiResponse("200", [OpenApiContent(Key::class)], "Key by alias")]
//    )
    fun getKeysDocs() = document()
        .operation { it.summary("Gets the metadata of a key specified by its alias").operationId("getKey").addTagsItem("Keys") }
        .json<String>("200") { it.description("Key by alias") }

    fun getKey(ctx: Context) {
        ctx.json(custodian.getKey(ctx.pathParam("alias")))
    }

    //    @OpenApi(
//        summary = "Lists all keys the custodian knows of", operationId = "listKeys", tags = ["Keys"],
//        responses = [OpenApiResponse("200", [OpenApiContent(ListKeyResponse::class)], "List of Keys")]
//    )
    fun listKeysDocs() = document()
        .operation { it.summary("Lists all keys the custodian knows of").operationId("listKeys").addTagsItem("Keys") }
        .json<String>("200") { it.description("Array of keys") }

    fun listKeys(ctx: Context) {
        ctx.json(ListKeyResponse(custodian.listKeys()))
    }

    //    @OpenApi(
//        summary = "Stores a key", operationId = "storeKey", tags = ["Keys"],
//        requestBody = OpenApiRequestBody([OpenApiContent(StoreKeyRequest::class)], true, "Store Key Request"),
//        responses = [OpenApiResponse("200")]
//    )

    fun importKeysDocs() = document().operation {
        it.summary("Import key").operationId("importKey").addTagsItem("Keys")
    }.body<String> {
        it.description("Imports the key (JWK and PEM format) to the key store")
    }.json<String>("200")

    fun importKey(ctx: Context) {
        ctx.json(custodian.importKey(ctx.body()))
    }

    //    @OpenApi(
//        summary = "Deletes a specific key", operationId = "deleteKey",
//        tags = ["Keys"], responses = [OpenApiResponse("200")]
//    )
    fun deleteKeysDocs() = document()
        .operation { it.summary("Deletes a specific key").operationId("deleteKey").addTagsItem("Keys") }
        .json<String>("200") { it.description("Http OK") }

    fun deleteKey(ctx: Context) {
        custodian.deleteKey(ctx.pathParam("id"))
    }

    fun exportKeysDocs() = document().operation {
        it.summary("Exports public and private key part (if supported by underlying keystore)").operationId("exportKey")
            .addTagsItem("Keys")
    }.body<ExportKeyRequest> { it.description("Exports the key in JWK or PEM format") }
        .json<String>("200") { it.description("The key in the desired format") }

    fun exportKey(ctx: Context) {
        val req = ctx.bodyAsClass(id.walt.rest.core.ExportKeyRequest::class.java)
        ctx.result(
            custodian.exportKey(
                req.keyAlias,
                req.format,
                if (req.exportPrivate) KeyType.PRIVATE else KeyType.PUBLIC
            )
        )
    }

    /* Credentials */

    data class ListCredentialsResponse(val list: List<VerifiableCredential>)
    data class ListCredentialIdsResponse(val list: List<String>)

    //    @OpenApi(
//        summary = "Gets a specific Credential by id", operationId = "getCredential", tags = ["Credentials"],
//        responses = [OpenApiResponse("200", [OpenApiContent(VerifiableCredential::class)], "Created Credential")]
//    )
    fun getCredentialDocs() = document()
        .operation { it.summary("Gets a specific Credential by id").operationId("getCredential").addTagsItem("Credentials") }
        .json<String>("200") { it.description("Created Credential") }
        .result<String>("404")

    fun getCredential(ctx: Context) {
        val vc = custodian.getCredential(ctx.pathParam("id"))
        if (vc == null)
            ctx.status(404).result("Not found")
        else
            ctx.json(vc)
    }

    //    @OpenApi(
//        summary = "Lists all credentials the custodian knows of", operationId = "listCredentials", tags = ["Credentials"],
//        responses = [OpenApiResponse("200", [OpenApiContent(ListCredentialsResponse::class)], "Credential list")]
//    )
    fun listCredentialsDocs() = document()
        .operation {
            it.summary("Lists all credentials the custodian knows of").operationId("listCredentials").addTagsItem("Credentials")
        }
        .queryParam<String>("id", isRepeatable = true)
        .json<String>("200") { it.description("Credentials list") }

    fun listCredentials(ctx: Context) {
        val ids = ctx.queryParams("id").toSet()
        if (ids.isEmpty())
            ctx.json(ListCredentialsResponse(custodian.listCredentials()))
        else
            ctx.json(ListCredentialsResponse(custodian.listCredentials().filter { it.id != null && ids.contains(it.id!!) }))
    }

    //    @OpenApi(
//        summary = "Lists all credential ids the custodian knows of", operationId = "listCredentialIds", tags = ["Credentials"],
//        responses = [OpenApiResponse("200", [OpenApiContent(ListCredentialIdsResponse::class)], "Credential id list")]
//    )
    fun listCredentialIdsDocs() = document()
        .operation {
            it.summary("Lists all credential IDs the custodian knows of").operationId("listCredentialIds")
                .addTagsItem("Credentials")
        }
        .json<String>("200") { it.description("Credentials ID list") }

    fun listCredentialIds(ctx: Context) {
        ctx.json(ListCredentialIdsResponse(custodian.listCredentialIds()))
    }

    //    @OpenApi(
//        summary = "Lists all credential ids the custodian knows of", operationId = "listCredentialIds", tags = ["Credentials"],
//        requestBody = OpenApiRequestBody([OpenApiContent(StoreCredentialRequest::class)], true, "Store Credential Request"),
//        responses = [OpenApiResponse("200")]
//    )
    fun storeCredentialsDocs() = document()
        .operation { it.summary("Stores a credential").operationId("storeCredential").addTagsItem("Credentials") }
        .body<VerifiableCredential> { it.description("the vc") }
        .json<Int>("201") { it.description("Http OK") }

    fun storeCredential(ctx: Context) {
        val vc = Klaxon().parse<VerifiableCredential>(ctx.body())!!

        custodian.storeCredential(ctx.pathParam("alias"), vc)
    }

    //    @OpenApi(
//        summary = "Deletes a specific credential by alias", operationId = "deleteCredential",
//        tags = ["Credentials"], responses = [OpenApiResponse("200")]
//    )
    fun deleteCredentialDocs() = document()
        .operation {
            it.summary("Deletes a specific credential by alias").operationId("deleteCredential").addTagsItem("Credentials")
        }
        .json<String>("200") { it.description("Http OK") }

    fun deleteCredential(ctx: Context) {
        custodian.deleteCredential(ctx.pathParam("alias"))
    }

    fun presentCredentialsDocs() = document()
        .operation {
            it.summary("Create a VerifiablePresentation from specific credentials)").operationId("presentCredentials")
                .addTagsItem("Credentials")
        }
        .body<PresentCredentialsRequest>()
        .json<VerifiablePresentation>("200") { it.description("The newly created VerifiablePresentation") }


    fun presentCredentials(ctx: Context) {
        val req = ctx.bodyAsClass<PresentCredentialsRequest>()
        ctx.result(custodian.createPresentation(req.vcs, req.holderDid, req.verifierDid, req.domain, req.challenge, null))
    }

    fun presentCredentialIdsDocs() = document()
        .operation {
            it.summary("Create a VerifiablePresentation from specific credential IDs)").operationId("presentCredentialIds")
                .addTagsItem("Credentials")
        }
        .body<PresentCredentialIdsRequest>()
        .json<VerifiablePresentation>("200") { it.description("The newly created VerifiablePresentation") }


    fun presentCredentialIds(ctx: Context) {
        val req = ctx.bodyAsClass<PresentCredentialIdsRequest>()

        val ids = req.vcIds.map { custodian.getCredential(it)!!.encode() }

        ctx.result(custodian.createPresentation(ids, req.holderDid, req.verifierDid, req.domain, req.challenge, null))
    }

}
